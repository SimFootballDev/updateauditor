package model

class PlayerPage(
    val user: String,
    val name: String,
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