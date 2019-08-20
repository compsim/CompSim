package com.compsim.gui.fxml

import com.adlerd.compsim.CompSim
import com.adlerd.compsim.gui.fx.CompSimGUI
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class CompSimTestGUI: Application() {

    lateinit var window: Stage

    override fun start(primaryStage: Stage) {
        window = primaryStage

        val root = FXMLLoader.load<Parent>(CompSimGUI::class.java.getResource("/CompSimGUI.fxml"))
        val scene = Scene (root)
        scene.stylesheets.add(CompSimGUI::class.java.getResource("/icons.css").toExternalForm())


        window.scene = scene
        window.title = "CompSim FXML ${CompSim.VERSION}"
        window.show()
    }
}