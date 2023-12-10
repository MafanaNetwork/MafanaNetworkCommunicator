package me.tahacheji.mafana.util;

import me.tahacheji.mafana.MafanaNetworkCommunicator;
import me.tahacheji.mafana.data.ProxyPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class ServerInformation{

    public void createServerId(Plugin plugin) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.contains("server-id")) {
            UUID serverId = UUID.randomUUID();

            config.set("server-id", serverId.toString());
            plugin.saveConfig();
        }
    }

    public UUID getServerId(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("server-id")) {
            String serverIdString = config.getString("server-id");
            return UUID.fromString(serverIdString);
        }

        return null;
    }


}
