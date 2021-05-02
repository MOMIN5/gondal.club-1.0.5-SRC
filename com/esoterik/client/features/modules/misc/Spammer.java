// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.misc;

import com.esoterik.client.esohack;
import java.util.Iterator;
import com.esoterik.client.util.FileUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import com.esoterik.client.util.Timer;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Spammer extends Module
{
    public Setting<Integer> delay;
    public Setting<Boolean> greentext;
    public Setting<Boolean> random;
    public Setting<Boolean> loadFile;
    private final Timer timer;
    private final List<String> sendPlayers;
    private static final String fileName = "client/util/Spammer.txt";
    private static final String defaultMessage;
    private static final List<String> spamMessages;
    private static final Random rnd;
    
    public Spammer() {
        super("Spammer", "Spams stuff.", Category.MISC, true, false, false);
        this.delay = (Setting<Integer>)this.register(new Setting("Delay", (T)10, (T)1, (T)20));
        this.greentext = (Setting<Boolean>)this.register(new Setting("Greentext", (T)false));
        this.random = (Setting<Boolean>)this.register(new Setting("Random", (T)false));
        this.loadFile = (Setting<Boolean>)this.register(new Setting("LoadFile", (T)false));
        this.timer = new Timer();
        this.sendPlayers = new ArrayList<String>();
    }
    
    @Override
    public void onLoad() {
        this.readSpamFile();
        this.disable();
    }
    
    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            this.disable();
            return;
        }
        this.readSpamFile();
    }
    
    @Override
    public void onLogin() {
        this.disable();
    }
    
    @Override
    public void onLogout() {
        this.disable();
    }
    
    @Override
    public void onDisable() {
        Spammer.spamMessages.clear();
        this.timer.reset();
    }
    
    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            this.disable();
            return;
        }
        if (this.loadFile.getValue()) {
            this.readSpamFile();
            this.loadFile.setValue(false);
        }
        if (!this.timer.passedS(this.delay.getValue())) {
            return;
        }
        if (Spammer.spamMessages.size() > 0) {
            String messageOut;
            if (this.random.getValue()) {
                final int index = Spammer.rnd.nextInt(Spammer.spamMessages.size());
                messageOut = Spammer.spamMessages.get(index);
                Spammer.spamMessages.remove(index);
            }
            else {
                messageOut = Spammer.spamMessages.get(0);
                Spammer.spamMessages.remove(0);
            }
            Spammer.spamMessages.add(messageOut);
            if (this.greentext.getValue()) {
                messageOut = "> " + messageOut;
            }
            Spammer.mc.player.connection.sendPacket((Packet)new CPacketChatMessage(messageOut.replaceAll("§", "")));
        }
        this.timer.reset();
    }
    
    private void readSpamFile() {
        final List<String> fileInput = FileUtil.readTextFileAllLines("client/util/Spammer.txt");
        final Iterator<String> i = fileInput.iterator();
        Spammer.spamMessages.clear();
        while (i.hasNext()) {
            final String s = i.next();
            if (!s.replaceAll("\\s", "").isEmpty()) {
                Spammer.spamMessages.add(s);
            }
        }
        if (Spammer.spamMessages.size() == 0) {
            Spammer.spamMessages.add(Spammer.defaultMessage);
        }
    }
    
    static {
        defaultMessage = esohack.getName() + " owns all https://discord.gg/wJq5nMEdNT";
        spamMessages = new ArrayList<String>();
        rnd = new Random();
    }
}
