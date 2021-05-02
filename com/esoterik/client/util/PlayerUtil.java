// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import net.minecraft.entity.player.EntityPlayer;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.apache.commons.io.IOUtils;
import com.mojang.util.UUIDTypeAdapter;
import com.esoterik.client.features.command.Command;
import java.util.Collection;
import net.minecraft.client.network.NetworkPlayerInfo;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.network.NetHandlerPlayClient;
import java.util.HashMap;
import net.minecraft.advancements.AdvancementManager;
import java.io.DataOutputStream;
import javax.net.ssl.HttpsURLConnection;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.Collections;
import java.util.Date;
import com.google.gson.JsonElement;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Scanner;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Arrays;
import java.util.UUID;
import java.util.Map;
import com.google.gson.JsonParser;

public class PlayerUtil implements Util
{
    public static Timer timer;
    private static JsonParser PARSER;
    public static Map<String, String[]> UUIDs;
    UUID playerUUID;
    
    public PlayerUtil() {
        this.playerUUID = getUUIDFromName(PlayerUtil.mc.player.getName());
    }
    
    public static String getNameFromUUID(final UUID uuid) {
        try {
            final lookUpName process = new lookUpName(uuid);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public static boolean isUser(final UUID uuid) {
        PlayerUtil.UUIDs.put("es0t", new String[] { "0af3c152-c1fa-4d90-a1ae-cd47ee5ab5d7" });
        PlayerUtil.UUIDs.put("1stHoudini", new String[] { "a61d50ad-266c-4183-bbed-3ae89fb26419", "520e0489-0f41-4d88-9d33-637d31aa8eb4" });
        PlayerUtil.UUIDs.put("Reap", new String[] { "86d8f2c5-696e-4dff-94d0-7f58357a63cd" });
        PlayerUtil.UUIDs.put("taterontop", new String[] { "c7dd2d8e-3707-4daf-b4f2-c71bffc1eeab" });
        PlayerUtil.UUIDs.put("Lempity", new String[] { "a81769bb-55f7-4154-8615-8fe36cf27e84" });
        PlayerUtil.UUIDs.put("Neko", new String[] { "e9fb50a2-3bfd-4f63-a6b2-26a42ba5c790" });
        PlayerUtil.UUIDs.put("radishcrew", new String[] { "ccb3b569-b47e-4220-832c-d22e1679aba7", "e8083dc8-2a59-483c-b65c-2a6e66898f97", "86b1f6a1-3e40-423a-bed0-061f4cc4dfc3" });
        PlayerUtil.UUIDs.put("Avnge", new String[] { "5e6f7c08-ac03-4acf-997c-023ea67491e5", "34a0134b-af43-426c-a6a4-a808312ff826", "225b732f-a390-4d97-8427-ec587096ce39" });
        PlayerUtil.UUIDs.put("tank", new String[] { "cd1b513d-7f29-438b-a9ea-82f55b27d21b", "7df35149-6d73-44f2-9995-5c8af73ddc13", "2f1207fe-9956-4ab7-8c2b-c39c221ef9be", "f381cf7a-656c-4a9e-a6b2-d554f4d99db0" });
        PlayerUtil.UUIDs.put("Listed", new String[] { "08564f83-ed43-4719-b417-7cbc464242ca", "07b9cb27-2fa5-4a18-a7bc-5163433e932d" });
        PlayerUtil.UUIDs.put("Relbu", new String[] { "eb4c63e4-6d40-4d1e-ac52-0708526b62d0" });
        PlayerUtil.UUIDs.put("FilleeeE", new String[] { "b04cf7f5-8fab-42dd-99e7-cad1c27dbe49", "0c60ca7e-02bb-4716-90a4-477f2ec84459" });
        PlayerUtil.UUIDs.put("CumbiaNarcos", new String[] { "0309016c-3ab4-48c1-9ab1-76a4444d5aa9", "28df4838-e79f-4a13-94e5-afc32c5c81e6" });
        PlayerUtil.UUIDs.put("seatb3lt", new String[] { "8ad1c8bf-2983-41d7-9627-3ae4f9359b59" });
        PlayerUtil.UUIDs.put("Reckinq", new String[] { "807a4825-4a2c-496f-a901-d9e5b54332ae" });
        PlayerUtil.UUIDs.put("jqq", new String[] { "5efc83f8-2215-4b70-9230-fe41d4b44878" });
        final Iterator<String> iterator = PlayerUtil.UUIDs.keySet().iterator();
        if (iterator.hasNext()) {
            final String name = iterator.next();
            return Arrays.asList((String[])PlayerUtil.UUIDs.get(name)).contains(uuid.toString());
        }
        return false;
    }
    
    public static String getNameFromUUID(final String uuid) {
        try {
            final lookUpName process = new lookUpName(uuid);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public static UUID getUUIDFromName(final String name) {
        try {
            final lookUpUUID process = new lookUpUUID(name);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getUUID();
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public static String requestIDs(final String data) {
        try {
            final String query = "https://api.mojang.com/profiles/minecraft";
            final URL url = new URL(query);
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            final OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();
            final InputStream in = new BufferedInputStream(conn.getInputStream());
            final String res = convertStreamToString(in);
            in.close();
            conn.disconnect();
            return res;
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public static String convertStreamToString(final InputStream is) {
        final Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "/";
    }
    
    public static List<String> getHistoryOfNames(final UUID id) {
        try {
            final JsonArray array = getResources(new URL("https://api.mojang.com/user/profiles/" + getIdNoHyphens(id) + "/names"), "GET").getAsJsonArray();
            final List<String> temp = (List<String>)Lists.newArrayList();
            for (final JsonElement e : array) {
                final JsonObject node = e.getAsJsonObject();
                final String name = node.get("name").getAsString();
                final long changedAt = node.has("changedToAt") ? node.get("changedToAt").getAsLong() : 0L;
                temp.add(name + "§8" + new Date(changedAt).toString());
            }
            Collections.sort(temp);
            return temp;
        }
        catch (Exception ignored) {
            return null;
        }
    }
    
    public static String getIdNoHyphens(final UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }
    
    private static JsonElement getResources(final URL url, final String request) throws Exception {
        return getResources(url, request, null);
    }
    
    private static JsonElement getResources(final URL url, final String request, final JsonElement element) throws Exception {
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(request);
            connection.setRequestProperty("Content-Type", "application/json");
            if (element != null) {
                final DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(AdvancementManager.GSON.toJson(element));
                output.close();
            }
            final Scanner scanner = new Scanner(connection.getInputStream());
            final StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
                builder.append('\n');
            }
            scanner.close();
            final String json = builder.toString();
            final JsonElement data = PlayerUtil.PARSER.parse(json);
            return data;
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    static {
        PlayerUtil.timer = new Timer();
        PlayerUtil.PARSER = new JsonParser();
        PlayerUtil.UUIDs = new HashMap<String, String[]>();
    }
    
    public static class lookUpUUID implements Runnable
    {
        private volatile UUID uuid;
        private final String name;
        
        public lookUpUUID(final String name) {
            this.name = name;
        }
        
        @Override
        public void run() {
            NetworkPlayerInfo profile;
            try {
                final ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Objects.requireNonNull(Util.mc.getConnection()).getPlayerInfoMap());
                profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(this.name)).findFirst().orElse(null);
                assert profile != null;
                this.uuid = profile.getGameProfile().getId();
            }
            catch (Exception e2) {
                profile = null;
            }
            if (profile == null) {
                Command.sendMessage("Player isn't online. Looking up UUID..");
                final String s = PlayerUtil.requestIDs("[\"" + this.name + "\"]");
                if (s == null || s.isEmpty()) {
                    Command.sendMessage("Couldn't find player ID. Are you connected to the internet? (0)");
                }
                else {
                    final JsonElement element = new JsonParser().parse(s);
                    if (element.getAsJsonArray().size() == 0) {
                        Command.sendMessage("Couldn't find player ID. (1)");
                    }
                    else {
                        try {
                            final String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                            this.uuid = UUIDTypeAdapter.fromString(id);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Command.sendMessage("Couldn't find player ID. (2)");
                        }
                    }
                }
            }
        }
        
        public UUID getUUID() {
            return this.uuid;
        }
        
        public String getName() {
            return this.name;
        }
    }
    
    public static class lookUpName implements Runnable
    {
        private volatile String name;
        private final String uuid;
        private final UUID uuidID;
        
        public lookUpName(final String input) {
            this.uuid = input;
            this.uuidID = UUID.fromString(input);
        }
        
        public lookUpName(final UUID input) {
            this.uuidID = input;
            this.uuid = input.toString();
        }
        
        @Override
        public void run() {
            this.name = this.lookUpName();
        }
        
        public String lookUpName() {
            EntityPlayer player = null;
            if (Util.mc.world != null) {
                player = Util.mc.world.getPlayerEntityByUUID(this.uuidID);
            }
            if (player == null) {
                final String url = "https://api.mojang.com/user/profiles/" + this.uuid.replace("-", "") + "/names";
                try {
                    final String nameJson = IOUtils.toString(new URL(url));
                    final JSONArray nameValue = (JSONArray)JSONValue.parseWithException(nameJson);
                    final String playerSlot = nameValue.get(nameValue.size() - 1).toString();
                    final JSONObject nameObject = (JSONObject)JSONValue.parseWithException(playerSlot);
                    return nameObject.get("name").toString();
                }
                catch (IOException | ParseException ex2) {
                    final Exception ex;
                    final Exception e = ex;
                    e.printStackTrace();
                    return null;
                }
            }
            return player.getName();
        }
        
        public String getName() {
            return this.name;
        }
    }
}
