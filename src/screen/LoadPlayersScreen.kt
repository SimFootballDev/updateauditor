package screen

import UpdateAuditor
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.stage.Stage
import model.SheetPage

class LoadPlayersScreen {

    private val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("res/screen_load_players.fxml"))

    fun start(application: UpdateAuditor, primaryStage: Stage) {

        primaryStage.scene.root = root

        (root.lookup("#buttonLoad") as Button).setOnAction {
            application.onLoadClicked(
                ((root.lookup("#textAreaPlayers") as TextArea).text)
                    .split("\n").let { it.subList(1, it.size) }
                    .map { player ->
                        val attributes = player.split(",")
                        SheetPage(
                            attributes[0] + " " + attributes[1].replace(" (R)", ""),
                            strength = attributes[10].toInt(),
                            agility = attributes[11].toInt(),
                            arm = attributes[12].toInt(),
                            intelligence = attributes[18].toInt(),
                            throwingAccuracy = attributes[15].toInt(),
                            tackling = attributes[20].toInt(),
                            speed = attributes[13].toInt(),
                            hands = attributes[14].toInt(),
                            passBlocking = attributes[17].toInt(),
                            runBlocking = attributes[16].toInt(),
                            endurance = attributes[19].toInt(),
                            kickPower = attributes[21].toInt(),
                            kickAccuracy = attributes[22].toInt(),
                            puntPower = attributes[23].toInt(),
                            puntAccuracy = attributes[24].toInt()
                        )
                    }
            )
        }
    }
}