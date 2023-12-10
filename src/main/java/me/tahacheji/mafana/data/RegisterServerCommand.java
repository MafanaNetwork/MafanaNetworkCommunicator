package me.tahacheji.mafana.data;

import me.tahacheji.mafana.MafanaNetworkCommunicator;
import me.tahacheji.mafana.commandExecutor.Command;
import me.tahacheji.mafana.commandExecutor.paramter.Param;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RegisterServerCommand {

    @Command(names = "mnc registerServer", permission = "mafana.admin", playerOnly = false)
    public void registerServer(CommandSender sender, @Param(name = "serverName") String serverName) {
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().registerServer(serverName);
        sender.sendMessage(ChatColor.GREEN + "You have registered this server.");
    }

}
