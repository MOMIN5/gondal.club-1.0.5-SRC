// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.client;

import com.esoterik.client.manager.FileManager;
import java.util.Iterator;
import com.esoterik.client.features.command.Command;
import com.esoterik.client.esohack;
import java.util.Collection;
import java.util.ArrayList;
import com.esoterik.client.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Notifications extends Module
{
    public Setting<Boolean> totemPops;
    public Setting<Boolean> totemNoti;
    public Setting<Integer> delay;
    public Setting<Boolean> clearOnLogout;
    public Setting<Boolean> visualRange;
    public Setting<Boolean> coords;
    public Setting<Boolean> leaving;
    public Setting<Boolean> crash;
    private List<EntityPlayer> knownPlayers;
    private static List<String> modules;
    private static final String fileName = "client/util/ModuleMessage_List.txt";
    private final Timer timer;
    public Timer totemAnnounce;
    private boolean check;
    private static Notifications INSTANCE;
    
    public Notifications() {
        super("Notifications", "Sends Messages.", Category.CLIENT, true, true, false);
        this.totemPops = (Setting<Boolean>)this.register(new Setting("TotemPops", (T)false));
        this.totemNoti = (Setting<Boolean>)this.register(new Setting("TotemNoti", (T)true, v -> this.totemPops.getValue()));
        this.delay = (Setting<Integer>)this.register(new Setting("Delay", (T)2000, (T)0, (T)5000, v -> this.totemPops.getValue(), "Delays messages."));
        this.clearOnLogout = (Setting<Boolean>)this.register(new Setting("LogoutClear", (T)false));
        this.visualRange = (Setting<Boolean>)this.register(new Setting("VisualRange", (T)false));
        this.coords = (Setting<Boolean>)this.register(new Setting("Coords", (T)true, v -> this.visualRange.getValue()));
        this.leaving = (Setting<Boolean>)this.register(new Setting("Leaving", (T)false, v -> this.visualRange.getValue()));
        this.crash = (Setting<Boolean>)this.register(new Setting("Crash", (T)false));
        this.knownPlayers = new ArrayList<EntityPlayer>();
        this.timer = new Timer();
        this.totemAnnounce = new Timer();
        this.setInstance();
    }
    
    private void setInstance() {
        Notifications.INSTANCE = this;
    }
    
    @Override
    public void onLoad() {
        this.check = true;
        this.loadFile();
        this.check = false;
    }
    
    @Override
    public void onEnable() {
        this.knownPlayers = new ArrayList<EntityPlayer>();
        if (!this.check) {
            this.loadFile();
        }
    }
    
    @Override
    public void onUpdate() {
        if (this.visualRange.getValue()) {
            final List<EntityPlayer> tickPlayerList = new ArrayList<EntityPlayer>(Notifications.mc.field_71441_e.field_73010_i);
            if (tickPlayerList.size() > 0) {
                for (final EntityPlayer player : tickPlayerList) {
                    if (player.func_70005_c_().equals(Notifications.mc.field_71439_g.func_70005_c_())) {
                        continue;
                    }
                    if (!this.knownPlayers.contains(player)) {
                        this.knownPlayers.add(player);
                        if (esohack.friendManager.isFriend(player)) {
                            Command.sendMessage("Player §a" + player.func_70005_c_() + "§r" + " entered your visual range" + (this.coords.getValue() ? (" at (" + (int)player.field_70165_t + ", " + (int)player.field_70163_u + ", " + (int)player.field_70161_v + ")!") : "!"), true);
                        }
                        else {
                            Command.sendMessage("Player §c" + player.func_70005_c_() + "§r" + " entered your visual range" + (this.coords.getValue() ? (" at (" + (int)player.field_70165_t + ", " + (int)player.field_70163_u + ", " + (int)player.field_70161_v + ")!") : "!"), true);
                        }
                        return;
                    }
                }
            }
            if (this.knownPlayers.size() > 0) {
                for (final EntityPlayer player : this.knownPlayers) {
                    if (!tickPlayerList.contains(player)) {
                        this.knownPlayers.remove(player);
                        if (this.leaving.getValue()) {
                            if (esohack.friendManager.isFriend(player)) {
                                Command.sendMessage("Player §a" + player.func_70005_c_() + "§r" + " left your visual range" + (this.coords.getValue() ? (" at (" + (int)player.field_70165_t + ", " + (int)player.field_70163_u + ", " + (int)player.field_70161_v + ")!") : "!"), true);
                            }
                            else {
                                Command.sendMessage("Player §c" + player.func_70005_c_() + "§r" + " left your visual range" + (this.coords.getValue() ? (" at (" + (int)player.field_70165_t + ", " + (int)player.field_70163_u + ", " + (int)player.field_70161_v + ")!") : "!"), true);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void loadFile() {
        final List<String> fileInput = FileManager.readTextFileAllLines("client/util/ModuleMessage_List.txt");
        final Iterator<String> i = fileInput.iterator();
        Notifications.modules.clear();
        while (i.hasNext()) {
            final String s = i.next();
            if (!s.replaceAll("\\s", "").isEmpty()) {
                Notifications.modules.add(s);
            }
        }
    }
    
    public static Notifications getInstance() {
        if (Notifications.INSTANCE == null) {
            Notifications.INSTANCE = new Notifications();
        }
        return Notifications.INSTANCE;
    }
    
    public static void displayCrash(final Exception e) {
        Command.sendMessage("§cException caught: " + e.getMessage());
    }
    
    static {
        Notifications.modules = new ArrayList<String>();
        Notifications.INSTANCE = new Notifications();
    }
}
