// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.event.events;

import net.minecraft.entity.player.EntityPlayer;
import com.esoterik.client.event.EventStage;

public class DeathEvent extends EventStage
{
    public EntityPlayer player;
    
    public DeathEvent(final EntityPlayer player) {
        this.player = player;
    }
}
