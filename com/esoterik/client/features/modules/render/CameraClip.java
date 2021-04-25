// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.render;

import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class CameraClip extends Module
{
    public Setting<Boolean> extend;
    public Setting<Double> distance;
    private static CameraClip INSTANCE;
    
    public CameraClip() {
        super("CameraClip", "Makes your Camera clip.", Category.RENDER, false, false, false);
        this.extend = (Setting<Boolean>)this.register(new Setting("Extend", (T)false));
        this.distance = (Setting<Double>)this.register(new Setting("Distance", (T)10.0, (T)0.0, (T)50.0, v -> this.extend.getValue(), "By how much you want to extend the distance."));
        this.setInstance();
    }
    
    private void setInstance() {
        CameraClip.INSTANCE = this;
    }
    
    public static CameraClip getInstance() {
        if (CameraClip.INSTANCE == null) {
            CameraClip.INSTANCE = new CameraClip();
        }
        return CameraClip.INSTANCE;
    }
    
    static {
        CameraClip.INSTANCE = new CameraClip();
    }
}
