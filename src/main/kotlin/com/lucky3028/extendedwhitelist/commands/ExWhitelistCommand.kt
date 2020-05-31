package com.lucky3028.extendedwhitelist.commands

import com.lucky3028.extendedwhitelist.utils.*
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.OfflinePlayer
import java.io.IOException
import java.net.URL
import java.lang.Exception
import org.bukkit.ChatColor
import org.bukkit.command.TabExecutor
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.json.simple.parser.ParseException
import java.nio.charset.Charset

@Suppress("Deprecation")
class ExWhitelistCommand : TabExecutor {
    override fun onTabComplete(p0: CommandSender?, p1: Command?, p2: String?, p3: Array<out String>?): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        //引数がない場合終了
        if (args.isEmpty()) {
            sendMsg(sender, "${ChatColor.RED}" + "引数が指定されていないためコマンドを実行できませんでした")
            sendMsg(sender, "${ChatColor.RED}" + "引数の詳細については「/exwl help」で確認してください")
            return true
        }

        //add, rem, checkで想定する引数の最大数が11のため、12個以上引数がある場合はは処理を終了する
        if (args.size >= 12) {
            sendMsg(sender, "${ChatColor.RED}" + "11名以上のプレイヤーを同時に処理することは出来ません")
            return true
        }

        //第2引数以下はMCIDが指定されていることを想定しているので、第2引数及びそれ以降の引数の値を取得する
        //なお、argsのインデックスは0から始まるので、第2引数はインデックス1。したがって、引数のArrayをインデックス1以降でフィルタしている
        //.toSet().toList()となっているのは、Setのままだと処理速度が遅いため
        //TODO: >= 1のときに正常に作動するか？
        val specifiedMcids = args.filterIndexed { idx, _ -> idx > 0 }.toSet().toList()
        val specifiedMcidsSize = specifiedMcids.size

        val wlPlayers = Bukkit.getWhitelistedPlayers().toList()

        when (args[0]) {
            "help" -> {
                val helpMsg = listOf(
                        "${ChatColor.YELLOW}${ChatColor.BOLD}" + "===ExtendedWhitelist コマンドリファレンス===",
                        "${ChatColor.RED}" + "/exwl on",
                        "ホワイトリストを有効にします",
                        "${ChatColor.RED}" + "/exwl off",
                        "ホワイトリストを無効にします",
                        "${ChatColor.RED}" + "/exwl reload",
                        "ホワイトリストを再読込みします",
                        "${ChatColor.RED}" + "/exwl list",
                        "ホワイトリストに登録されているMCIDを一覧表示します",
                        "${ChatColor.RED}" + "/exwl add [MCID] [MCID] ... [MCID]",
                        "指定したMCIDをホワイトリストに追加します。最大で10人を同時に追加できます",
                        "${ChatColor.RED}" + "/exwl [remove/rem] [MCID] [MCID] ... [MCID]",
                        "指定したMCIDをホワイトリストから削除します。最大で10人を同時に削除できます",
                        "${ChatColor.RED}" + "/exwl clear",
                        "ホワイトリストに登録されたMCIDをすべて削除します",
                        "${ChatColor.RED}" + "/exwl [check/chk] [MCID] [MCID] ... [MCID]",
                        "指定したMCIDがホワイトリストに登録されているか確認します。最大で10人を同時に処理できます"
                )
                helpMsg.forEach {sendMsg(sender, it)}
            }

            "on" -> {
                Bukkit.setWhitelist(true)
                logInfo("${ChatColor.GREEN}" + "ホワイトリストをオンにしました")
                sendMsg(sender, "${ChatColor.GREEN}" + "ホワイトリストをオンにしました")
            }
            "off" -> {
                Bukkit.setWhitelist(false)
                logInfo("${ChatColor.GREEN}" + "ホワイトリストをオフにしました")
                sendMsg(sender, "${ChatColor.GREEN}" + "ホワイトリストをオフにしました")
            }

            "reload" -> {
                Bukkit.reloadWhitelist()
                logInfo("ホワイトリストを再読み込みしました")
            }

            //ホワイトリストに登録されているプレイヤーを一覧表示する
            "list" -> {
                val wlSize = wlPlayers.size

                if (wlSize == 0) {
                    sendMsg(sender, "ホワイトリストに登録されているプレイヤーはいません")
                    return true
                }

                var wlPlayersNames = ""
                //出力するプレイヤー一覧を作成
                wlPlayers.withIndex().forEach { (index, wlPlayer) ->
                    val separator = if (index == wlSize - 1) " and " else ", "
                    wlPlayersNames += separator + wlPlayer.name
                }

                wlPlayersNames = wlPlayersNames.removePrefix(", ")
                wlPlayersNames = wlPlayersNames.removePrefix(" and ")

                sendMsg(sender, "ホワイトリストには${wlSize}名のプレイヤーが登録されています：")
                sendMsg(sender, wlPlayersNames)
            }

            "add" -> {
                sendMsg(sender, "${ChatColor.GREEN}" + "MCIDが有効かどうか確認するために1秒ほど時間がかかります。ご留意ください")

                //MCIDが引数に指定されていない場合終了
                if (specifiedMcidsSize == 0) {
                    sendMsg(sender, "${ChatColor.RED}" + "MCIDが指定されていません")
                    return false
                }

                specifiedMcids.forEach {
                    val player = getOfflinePlayer(it) ?: run {
                        sendMsg(sender, "${ChatColor.RED}" + "指定されたMCID（${it}）を取得できませんでした")
                        return@forEach
                    }

                    player.isWhitelisted = true
                    sendMsg(sender, "${ChatColor.GREEN}" + "${player.name}をホワイトリストに追加しました")
                }
            }
            "remove", "rem" -> {
                sendMsg(sender, "${ChatColor.GREEN}" + "MCIDが有効かどうか確認するために1秒ほど時間がかかります。ご留意ください")

                //MCIDが引数に指定されていない場合終了
                if (specifiedMcidsSize == 0) {
                    sendMsg(sender, "${ChatColor.RED}" + "MCIDが指定されていません")
                    return false
                }

                specifiedMcids.forEach {
                    val player = getOfflinePlayer(it) ?: run {
                        sendMsg(sender, "${ChatColor.RED}" + "指定されたMCID（${it}）を取得できませんでした")
                        return@forEach
                    }

                    player.isWhitelisted = false
                    sendMsg(sender, "${ChatColor.GREEN}" + "${player.name}をホワイトリストから削除しました")
                }
            }
            //指定されたMCIDがリストの中に存在するか
            "check", "chk" -> {
                sendMsg(sender, "${ChatColor.GREEN}" + "MCIDが有効かどうか確認するために1秒ほど時間がかかります。ご留意ください")

                //MCIDが引数に指定されていない場合終了
                if (specifiedMcidsSize == 0) {
                    sendMsg(sender, "${ChatColor.RED}" + "MCIDが指定されていません")
                    return false
                }

                specifiedMcids.forEach {
                    val player = getOfflinePlayer(it) ?: run {
                        sendMsg(sender, "${ChatColor.RED}" + "指定されたMCID（${it}）を取得できませんでした")
                        return@forEach
                    }

                    when (player.isWhitelisted) {
                        true -> sendMsg(sender, "指定されたMCID（${player.name}）はホワイトリストに登録されています")
                        false -> sendMsg(sender, "指定されたMCID（${player.name}）はホワイトリストに登録されていません")
                    }
                }
            }

            //ホワイトリストに登録されたプレイヤーをすべて削除
            "clear"-> {
                wlPlayers.forEach {it.isWhitelisted = false}
                sendMsg(sender, "ホワイトリストに登録されたMCIDをすべて削除しました")
            }

            else -> {
                sendMsg(sender, "${ChatColor.RED}" + "適切な引数が指定されていないためコマンドを実行できませんでした")
                sendMsg(sender, "${ChatColor.RED}" + "引数の詳細については「/exwl help」で確認してください")
            }
        }

        return true
    }

    /**
     * mcidExists関数の結果を受け取り、OfflinePlayer型にして返す
     * @param name 確認したいMCID
     * @return OfflinePlayer? 存在しない場合にnullを返す
     */
    private fun getOfflinePlayer(name: String) :OfflinePlayer? {
        val checkedMcid = mcidExists(name) ?: return null
        return Bukkit.getOfflinePlayer(checkedMcid)
    }

    /**
     * MCIDが存在するかを確認し、その真偽値と存在する場合はMCIDを返す。
     * WARN: MojangのAPIにアクセスしているので、1つのMCIDを確認するのに1秒ほど時間がかかる。
     * @param name 確認したいMCID
     * @return String? 存在するならばUUID.toString、しなければnull
     */
    private fun mcidExists(name: String): String? {
        val mojangApiUrl = URL("https://api.mojang.com/users/profiles/minecraft/${name}")
        try {
            val mojangApiStream = mojangApiUrl.openStream()
            //URL先のJSONの内容をStringで取得
            val resMojangApi = mojangApiStream.readBytes().toString(Charset.defaultCharset())
            val parsedProfile = JSONValue.parseWithException(resMojangApi) as JSONObject
            return parsedProfile["name"].toString()
        } catch (e: Exception) {
            when (e) {
                is IOException, is ParseException -> {
                    logError("${ChatColor.RED}" + "MCIDの取得中にエラーが発生しました。以下にエラー文を表示します")
                    logError(e.toString())
                }
            }
        }
        return null
    }
}