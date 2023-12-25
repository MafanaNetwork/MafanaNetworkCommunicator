package me.tahacheji.mafana.event;

import me.tahacheji.mafana.MafanaNetworkCommunicator;
import me.tahacheji.mafana.data.OfflineProxyPlayer;
import me.tahacheji.mafana.data.ProxyPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeave implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ProxyPlayer proxyPlayer = MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().getProxyPlayer(player);
        MafanaNetworkCommunicator.getInstance().getPlayerDatabase().setOfflineProxyPlayer(event.getPlayer().getUniqueId(),
                new OfflineProxyPlayer(player.getUniqueId().toString(), player.getName(), player.getDisplayName(), proxyPlayer.getServerID().toString(), proxyPlayer.getServerName()));
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().unregisterOnlinePlayer(event.getPlayer());
    }
}
