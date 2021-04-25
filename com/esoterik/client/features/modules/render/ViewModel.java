// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.render;

import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class ViewModel extends Module
{
    public Setting<Float> sizeX;
    public Setting<Float> sizeY;
    public Setting<Float> sizeZ;
    public Setting<Float> rotationX;
    public Setting<Float> rotationY;
    public Setting<Float> rotationZ;
    public Setting<Float> positionX;
    public Setting<Float> positionY;
    public Setting<Float> positionZ;
    public Setting<Float> itemFOV;
    private static ViewModel INSTANCE;
    
    public ViewModel() {
        super("Viewmodel", "Changes to the viewmodel.", Category.RENDER, false, false, false);
        this.sizeX = (Setting<Float>)this.register(new Setting("SizeX", (T)1.0f, (T)0.0f, (T)2.0f));
        this.sizeY = (Setting<Float>)this.register(new Setting("SizeY", (T)1.0f, (T)0.0f, (T)2.0f));
        this.sizeZ = (Setting<Float>)this.register(new Setting("SizeZ", (T)1.0f, (T)0.0f, (T)2.0f));
        this.rotationX = (Setting<Float>)this.register(new Setting("rotationX", (T)0.0f, (T)0.0f, (T)1.0f));
        this.rotationY = (Setting<Float>)this.register(new Setting("rotationY", (T)0.0f, (T)0.0f, (T)1.0f));
        this.rotationZ = (Setting<Float>)this.register(new Setting("rotationZ", (T)0.0f, (T)0.0f, (T)1.0f));
        this.positionX = (Setting<Float>)this.register(new Setting("positionX", (T)0.0f, (T)(-2.0f), (T)2.0f));
        this.positionY = (Setting<Float>)this.register(new Setting("positionY", (T)0.0f, (T)(-2.0f), (T)2.0f));
        this.positionZ = (Setting<Float>)this.register(new Setting("positionZ", (T)0.0f, (T)(-2.0f), (T)2.0f));
        this.itemFOV = (Setting<Float>)this.register(new Setting("ItemFOV", (T)1.0f, (T)0.0f, (T)2.0f));
        this.setInstance();
    }
    
    private void setInstance() {
        ViewModel.INSTANCE = this;
    }
    
    public static ViewModel getINSTANCE() {
        if (ViewModel.INSTANCE == null) {
            ViewModel.INSTANCE = new ViewModel();
        }
        return ViewModel.INSTANCE;
    }
    
    static {
        ViewModel.INSTANCE = new ViewModel();
    }
}
