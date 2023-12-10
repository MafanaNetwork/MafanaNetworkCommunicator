package me.tahacheji.mafanatextnetwork.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.tahacheji.mafanatextnetwork.data.GamePlayerPrivateMessaging;
import me.tahacheji.mafanatextnetwork.data.GamePlayerPublicMessaging;
import me.tahacheji.mafanatextnetwork.data.adapter.GamePlayerPrivateMessagingAdapter;
import me.tahacheji.mafanatextnetwork.data.adapter.GamePlayerPublicMessagingAdapter;
import me.tahacheji.mafanatextnetwork.data.adapter.PlayerTypeAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

public class EncryptionUtil {



    private final ObjectMapper objectMapper = new ObjectMapper();

    public String encryptPrivateMessages(List<GamePlayerPrivateMessaging> messages) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GamePlayerPrivateMessaging.class, new GamePlayerPrivateMessagingAdapter())
                .create();
        return gson.toJson(messages);
    }

    public List<GamePlayerPrivateMessaging> decryptPrivateMessages(String s) throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GamePlayerPrivateMessaging.class, new GamePlayerPrivateMessagingAdapter())
                .create();
        return gson.fromJson(s, new TypeToken<List<GamePlayerPrivateMessaging>>() {}.getType());
    }

    public String encryptPublicMessages(List<GamePlayerPublicMessaging> messages) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GamePlayerPublicMessaging.class, new GamePlayerPublicMessagingAdapter())
                .create();
        return gson.toJson(messages);
    }

    public List<GamePlayerPublicMessaging> decryptPublicMessages(String s) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GamePlayerPublicMessaging.class, new GamePlayerPublicMessagingAdapter())
                .create();
        return gson.fromJson(s, new TypeToken<List<GamePlayerPublicMessaging>>() {}.getType());
    }


    public String encryptPlayers(List<UUID> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    public List<UUID> decryptPlayers(String s) {
        Gson gson = new Gson();
        return gson.fromJson(s, new TypeToken<List<UUID>>() {
        }.getType());
    }


    public String itemToBase64(ItemStack item) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Save the item
            dataOutput.writeObject(item);

            // Serialize the item
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    public ItemStack itemFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Read the serialized item
            ItemStack item = (ItemStack) dataInput.readObject();

            dataInput.close();
            return item;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
