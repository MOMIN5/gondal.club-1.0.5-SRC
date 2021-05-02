// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.movement;

import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Step extends Module
{
    public Setting<Integer> height;
    
    public Step() {
        super("Step", "Allows you to step up blocks", Category.MOVEMENT, true, false, false);
        this.height = (Setting<Integer>)this.register(new Setting("Height", (T)2, (T)0, (T)5));
    }
    
    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        Step.mc.player.stepHeight = 2.0f;
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        Step.mc.player.stepHeight = 0.6f;
    }
}
