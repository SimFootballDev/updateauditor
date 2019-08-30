import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.regex.Pattern

fun main() {

    //Update Page Not Found
    //Update Page Not Updated
    //Player Page Not Updated
    //Sheet Not Updated

    val updatePageList = getUpdatePageList()
    val playerPageList = getPlayerPageList()

    playerPageList.forEach { playerPage ->
        val updatePage = updatePageList.firstOrNull { updatePage -> updatePage.user == playerPage.user }
        if (updatePage == null) {
            System.out.println("Update Page Not Found: ${playerPage.user}")
        }
    }

    System.out.println("--------------------------------------------------")

    updatePageList.forEach { updatePage ->
        if (!updatePage.lastPostIsRecent && updatePage.lastPostIsFromOwner) {
            System.out.println("Update Page Not Updated: ${updatePage.user}")
        }
    }

    System.out.println("--------------------------------------------------")

    updatePageList.forEach { updatePage ->
        if (updatePage.lastPostIsRecent) {
            playerPageList.firstOrNull { playerPage -> playerPage.user == updatePage.user }?.let { playerPage ->
                if (playerPage.tpeHistoryList[playerPage.tpeHistoryList.lastIndex].second ==
                    playerPage.tpeHistoryList[playerPage.tpeHistoryList.lastIndex - 1].second
                ) {
                    System.out.println("Player Page Not Updated: ${updatePage.user}")
                }
            }
        }
    }

    System.out.println("--------------------------------------------------")
}

private fun getUpdatePageList(): List<UpdatePage> {

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
                val ownerStart = rowText.indexOf("showuser=")
                val ownerEnd = rowText.indexOf("</a>", ownerStart + 9)

                val repliesStart = rowText.indexOf("who_posted", ownerEnd)
                val repliesEnd = rowText.indexOf("</a>", repliesStart + 10)

                val lastPostTimeStart = rowText.indexOf("<td class=\"row2\"><span class=\"desc\"", repliesEnd)
                val lastPostTimeEnd = rowText.indexOf("<br>", lastPostTimeStart + 35)

                val lastPostByStart = rowText.indexOf("showuser=", lastPostTimeEnd)
                val lastPostByEnd = rowText.indexOf("</a>", lastPostByStart + 9)

                val owner = rowText.subSequence(ownerStart, ownerEnd).let { it.substring(it.lastIndexOf(">") + 1) }
                val replies =
                    rowText.subSequence(repliesStart, repliesEnd).let { it.substring(it.lastIndexOf(">") + 1) }
                val lastPostTime =
                    rowText.subSequence(lastPostTimeStart, lastPostTimeEnd)
                        .let { it.substring(it.lastIndexOf(">") + 1) }
                val lastPostBy =
                    rowText.subSequence(lastPostByStart, lastPostByEnd).let { it.substring(it.lastIndexOf(">") + 1) }

                if (replies.toInt() > 0) {
                    updatePageList.add(
                        UpdatePage(
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

private class UpdatePage(
    val user: String,
    val lastPostIsRecent: Boolean,
    val lastPostIsFromOwner: Boolean
)

private class PlayerPage(
    val user: String,
    val team: String,
    val tpeHistoryList: ArrayList<Pair<String, String>>,
    val strength: Int,
    val agility: Int,
    val arm: Int,
    val intelligence: Int,
    val throwingAccuracy: Int,
    val tackling: Int,
    val speed: Int,
    val hands: Int,
    val passBlocking: Int,
    val runBlocking: Int,
    val endurance: Int,
    val kickPower: Int,
    val kickAccuracy: Int
)

private class SheetPage(
    val user: String,
    val strength: Int,
    val agility: Int,
    val arm: Int,
    val intelligence: Int,
    val throwingAccuracy: Int,
    val tackling: Int,
    val speed: Int,
    val hands: Int,
    val passBlocking: Int,
    val runBlocking: Int,
    val endurance: Int,
    val kickPower: Int,
    val kickAccuracy: Int
)

private class PlayerPageListResponse : ArrayList<PlayerPage>()

private enum class Team(val id: String) {
    BALTIMORE_HAWKS("70"),
    CHICAGO_BUTCHERS("325"),
    COLORADO_YETI("56"),
    PHILADELPHIA_LIBERTY("111"),
    YELLOWKNIFE_WRAITHS("58"),
    ARIZONA_OUTLAWS("73"),
    AUSTIN_COPPERHEADS("328"),
    NEW_ORLEANS_SECOND_LINE("114"),
    ORANGE_COUNTY_OTTERS("54"),
    SAN_JOSE_SABERCATS("52")
}