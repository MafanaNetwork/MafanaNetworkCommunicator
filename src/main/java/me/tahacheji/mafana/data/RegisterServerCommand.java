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
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().registerServer(serverName, null);
        sender.sendMessage(ChatColor.GREEN + "You have registered this server.");
    }

    @Command(names = "mnc setNickName", permission = "mafana.admin", playerOnly = true)
    public void setNickName(CommandSender sender, @Param(name = "nickName") String nickName) {
        Player player = (Player) sender;
        ProxyPlayer proxyPlayer = MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().getProxyPlayer(player);
        if(proxyPlayer != null) {
            MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().setServerNickName(proxyPlayer, nickName);
        }
    }

    @Command(names = "mnc addValue", permission = "mafana.admin", playerOnly = true)
    public void addServerValue(CommandSender sender, @Param(name = "value") String value) {
        Player player = (Player) sender;
        ProxyPlayer proxyPlayer = MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().getProxyPlayer(player);
        if(proxyPlayer != null) {
            MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().addServerValue(proxyPlayer.getServerID(), value);
        }
    }

    @Command(names = "mnc removeValue", permission = "mafana.admin", playerOnly = true)
    public void removeServerValue(CommandSender sender, @Param(name = "value") String value) {
        Player player = (Player) sender;
        ProxyPlayer proxyPlayer = MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().getProxyPlayer(player);
        if(proxyPlayer != null) {
            MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().removeServerValue(proxyPlayer.getServerID(), value);
        }
    }



}
