package com.lucky3028.extendedwhitelist

import org.bukkit.plugin.java.JavaPlugin
import com.lucky3028.extendedwhitelist.commands.ExWhitelistCommand

class Main : JavaPlugin() {

    companion object {
        lateinit var PLUGIN: Main
            private set
    }

    override fun onEnable() {
        PLUGIN = this

        getCommand("exwl").executor = ExWhitelistCommand()
    }
}