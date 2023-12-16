package me.tahacheji.mafana;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.tahacheji.mafana.commandExecutor.CommandHandler;
import me.tahacheji.mafana.data.*;
import me.tahacheji.mafana.event.PlayerJoin;
import me.tahacheji.mafana.event.PlayerLeave;
import me.tahacheji.mafana.util.ServerInformation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MafanaNetworkCommunicator extends JavaPlugin {


    private NetworkCommunicatorDatabase networkCommunicatorDatabase;
    private PlayerDatabase playerDatabase;
    private static MafanaNetworkCommunicator instance;

    @Override
    public void onEnable() {
        instance = this;
        networkCommunicatorDatabase = new NetworkCommunicatorDatabase();
        playerDatabase = new PlayerDatabase();
        playerDatabase.connect();
        networkCommunicatorDatabase.connect();
        new ServerInformation().createServerId(this);
        networkCommunicatorDatabase.clearAllPlayer(getServerId());
        networkCommunicatorDatabase.clearAllTasks(getServerId());
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new PlayerLeave(), this);
        CommandHandler.registerCommands(RegisterServerCommand.class, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        new BukkitRunnable() {
            @Override
            public void run() {
                executeNetworkTasks();
            }
        }.runTaskTimerAsynchronously(this, 0L, 10L);
    }

    @Override
    public void onDisable() {
        networkCommunicatorDatabase.disconnect();
        playerDatabase.disconnect();
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    private void executeNetworkTasks() {
        MafanaNetworkCommunicator pluginInstance = MafanaNetworkCommunicator.getInstance();
        UUID serverId = pluginInstance.getServerId();
        List<NetworkTask> tasks = new ArrayList<>();

        if (networkCommunicatorDatabase.getNetworkTasks(serverId) != null) {
            tasks.addAll(networkCommunicatorDatabase.getNetworkTasks(serverId));
        }

        for (NetworkTask task : tasks) {
            Bukkit.getScheduler().runTask(pluginInstance, () -> {
                if (task.getTaskID().equalsIgnoreCase(String.valueOf(Task.CONSOLE_PREFORM_COMMAND))) {
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), task.getTask());
                } else if (task.getTaskID().equalsIgnoreCase(String.valueOf(Task.PLAYER_PREFORM_COMMAND))) {
                    Player player = Bukkit.getPlayer(UUID.fromString(task.getPlayerUUID()));
                    if (player != null) {
                        player.performCommand(task.getTask());
                    }
                } else if (task.getTaskID().equalsIgnoreCase(String.valueOf(Task.SENDING_PLAYER_MESSAGE))) {
                    Player player = Bukkit.getPlayer(UUID.fromString(task.getPlayerUUID()));
                    if (player != null) {
                        player.sendMessage(task.getTask());
                    }
                }

            });

            networkCommunicatorDatabase.removeNetworkTask(serverId, task.getTaskID());
        }
    }

    public void preformConsoleCommand(String server, String command) {
        NetworkTask networkTask = new NetworkTask(Task.CONSOLE_PREFORM_COMMAND.toString(), server, command);
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase().addNetworkTask(getNetworkCommunicatorDatabase().getServerID(server), networkTask);
    }

    public PlayerDatabase getPlayerDatabase() {
        return playerDatabase;
    }

    public UUID getServerId() {
        return new ServerInformation().getServerId(this);
    }

    public NetworkCommunicatorDatabase getNetworkCommunicatorDatabase() {
        return networkCommunicatorDatabase;
    }

    public static MafanaNetworkCommunicator getInstance() {
        return instance;
    }

}
