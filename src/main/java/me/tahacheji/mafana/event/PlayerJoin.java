package me.tahacheji.mafana.event;

import me.tahacheji.mafana.MafanaNetworkCommunicator;
import me.tahacheji.mafana.data.OfflineProxyPlayer;
import me.tahacheji.mafana.data.ProxyPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().registerOnlinePlayer(event.getPlayer());
        MafanaNetworkCommunicator.getInstance().getPlayerDatabase().addPlayer(event.getPlayer());
        ProxyPlayer proxyPlayer = MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().getProxyPlayer(player);
        MafanaNetworkCommunicator.getInstance().getPlayerDatabase().setOfflineProxyPlayer(event.getPlayer().getUniqueId(),
                new OfflineProxyPlayer(player.getUniqueId().toString(), player.getName(), player.getDisplayName(), proxyPlayer.getServerID().toString(), proxyPlayer.getServerName()));
    }
}
