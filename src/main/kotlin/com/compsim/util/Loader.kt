package com.compsim.util

import com.compsim.util.Logger.errorln
import javafx.scene.image.Image
import java.lang.NullPointerException
import java.net.URL

object Loader {
    private const val ERROR_IMAGE = ""

    /**
     * Super simple function to make loading images slightly easier
     * @param path path the the desired image (ex. "compsim.ico" -> icon used in the Windows executable)
     * @return Image object from input
     */
    fun loadRes(path: String): String {
        try {
            return this::class.java.getResource("/$path").toString()
        } catch (npe: NullPointerException) {
            errorln("Failed to locate resource at ${this::class.java.getResource("")}$path")
        }

        // If image load fails load generic replacement image
        return ""
    }

    /**
     * Super simple function to make loading images slightly easier
     * @param path path the the desired image (ex. "compsim.ico" -> icon used in the Windows executable)
     * @return Image object from input
     */
    fun loadImg(path: String): Image {
        try {
            return Image(this::class.java.getResource("/$path").toString())
        } catch (npe: NullPointerException) {
            errorln("Failed to locate image at ${this::class.java.getResource("")}$path")
        }

        // If image load fails load generic replacement image
        return Image(this::class.java.getResource(ERROR_IMAGE).toString())
    }

    /**
     * Easier way to load an image with a specific width
     * @param path path the the desired image (ex. "compsim.ico" -> icon used in the Windows executable)
     * @param width width of the image to create in pixels
     * @param height height of the image to create in pixels
     * @return Image object from input
     */
    fun loadImg(path: String, width: Double, height: Double): Image {
        try {
            return Image(this::class.java.getResource("/$path").toString(), width, height, true, false)
        } catch (npe: NullPointerException) {
            errorln("Failed to locate image at ${this::class.java.getResource("")}$path")
        }

        // If image load fails load generic replacement image
        return Image(this::class.java.getResource(ERROR_IMAGE).toString(), width, height, true, false)
    }

    /**
     * Easier way to load a square image (ideal for icons and square buttons)
     * @param path path the the desired image (ex. "compsim.ico" -> icon used in the Windows executable)
     * @param size size to use for width and height of the image to create in pixels
     * @return Image made of the
     */
    fun loadImg(path: String, size: Double): Image {
        try {
            return Image(this::class.java.getResource("/$path").toString(), size, size, true, false)
        } catch (npe: NullPointerException) {
            errorln("Failed to locate image at ${this::class.java.getResource("")}$path")
        }

        // If image load fails load generic replacement image
        return Image(this::class.java.getResource(ERROR_IMAGE).toString(), size, size, true, false)
    }
}