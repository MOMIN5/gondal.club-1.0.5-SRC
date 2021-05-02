// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.render;

import java.util.Iterator;
import com.esoterik.client.util.RenderUtil;
import com.esoterik.client.features.modules.client.Colors;
import com.esoterik.client.util.RotationUtil;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.esohack;
import java.util.Random;
import com.esoterik.client.event.events.Render3DEvent;
import java.awt.Color;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class HoleESP extends Module
{
    private static HoleESP INSTANCE;
    public Setting<Boolean> ownHole;
    public Setting<Boolean> box;
    public Setting<Boolean> gradientBox;
    public Setting<Boolean> pulseAlpha;
    public Setting<Boolean> invertGradientBox;
    public Setting<Boolean> outline;
    public Setting<Boolean> gradientOutline;
    public Setting<Boolean> invertGradientOutline;
    public Setting<Double> height;
    public Setting<Boolean> sync;
    public Setting<Sync> syncMode;
    public Setting<Boolean> safeColor;
    private final Setting<Integer> holes;
    private final Setting<Integer> minPulseAlpha;
    private final Setting<Integer> maxPulseAlpha;
    private final Setting<Integer> pulseSpeed;
    private final Setting<Integer> red;
    private final Setting<Integer> green;
    private final Setting<Integer> blue;
    private final Setting<Integer> alpha;
    private final Setting<Integer> boxAlpha;
    private final Setting<Float> lineWidth;
    private final Setting<Integer> safeRed;
    private final Setting<Integer> safeGreen;
    private final Setting<Integer> safeBlue;
    private final Setting<Integer> safeAlpha;
    private boolean pulsing;
    private boolean shouldDecrease;
    private int pulseDelay;
    private int currentPulseAlpha;
    private int currentAlpha;
    Color safecolor;
    Color obbycolor;
    
    public HoleESP() {
        super("HoleESP", "Shows safe spots.", Category.RENDER, false, false, false);
        this.ownHole = (Setting<Boolean>)this.register(new Setting("OwnHole", (T)false));
        this.box = (Setting<Boolean>)this.register(new Setting("Box", (T)true));
        this.gradientBox = (Setting<Boolean>)this.register(new Setting("GradientBox", (T)false, v -> this.box.getValue()));
        this.pulseAlpha = (Setting<Boolean>)this.register(new Setting("PulseAlpha", (T)false, v -> this.gradientBox.getValue()));
        this.invertGradientBox = (Setting<Boolean>)this.register(new Setting("InvertGradientBox", (T)false, v -> this.gradientBox.getValue()));
        this.outline = (Setting<Boolean>)this.register(new Setting("Outline", (T)true));
        this.gradientOutline = (Setting<Boolean>)this.register(new Setting("GradientOutline", (T)false, v -> this.outline.getValue()));
        this.invertGradientOutline = (Setting<Boolean>)this.register(new Setting("InvertGradientOutline", (T)false, v -> this.gradientOutline.getValue()));
        this.height = (Setting<Double>)this.register(new Setting("Height", (T)0.0, (T)(-2.0), (T)2.0));
        this.sync = (Setting<Boolean>)this.register(new Setting("ColorSync", (T)false));
        this.syncMode = (Setting<Sync>)this.register(new Setting("SyncMode", (T)Sync.BOTH, v -> this.sync.getValue()));
        this.safeColor = (Setting<Boolean>)this.register(new Setting("SafeColor", (T)false));
        this.holes = (Setting<Integer>)this.register(new Setting("Holes", (T)3, (T)1, (T)500));
        this.minPulseAlpha = (Setting<Integer>)this.register(new Setting("MinPulse", (T)10, (T)0, (T)255, v -> this.pulseAlpha.getValue()));
        this.maxPulseAlpha = (Setting<Integer>)this.register(new Setting("MaxPulse", (T)40, (T)0, (T)255, v -> this.pulseAlpha.getValue()));
        this.pulseSpeed = (Setting<Integer>)this.register(new Setting("PulseSpeed", (T)10, (T)1, (T)50, v -> this.pulseAlpha.getValue()));
        this.red = (Setting<Integer>)this.register(new Setting("Red", (T)0, (T)0, (T)255));
        this.green = (Setting<Integer>)this.register(new Setting("Green", (T)255, (T)0, (T)255));
        this.blue = (Setting<Integer>)this.register(new Setting("Blue", (T)0, (T)0, (T)255));
        this.alpha = (Setting<Integer>)this.register(new Setting("Alpha", (T)255, (T)0, (T)255));
        this.boxAlpha = (Setting<Integer>)this.register(new Setting("BoxAlpha", (T)125, (T)0, (T)255, v -> this.box.getValue()));
        this.lineWidth = (Setting<Float>)this.register(new Setting("LineWidth", (T)1.0f, (T)0.1f, (T)5.0f, v -> this.outline.getValue()));
        this.safeRed = (Setting<Integer>)this.register(new Setting("SafeRed", (T)0, (T)0, (T)255, v -> this.safeColor.getValue()));
        this.safeGreen = (Setting<Integer>)this.register(new Setting("SafeGreen", (T)255, (T)0, (T)255, v -> this.safeColor.getValue()));
        this.safeBlue = (Setting<Integer>)this.register(new Setting("SafeBlue", (T)0, (T)0, (T)255, v -> this.safeColor.getValue()));
        this.safeAlpha = (Setting<Integer>)this.register(new Setting("SafeAlpha", (T)255, (T)0, (T)255, v -> this.safeColor.getValue()));
        this.pulsing = false;
        this.shouldDecrease = false;
        this.pulseDelay = 0;
        this.currentAlpha = 0;
        this.safecolor = new Color(this.safeRed.getValue(), this.safeGreen.getValue(), this.safeBlue.getValue(), this.safeAlpha.getValue());
        this.obbycolor = new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
        this.setInstance();
    }
    
    private void setInstance() {
        HoleESP.INSTANCE = this;
    }
    
    public static HoleESP getInstance() {
        if (HoleESP.INSTANCE == null) {
            HoleESP.INSTANCE = new HoleESP();
        }
        return HoleESP.INSTANCE;
    }
    
    @Override
    public void onRender3D(final Render3DEvent event) {
        int drawnHoles = 0;
        if (!this.pulsing && this.pulseAlpha.getValue()) {
            final Random rand = new Random();
            this.currentPulseAlpha = rand.nextInt(this.maxPulseAlpha.getValue() - this.minPulseAlpha.getValue() + 1) + this.minPulseAlpha.getValue();
            this.pulsing = true;
            this.shouldDecrease = false;
        }
        if (this.pulseDelay == 0) {
            if (this.pulsing && this.pulseAlpha.getValue() && !this.shouldDecrease) {
                ++this.currentAlpha;
                if (this.currentAlpha >= this.currentPulseAlpha) {
                    this.shouldDecrease = true;
                }
            }
            if (this.pulsing && this.pulseAlpha.getValue() && this.shouldDecrease) {
                --this.currentAlpha;
            }
            if (this.currentAlpha <= 0) {
                this.pulsing = false;
                this.shouldDecrease = false;
            }
            ++this.pulseDelay;
        }
        else {
            ++this.pulseDelay;
            if (this.pulseDelay == 51 - this.pulseSpeed.getValue()) {
                this.pulseDelay = 0;
            }
        }
        if (!this.pulseAlpha.getValue() || !this.pulsing) {
            this.currentAlpha = 0;
        }
        for (final BlockPos pos : esohack.holeManager.getSortedHoles()) {
            if (drawnHoles >= this.holes.getValue()) {
                break;
            }
            if (pos.equals((Object)new BlockPos(HoleESP.mc.player.posX, HoleESP.mc.player.posY, HoleESP.mc.player.posZ)) && !this.ownHole.getValue()) {
                continue;
            }
            if (!RotationUtil.isInFov(pos)) {
                continue;
            }
            if (this.safeColor.getValue() && esohack.holeManager.isSafe(pos)) {
                RenderUtil.drawBoxESP(pos, ((this.syncMode.getValue() == Sync.SAFE || this.syncMode.getValue() == Sync.BOTH) && this.sync.getValue()) ? Colors.INSTANCE.getCurrentColor() : this.safecolor, false, ((this.syncMode.getValue() == Sync.SAFE || this.syncMode.getValue() == Sync.BOTH) && this.sync.getValue()) ? Colors.INSTANCE.getCurrentColor() : this.safecolor, this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true, this.height.getValue(), this.gradientBox.getValue(), this.gradientOutline.getValue(), this.invertGradientBox.getValue(), this.invertGradientOutline.getValue(), this.currentAlpha);
            }
            else {
                RenderUtil.drawBoxESP(pos, ((this.syncMode.getValue() == Sync.OBBY || this.syncMode.getValue() == Sync.BOTH) && this.sync.getValue()) ? Colors.INSTANCE.getCurrentColor() : this.obbycolor, false, ((this.syncMode.getValue() == Sync.OBBY || this.syncMode.getValue() == Sync.BOTH) && this.sync.getValue()) ? Colors.INSTANCE.getCurrentColor() : this.obbycolor, this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true, this.height.getValue(), this.gradientBox.getValue(), this.gradientOutline.getValue(), this.invertGradientBox.getValue(), this.invertGradientOutline.getValue(), this.currentAlpha);
            }
            ++drawnHoles;
        }
    }
    
    static {
        HoleESP.INSTANCE = new HoleESP();
    }
    
    private enum Sync
    {
        OBBY, 
        SAFE, 
        BOTH;
    }
}
