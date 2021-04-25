// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.manager;

import net.minecraft.util.math.MathHelper;
import com.esoterik.client.esohack;
import com.esoterik.client.features.modules.client.FontMod;
import java.awt.Font;
import com.esoterik.client.util.Timer;
import com.esoterik.client.features.gui.font.CustomFont;
import com.esoterik.client.features.Feature;

public class TextManager extends Feature
{
    private CustomFont customFont;
    public int scaledWidth;
    public int scaledHeight;
    public int scaleFactor;
    private final Timer idleTimer;
    private boolean idling;
    
    public TextManager() {
        this.customFont = new CustomFont(new Font("Verdana", 0, 17), true, false);
        this.idleTimer = new Timer();
        this.updateResolution();
    }
    
    public void init(final boolean startup) {
        final FontMod cFont = esohack.moduleManager.getModuleByClass(FontMod.class);
        try {
            this.setFontRenderer(new Font(cFont.fontName.getValue(), cFont.fontStyle.getValue(), cFont.fontSize.getValue()), cFont.antiAlias.getValue(), cFont.fractionalMetrics.getValue());
        }
        catch (Exception ex) {}
    }
    
    public void drawStringWithShadow(final String text, final float x, final float y, final int color) {
        this.drawString(text, x, y, color, true);
    }
    
    public void drawString(final String text, final float x, final float y, final int color, final boolean shadow) {
        if (esohack.moduleManager.isModuleEnabled(FontMod.class)) {
            if (shadow) {
                this.customFont.drawStringWithShadow(text, x, y, color);
            }
            else {
                this.customFont.drawString(text, x, y, color);
            }
            return;
        }
        TextManager.mc.field_71466_p.func_175065_a(text, x, y, color, shadow);
    }
    
    public int getStringWidth(final String text) {
        if (esohack.moduleManager.isModuleEnabled(FontMod.class)) {
            return this.customFont.getStringWidth(text);
        }
        return TextManager.mc.field_71466_p.func_78256_a(text);
    }
    
    public int getFontHeight() {
        if (esohack.moduleManager.isModuleEnabled(FontMod.class)) {
            final String text = "A";
            return this.customFont.getStringHeight(text);
        }
        return TextManager.mc.field_71466_p.field_78288_b;
    }
    
    public void setFontRenderer(final Font font, final boolean antiAlias, final boolean fractionalMetrics) {
        this.customFont = new CustomFont(font, antiAlias, fractionalMetrics);
    }
    
    public Font getCurrentFont() {
        return this.customFont.getFont();
    }
    
    public void updateResolution() {
        this.scaledWidth = TextManager.mc.field_71443_c;
        this.scaledHeight = TextManager.mc.field_71440_d;
        this.scaleFactor = 1;
        final boolean flag = TextManager.mc.func_152349_b();
        int i = TextManager.mc.field_71474_y.field_74335_Z;
        if (i == 0) {
            i = 1000;
        }
        while (this.scaleFactor < i && this.scaledWidth / (this.scaleFactor + 1) >= 320 && this.scaledHeight / (this.scaleFactor + 1) >= 240) {
            ++this.scaleFactor;
        }
        if (flag && this.scaleFactor % 2 != 0 && this.scaleFactor != 1) {
            --this.scaleFactor;
        }
        final double scaledWidthD = this.scaledWidth / (double)this.scaleFactor;
        final double scaledHeightD = this.scaledHeight / (double)this.scaleFactor;
        this.scaledWidth = MathHelper.func_76143_f(scaledWidthD);
        this.scaledHeight = MathHelper.func_76143_f(scaledHeightD);
    }
    
    public String getIdleSign() {
        if (this.idleTimer.passedMs(500L)) {
            this.idling = !this.idling;
            this.idleTimer.reset();
        }
        if (this.idling) {
            return "_";
        }
        return "";
    }
}
