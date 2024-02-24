package me.tahacheji.mafana.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDatabase extends MySQL {
    SQLGetter sqlGetter = new SQLGetter(this);
    public PlayerDatabase() {
        super("162.254.145.231", "3306", "51252", "51252", "346a1ef0fc");
    }

    public CompletableFuture<Void> addPlayer(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return sqlGetter.existsAsync(player.getUniqueId()).thenCompose(exists -> {
            if (!exists) {
                CompletableFuture<Void> setNameFuture = sqlGetter.setStringAsync(new DatabaseValue("PLAYER_NAME", uuid, player.getName()));
                CompletableFuture<Void> setOfflineProxyFuture = sqlGetter.setStringAsync(new DatabaseValue("OFFLINE_PROXY_PLAYER", uuid, ""));
                CompletableFuture<Void> setPlayerValuesFuture = sqlGetter.setStringAsync(new DatabaseValue("PLAYER_VALUES", uuid, ""));

                return CompletableFuture.allOf(setNameFuture, setOfflineProxyFuture, setPlayerValuesFuture);
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Void> setOfflineProxyPlayer(UUID uuid, OfflineProxyPlayer offlineProxyPlayer) {
        Gson gson = new Gson();
        return sqlGetter.setStringAsync(new DatabaseValue("OFFLINE_PROXY_PLAYER", uuid, gson.toJson(offlineProxyPlayer)))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }
    public CompletableFuture<OfflineProxyPlayer> getOfflineProxyPlayerAsync(String name) {
        return getAllOfflineProxyPlayersAsync().thenApply(offlineProxyPlayers -> {
            for (OfflineProxyPlayer offlineProxyPlayer : offlineProxyPlayers) {
                if (offlineProxyPlayer.getPlayerName().equalsIgnoreCase(name)) {
                    return offlineProxyPlayer;
                }
            }
            return null;
        });
    }


    public CompletableFuture<OfflineProxyPlayer> getOfflineProxyPlayerAsync(UUID uuid) {
        return sqlGetter.getStringAsync(uuid, new DatabaseValue("OFFLINE_PROXY_PLAYER")).thenApply(x -> {
            Gson gson = new Gson();
            return gson.fromJson(x, new TypeToken<OfflineProxyPlayer>() {}.getType());
        });
    }

    public CompletableFuture<List<OfflineProxyPlayer>> getAllOfflineProxyPlayersAsync() {
        return sqlGetter.getAllStringAsync(new DatabaseValue("OFFLINE_PROXY_PLAYER")).thenApply(offlinePlayerStrings -> {
            List<OfflineProxyPlayer> offlineProxyPlayers = new ArrayList<>();
            if (offlinePlayerStrings != null) {
                Gson gson = new Gson();
                for (String s : offlinePlayerStrings) {
                    OfflineProxyPlayer offlineProxyPlayer = gson.fromJson(s, new TypeToken<OfflineProxyPlayer>() {}.getType());

                    if (offlineProxyPlayer != null) {
                        offlineProxyPlayers.add(offlineProxyPlayer);
                    }
                }
            }
            return offlineProxyPlayers;
        });
    }


    public CompletableFuture<Void> addPlayerValue(OfflinePlayer player, String value) {
        return getAllPlayerValuesAsync(player).thenCompose(values -> {
            List<String> x = new ArrayList<>();
            if (values != null) {
                x.addAll(values);
            }
            x.add(value);
            return setPlayerValuesAsync(player, x);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Void> removePlayerValue(OfflinePlayer player, String value) {
        return getAllPlayerValuesAsync(player).thenCompose(values -> {
            String m = null;
            List<String> x = new ArrayList<>();
            if (values != null) {
                x.addAll(values);
            }
            for (String s : x) {
                if (s.equalsIgnoreCase(value)) {
                    m = s;
                    break;
                }
            }
            x.remove(m);
            return setPlayerValuesAsync(player, x);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }


    public CompletableFuture<Boolean> hasPlayerValueAsync(OfflinePlayer player, String value) {
        return getAllPlayerValuesAsync(player).thenApply(values -> {
            for (String s : values) {
                if (s.equalsIgnoreCase(value)) {
                    return true;
                }
            }
            return false;
        });
    }


    public CompletableFuture<List<String>> getAllPlayerValuesAsync(OfflinePlayer player) {
        return sqlGetter.getStringAsync(player.getUniqueId(), new DatabaseValue("PLAYER_VALUES")).thenApply(tasksJson -> {
            Gson gson = new Gson();
            return gson.fromJson(tasksJson, new TypeToken<List<String>>() {}.getType());
        });
    }


    public CompletableFuture<Void> setPlayerValuesAsync(OfflinePlayer player, List<String> serverValues) {
        Gson gson = new Gson();
        return sqlGetter.setStringAsync(new DatabaseValue("PLAYER_VALUES", player.getUniqueId(), gson.toJson(serverValues)))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public OfflineProxyPlayer getOfflineProxyPlayerSync(String name) {
        OfflineProxyPlayer player = null;
        for(OfflineProxyPlayer offlineProxyPlayer : getAllOfflineProxyPlayerSync()) {
            if(offlineProxyPlayer.getPlayerName().equalsIgnoreCase(name)) {
                player = offlineProxyPlayer;
            }
        }
        return player;
    }

    public List<OfflineProxyPlayer> getAllOfflineProxyPlayerSync() {
        List<OfflineProxyPlayer> offlineProxyPlayers = new ArrayList<>();
        try {
            List<String> offlinePlayerStrings = sqlGetter.getAllStringSync(new DatabaseValue("OFFLINE_PROXY_PLAYER"));

            if (offlinePlayerStrings != null) {
                Gson gson = new Gson();
                for (String s : offlinePlayerStrings) {
                    OfflineProxyPlayer offlineProxyPlayer = gson.fromJson(s, new TypeToken<OfflineProxyPlayer>() {}.getType());

                    if (offlineProxyPlayer != null) {
                        offlineProxyPlayers.add(offlineProxyPlayer);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return offlineProxyPlayers;
    }

    @Override
    public void connect() {
        super.connect();
        if (this.isConnected()) sqlGetter.createTable("mafana_player_database",
                new DatabaseValue("PLAYER_NAME", ""),
                new DatabaseValue("OFFLINE_PROXY_PLAYER", ""),
                new DatabaseValue("PLAYER_VALUES", ""));
    }

}
