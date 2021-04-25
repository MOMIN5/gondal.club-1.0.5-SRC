// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.event.events;

import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.Feature;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import com.esoterik.client.event.EventStage;

@Cancelable
public class ClientEvent extends EventStage
{
    private Feature feature;
    private Setting setting;
    
    public ClientEvent(final int stage, final Feature feature) {
        super(stage);
        this.feature = feature;
    }
    
    public ClientEvent(final Setting setting) {
        super(2);
        this.setting = setting;
    }
    
    public Feature getFeature() {
        return this.feature;
    }
    
    public Setting getSetting() {
        return this.setting;
    }
}
