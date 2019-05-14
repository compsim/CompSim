package com.adlerd.gui.fx.layout

import com.adlerd.Machine
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane

class RegistersPane(machine: Machine): TitledPane() {
    // Register Table Values
    val rootPane = BorderPane()
    val regGrid = GridPane()
    val r0 = RegField("x0000")
    val r1 = RegField("x0000")
    val r2 = RegField("x0000")
    val r3 = RegField("x0000")
    val r4 = RegField("x0000")
    val r5 = RegField("x0000")
    val r6 = RegField("x0000")
    val r7 = RegField("x0000")
    val pc = RegField("x0200")
    val mpr = RegField("x0000")
    val psr = RegField("x8002")
    val cc = RegField("Z")


    init {
        //TODO: Remove unnecessary hard-coded strings
        this.content = rootPane

        regGrid.isGridLinesVisible = true
        regGrid.addColumn(0, RegLabel("R0"), RegLabel("R1"), RegLabel("R2"), RegLabel("R3"), RegLabel("R4"), RegLabel("R5"))
        regGrid.addColumn(1, r0, r1, r2, r3, r4, r5)
        regGrid.addColumn(2, RegLabel("R6"), RegLabel("R7"), RegLabel("PC"), RegLabel("MPR"), RegLabel("PSR"), RegLabel("CC"))
        regGrid.addColumn(3, r6, r7, pc, mpr, psr, cc)

        regGrid.isGridLinesVisible = true
        this.text = "Registers"
        this.isCollapsible = false
        this.isAnimated = false
        this.minHeight = 180.0
        this.prefHeight = 180.0
        rootPane.center = regGrid
    }



    inner class RegLabel(label: String): Label() {

        init {
            this.text = label
            this.alignment = Pos.CENTER_LEFT
            this.padding = Insets(2.0)
            this.minHeight = 20.0
            this.prefHeight = 20.0
            this.maxHeight = 20.0
            this.minWidth = 35.0
            this.prefWidth = 35.0
            this.maxWidth = 35.0
        }

    }

    inner class RegField(content: String): TextField() {
        constructor(): this(content = "")

        init {
            this.text = content
            this.alignment = Pos.CENTER_RIGHT
            this.setMinSize(90.0, 25.0)
            this.setPrefSize(90.0, 25.0)
            this.setMaxSize(90.0, 25.0)
        }

    }
}