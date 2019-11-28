package model

class PlayerPage(
    val id: Int,
    val user: String,
    val name: String,
    val team: String,
    val position: String,
    val draftYear: String,
    val currentTPE: Int,
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
) {

    fun calculateExperience(currentSeason: Int, blockingBack: Boolean): Int {
        return if (blockingBack) {
            12
        } else {
            when (position) {
                "WR", "TE" -> 12
                "S" -> {
                    3 + (currentTPE / 150)
                }
                "CB" -> {
                    5 + (currentTPE / 150)
                }
                else -> {
                    currentSeason - getDraftSeason() + 1
                }
            }
        }
    }

    private fun getDraftSeason() = draftYear.substring(1).toIntOrNull() ?: -999
}