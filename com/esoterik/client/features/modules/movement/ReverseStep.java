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
        if (Feature.fullNullCheck() || ReverseStep.mc.field_71439_g.func_70090_H() || ReverseStep.mc.field_71439_g.func_180799_ab() || ReverseStep.mc.field_71439_g.func_70617_f_()) {
            return;
        }
        if (ReverseStep.mc.field_71439_g.field_70122_E) {
            final EntityPlayerSP field_71439_g = ReverseStep.mc.field_71439_g;
            field_71439_g.field_70181_x -= this.speed.getValue() / 10.0f;
        }
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        ReverseStep.mc.field_71439_g.field_70181_x = 0.0;
    }
}
