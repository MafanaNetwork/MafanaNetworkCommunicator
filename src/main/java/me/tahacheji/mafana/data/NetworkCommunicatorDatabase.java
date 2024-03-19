package me.tahacheji.mafana.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.tahacheji.mafana.MafanaNetworkCommunicator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class NetworkCommunicatorDatabase extends MySQL {
    SQLGetter sqlGetter = new SQLGetter(this);

    public NetworkCommunicatorDatabase() {
        super("162.254.145.231", "3306", "51252", "51252", "346a1ef0fc");
    }

    public CompletableFuture<Void> registerServer(String serverName, String nickname) {
        UUID uuid = MafanaNetworkCommunicator.getInstance().getServerId();

        return sqlGetter.existsAsync(uuid).thenComposeAsync(exists -> {
            if (!exists) {
                return registerServerAsync(uuid, serverName, nickname);
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private CompletableFuture<Void> registerServerAsync(UUID uuid, String serverName, String nickname) {
        return CompletableFuture.allOf(
                sqlGetter.setStringAsync(new DatabaseValue("SERVER_NAME", uuid, serverName)),
                sqlGetter.setStringAsync(new DatabaseValue("ONLINE_PLAYERS", uuid, "")),
                sqlGetter.setStringAsync(new DatabaseValue("SERVER_VALUES", uuid, "")),
                sqlGetter.setStringAsync(new DatabaseValue("SERVER_NICKNAME", uuid, (nickname != null) ? nickname : "")),
                sqlGetter.setStringAsync(new DatabaseValue("TASKS", uuid, ""))
        ).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }


    public CompletableFuture<Server> getCombinedServerFromNickNameAsync(String x) {
        return getAllServersAsync().thenApplyAsync(servers -> {
            String serverName = "NULL";
            List<ProxyPlayer> onlinePlayers = new ArrayList<>();
            List<String> serverValues = new ArrayList<>();
            for (Server s : servers) {
                if (Objects.equals(serverName, "NULL")) {
                    serverName = s.getServerID();
                }
                onlinePlayers.addAll(s.getOnlinePlayers());
                serverValues.addAll(s.getServerValues());
            }
            return new Server(serverName, onlinePlayers, serverValues, x);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Server> getRandomServerFromNickNameAsync(String x) {
        return getAllServersAsync().thenApplyAsync(servers -> {
            for (Server server : servers) {
                if (server.getServerNickName().equalsIgnoreCase(x)) {
                    return server;
                }
            }
            return null;
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Server> getServerFromIDAsync(String x) {
        return getAllServersAsync().thenApplyAsync(servers -> {
            for (Server server : servers) {
                if (server.getServerID().equalsIgnoreCase(x)) {
                    return server;
                }
            }
            return null;
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }


    public CompletableFuture<List<Server>> getAllServersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            List<Server> servers = new ArrayList<>();
            try {
                List<UUID> uuids = sqlGetter.getAllUUIDAsync(new DatabaseValue("UUID")).join();
                List<String> serverNames = sqlGetter.getAllStringAsync(new DatabaseValue("SERVER_NAME")).join();
                List<String> serverValues = sqlGetter.getAllStringAsync(new DatabaseValue("SERVER_VALUES")).join();
                List<String> serverNicknames = sqlGetter.getAllStringAsync(new DatabaseValue("SERVER_NICKNAME")).join();

                for (int i = 0; i < uuids.size(); i++) {
                    UUID uuid = uuids.get(i);
                    String serverName = serverNames.get(i);
                    String serverValuesJson = serverValues.get(i);
                    String serverNickname = serverNicknames.get(i);

                    Gson gson = new Gson();

                    List<ProxyPlayer> onlinePlayers = getAllConnectedPlayersAsync(uuid).join();
                    List<String> serverValuesList = gson.fromJson(serverValuesJson, new TypeToken<List<String>>() {
                    }.getType());

                    Server server = new Server(serverName, onlinePlayers, serverValuesList, serverNickname);
                    servers.add(server);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return servers;
        });
    }


    public CompletableFuture<Void> clearAllPlayer(UUID uuid) {
        return sqlGetter.existsAsync(uuid).thenComposeAsync(exists -> {
            if (exists) {
                return sqlGetter.setStringAsync(new DatabaseValue("ONLINE_PLAYERS", uuid, ""))
                        .exceptionally(e -> {
                            e.printStackTrace();
                            return null;
                        });
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }


    public CompletableFuture<String> getServerNickNameAsync(ProxyPlayer proxyPlayer) {
        return sqlGetter.getStringAsync(proxyPlayer.getServerID(), new DatabaseValue("SERVER_NICKNAME"));
    }

    public CompletableFuture<String> getServerNickNameAsync(UUID uuid) {
        return sqlGetter.getStringAsync(uuid, new DatabaseValue("SERVER_NICKNAME"));
    }

    public CompletableFuture<Void> setServerNickNameAsync(ProxyPlayer proxyPlayer, String n) {
        return sqlGetter.setStringAsync(new DatabaseValue("SERVER_NICKNAME", proxyPlayer.getServerID(), n))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public CompletableFuture<Void> setServerNickNameAsync(UUID uuid, String n) {
        return sqlGetter.setStringAsync(new DatabaseValue("SERVER_NICKNAME", uuid, n))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public CompletableFuture<Void> clearAllTasks(UUID uuid) {
        return sqlGetter.existsAsync(uuid).thenComposeAsync(exists -> {
            if (exists) {
                return sqlGetter.setStringAsync(new DatabaseValue("TASKS", uuid, ""))
                        .exceptionally(e -> {
                            e.printStackTrace();
                            return null;
                        });
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<UUID> getServerIDAsync(String name) {
        return sqlGetter.getAllUUIDAsync(new DatabaseValue("UUID")).thenCombine(
                sqlGetter.getAllStringAsync(new DatabaseValue("SERVER_NAME")),
                (uuids, names) -> {
                    for (int i = 0; i < uuids.size(); i++) {
                        if (names.get(i).equalsIgnoreCase(name)) {
                            return uuids.get(i);
                        }
                    }
                    return null;
                }
        ).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Void> registerOnlinePlayer(Player player) {
        UUID serverId = MafanaNetworkCommunicator.getInstance().getServerId();
        if (serverId != null) {
            return sqlGetter.existsAsync(serverId).thenComposeAsync(exists -> {
                if (exists) {
                    return getServerFromUUIDAsync(serverId).thenComposeAsync(serverName -> {
                        UUID playerUUID = player.getUniqueId();
                        ProxyPlayer x = new ProxyPlayer(playerUUID.toString(), player.getName(), serverId.toString(), serverName.getServerID());
                        MafanaNetworkCommunicator.getInstance().getLogger().log(Level.INFO, x.getServerID() + " " + x.getServerName());
                        return getAllConnectedPlayersAsync(serverId).thenAccept(m -> {
                            m.add(x);
                            setAllConnectedPlayersAsync(serverId, m).join();
                        });
                    });
                }
                return CompletableFuture.completedFuture(null);
            }).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }
        return CompletableFuture.completedFuture(null);
    }


    public CompletableFuture<Void> addNetworkTask(UUID serverId, NetworkTask task) {
        return getAllNetworkTasksAsync(serverId).thenComposeAsync(tasks -> {
            if (tasks != null) {
                tasks.add(task);
            } else {
                tasks = new ArrayList<>();
                tasks.add(task);
            }
            return setNetworkTasksAsync(serverId, tasks);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<List<NetworkTask>> getAllNetworkTasksAsync(UUID serverId) {
        return sqlGetter.getStringAsync(serverId, new DatabaseValue("TASKS")).thenApplyAsync(tasksJson -> {
            Gson gson = new Gson();
            return gson.fromJson(tasksJson, new TypeToken<List<NetworkTask>>() {
            }.getType());
        });
    }

    public CompletableFuture<Void> setNetworkTasksAsync(UUID serverId, List<NetworkTask> tasks) {
        Gson gson = new Gson();
        return sqlGetter.setStringAsync(new DatabaseValue("TASKS", serverId, gson.toJson(tasks)))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public CompletableFuture<Void> removeNetworkTask(UUID serverId, String taskId) {
        return getAllNetworkTasksAsync(serverId).thenComposeAsync(tasks -> {
            if (tasks != null) {
                tasks.removeIf(task -> task.getTaskID().equalsIgnoreCase(taskId));
                return setNetworkTasksAsync(serverId, tasks);
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }


    public CompletableFuture<List<ProxyPlayer>> getAllConnectedPlayersAsync() {
        return sqlGetter.getAllStringAsync(new DatabaseValue("ONLINE_PLAYERS")).thenApplyAsync(proxyStringList -> {
            List<ProxyPlayer> proxyPlayers = new ArrayList<>();
            Gson gson = new Gson();
            for (String proxyString : proxyStringList) {
                List<ProxyPlayer> proxyPlayer = gson.fromJson(proxyString, new TypeToken<List<ProxyPlayer>>() {
                }.getType());
                if (proxyPlayer != null) {
                    proxyPlayers.addAll(proxyPlayer);
                }
            }
            return proxyPlayers;
        }).exceptionally(e -> {
            e.printStackTrace();
            return new ArrayList<>();
        });
    }


    public CompletableFuture<ProxyPlayer> getProxyPlayerAsync(String name) {
        return getAllConnectedPlayersAsync().thenApplyAsync(players -> {
            for (ProxyPlayer offlineProxyPlayer : players) {
                if (offlineProxyPlayer.getPlayerName().equalsIgnoreCase(name)) {
                    return offlineProxyPlayer;
                }
            }
            return null;
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Void> unregisterOnlinePlayer(Player player) {
        UUID serverId = MafanaNetworkCommunicator.getInstance().getServerId();
        if (serverId != null) {
            return sqlGetter.existsAsync(serverId).thenComposeAsync(exists -> {
                if (exists) {
                    UUID playerUUID = player.getUniqueId();
                    return getAllConnectedPlayersAsync(serverId).thenComposeAsync(players -> {
                        ProxyPlayer playerToRemove = null;
                        for (ProxyPlayer proxyPlayer : players) {
                            if (proxyPlayer.getPlayer().getUniqueId().equals(playerUUID)) {
                                playerToRemove = proxyPlayer;
                                break;
                            }
                        }
                        if (playerToRemove != null) {
                            players.remove(playerToRemove);
                            return setAllConnectedPlayersAsync(serverId, players);
                        }
                        return CompletableFuture.completedFuture(null);
                    });
                }
                return CompletableFuture.completedFuture(null);
            }).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<List<ProxyPlayer>> getAllConnectedPlayersAsync(String serverName) {
        return getAllConnectedPlayersAsync().thenApplyAsync(players -> {
            List<ProxyPlayer> proxyPlayers = new ArrayList<>();
            for (ProxyPlayer proxyPlayer : players) {
                if (proxyPlayer.getServerName().equalsIgnoreCase(serverName)) {
                    proxyPlayers.add(proxyPlayer);
                }
            }
            return proxyPlayers;
        }).exceptionally(e -> {
            e.printStackTrace();
            return new ArrayList<>();
        });
    }

    public CompletableFuture<List<ProxyPlayer>> getAllConnectedPlayersAsync(UUID serverID) {
        return getAllConnectedPlayersAsync().thenApplyAsync(players -> {
            List<ProxyPlayer> proxyPlayers = new ArrayList<>();
            for (ProxyPlayer proxyPlayer : players) {
                if (proxyPlayer.getServerID().equals(serverID)) {
                    proxyPlayers.add(proxyPlayer);
                }
            }
            return proxyPlayers;
        }).exceptionally(e -> {
            e.printStackTrace();
            return new ArrayList<>();
        });
    }

    public CompletableFuture<ProxyPlayer> getProxyPlayerAsync(OfflinePlayer player) {
        return getAllConnectedPlayersAsync().thenApplyAsync(players -> {
            for (ProxyPlayer proxyPlayer : players) {
                if (proxyPlayer.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                    return proxyPlayer;
                }
            }
            return null;
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<ProxyPlayer> getProxyPlayerAsync(UUID uuid) {
        return getAllConnectedPlayersAsync().thenApplyAsync(players -> {
            for (ProxyPlayer proxyPlayer : players) {
                if (proxyPlayer.getPlayer().getUniqueId().equals(uuid)) {
                    return proxyPlayer;
                }
            }
            return null;
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Void> addServerValueAsync(UUID serverId, String value) {
        return getAllServerValuesAsync(serverId).thenAcceptAsync(serverValues -> {
            if (serverValues != null) {
                serverValues.add(value);
            } else {
                serverValues = new ArrayList<>();
                serverValues.add(value);
            }
            setServerValuesAsync(serverId, serverValues);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Void> removeServerValueAsync(UUID serverId, String value) {
        return getAllServerValuesAsync(serverId).thenAcceptAsync(serverValues -> {
            if (serverValues != null) {
                serverValues.removeIf(s -> s.equalsIgnoreCase(value));
                setServerValuesAsync(serverId, serverValues);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Boolean> hasServerValueAsync(UUID serverId, String value) {
        return getAllServerValuesAsync(serverId).thenApplyAsync(serverValues -> {
            if (serverValues != null) {
                return serverValues.contains(value);
            }
            return false;
        }).exceptionally(e -> {
            e.printStackTrace();
            return false;
        });
    }

    public CompletableFuture<List<String>> getAllServerValuesAsync(UUID serverId) {
        return sqlGetter.getStringAsync(serverId, new DatabaseValue("SERVER_VALUES")).thenApply(tasksJson -> {
            Gson gson = new Gson();
            return gson.fromJson(tasksJson, new TypeToken<List<String>>() {
            }.getType());
        });
    }

    public CompletableFuture<Void> setServerValuesAsync(UUID serverId, List<String> serverValues) {
        Gson gson = new Gson();
        return sqlGetter.setStringAsync(new DatabaseValue("SERVER_VALUES", serverId, gson.toJson(serverValues)))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public CompletableFuture<Void> setAllConnectedPlayersAsync(UUID serverId, List<ProxyPlayer> x) {
        Gson gson = new Gson();
        return sqlGetter.setStringAsync(new DatabaseValue("ONLINE_PLAYERS", serverId, gson.toJson(x)))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public List<ProxyPlayer> getAllConnectedPlayerSync() {
        List<ProxyPlayer> proxyPlayers = new ArrayList<>();
        try {
            List<String> proxyString = sqlGetter.getAllStringSync(new DatabaseValue("ONLINE_PLAYERS"));

            if (proxyString != null) {
                Gson gson = new Gson();
                for (String s : proxyString) {
                    List<ProxyPlayer> proxyPlayer = gson.fromJson(s, new TypeToken<List<ProxyPlayer>>() {
                    }.getType());

                    if (proxyPlayer != null) {
                        proxyPlayers.addAll(proxyPlayer);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proxyPlayers;
    }

    public CompletableFuture<Server> getServerFromUUIDAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
           Server server = new Server(sqlGetter.getStringAsync(uuid, new DatabaseValue("SERVER_NAME")).join(), getAllConnectedPlayersAsync(uuid).join(), getAllServerValuesAsync(uuid).join(), getServerNickNameAsync(uuid).join());
            return server;
        });
    }

    public List<Server> getAllServerSync() {
        List<Server> servers = new ArrayList<>();
        try {
            List<UUID> uuids = sqlGetter.getAllUUIDSync(new DatabaseValue("UUID"));
            List<String> serverNames = sqlGetter.getAllStringSync(new DatabaseValue("SERVER_NAME"));
            List<String> serverValues = sqlGetter.getAllStringSync(new DatabaseValue("SERVER_VALUES"));
            List<String> serverNicknames = sqlGetter.getAllStringSync(new DatabaseValue("SERVER_NICKNAME"));

            for (int i = 0; i < uuids.size(); i++) {
                UUID uuid = uuids.get(i);
                String serverName = serverNames.get(i);
                String serverValuesJson = serverValues.get(i);
                String serverNickname = serverNicknames.get(i);

                Gson gson = new Gson();

                List<ProxyPlayer> onlinePlayers = getAllConnectedPlayerSync(uuid);
                List<String> serverValuesList = gson.fromJson(serverValuesJson, new TypeToken<List<String>>() {
                }.getType());

                Server server = new Server(serverName, onlinePlayers, serverValuesList, serverNickname);
                servers.add(server);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }

    public List<ProxyPlayer> getAllConnectedPlayerSync(UUID serverID) {
        List<ProxyPlayer> proxyPlayers = new ArrayList<>();
        for (ProxyPlayer proxyPlayer : getAllConnectedPlayerSync()) {
            if (proxyPlayer.getServerID().equals(serverID)) {
                proxyPlayers.add(proxyPlayer);
            }
        }
        return proxyPlayers;
    }

    public ProxyPlayer getProxyPlayer(String name) {
        ProxyPlayer player = null;
        for (ProxyPlayer offlineProxyPlayer : getAllConnectedPlayerSync()) {
            if (offlineProxyPlayer.getPlayerName().equalsIgnoreCase(name)) {
                player = offlineProxyPlayer;
            }
        }
        return player;
    }


    public Server getServerFromID(String x) {
        for (Server server : getAllServerSync()) {
            if (server.getServerID().equalsIgnoreCase(x)) {
                return server;
            }
        }
        return null;
    }

    public void connect() {
        sqlGetter.createTable("mafana_network_communicator",
                new DatabaseValue("SERVER_NAME", ""),
                new DatabaseValue("ONLINE_PLAYERS", ""),
                new DatabaseValue("SERVER_VALUES", ""),
                new DatabaseValue("SERVER_NICKNAME", ""),
                new DatabaseValue("TASKS", ""));
    }


}
