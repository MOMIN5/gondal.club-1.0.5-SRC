// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.client;

import java.awt.GraphicsEnvironment;
import com.esoterik.client.esohack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.esoterik.client.features.command.Command;
import com.esoterik.client.event.events.ClientEvent;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class FontMod extends Module
{
    private boolean reloadFont;
    public Setting<String> fontName;
    public Setting<Integer> fontSize;
    public Setting<Integer> fontStyle;
    public Setting<Boolean> antiAlias;
    public Setting<Boolean> fractionalMetrics;
    public Setting<Boolean> shadow;
    public Setting<Boolean> showFonts;
    private static FontMod INSTANCE;
    
    public FontMod() {
        super("CustomFont", "CustomFont for all of the clients text. Use the font command.", Category.CLIENT, true, false, false);
        this.reloadFont = false;
        this.fontName = (Setting<String>)this.register(new Setting("FontName", (T)"Arial", "Name of the font."));
        this.fontSize = (Setting<Integer>)this.register(new Setting("FontSize", (T)18, "Size of the font."));
        this.fontStyle = (Setting<Integer>)this.register(new Setting("FontStyle", (T)0, "Style of the font."));
        this.antiAlias = (Setting<Boolean>)this.register(new Setting("AntiAlias", (T)true, "Smoother font."));
        this.fractionalMetrics = (Setting<Boolean>)this.register(new Setting("Metrics", (T)true, "Thinner font."));
        this.shadow = (Setting<Boolean>)this.register(new Setting("Shadow", (T)true, "Less shadow offset font."));
        this.showFonts = (Setting<Boolean>)this.register(new Setting("Fonts", (T)false, "Shows all fonts."));
        this.setInstance();
    }
    
    private void setInstance() {
        FontMod.INSTANCE = this;
    }
    
    public static FontMod getInstance() {
        if (FontMod.INSTANCE == null) {
            FontMod.INSTANCE = new FontMod();
        }
        return FontMod.INSTANCE;
    }
    
    @SubscribeEvent
    public void onSettingChange(final ClientEvent event) {
        if (event.getStage() == 2) {
            final Setting setting = event.getSetting();
            if (setting != null && setting.getFeature().equals(this)) {
                if (setting.getName().equals("FontName") && !checkFont(setting.getPlannedValue().toString(), false)) {
                    Command.sendMessage("§cThat font doesnt exist.");
                    event.setCanceled(true);
                    return;
                }
                this.reloadFont = true;
            }
        }
    }
    
    @Override
    public void onTick() {
        if (this.showFonts.getValue()) {
            checkFont("Hello", true);
            Command.sendMessage("Current Font: " + this.fontName.getValue());
            this.showFonts.setValue(false);
        }
        if (this.reloadFont) {
            esohack.textManager.init(false);
            this.reloadFont = false;
        }
    }
    
    public static boolean checkFont(final String font, final boolean message) {
        final String[] availableFontFamilyNames;
        final String[] fonts = availableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (final String s : availableFontFamilyNames) {
            if (!message && s.equals(font)) {
                return true;
            }
            if (message) {
                Command.sendMessage(s);
            }
        }
        return false;
    }
    
    static {
        FontMod.INSTANCE = new FontMod();
    }
}
