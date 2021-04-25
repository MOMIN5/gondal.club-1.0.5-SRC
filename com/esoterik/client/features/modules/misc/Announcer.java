// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.misc;

import java.util.Random;
import java.util.Iterator;
import java.util.List;
import com.esoterik.client.manager.FileManager;
import com.esoterik.client.features.command.Command;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Announcer extends Module
{
    private final Setting<Boolean> join;
    private final Setting<Boolean> leave;
    private final Setting<Boolean> eat;
    private final Setting<Boolean> walk;
    private final Setting<Boolean> mine;
    private final Setting<Boolean> place;
    private final Setting<Boolean> totem;
    private final Setting<Boolean> random;
    private final Setting<Boolean> greentext;
    private final Setting<Boolean> loadFiles;
    private final Setting<Integer> delay;
    private final Setting<Integer> queueSize;
    private final Setting<Integer> mindistance;
    private final Setting<Boolean> clearQueue;
    private static final String directory = "client/announcer/";
    private Map<Action, ArrayList<String>> loadedMessages;
    private Map<Action, Message> queue;
    
    public Announcer() {
        super("Announcer", "How to get muted quick.", Category.MISC, true, false, false);
        this.join = (Setting<Boolean>)this.register(new Setting("Join", (T)true));
        this.leave = (Setting<Boolean>)this.register(new Setting("Leave", (T)true));
        this.eat = (Setting<Boolean>)this.register(new Setting("Eat", (T)true));
        this.walk = (Setting<Boolean>)this.register(new Setting("Walk", (T)true));
        this.mine = (Setting<Boolean>)this.register(new Setting("Mine", (T)true));
        this.place = (Setting<Boolean>)this.register(new Setting("Place", (T)true));
        this.totem = (Setting<Boolean>)this.register(new Setting("TotemPop", (T)true));
        this.random = (Setting<Boolean>)this.register(new Setting("Random", (T)true));
        this.greentext = (Setting<Boolean>)this.register(new Setting("Greentext", (T)false));
        this.loadFiles = (Setting<Boolean>)this.register(new Setting("LoadFiles", (T)false));
        this.delay = (Setting<Integer>)this.register(new Setting("SendDelay", (T)40));
        this.queueSize = (Setting<Integer>)this.register(new Setting("QueueSize", (T)5, (T)1, (T)100));
        this.mindistance = (Setting<Integer>)this.register(new Setting("Min Distance", (T)10, (T)1, (T)100));
        this.clearQueue = (Setting<Boolean>)this.register(new Setting("ClearQueue", (T)false));
        this.loadedMessages = new HashMap<Action, ArrayList<String>>();
        this.queue = new HashMap<Action, Message>();
    }
    
    @Override
    public void onLoad() {
        this.loadMessages();
    }
    
    @Override
    public void onEnable() {
        this.loadMessages();
    }
    
    @Override
    public void onUpdate() {
        if (this.loadFiles.getValue()) {
            this.loadMessages();
            Command.sendMessage("<Announcer> Loaded messages.");
            this.loadFiles.setValue(false);
        }
    }
    
    public void loadMessages() {
        final HashMap<Action, ArrayList<String>> newLoadedMessages = new HashMap<Action, ArrayList<String>>();
        for (final Action action : Action.values()) {
            final String fileName = "client/announcer/" + action.getName() + ".txt";
            final List<String> fileInput = FileManager.readTextFileAllLines(fileName);
            final Iterator<String> i = fileInput.iterator();
            final ArrayList<String> msgs = new ArrayList<String>();
            while (i.hasNext()) {
                final String string = i.next();
                if (!string.replaceAll("\\s", "").isEmpty()) {
                    msgs.add(string);
                }
            }
            if (msgs.isEmpty()) {
                msgs.add(action.getStandartMessage());
            }
            newLoadedMessages.put(action, msgs);
        }
        this.loadedMessages = newLoadedMessages;
    }
    
    private String getMessage(final Action action, final int number, final String info) {
        return "";
    }
    
    private Action getRandomAction() {
        final Random rnd = new Random();
        final int index = rnd.nextInt(7);
        int i = 0;
        for (final Action action : Action.values()) {
            if (i == index) {
                return action;
            }
            ++i;
        }
        return Action.WALK;
    }
    
    public static class Message
    {
        public final Action action;
        public final String name;
        public final int amount;
        
        public Message(final Action action, final String name, final int amount) {
            this.action = action;
            this.name = name;
            this.amount = amount;
        }
    }
    
    public enum Action
    {
        JOIN("Join", "Welcome _!"), 
        LEAVE("Leave", "Goodbye _!"), 
        EAT("Eat", "I just ate % _!"), 
        WALK("Walk", "I just walked % Blocks!"), 
        MINE("Mine", "I mined % _!"), 
        PLACE("Place", "I just placed % _!"), 
        TOTEM("Totem", "_ just popped % Totems!");
        
        private final String name;
        private final String standartMessage;
        
        private Action(final String name, final String standartMessage) {
            this.name = name;
            this.standartMessage = standartMessage;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getStandartMessage() {
            return this.standartMessage;
        }
    }
}
