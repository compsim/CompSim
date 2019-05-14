package com.adlerd.util

import com.adlerd.CompSim
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

object Logger {
    var isLogOpen = false
    val logFolder = File("${System.getProperty("user.home")}/.tcLogs/")
    var logWriter: PrintWriter? = null


    /**
     * Prints the given message to the standard output stream.
     */
    inline fun outputln(message: Any, shouldLog: Boolean = true) {
        if (shouldLog) {
            println(log(">>> $message"))
        } else {
            println(">>> $message")
        }
    }

    /**
     * Prints the given message to the standard output stream.
     */
    inline fun infoln(message: Any) {
        println(log("[ ${getTime()} INFO ]: $message"))
    }

    /**
     * Prints the given message to the standard output stream.
     * YES THE LINE NUMBER PORTION IS BROKEN!
     */
    inline fun debugln(message: Any?, classObject: Class<out Any>) {
        print(log("[ ${getTime()} DEBUG ]: (${classObject.name.split('.').asReversed()[0]}: Line ${Thread.currentThread().stackTrace[1].lineNumber - 9} ) $message"))
    }

    /**
     * Prints the given message to the standard output stream.
     */
    inline fun warningln(message: Any?) {
        System.err.println(log("[ ${getTime()} WARNING ]: $message"))
    }

    /**
     * Prints the given message to the standard output stream.
     */
    inline fun errorln(message: Any?, exit: Boolean = false, status: Int = -1) {
        if (exit) {
            System.err.println(log("[ ${getTime()} FATAL ERROR ]: $message"))
            exitProcess(status = status)
        } else System.err.println(log("[ ${getTime()} FATAL ERROR ]: $message"))
    }

    fun getTime(): String {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    fun getDate(): String {
        val rawTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val time = StringBuilder()
        for (char in rawTime) {
            if (char == ':') {
                time.append('_')
            } else {
                time.append(char)
            }
        }

        return time.toString()
    }

    /**
     * Initializes the output stream and file for logging
     *
     * This should be declared on the first line of your main method for easy use
     */
    fun start() {
//        if (!isLogOpen) {
            if (!logFolder.exists()) {
                assert(logFolder.mkdirs())
            }

            try {
                isLogOpen = true
//                logWriter = PrintWriter("${logFolder.absolutePath}/tcsim ${getDate()} ${getTime()}.txt")
                logWriter = PrintWriter("${CompSim.TITLE} ${getDate()} ${getTime()}.txt")
                debugln("Successfully created log writer!", this::class.java)
            } catch (e: IOException) {
                errorln("Failed to initialize the log file at $logFolder", true)
            }
//        } else if (isLogOpen) {
//            debugln("Log is already open!", this::class.java)
//        } else {
//            warningln("Unknown log state!")
//            if (logWriter != null) {
//                logWriter?.close()
//            }
//            isLogOpen = false
//        }
    }

    fun close() {
        logWriter?.close()
    }

    fun log(message: Any?): Any {
        if (logWriter != null) {
            logWriter?.println(message.toString())
        }
        return message!!
    }
}