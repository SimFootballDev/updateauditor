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

        val playerSheetMatchList = ArrayList<Pair<PlayerPage, SheetPage>>()

        resultList.add("Player Missing From Sheet\n")
        playerPageList.forEach { playerPage ->

            val sheetPage = sheetPageList
                .firstOrNull { sheetPage ->

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

            if (sheetPage == null) {
                resultList.add("${playerPage.user} - ${playerPage.name} - ${playerPage.team}")
            } else {
                playerSheetMatchList.add(Pair(playerPage, sheetPage))
            }
        }
        resultList.add("\n--------------------------------------------------\n")

        resultList.add("Sheet Attribute Mismatches\n")
        playerSheetMatchList.forEach { match ->

            val mismatchList = ArrayList<String>()

            if (match.first.strength != match.second.strength) {
                mismatchList.add("strength mismatch")
            }
            if (match.first.agility != match.second.agility) {
                mismatchList.add("agility mismatch")
            }
            if (match.first.arm != match.second.arm) {
                mismatchList.add("arm mismatch")
            }
            if (match.first.intelligence != match.second.intelligence) {
                mismatchList.add("intelligence mismatch")
            }
            if (match.first.throwingAccuracy != match.second.throwingAccuracy) {
                mismatchList.add("throwingAccuracy mismatch")
            }
            if (match.first.tackling != match.second.tackling) {
                mismatchList.add("tackling mismatch")
            }
            if (match.first.speed != match.second.speed) {
                mismatchList.add("speed mismatch")
            }
            if (match.first.hands != match.second.hands) {
                mismatchList.add("hands mismatch")
            }
            if (match.first.passBlocking != match.second.passBlocking) {
                mismatchList.add("passBlocking mismatch")
            }
            if (match.first.runBlocking != match.second.runBlocking) {
                mismatchList.add("runBlocking mismatch")
            }
            if (match.first.endurance != match.second.endurance) {
                mismatchList.add("endurance mismatch")
            }
            if (match.first.kickPower != match.second.kickPower) {
                mismatchList.add("kickPower mismatch")
            }
            if (match.first.kickAccuracy != match.second.kickAccuracy) {
                mismatchList.add("kickAccuracy mismatch")
            }
            if (match.first.kickPower != match.second.puntPower) {
                mismatchList.add("puntPower mismatch")
            }
            if (match.first.kickAccuracy != match.second.puntAccuracy) {
                mismatchList.add("puntAccuracy mismatch")
            }

            if (mismatchList.isNotEmpty()) {
                resultList.add("${match.first.user} - ${match.first.name} - ${match.first.team} - ${mismatchList.joinToString()}")
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