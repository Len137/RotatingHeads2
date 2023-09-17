package cz.gennario.newrotatingheads.utils.items;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import cz.gennario.newrotatingheads.Main;
import cz.gennario.newrotatingheads.utils.TimeUtils;
import cz.gennario.newrotatingheads.utils.config.Config;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * HeadSystem [1.1]
 * <p>
 * Util class that solves a bug with gaining a player's head on versions 1.16 and higher.
 * Created by Gennario with <3
 */
public final class HeadManager {

    public enum CacheType {
        CONFIG,
        MEMORY
    }
    public static final CacheType cacheType = CacheType.valueOf(Main.getInstance().getConfigFile().getYamlDocument().getString("skull-cache.type"));
    public static final Map<String, String> memoryCache = new HashMap<>();

    /**
     * Generation head type enum
     */
    public enum HeadType {
        PLAYER_HEAD,
        BASE64
    }

    /**
     * With this method you can get a player's head by nickname or a base64 head by base64 code
     *
     * @param type  Determines whether you want to get the head by name or by base64
     * @param value If you want a player's head, then the player's name. If you want base64, then base64 code.
     * @return Head itemStack
     */
    public static ItemStack convert(HeadType type, String value) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta itemMeta = (SkullMeta) head.getItemMeta();

        if (type.equals(HeadType.PLAYER_HEAD)) {
            assert itemMeta != null;
            try {
                head = getSkullByTexture("https://mc-heads.net/minecraft/profile/"+value);
            }catch (Exception e) {
                e.printStackTrace();
            }

            return head;
        } else {
            return getSkullByTexture(value);
        }
    }

    private static ItemStack getSkullByTexture(String url) {
        ItemStack head = getAllVersionStack("SKULL_ITEM", "PLAYER_HEAD", 3);
        if (url.isEmpty() || url.equals("none")) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", url));
        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }

    public static String getPlayerHeadTexture(String username) {
        if (getPlayerId(username).equals("none")) return "none";
        //String url = "http://api.minetools.eu/profile/" + getPlayerId(username);
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + getPlayerId(username);

        String fromCache = getFromCache(username);
        if(fromCache != null) {
            return fromCache;
        }

        try {
            JSONParser jsonParser = new JSONParser();
            String userData = readUrl(url);
            Object parsedData = jsonParser.parse(userData);

            /*
            old method

            JSONObject jsonData = (JSONObject) parsedData;
            JSONObject decoded = (JSONObject) jsonData.get("raw");
            JSONArray textures = (JSONArray) decoded.get("properties");
            JSONObject data = (JSONObject) textures.get(0);*/

            JSONObject jsonData = (JSONObject) parsedData;
            JSONArray textures = (JSONArray) jsonData.get("properties");
            JSONObject data = (JSONObject) textures.get(0);

            String value = data.get("value").toString();
            saveToCache(username, value);
            return value;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "none";
        }
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
            return buffer.toString();
        } finally {
            if (reader != null) reader.close();
        }
    }


    private static String getPlayerId(String playerName) {
        try {
            String url = "https://api.minetools.eu/uuid/" + playerName;
            JSONParser jsonParser = new JSONParser();
            String userData = readUrl(url);
            Object parsedData = jsonParser.parse(userData);

            JSONObject jsonData = (JSONObject) parsedData;

            if (jsonData.get("id") != null) return jsonData.get("id").toString();
            return "";
        } catch (Exception ex) {
            return "none";
        }
    }

    private static ItemStack getAllVersionStack(String oldName, String newName, int data) {
        Material material = null;
        try {
            material = Material.valueOf(oldName);
        } catch (Exception exception) {
            material = Material.valueOf(newName);
            data = 0;
        }
        return new ItemStack(material, 1, (byte) data);
    }

    public static String getFromCache(String username) {
        switch (cacheType) {
            case MEMORY:
                return memoryCache.getOrDefault(username, null);
            case CONFIG:
                if(Main.getInstance().getHeadCache().getYamlDocument().getSection("values").contains(username)) {
                    return Main.getInstance().getHeadCache().getYamlDocument().getString("values."+username);
                }

                return null;
        }
        return null;
    }

    public static void saveToCache(String username, String value) {
        switch (cacheType) {
            case MEMORY:
                memoryCache.put(username, value);
                return;
            case CONFIG:
                Main.getInstance().getHeadCache().getYamlDocument().set("values."+username, value);
                try {
                    Main.getInstance().getHeadCache().getYamlDocument().save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

        }
    }

}