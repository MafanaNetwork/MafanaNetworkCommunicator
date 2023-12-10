package me.tahacheji.mafana.event;

import me.tahacheji.mafana.MafanaNetworkCommunicator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().registerOnlinePlayer(event.getPlayer());
    }
}
