// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.client;

import java.awt.Color;
import net.minecraft.client.gui.GuiScreen;
import com.esoterik.client.features.gui.esohackGui;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.esoterik.client.features.command.Command;
import com.esoterik.client.esohack;
import com.esoterik.client.event.events.ClientEvent;
import net.minecraft.client.settings.GameSettings;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class ClickGui extends Module
{
    public Setting<String> prefix;
    public Setting<Integer> red;
    public Setting<Integer> green;
    public Setting<Integer> blue;
    public Setting<Integer> hoverAlpha;
    public Setting<Integer> alpha;
    public Setting<Integer> backgroundAlpha;
    public Setting<Boolean> customFov;
    public Setting<Float> fov;
    public Setting<String> moduleButton;
    public Setting<Boolean> colorSync;
    private static ClickGui INSTANCE;
    
    public ClickGui() {
        super("ClickGui", "Opens the ClickGui", Category.CLIENT, true, false, false);
        this.prefix = (Setting<String>)this.register(new Setting("Prefix", (T)"."));
        this.red = (Setting<Integer>)this.register(new Setting("Red", (T)255, (T)0, (T)255));
        this.green = (Setting<Integer>)this.register(new Setting("Green", (T)0, (T)0, (T)255));
        this.blue = (Setting<Integer>)this.register(new Setting("Blue", (T)0, (T)0, (T)255));
        this.hoverAlpha = (Setting<Integer>)this.register(new Setting("Alpha", (T)180, (T)0, (T)255));
        this.alpha = (Setting<Integer>)this.register(new Setting("HoverAlpha", (T)240, (T)0, (T)255));
        this.backgroundAlpha = (Setting<Integer>)this.register(new Setting("BackgroundAlpha", (T)140, (T)0, (T)255));
        this.customFov = (Setting<Boolean>)this.register(new Setting("CustomFov", (T)false));
        this.fov = (Setting<Float>)this.register(new Setting("Fov", (T)150.0f, (T)(-180.0f), (T)180.0f, v -> this.customFov.getValue()));
        this.moduleButton = (Setting<String>)this.register(new Setting("Buttons", (T)""));
        this.colorSync = (Setting<Boolean>)this.register(new Setting("ColorSync", (T)false));
        this.setInstance();
    }
    
    private void setInstance() {
        ClickGui.INSTANCE = this;
    }
    
    public static ClickGui getInstance() {
        if (ClickGui.INSTANCE == null) {
            ClickGui.INSTANCE = new ClickGui();
        }
        return ClickGui.INSTANCE;
    }
    
    @Override
    public void onUpdate() {
        if (this.customFov.getValue()) {
            ClickGui.mc.gameSettings.setOptionFloatValue(GameSettings.Options.FOV, (float)this.fov.getValue());
        }
    }
    
    @SubscribeEvent
    public void onSettingChange(final ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                esohack.commandManager.setPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to §a" + esohack.commandManager.getPrefix());
            }
            esohack.colorManager.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(), this.hoverAlpha.getPlannedValue());
        }
    }
    
    @Override
    public void onEnable() {
        ClickGui.mc.displayGuiScreen((GuiScreen)new esohackGui());
    }
    
    @Override
    public void onLoad() {
        esohack.colorManager.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.hoverAlpha.getValue());
        esohack.commandManager.setPrefix(this.prefix.getValue());
    }
    
    public Color getColor() {
        return new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
    }
    
    @Override
    public void onTick() {
        if (!(ClickGui.mc.currentScreen instanceof esohackGui)) {
            this.disable();
        }
    }
    
    @Override
    public void onDisable() {
        if (ClickGui.mc.currentScreen instanceof esohackGui) {
            ClickGui.mc.displayGuiScreen((GuiScreen)null);
        }
    }
    
    static {
        ClickGui.INSTANCE = new ClickGui();
    }
}
