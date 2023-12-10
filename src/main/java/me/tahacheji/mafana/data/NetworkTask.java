package me.tahacheji.mafana.data;

import org.bukkit.entity.Player;

import java.util.UUID;

public class NetworkTask {

    private String TaskID;
    private String serverIdTo;
    private String playerUUID;
    private String task;

    public NetworkTask(String taskID, String serverIdTo) {
        TaskID = taskID;
        this.serverIdTo = serverIdTo;
    }

    public NetworkTask(String taskID, String serverIdTo, String playerUUID, String task) {
        TaskID = taskID;
        this.serverIdTo = serverIdTo;
        this.playerUUID = playerUUID;
        this.task = task;
    }

    public NetworkTask(String taskID, String serverIdTo, String task) {
        TaskID = taskID;
        this.serverIdTo = serverIdTo;
        this.task = task;
    }

    public String getTaskID() {
        return TaskID;
    }

    public String getServerIdTo() {
        return serverIdTo;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public String getTask() {
        return task;
    }

    public void setTaskID(String taskID) {
        TaskID = taskID;
    }

    public void setServerIdTo(String serverIdTo) {
        this.serverIdTo = serverIdTo;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
