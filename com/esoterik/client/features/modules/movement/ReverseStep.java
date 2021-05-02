// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import com.esoterik.client.features.Feature;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class ReverseStep extends Module
{
    private final Setting<Integer> speed;
    
    public ReverseStep() {
        super("ReverseStep", "Go down", Category.MOVEMENT, true, false, false);
        this.speed = (Setting<Integer>)this.register(new Setting("Speed", (T)10, (T)1, (T)20));
    }
    
    @Override
    public void onUpdate() {
        if (Feature.fullNullCheck() || ReverseStep.mc.player.isInWater() || ReverseStep.mc.player.isInLava() || ReverseStep.mc.player.isOnLadder()) {
            return;
        }
        if (ReverseStep.mc.player.onGround) {
            final EntityPlayerSP player = ReverseStep.mc.player;
            player.motionY -= this.speed.getValue() / 10.0f;
        }
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        ReverseStep.mc.player.motionY = 0.0;
    }
}
