package com.lucky3028.extendedwhitelist.utils

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

fun logInfo(msg: String) {Bukkit.getLogger().info(msg)}

fun logError(msg: String) {Bukkit.getLogger().severe(msg)}

fun sendMsg(sender: CommandSender, msg: String) {sender.sendMessage(msg)}