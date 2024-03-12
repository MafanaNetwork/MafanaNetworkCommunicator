package me.tahacheji.mafana;

//import me.tahacheji.mafana.commandExecutor.CommandHandler;
import me.tahacheji.mafana.commandExecutor.CommandHandler;
import me.tahacheji.mafana.data.*;
import me.tahacheji.mafana.event.PlayerJoin;
import me.tahacheji.mafana.event.PlayerLeave;
import me.tahacheji.mafana.util.ServerInformation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        networkCommunicatorDatabase.close();
        playerDatabase.close();
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    private void executeNetworkTasks() {
        MafanaNetworkCommunicator pluginInstance = MafanaNetworkCommunicator.getInstance();
        UUID serverId = pluginInstance.getServerId();
        networkCommunicatorDatabase.getAllNetworkTasksAsync(serverId).thenAcceptAsync(tasks -> {
            if (tasks != null) {
                CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
                for (NetworkTask task : tasks) {
                    future = future.thenRunAsync(() -> {
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
                        networkCommunicatorDatabase.removeNetworkTask(serverId, task.getTaskID()).join();
                    });
                }
                future.join();
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }


    public void preformConsoleCommand(String server, String command) {
        NetworkTask networkTask = new NetworkTask(Task.CONSOLE_PREFORM_COMMAND.toString(), server, command);
        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase()
                .getServerIDAsync(server)
                .thenAccept(serverId -> {
                    if (serverId != null) {
                        MafanaNetworkCommunicator.getInstance().getNetworkCommunicatorDatabase()
                                .addNetworkTask(serverId, networkTask)
                                .exceptionally(e -> {
                                    e.printStackTrace();
                                    return null;
                                });
                    } else {
                        System.err.println("Server ID not found for server: " + server);
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
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
