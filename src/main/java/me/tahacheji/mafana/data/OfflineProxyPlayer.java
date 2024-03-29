package me.tahacheji.mafana.data;

import me.tahacheji.mafana.MafanaNetworkCommunicator;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OfflineProxyPlayer {

    private String playerUUID;
    private String playerName;
    private String playerDisplayName;
    private String lastServerID;
    private String lastServerName;

    public OfflineProxyPlayer(String playerUUID, String playerName, String playerDisplayName, String lastServerID, String lastServerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.playerDisplayName = playerDisplayName;
        this.lastServerID = lastServerID;
        this.lastServerName = lastServerName;
    }

    public CompletableFuture<ProxyPlayer> getProxyPlayer() {
        return MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().getProxyPlayerAsync(UUID.fromString(playerUUID));
    }

    public CompletableFuture<Boolean> isOnline() {
        return getProxyPlayer().thenApplyAsync(Objects::nonNull);
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerDisplayName() {
        return playerDisplayName;
    }

    public String getLastServerID() {
        return lastServerID;
    }

    public String getLastServerName() {
        return lastServerName;
    }
}
