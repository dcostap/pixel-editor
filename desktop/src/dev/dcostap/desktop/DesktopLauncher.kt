package dev.dcostap.desktop

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import dev.dcostap.Main
import dev.dcostap.AssetsExporter

/** Launches the desktop (LWJGL3) application.  */
object DesktopLauncher {

    fun main(arg: Array<String>) {
        Main.launchDesktopApp()
    }
}

object UpdateAssets {

    fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(10, 10)
        }

        Lwjgl3Application(object : ApplicationAdapter() {
            override fun create() {
                AssetsExporter.exportAssets()
                Gdx.app.exit()
            }
        }, config)
    }
}