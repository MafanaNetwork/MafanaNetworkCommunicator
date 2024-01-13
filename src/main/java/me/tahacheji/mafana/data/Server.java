package me.tahacheji.mafana.data;

import java.util.ArrayList;
import java.util.List;

public class Server {

    private String serverID;
    private List<ProxyPlayer> onlinePlayers = new ArrayList<>();
    private List<String> serverValues = new ArrayList<>();
    private String serverNickName;

    public Server(String serverID, List<ProxyPlayer> onlinePlayers, List<String> serverValues, String serverNickName) {
        this.serverID = serverID;
        this.onlinePlayers = onlinePlayers;
        this.serverValues = serverValues;
        this.serverNickName = serverNickName;
    }

    public String getServerID() {
        return serverID;
    }

    public List<ProxyPlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public List<String> getServerValues() {
        return serverValues;
    }

    public String getServerNickName() {
        return serverNickName;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public void setOnlinePlayers(List<ProxyPlayer> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public void setServerValues(List<String> serverValues) {
        this.serverValues = serverValues;
    }

    public void setServerNickName(String serverNickName) {
        this.serverNickName = serverNickName;
    }
}
