package me.tahacheji.mafana.event;

import me.tahacheji.mafana.MafanaNetworkCommunicator;
import me.tahacheji.mafana.data.OfflineProxyPlayer;
import me.tahacheji.mafana.data.ProxyPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CompletableFuture<Void> registerOnlinePlayerFuture = MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().registerOnlinePlayer(player);
        CompletableFuture<Void> addPlayerFuture = MafanaNetworkCommunicator.getInstance().getPlayerDatabase().addPlayer(player);
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(registerOnlinePlayerFuture, addPlayerFuture);
        combinedFuture.thenCompose(voidResult -> MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().getProxyPlayerAsync(player).thenAccept(proxyPlayer -> {
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
        })).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

}
