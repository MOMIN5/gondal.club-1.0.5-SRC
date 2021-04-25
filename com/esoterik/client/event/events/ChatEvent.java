// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.event.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import com.esoterik.client.event.EventStage;

@Cancelable
public class ChatEvent extends EventStage
{
    private final String msg;
    
    public ChatEvent(final String msg) {
        this.msg = msg;
    }
    
    public String getMsg() {
        return this.msg;
    }
}
