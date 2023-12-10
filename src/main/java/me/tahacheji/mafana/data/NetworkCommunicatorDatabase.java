package me.tahacheji.mafana.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.TahaCheji.mysqlData.MySQL;
import me.TahaCheji.mysqlData.MysqlValue;
import me.TahaCheji.mysqlData.SQLGetter;
import me.tahacheji.mafana.MafanaNetworkCommunicator;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkCommunicatorDatabase extends MySQL {
    SQLGetter sqlGetter = new SQLGetter(this);
    public NetworkCommunicatorDatabase() {
        super("162.254.145.231", "3306", "51252", "51252", "346a1ef0fc");
    }

    public void registerServer(String serverName) {
        UUID uuid = MafanaNetworkCommunicator.getInstance().getServerId();
        if(!sqlGetter.exists(uuid)) {
          sqlGetter.setString(new MysqlValue("SERVER_NAME", uuid, serverName));
          sqlGetter.setString(new MysqlValue("ONLINE_PLAYERS", uuid, ""));
          sqlGetter.setString(new MysqlValue("TASKS", uuid, ""));
        }
    }

    public void clearAllPlayer(UUID uuid) {
        if(sqlGetter.exists(uuid)) {
            sqlGetter.setString(new MysqlValue("ONLINE_PLAYERS", uuid, ""));
        }
    }

    public void clearAllTasks(UUID uuid) {
        if(sqlGetter.exists(uuid)) {
            sqlGetter.setString(new MysqlValue("TASKS", uuid, ""));
        }
    }

    public UUID getServerID(String name) {
        try {
            List<UUID> uuids = sqlGetter.getAllUUID(new MysqlValue("UUID"));
            List<String> names = sqlGetter.getAllString(new MysqlValue("SERVER_NAME"));
            for (int i = 0; i < uuids.size(); i++) {
                if (names.get(i).equalsIgnoreCase(name)) {
                    return uuids.get(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public void registerOnlinePlayer(Player player) {
        UUID serverId = MafanaNetworkCommunicator.getInstance().getServerId();
        if(serverId != null && sqlGetter.exists(serverId)) {
            String serverName = sqlGetter.getString(serverId, new MysqlValue("SERVER_NAME"));
            UUID playerUUID = player.getUniqueId();
            ProxyPlayer x = new ProxyPlayer(playerUUID.toString(), serverId.toString(), serverName);
            List<ProxyPlayer> m = getAllConnectedPlayers(serverId);
            m.add(x);
            setAllConnectedPlayers(serverId, m);
        }
    }

    public void addNetworkTask(UUID serverId, NetworkTask task) {
        List<NetworkTask> tasks = new ArrayList<>();
        if(getNetworkTasks(serverId) != null) {
            tasks.addAll(getNetworkTasks(serverId));
        }
        tasks.add(task);
        setNetworkTasks(serverId, tasks);
    }

    public List<NetworkTask> getNetworkTasks(UUID serverId) {
        String tasksJson = sqlGetter.getString(serverId, new MysqlValue("TASKS"));
        Gson gson = new Gson();
        return gson.fromJson(tasksJson, new TypeToken<List<NetworkTask>>() {}.getType());
    }

    public void setNetworkTasks(UUID serverId, List<NetworkTask> tasks) {
        Gson gson = new Gson();
        sqlGetter.setString(new MysqlValue("TASKS", serverId, gson.toJson(tasks)));
    }

    public void removeNetworkTask(UUID serverId, String taskId) {
        List<NetworkTask> tasks = new ArrayList<>();
        if(getNetworkTasks(serverId) != null) {
            tasks.addAll(getNetworkTasks(serverId));
        }
        NetworkTask task = null;
        for(NetworkTask networkTask : tasks) {
            if(networkTask.getTaskID().equalsIgnoreCase(taskId)) {
                task = networkTask;
            }
        }
        tasks.remove(task);
        setNetworkTasks(serverId, tasks);
    }

    public List<ProxyPlayer> getAllConnectedPlayers() {
        List<ProxyPlayer> proxyPlayers = new ArrayList<>();
        try {
            List<String> proxyString = sqlGetter.getAllString(new MysqlValue("ONLINE_PLAYERS"));

            if (proxyString != null) {
                Gson gson = new Gson();
                for (String s : proxyString) {
                    List<ProxyPlayer> proxyPlayer = gson.fromJson(s, new TypeToken<List<ProxyPlayer>>() {}.getType());

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

    public void unregisterOnlinePlayer(Player player) {
        UUID serverId = MafanaNetworkCommunicator.getInstance().getServerId();
        if (serverId != null && sqlGetter.exists(serverId)) {
            UUID playerUUID = player.getUniqueId();
            List<ProxyPlayer> m = getAllConnectedPlayers(serverId);
            ProxyPlayer playerToRemove = null;
            for(ProxyPlayer proxyPlayer : m) {
                if(proxyPlayer.getPlayer().getUniqueId().equals(playerUUID)) {
                    playerToRemove = proxyPlayer;
                    break;
                }
            }
            m.remove(playerToRemove);
            setAllConnectedPlayers(serverId, m);
        }
    }


    public List<ProxyPlayer> getAllConnectedPlayers(String serverName) {
        List<ProxyPlayer> proxyPlayers = new ArrayList<>();
        for(ProxyPlayer proxyPlayer : getAllConnectedPlayers()) {
            if(proxyPlayer.getServerName().equalsIgnoreCase(serverName)) {
                proxyPlayers.add(proxyPlayer);
            }
        }
        return proxyPlayers;
    }

    public List<ProxyPlayer> getAllConnectedPlayers(UUID serverID) {
        List<ProxyPlayer> proxyPlayers = new ArrayList<>();
        for(ProxyPlayer proxyPlayer : getAllConnectedPlayers()) {
            if(proxyPlayer.getServerID().equals(serverID)) {
                proxyPlayers.add(proxyPlayer);
            }
        }
        return proxyPlayers;
    }

    public ProxyPlayer getProxyPlayer(OfflinePlayer player) {
        for(ProxyPlayer proxyPlayer : getAllConnectedPlayers()) {
            if(proxyPlayer.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return proxyPlayer;
            }
         }
        return null;
    }

    public void setAllConnectedPlayers(UUID serverId, List<ProxyPlayer> x) {
        Gson gson = new Gson();
        sqlGetter.setString(new MysqlValue("ONLINE_PLAYERS", serverId, gson.toJson(x)));
    }


    @Override
    public void connect() {
        super.connect();
        if (this.isConnected()) sqlGetter.createTable("mafana_network_communicator",
                new MysqlValue("SERVER_NAME", ""),
                new MysqlValue("ONLINE_PLAYERS", ""),
                new MysqlValue("TASKS", ""));
    }


}
