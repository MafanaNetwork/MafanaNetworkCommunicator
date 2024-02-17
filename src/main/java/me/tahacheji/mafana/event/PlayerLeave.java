package me.tahacheji.mafana.event;

import me.tahacheji.mafana.MafanaNetworkCommunicator;
import me.tahacheji.mafana.data.OfflineProxyPlayer;
import me.tahacheji.mafana.data.ProxyPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerLeave implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CompletableFuture<ProxyPlayer> getProxyPlayerFuture = MafanaNetworkCommunicator.getInstance()
                .getNetworkCommunicatorDatabase()
                .getProxyPlayerAsync(player);
        getProxyPlayerFuture.thenAccept(proxyPlayer -> {
            if (proxyPlayer != null) {
                OfflineProxyPlayer offlineProxyPlayer = new OfflineProxyPlayer(
                        player.getUniqueId().toString(),
                        player.getName(),
                        player.getDisplayName(),
                        proxyPlayer.getServerID().toString(),
                        proxyPlayer.getServerName()
                );
                MafanaNetworkCommunicator.getInstance().getPlayerDatabase().setOfflineProxyPlayer(player.getUniqueId(), offlineProxyPlayer);
            }
        }).thenRun(() -> {
            MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().unregisterOnlinePlayer(player);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

}
