package me.tahacheji.mafana.data;

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
