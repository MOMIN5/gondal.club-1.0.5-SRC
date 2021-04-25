// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.event.events;

import net.minecraft.entity.Entity;
import com.esoterik.client.event.EventStage;

public class EntityAddedEvent extends EventStage
{
    private Entity entity;
    
    public EntityAddedEvent(final Entity entity) {
        this.entity = entity;
    }
    
    public Entity getEntity() {
        return this.entity;
    }
}
