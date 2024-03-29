package me.tahacheji.mafana.data;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.tahacheji.mafana.MafanaNetworkCommunicator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ProxyPlayer {

    private String playerUUID;
    private String playerName;
    private String serverID;
    private String serverName;

    public ProxyPlayer(String playerUUID, String playerName, String serverID, String serverName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.serverID = serverID;
        this.serverName = serverName;
    }

    public CompletableFuture<OfflineProxyPlayer> getOfflineProxyPlayer() {
        return MafanaNetworkCommunicator.getInstance().getPlayerDatabase().getOfflineProxyPlayerAsync(UUID.fromString(playerUUID));
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public UUID getServerID() {
        return UUID.fromString(serverID);
    }

    public String getServerName() {
        return serverName;
    }

    public CompletableFuture<List<String>> getPlayerValues() {
        return MafanaNetworkCommunicator.getInstance().getPlayerDatabase().getAllPlayerValuesAsync(getPlayer());
    }

    public CompletableFuture<Boolean> hasPlayerValue(String value) {
        return MafanaNetworkCommunicator.getInstance().getPlayerDatabase().hasPlayerValueAsync(getPlayer(), value);
    }

    public CompletableFuture<Boolean> hasPlayerValues(String... values) {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(null);
        for (String x : values) {
            future = future.thenComposeAsync(v -> hasPlayerValue(x));
        }
        return future;
    }

    public CompletableFuture<Void> removePlayerValue(String value) {
        return MafanaNetworkCommunicator.getInstance().getPlayerDatabase().removePlayerValue(getPlayer(), value);
    }

    public CompletableFuture<Void> removePlayerValues(String... values) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (String x : values) {
            future = future.thenComposeAsync(v -> removePlayerValue(x));
        }
        return future;
    }

    public CompletableFuture<Void> addPlayerValue(String value) {
        return MafanaNetworkCommunicator.getInstance().getPlayerDatabase().addPlayerValue(getPlayer(), value);
    }

    public CompletableFuture<Void> addPlayerValues(String... values) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (String x : values) {
            future = future.thenComposeAsync(v -> addPlayerValue(x));
        }
        return future;
    }

    public CompletableFuture<Void> sendMessage(String message) {
        NetworkTask networkTask = new NetworkTask(Task.SENDING_PLAYER_MESSAGE.toString(), getServerName(), getPlayer().getUniqueId().toString(), message);
        return MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().addNetworkTask(getServerID(), networkTask);
    }

    public CompletableFuture<Void> sendMessages(String... messages) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (String message : messages) {
            future = future.thenComposeAsync(v -> sendMessage(message));
        }
        return future;
    }

    public CompletableFuture<Void> preformCommand(String command) {
        NetworkTask networkTask = new NetworkTask(Task.PLAYER_PREFORM_COMMAND.toString(), getServerName(), getPlayer().getUniqueId().toString(), command);
        return MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().addNetworkTask(getServerID(), networkTask);
    }

    public CompletableFuture<Void> preformCommands(String... commands) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (String x : commands) {
            future = future.thenComposeAsync(v -> preformCommand(x));
        }
        return future;
    }

    public void connectPlayerToServer(String serverName) {
            ByteArrayOutputStream x = new ByteArrayOutputStream();
            DataOutput m = new DataOutputStream(x);
            try {
                m.writeUTF("ConnectOther");
                m.writeUTF(getPlayerName());
                m.writeUTF(serverName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Player n = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            n.sendPluginMessage(MafanaNetworkCommunicator.getInstance(), "BungeeCord", x.toByteArray());
    }
}
