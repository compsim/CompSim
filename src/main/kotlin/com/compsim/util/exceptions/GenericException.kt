package com.compsim.util.exceptions

import java.awt.Container
import javax.swing.JOptionPane

abstract class GenericException : Exception {

    open val exceptionDescription: String
        get() = "Generic Exception: " + this.message

    constructor()

    constructor(var1: String) : super(var1)

    fun showMessageDialog(var1: Container) {
        JOptionPane.showMessageDialog(var1, this.exceptionDescription)
//        com.adlerd.compsim.Console.println("Exception: " + this.exceptionDescription)
    }
}
