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
import java.util.Objects;
import java.util.UUID;

public class NetworkCommunicatorDatabase extends MySQL {
    SQLGetter sqlGetter = new SQLGetter(this);
    public NetworkCommunicatorDatabase() {
        super("162.254.145.231", "3306", "51252", "51252", "346a1ef0fc");
    }

    public void registerServer(String serverName, String n) {
        UUID uuid = MafanaNetworkCommunicator.getInstance().getServerId();
        if(!sqlGetter.exists(uuid)) {
          sqlGetter.setString(new MysqlValue("SERVER_NAME", uuid, serverName));
          sqlGetter.setString(new MysqlValue("ONLINE_PLAYERS", uuid, ""));
          sqlGetter.setString(new MysqlValue("SERVER_VALUES", uuid, ""));
          if(n != null) {
              sqlGetter.setString(new MysqlValue("SERVER_NICKNAME", uuid, n));
          } else {
              sqlGetter.setString(new MysqlValue("SERVER_NICKNAME", uuid, ""));
          }
          sqlGetter.setString(new MysqlValue("TASKS", uuid, ""));
        }
    }

    public Server getCombinedServerFromNickName(String x) {
        String serverName = "NULL";
        List<ProxyPlayer> onlinePlayers = new ArrayList<>();
        List<String> serverValues = new ArrayList<>();
        for(Server s : getAllServers()) {
            if(Objects.equals(serverName, "NULL")) {
                serverName = s.getServerID();
            }
            onlinePlayers.addAll(s.getOnlinePlayers());
            serverValues.addAll(s.getServerValues());
        }
        return new Server(serverName, onlinePlayers, serverValues, x);
    }

    public Server getRandomServerFromNickName(String x) {
        for(Server server : getAllServers()) {
            if(server.getServerNickName().equalsIgnoreCase(x)) {
                return server;
            }
        }
        return null;
    }

    public Server getServerFromID(String x) {
        for(Server server : getAllServers()) {
            if(server.getServerID().equalsIgnoreCase(x)) {
                return server;
            }
        }
        return null;
    }

    public List<Server> getAllServers() {
        List<Server> servers = new ArrayList<>();
        try {
            List<UUID> uuids = sqlGetter.getAllUUID(new MysqlValue("UUID"));
            List<String> serverNames = sqlGetter.getAllString(new MysqlValue("SERVER_NAME"));
            List<String> serverValues = sqlGetter.getAllString(new MysqlValue("SERVER_VALUES"));
            List<String> serverNicknames = sqlGetter.getAllString(new MysqlValue("SERVER_NICKNAME"));

            for (int i = 0; i < uuids.size(); i++) {
                UUID uuid = uuids.get(i);
                String serverName = serverNames.get(i);
                String serverValuesJson = serverValues.get(i);
                String serverNickname = serverNicknames.get(i);

                Gson gson = new Gson();

                List<ProxyPlayer> onlinePlayers = getAllConnectedPlayers(uuid);
                List<String> serverValuesList = gson.fromJson(serverValuesJson, new TypeToken<List<String>>() {}.getType());

                Server server = new Server(serverName, onlinePlayers, serverValuesList, serverNickname);
                servers.add(server);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }

    public void clearAllPlayer(UUID uuid) {
        if(sqlGetter.exists(uuid)) {
            sqlGetter.setString(new MysqlValue("ONLINE_PLAYERS", uuid, ""));
        }
    }

    public String getServerNickName(ProxyPlayer proxyPlayer) {
        return sqlGetter.getString(proxyPlayer.getServerID(), new MysqlValue("SERVER_NICKNAME"));
    }

    public String getServerNickName(UUID uuid) {
        return sqlGetter.getString(uuid, new MysqlValue("SERVER_NICKNAME"));
    }

    public void setServerNickName(ProxyPlayer proxyPlayer, String n) {
        sqlGetter.setString(new MysqlValue("SERVER_NICKNAME", proxyPlayer.getServerID(), n));
    }

    public void setServerNickName(UUID uuid, String n) {
        sqlGetter.setString(new MysqlValue("SERVER_NICKNAME", uuid, n));
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
            ProxyPlayer x = new ProxyPlayer(playerUUID.toString(), player.getName(), serverId.toString(), serverName);
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

    public ProxyPlayer getProxyPlayer(String name) {
        ProxyPlayer player = null;
        for(ProxyPlayer offlineProxyPlayer : getAllConnectedPlayers()) {
            if(offlineProxyPlayer.getPlayerName().equalsIgnoreCase(name)) {
                player = offlineProxyPlayer;
            }
        }
        return player;
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

    public ProxyPlayer getProxyPlayer(UUID uuid) {
        for(ProxyPlayer proxyPlayer : getAllConnectedPlayers()) {
            if(proxyPlayer.getPlayer().getUniqueId().equals(uuid)) {
                return proxyPlayer;
            }
        }
        return null;
    }

    public void addServerValue(UUID serverId, String value) {
        List<String> x = new ArrayList<>();
        if(getAllServerValues(serverId) != null) {
            x.addAll(getAllServerValues(serverId));
        }
        x.add(value);
        setServerValues(serverId, x);
    }

    public void removeServerValue(UUID serverId, String value) {
        String m = null;
        List<String> x = new ArrayList<>();
        if(getAllServerValues(serverId) != null) {
            x.addAll(getAllServerValues(serverId));
        }
        for(String s : x) {
            if(s.equalsIgnoreCase(value)) {
                m = s;
            }
        }
        x.remove(m);
        setServerValues(serverId, x);
    }

    public boolean hasServerValue(UUID serverId, String value) {
        for(String s : getAllServerValues(serverId)) {
            if(s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getAllServerValues(UUID serverId) {
        String tasksJson = sqlGetter.getString(serverId, new MysqlValue("SERVER_VALUES"));
        Gson gson = new Gson();
        return gson.fromJson(tasksJson, new TypeToken<List<String>>() {}.getType());
    }

    public void setServerValues(UUID serverId, List<String> serverValues) {
        Gson gson = new Gson();
        sqlGetter.setString(new MysqlValue("SERVER_VALUES", serverId, gson.toJson(serverValues)));
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
                new MysqlValue("SERVER_VALUES", ""),
                new MysqlValue("SERVER_NICKNAME", ""),
                new MysqlValue("TASKS", ""));
    }


}
