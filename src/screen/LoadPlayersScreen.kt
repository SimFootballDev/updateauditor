package screen

import UpdateAuditor
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.stage.Stage
import model.SheetPage
import model.Team
import model.getDSFLTeamList
import model.getNSFLTeamList

class LoadPlayersScreen {

    private val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("res/screen_load_players.fxml"))

    fun start(application: UpdateAuditor, primaryStage: Stage) {

        primaryStage.scene.root = root

        (root.lookup("#buttonLoadNSFL") as Button).setOnAction {
            onLoadClicked(application, getNSFLTeamList())
        }

        (root.lookup("#buttonLoadDSFL") as Button).setOnAction {
            onLoadClicked(application, getDSFLTeamList())
        }
    }

    private fun onLoadClicked(application: UpdateAuditor, teamList: List<Team>) {

        val sheetPageList = ((root.lookup("#textAreaPlayers") as TextArea).text)
            .split("\n").let { it.subList(1, it.size) }
            .map { player ->
                val attributes = player.split(",")
                SheetPage(
                    playerName = attributes[0] + " " + attributes[1].replace(" (R)", ""),
                    team = teamList.firstOrNull { it.simId == attributes[9] } ?: Team("", "", ""),
                    position = attributes[3],
                    experience = attributes[7].toIntOrNull() ?: 0,
                    strength = attributes[10].toIntOrNull() ?: 0,
                    agility = attributes[11].toIntOrNull() ?: 0,
                    arm = attributes[12].toIntOrNull() ?: 0,
                    intelligence = attributes[18].toIntOrNull() ?: 0,
                    throwingAccuracy = attributes[15].toIntOrNull() ?: 0,
                    tackling = attributes[20].toIntOrNull() ?: 0,
                    speed = attributes[13].toIntOrNull() ?: 0,
                    hands = attributes[14].toIntOrNull() ?: 0,
                    passBlocking = attributes[17].toIntOrNull() ?: 0,
                    runBlocking = attributes[16].toIntOrNull() ?: 0,
                    endurance = attributes[19].toIntOrNull() ?: 0,
                    kickPower = attributes[21].toIntOrNull() ?: 0,
                    kickAccuracy = attributes[22].toIntOrNull() ?: 0,
                    puntPower = attributes[23].toIntOrNull() ?: 0,
                    puntAccuracy = attributes[24].toIntOrNull() ?: 0
                )
            }

        val currentSeason = ((root.lookup("#textAreaCurrentSeason") as TextArea).text).toInt()

        application.onLoadClicked(sheetPageList, teamList, currentSeason)
    }
}