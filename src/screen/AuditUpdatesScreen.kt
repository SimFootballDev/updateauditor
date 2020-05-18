package screen

import com.google.gson.GsonBuilder
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.ScrollPane
import javafx.scene.text.Text
import javafx.stage.Stage
import model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.FileWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class AuditUpdatesScreen {

    private val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("res/screen_audit_updates.fxml"))

    fun start(primaryStage: Stage, sheetPageList: List<SheetPage>, teamList: List<Team>, currentSeason: Int,
              auditingDSFL: Boolean) {

        primaryStage.scene.root = root

        (root.lookup("#scrollPane") as ScrollPane).content = Text("Processing...")
        (root.lookup("#scrollPane") as ScrollPane).vvalue = 0.0

        Thread {
            val resultList = auditUpdates(sheetPageList, teamList, currentSeason, auditingDSFL)
            Platform.runLater {
                (root.lookup("#scrollPane") as ScrollPane).content = Text(resultList.joinToString("\n"))
                (root.lookup("#scrollPane") as ScrollPane).vvalue = 0.0
            }
            FileWriter("output.txt").apply {
                write(resultList.joinToString("\n"))
                flush()
                close()
            }
        }.start()
    }

    private fun auditUpdates(sheetPageList: List<SheetPage>, teamList: List<Team>, currentSeason: Int, auditingDSFL: Boolean): List<String> {

        val resultList = ArrayList<String>()

        val updatePageList = getUpdatePageList(teamList)
        val playerPageList = getPlayerPageList(teamList)

        resultList.add("Update Page Not Found\n")
        playerPageList.forEach { playerPage ->
            val updatePage = updatePageList.firstOrNull { updatePage -> updatePage.user == playerPage.user }
            if (updatePage == null) {
                resultList.add(playerPage.user)
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        resultList.add("Update Page Not Processed\n")
        updatePageList.forEach { updatePage ->
            if (!updatePage.lastPostIsRecent && updatePage.lastPostIsFromOwner) {
                resultList.add("${updatePage.user}: ${updatePage.title}")
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        resultList.add("Player Page Not Updated\n")
        updatePageList.forEach { updatePage ->
            if (updatePage.lastPostIsRecent) {
                playerPageList.firstOrNull { playerPage -> playerPage.user == updatePage.user }?.let { playerPage ->
                    if (playerPage.tpeHistoryList[playerPage.tpeHistoryList.lastIndex].second ==
                        playerPage.tpeHistoryList[playerPage.tpeHistoryList.lastIndex - 1].second
                    ) {
                        resultList.add("${playerPage.user} - ${playerPage.name} - ${playerPage.team}")
                    }
                }
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        if (sheetPageList.isEmpty()) {
            return resultList
        }

        val playerSheetMatchList = ArrayList<Pair<PlayerPage, List<SheetPage>>>()

        resultList.add("Player Missing From Sheet\n")
        playerPageList.forEach { playerPage ->

            val filteredSheetPageList = sheetPageList
                .filter { sheetPage ->

                    val regex = Pattern.compile("[^a-zA-Z0-9]").toRegex()

                    val sheetPageName = sheetPage.playerName.toLowerCase().replace(regex, "")

                    val playerPageName1 = playerPage.name.split(" ").let {
                        if (it.size == 1) {
                            "None" + it.first()
                        } else {
                            it.first() + it.last()
                        }
                    }.toLowerCase().replace(regex, "")

                    val playerPageName2 = playerPage.name.toLowerCase().replace(regex, "")

                    sheetPageName == playerPageName1 || sheetPageName == playerPageName2
                }

            if (filteredSheetPageList.isEmpty()) {
                resultList.add("${playerPage.user} - ${playerPage.name} - ${playerPage.team}")
            } else {
                playerSheetMatchList.add(Pair(playerPage, filteredSheetPageList))
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        resultList.add("Player On Multiple Teams\n")
        playerSheetMatchList.forEach { match ->
            if (match.second.size > 1) {
                resultList.add("${match.first.user} - ${match.first.name} - ${match.first.team}")
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        resultList.add("Player On Wrong Team\n")
        playerSheetMatchList.forEach { match ->
            if (match.second.firstOrNull { it.team.name == match.first.team } == null) {
                resultList.add("${match.first.user} - ${match.first.name} - ${match.first.team}")
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        resultList.add("Sheet Attribute Mismatches\n")
        playerSheetMatchList.forEach { match ->

            val listsOfMismatchList = ArrayList<ArrayList<String>>()

            match.second.forEach { sheetPage ->

                val mismatchList = ArrayList<String>()

                val isBlockingBack = parseIsBlockingBack(match.first)
                if (isBlockingBack && sheetPage.position != "FB") {
                    mismatchList.add("position mismatch: FB - ${sheetPage.position}")
                }

                val experience = match.first.calculateExperience(currentSeason, isBlockingBack, auditingDSFL)
                if (experience != sheetPage.experience) {
                    mismatchList.add("experience mismatch: update $experience - sheet ${sheetPage.experience}")
                }

                if (match.first.strength != sheetPage.strength) {
                    mismatchList.add("strength mismatch: update ${match.first.strength} - sheet ${sheetPage.strength}")
                }
                if (match.first.agility != sheetPage.agility) {
                    mismatchList.add("agility mismatch: update ${match.first.agility} - sheet ${sheetPage.agility}")
                }
                if (match.first.arm != sheetPage.arm) {
                    mismatchList.add("arm mismatch: update ${match.first.arm} - sheet ${sheetPage.arm}")
                }
                if (match.first.intelligence != sheetPage.intelligence) {
                    mismatchList.add("intelligence mismatch: update ${match.first.intelligence} - sheet ${sheetPage.intelligence}")
                }
                if (match.first.throwingAccuracy != sheetPage.throwingAccuracy) {
                    mismatchList.add("throwingAccuracy mismatch: update ${match.first.throwingAccuracy} - sheet ${sheetPage.throwingAccuracy}")
                }
                if (match.first.tackling != sheetPage.tackling) {
                    mismatchList.add("tackling mismatch: update ${match.first.tackling} - sheet ${sheetPage.tackling}")
                }
                if (match.first.speed != sheetPage.speed) {
                    mismatchList.add("speed mismatch: update ${match.first.speed} - sheet ${sheetPage.speed}")
                }
                if (match.first.hands != sheetPage.hands) {
                    mismatchList.add("hands mismatch: update ${match.first.hands} - sheet ${sheetPage.hands}")
                }
                if (match.first.passBlocking != sheetPage.passBlocking) {
                    mismatchList.add("passBlocking mismatch: update ${match.first.passBlocking} - sheet ${sheetPage.passBlocking}")
                }
                if (match.first.runBlocking != sheetPage.runBlocking) {
                    mismatchList.add("runBlocking mismatch: update ${match.first.runBlocking} - sheet ${sheetPage.runBlocking}")
                }
                if (match.first.endurance != sheetPage.endurance) {
                    mismatchList.add("endurance mismatch: update ${match.first.endurance} - sheet ${sheetPage.endurance}")
                }
                if (match.first.kickPower != sheetPage.kickPower) {
                    mismatchList.add("kickPower mismatch: update ${match.first.kickPower} - sheet ${sheetPage.kickPower}")
                }
                if (match.first.kickAccuracy != sheetPage.kickAccuracy) {
                    mismatchList.add("kickAccuracy mismatch: update ${match.first.kickAccuracy} - sheet${sheetPage.kickAccuracy}")
                }
                if (match.first.kickPower != sheetPage.puntPower) {
                    mismatchList.add("puntPower mismatch: update ${match.first.kickPower} - sheet ${sheetPage.puntPower}")
                }
                if (match.first.kickAccuracy != sheetPage.puntAccuracy) {
                    mismatchList.add("puntAccuracy mismatch: update ${match.first.kickAccuracy} - sheet ${sheetPage.puntAccuracy}")
                }

                listsOfMismatchList.add(mismatchList)
            }

            listsOfMismatchList.forEach { mismatchList ->
                if (mismatchList.isNotEmpty()) {
                    resultList.add("${match.first.user} - ${match.first.name} - ${match.first.team} - ${mismatchList.joinToString()}")
                }
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        return resultList
    }

    private fun getUpdatePageList(teamList: List<Team>): List<UpdatePage> {

        val changedUserNameList = arrayListOf(Pair("EnfysNest", "Baron1898"))

        val updatePageList = ArrayList<UpdatePage>()

        val documentList = ArrayList<Document>()
        teamList.forEach {

            val firstDocument = connect("http://nsfl.jcink.net/index.php?showforum=${it.forumId}")
            documentList.add(firstDocument)

            val pageCount = parsePageCount(firstDocument.body().toString())

            for (i in 1..(pageCount - 1)) {
                documentList.add(connect("http://nsfl.jcink.net/index.php?showforum=${it.forumId}&st=${i * 15}"))
            }
        }

        documentList.forEach { document ->
            document.body().getElementsByClass("topic-row").map { it.toString() }.forEach { rowText ->

                try {

                    val titleStart = rowText.indexOf(">", rowText.indexOf("title="))
                    val titleEnd = rowText.indexOf("<", titleStart + 1)

                    val ownerStart = rowText.indexOf("showuser=", titleEnd)
                    val ownerEnd = rowText.indexOf("</a>", ownerStart + 9)

                    val repliesStart = rowText.indexOf("who_posted", ownerEnd)
                    val repliesEnd = rowText.indexOf("</a>", repliesStart + 10)

                    val lastPostTimeStart = rowText.indexOf("<td class=\"row2\"><span class=\"desc\"", repliesEnd)
                    val lastPostTimeEnd = rowText.indexOf("<br>", lastPostTimeStart + 35)

                    val lastPostByStart = rowText.indexOf("showuser=", lastPostTimeEnd)
                    val lastPostByEnd = rowText.indexOf("</a>", lastPostByStart + 9)

                    val title = rowText.substring(titleStart, titleEnd)
                        .let { it.substring(it.lastIndexOf(">") + 1) }

                    var owner = rowText.substring(ownerStart, ownerEnd)
                        .let { it.substring(it.lastIndexOf(">") + 1) }
                        .replace("'", "’")

                    changedUserNameList.firstOrNull { it.first == owner }?.let {
                        owner = it.second
                    }

                    val replies = rowText.substring(repliesStart, repliesEnd)
                        .let { it.substring(it.lastIndexOf(">") + 1) }

                    val lastPostTime = rowText.substring(lastPostTimeStart, lastPostTimeEnd)
                        .let { it.substring(it.lastIndexOf(">") + 1) }

                    var lastPostBy = rowText.substring(lastPostByStart, lastPostByEnd)
                        .let { it.substring(it.lastIndexOf(">") + 1) }
                        .replace("'", "’")

                    changedUserNameList.firstOrNull { it.first == lastPostBy }?.let {
                        lastPostBy = it.second
                    }

                    if (replies.toInt() > 0) {
                        updatePageList.add(
                            UpdatePage(
                                title,
                                owner,
                                lastPostTime.contains("seconds") || lastPostTime.contains("minutes") ||
                                        lastPostTime.contains("Today") || lastPostTime.contains("Yesterday"),
                                owner == lastPostBy
                            )
                        )
                    }
                } catch (exception: Exception) {
                }
            }
        }

        return updatePageList
    }

    private fun getPlayerPageList(teamList: List<Team>): List<PlayerPage> {
        val urlconn = URL("http://tpetracker.herokuapp.com/players_json").openConnection() as HttpURLConnection
        val instream = urlconn.inputStream
        val contents = String(instream.readAllBytes())

        return GsonBuilder().create().fromJson(
            contents,
            PlayerPageListResponse::class.java
        ).filter { playerPage ->
            teamList.map { team -> team.name }.contains(playerPage.team)
        }
    }

    private fun parseIsBlockingBack(playerPage: PlayerPage): Boolean {
        return if (playerPage.position == "RB") {
            val playerPageText = connect("http://nsfl.jcink.net/index.php?showtopic=${playerPage.id}")
                .body()
                .getElementsByClass("post-normal")[0]
                .toString()
            var lastIndex = 0
            var count = 0
            while (lastIndex != -1) {
                lastIndex = playerPageText.indexOf("blocking", lastIndex, true)
                if (lastIndex != -1) {
                    count++
                    lastIndex += 8
                }
            }
            count > 2
        } else {
            false
        }
    }

    private fun parsePageCount(document: String): Int {
        return try {
            val startIndex = document.indexOf("Pages:</a>")
            val endIndex = document.indexOf(")", startIndex)
            document.substring(startIndex, endIndex)
                .replace(Pattern.compile("[^0-9.]").toRegex(), "")
                .toInt()
        } catch (exception: Exception) {
            1
        }
    }

    private fun connect(url: String): Document {
        while (true) {
            try {
                return Jsoup.connect(url).get()
            } catch (exception: Exception) {
            }
        }
    }
}
