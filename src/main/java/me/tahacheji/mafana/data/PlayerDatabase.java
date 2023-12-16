package me.tahacheji.mafana.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.TahaCheji.mysqlData.MySQL;
import me.TahaCheji.mysqlData.MysqlValue;
import me.TahaCheji.mysqlData.SQLGetter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDatabase extends MySQL {
    SQLGetter sqlGetter = new SQLGetter(this);
    public PlayerDatabase() {
        super("162.254.145.231", "3306", "51252", "51252", "346a1ef0fc");
    }

    public void addPlayer(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        if(!sqlGetter.exists(player.getUniqueId())) {
            sqlGetter.setString(new MysqlValue("PLAYER_NAME", uuid, player.getName()));
            sqlGetter.setString(new MysqlValue("PLAYER_VALUES", uuid, ""));
        }
    }

    public void addPlayerValue(OfflinePlayer player, String value) {
        List<String> x = new ArrayList<>();
        if(getAllPlayerValues(player) != null) {
            x.addAll(getAllPlayerValues(player));
        }
        x.add(value);
        setPlayerValues(player, x);
    }

    public void removePlayerValue(OfflinePlayer player, String value) {
        String m = null;
        List<String> x = new ArrayList<>();
        if(getAllPlayerValues(player) != null) {
            x.addAll(getAllPlayerValues(player));
        }
        for(String s : x) {
            if(s.equalsIgnoreCase(value)) {
                m = s;
            }
        }
        x.remove(m);
        setPlayerValues(player, x);
    }

    public boolean hasPlayerValue(OfflinePlayer player, String value) {
        for(String s : getAllPlayerValues(player)) {
            if(s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getAllPlayerValues(OfflinePlayer player) {
        String tasksJson = sqlGetter.getString(player.getUniqueId(), new MysqlValue("PLAYER_VALUES"));
        Gson gson = new Gson();
        return gson.fromJson(tasksJson, new TypeToken<List<String>>() {}.getType());
    }

    public void setPlayerValues(OfflinePlayer player, List<String> serverValues) {
        Gson gson = new Gson();
        sqlGetter.setString(new MysqlValue("PLAYER_VALUES", player.getUniqueId(), gson.toJson(serverValues)));
    }

    @Override
    public void connect() {
        super.connect();
        if (this.isConnected()) sqlGetter.createTable("mafana_player_database",
                new MysqlValue("PLAYER_NAME", ""),
                new MysqlValue("PLAYER_VALUES", ""));
    }

}
