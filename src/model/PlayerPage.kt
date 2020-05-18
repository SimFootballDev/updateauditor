package model

import kotlin.math.floor
import kotlin.math.max

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

    fun calculateExperience(currentSeason: Int, blockingBack: Boolean, auditingDsfl: Boolean): Int {
        return if (blockingBack) {
            12
        } else {
            when (position) {
                "WR", "TE" -> 12
                "S" -> {
                    if (auditingDsfl && currentTPE >= 250) {
                        4
                    } else {
                        3 + floor((currentTPE.toFloat() - 1F) / 150F).toInt()
                    }
                }
                "CB" -> {
                    if (auditingDsfl && currentTPE >= 250) {
                        6
                    } else {
                        5 + floor((currentTPE.toFloat() - 1F) / 150F).toInt()
                    }
                }
                else -> {
                    max(currentSeason - getDraftSeason() + 1, 0)
                }
            }
        }
    }

    private fun getDraftSeason() = draftYear.substring(1).toIntOrNull() ?: -999
}
