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

public class ProxyPlayer {

    private String player;
    private String serverID;
    private String serverName;

    public ProxyPlayer(String player, String serverID, String serverName) {
        this.player = player;
        this.serverID = serverID;
        this.serverName = serverName;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(player));
    }

    public UUID getServerID() {
        return UUID.fromString(serverID);
    }

    public String getServerName() {
        return serverName;
    }

    public List<String> getPlayerValues() {
        return MafanaNetworkCommunicator.getInstance().getPlayerDatabase().getAllPlayerValues(getPlayer());
    }

    public boolean hasPlayerValue(String value) {
        return MafanaNetworkCommunicator.getInstance().getPlayerDatabase().hasPlayerValue(getPlayer(), value);
    }

    public void removePlayerValue(String value) {
        MafanaNetworkCommunicator.getInstance().getPlayerDatabase().removePlayerValue(getPlayer(), value);
    }

    public void addPlayerValue(String value) {
        MafanaNetworkCommunicator.getInstance().getPlayerDatabase().addPlayerValue(getPlayer(), value);
    }

    public void sendMessage(String message) {
        NetworkTask networkTask = new NetworkTask(Task.SENDING_PLAYER_MESSAGE.toString(), getServerName(), getPlayer().getUniqueId().toString(), message);
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().addNetworkTask(getServerID(), networkTask);
    }

    public void preformCommand(String command) {
        NetworkTask networkTask = new NetworkTask(Task.PLAYER_PREFORM_COMMAND.toString(), getServerName(), getPlayer().getUniqueId().toString(), command);
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().addNetworkTask(getServerID(), networkTask);
    }

    public void connectPlayerToServer(String serverName) {
        OfflinePlayer b = Bukkit.getOfflinePlayer(UUID.fromString(player));
        if(b != null) {
            ByteArrayOutputStream x = new ByteArrayOutputStream();
            DataOutput m = new DataOutputStream(x);
            try {
                m.writeUTF("ConnectOther");
                m.writeUTF(b.getName());
                m.writeUTF(serverName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Player n = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            n.sendPluginMessage(MafanaNetworkCommunicator.getInstance(), "BungeeCord", x.toByteArray());
        }
    }
}
