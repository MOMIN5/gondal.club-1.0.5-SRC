// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.event.events;

import net.minecraft.entity.player.EntityPlayer;
import com.esoterik.client.event.EventStage;

public class TotemPopEvent extends EventStage
{
    private EntityPlayer entity;
    
    public TotemPopEvent(final EntityPlayer entity) {
        this.entity = entity;
    }
    
    public EntityPlayer getEntity() {
        return this.entity;
    }
}
