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
import java.util.regex.Pattern

class AuditUpdatesScreen {

    private val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("res/screen_audit_updates.fxml"))

    fun start(primaryStage: Stage, sheetPageList: List<SheetPage>) {

        primaryStage.scene.root = root

        (root.lookup("#scrollPane") as ScrollPane).content = Text("Processing...")
        (root.lookup("#scrollPane") as ScrollPane).vvalue = 0.0

        Thread {
            val resultList = auditUpdates(sheetPageList)
            Platform.runLater {
                (root.lookup("#scrollPane") as ScrollPane).content = Text(resultList.joinToString("\n"))
                (root.lookup("#scrollPane") as ScrollPane).vvalue = 0.0
            }
        }.start()
    }

    private fun auditUpdates(sheetPageList: List<SheetPage>): List<String> {

        val resultList = ArrayList<String>()

        val updatePageList = getUpdatePageList()
        val playerPageList = getPlayerPageList()

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

        resultList.add("Sheet Attribute Mismatches\n")
        playerSheetMatchList.forEach { match ->

            val listsOfMismatchList = ArrayList<ArrayList<String>>()

            match.second.forEach { sheetPage ->

                val mismatchList = ArrayList<String>()

                if (match.first.strength != sheetPage.strength) {
                    mismatchList.add("strength mismatch: ${match.first.strength} - ${sheetPage.strength}")
                }
                if (match.first.agility != sheetPage.agility) {
                    mismatchList.add("agility mismatch: ${match.first.agility} - ${sheetPage.agility}")
                }
                if (match.first.arm != sheetPage.arm) {
                    mismatchList.add("arm mismatch: ${match.first.arm} - ${sheetPage.arm}")
                }
                if (match.first.intelligence != sheetPage.intelligence) {
                    mismatchList.add("intelligence mismatch: ${match.first.intelligence} - ${sheetPage.intelligence}")
                }
                if (match.first.throwingAccuracy != sheetPage.throwingAccuracy) {
                    mismatchList.add("throwingAccuracy mismatch: ${match.first.throwingAccuracy} - ${sheetPage.throwingAccuracy}")
                }
                if (match.first.tackling != sheetPage.tackling) {
                    mismatchList.add("tackling mismatch: ${match.first.tackling} - ${sheetPage.tackling}")
                }
                if (match.first.speed != sheetPage.speed) {
                    mismatchList.add("speed mismatch: ${match.first.speed} - ${sheetPage.speed}")
                }
                if (match.first.hands != sheetPage.hands) {
                    mismatchList.add("hands mismatch: ${match.first.hands} - ${sheetPage.hands}")
                }
                if (match.first.passBlocking != sheetPage.passBlocking) {
                    mismatchList.add("passBlocking mismatch: ${match.first.passBlocking} - ${sheetPage.passBlocking}")
                }
                if (match.first.runBlocking != sheetPage.runBlocking) {
                    mismatchList.add("runBlocking mismatch: ${match.first.runBlocking} - ${sheetPage.runBlocking}")
                }
                if (match.first.endurance != sheetPage.endurance) {
                    mismatchList.add("endurance mismatch: ${match.first.endurance} - ${sheetPage.endurance}")
                }
                if (match.first.kickPower != sheetPage.kickPower) {
                    mismatchList.add("kickPower mismatch: ${match.first.kickPower} - ${sheetPage.kickPower}")
                }
                if (match.first.kickAccuracy != sheetPage.kickAccuracy) {
                    mismatchList.add("kickAccuracy mismatch: ${match.first.kickAccuracy} - ${sheetPage.kickAccuracy}")
                }
                if (match.first.kickPower != sheetPage.puntPower) {
                    mismatchList.add("puntPower mismatch: ${match.first.kickPower} - ${sheetPage.puntPower}")
                }
                if (match.first.kickAccuracy != sheetPage.puntAccuracy) {
                    mismatchList.add("puntAccuracy mismatch: ${match.first.kickAccuracy} - ${sheetPage.puntAccuracy}")
                }

                listsOfMismatchList.add(mismatchList)
            }

            listsOfMismatchList.minBy { it.size }?.let { mismatchList ->
                if (mismatchList.isNotEmpty()) {
                    resultList.add("${match.first.user} - ${match.first.name} - ${match.first.team} - ${mismatchList.joinToString()}")
                }
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        return resultList
    }

    private fun getUpdatePageList(): List<UpdatePage> {

        val changedUserNameList = arrayListOf(Pair("EnfysNest", "Baron1898"))

        val updatePageList = ArrayList<UpdatePage>()

        val documentList = ArrayList<Document>()
        Team.values().forEach {

            val firstDocument = connect("http://nsfl.jcink.net/index.php?showforum=${it.id}")
            documentList.add(firstDocument)

            val pageCount = parsePageCount(firstDocument.body().toString())

            for (i in 1..(pageCount - 1)) {
                documentList.add(connect("http://nsfl.jcink.net/index.php?showforum=${it.id}&st=${i * 15}"))
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

    private fun getPlayerPageList(): List<PlayerPage> {
        return GsonBuilder().create().fromJson(
            connect("http://tpetracker.herokuapp.com/players_json").text(),
            PlayerPageListResponse::class.java
        ).filter { playerPage ->
            Team.values().map { team -> team.name }.contains(playerPage.team)
        }
    }
}