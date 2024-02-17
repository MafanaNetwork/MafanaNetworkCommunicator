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
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase()
                .registerServer(serverName, null)
                .thenAcceptAsync(result -> {
                    sender.sendMessage(ChatColor.GREEN + "You have registered this server.");
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    @Command(names = "mnc setNickName", permission = "mafana.admin", playerOnly = true)
    public void setNickName(CommandSender sender, @Param(name = "nickName") String nickName) {
        Player player = (Player) sender;
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase()
                .getProxyPlayerAsync(player)
                .thenAcceptAsync(proxyPlayer -> {
                    if(proxyPlayer != null) {
                        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().setServerNickNameAsync(proxyPlayer, nickName);
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    @Command(names = "mnc addValue", permission = "mafana.admin", playerOnly = true)
    public void addServerValue(CommandSender sender, @Param(name = "value") String value) {
        Player player = (Player) sender;
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase()
                .getProxyPlayerAsync(player)
                .thenAcceptAsync(proxyPlayer -> {
                    if(proxyPlayer != null) {
                        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().addServerValueAsync(proxyPlayer.getServerID(), value);
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    @Command(names = "mnc removeValue", permission = "mafana.admin", playerOnly = true)
    public void removeServerValue(CommandSender sender, @Param(name = "value") String value) {
        Player player = (Player) sender;
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase()
                .getProxyPlayerAsync(player)
                .thenAcceptAsync(proxyPlayer -> {
                    if(proxyPlayer != null) {
                        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().removeServerValueAsync(proxyPlayer.getServerID(), value);
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    @Command(names = "mnc list", permission = "mafana.admin", playerOnly = true)
    public void listServers(CommandSender sender) {
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase()
                .getAllServersAsync()
                .thenAcceptAsync(servers -> {
                    for(Server server : servers) {
                        sender.sendMessage(server.getServerID());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }




}
