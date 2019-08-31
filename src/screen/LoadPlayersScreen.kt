package screen

import UpdateAuditor
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.stage.Stage

class LoadPlayersScreen {

    private val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("res/screen_load_players.fxml"))

    fun start(application: UpdateAuditor, primaryStage: Stage) {

        primaryStage.scene.root = root

        (root.lookup("#buttonLoad") as Button).setOnAction {

            // fixme: parse csv
//            ((root.lookup("#textAreaPlayers") as TextArea).text).split("\n").map {
//                val attributes = it.split(",")
//                ""
//            }

            application.onLoadClicked()
        }
    }
}