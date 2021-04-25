// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.client;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.esoterik.client.event.events.ClientEvent;
import com.esoterik.client.esohack;
import com.esoterik.client.util.TextUtil;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Managers extends Module
{
    public Setting<Boolean> betterFrames;
    private static Managers INSTANCE;
    public Setting<Integer> betterFPS;
    public Setting<Boolean> potions;
    public Setting<Integer> textRadarUpdates;
    public Setting<Integer> respondTime;
    public Setting<Float> holeRange;
    public Setting<Boolean> speed;
    public Setting<Boolean> tRadarInv;
    public Setting<Boolean> unfocusedCpu;
    public Setting<Integer> cpuFPS;
    public Setting<Boolean> safety;
    public Setting<Integer> safetyCheck;
    public Setting<Integer> safetySync;
    public Setting<Boolean> oneDot15;
    public Setting<Integer> holeUpdates;
    public Setting<Integer> holeSync;
    public Setting<ThreadMode> holeThread;
    public TextUtil.Color bracketColor;
    public TextUtil.Color commandColor;
    public String commandBracket;
    public String commandBracket2;
    public String command;
    public int moduleListUpdates;
    public boolean rainbowPrefix;
    
    public Managers() {
        super("Management", "ClientManagement", Category.CLIENT, false, true, true);
        this.betterFrames = (Setting<Boolean>)this.register(new Setting("BetterMaxFPS", (T)false));
        this.betterFPS = (Setting<Integer>)this.register(new Setting("MaxFPS", (T)300, (T)30, (T)1000, v -> this.betterFrames.getValue()));
        this.potions = (Setting<Boolean>)this.register(new Setting("Potions", (T)true));
        this.textRadarUpdates = (Setting<Integer>)this.register(new Setting("TRUpdates", (T)500, (T)0, (T)1000));
        this.respondTime = (Setting<Integer>)this.register(new Setting("SeverTime", (T)500, (T)0, (T)1000));
        this.holeRange = (Setting<Float>)this.register(new Setting("HoleRange", (T)6.0f, (T)1.0f, (T)32.0f));
        this.speed = (Setting<Boolean>)this.register(new Setting("Speed", (T)true));
        this.tRadarInv = (Setting<Boolean>)this.register(new Setting("TRadarInv", (T)true));
        this.unfocusedCpu = (Setting<Boolean>)this.register(new Setting("UnfocusedCPU", (T)false));
        this.cpuFPS = (Setting<Integer>)this.register(new Setting("UnfocusedFPS", (T)60, (T)1, (T)60, v -> this.unfocusedCpu.getValue()));
        this.safety = (Setting<Boolean>)this.register(new Setting("SafetyPlayer", (T)false));
        this.safetyCheck = (Setting<Integer>)this.register(new Setting("SafetyCheck", (T)50, (T)1, (T)150));
        this.safetySync = (Setting<Integer>)this.register(new Setting("SafetySync", (T)250, (T)1, (T)10000));
        this.oneDot15 = (Setting<Boolean>)this.register(new Setting("1.15", (T)false));
        this.holeUpdates = (Setting<Integer>)this.register(new Setting("HoleUpdates", (T)100, (T)0, (T)1000));
        this.holeSync = (Setting<Integer>)this.register(new Setting("HoleSync", (T)10000, (T)1, (T)10000));
        this.holeThread = (Setting<ThreadMode>)this.register(new Setting("HoleThread", (T)ThreadMode.WHILE));
        this.bracketColor = TextUtil.Color.WHITE;
        this.commandColor = TextUtil.Color.DARK_PURPLE;
        this.commandBracket = "[";
        this.commandBracket2 = "]";
        this.command = esohack.getName();
        this.moduleListUpdates = 0;
        this.rainbowPrefix = true;
        this.setInstance();
    }
    
    public static Managers getInstance() {
        if (Managers.INSTANCE == null) {
            Managers.INSTANCE = new Managers();
        }
        return Managers.INSTANCE;
    }
    
    private void setInstance() {
        Managers.INSTANCE = this;
    }
    
    @Override
    public void onLoad() {
        esohack.commandManager.setClientMessage(this.getCommandMessage());
    }
    
    @SubscribeEvent
    public void onSettingChange(final ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting() != null && this.equals(event.getSetting().getFeature())) {
            if (event.getSetting().equals(this.holeThread)) {
                esohack.holeManager.settingChanged();
            }
            esohack.commandManager.setClientMessage(this.getCommandMessage());
        }
    }
    
    public String getCommandMessage() {
        return TextUtil.coloredString(this.commandBracket, this.bracketColor) + TextUtil.coloredString(this.command, this.commandColor) + TextUtil.coloredString(this.commandBracket2, this.bracketColor);
    }
    
    public String getRawCommandMessage() {
        return this.commandBracket + this.command + this.commandBracket2;
    }
    
    static {
        Managers.INSTANCE = new Managers();
    }
    
    public enum ThreadMode
    {
        POOL, 
        WHILE, 
        NONE;
    }
}
