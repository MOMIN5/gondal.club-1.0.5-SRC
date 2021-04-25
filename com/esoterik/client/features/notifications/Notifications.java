// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.notifications;

import com.esoterik.client.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.Minecraft;
import com.esoterik.client.esohack;
import com.esoterik.client.features.modules.client.HUD;
import com.esoterik.client.util.Timer;

public class Notifications
{
    private final String text;
    private final long disableTime;
    private final float width;
    private final Timer timer;
    
    public Notifications(final String text, final long disableTime) {
        this.timer = new Timer();
        this.text = text;
        this.disableTime = disableTime;
        this.width = (float)esohack.moduleManager.getModuleByClass(HUD.class).renderer.getStringWidth(text);
        this.timer.reset();
    }
    
    public void onDraw(final int y) {
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.func_71410_x());
        if (this.timer.passedMs(this.disableTime)) {
            esohack.notificationManager.getNotifications().remove(this);
        }
        RenderUtil.drawRect(scaledResolution.func_78326_a() - 4 - this.width, (float)y, (float)(scaledResolution.func_78326_a() - 2), (float)(y + esohack.moduleManager.getModuleByClass(HUD.class).renderer.getFontHeight() + 3), 1962934272);
        esohack.moduleManager.getModuleByClass(HUD.class).renderer.drawString(this.text, scaledResolution.func_78326_a() - this.width - 3.0f, (float)(y + 2), -1, true);
    }
}
