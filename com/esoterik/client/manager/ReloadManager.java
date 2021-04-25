// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.manager;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.esoterik.client.esohack;
import net.minecraft.network.play.client.CPacketChatMessage;
import com.esoterik.client.event.events.PacketEvent;
import com.esoterik.client.features.command.Command;
import net.minecraftforge.common.MinecraftForge;
import com.esoterik.client.features.Feature;

public class ReloadManager extends Feature
{
    public String prefix;
    
    public void init(final String prefix) {
        this.prefix = prefix;
        MinecraftForge.EVENT_BUS.register((Object)this);
        if (!Feature.fullNullCheck()) {
            Command.sendMessage("§cPhobos has been unloaded. Type " + prefix + "reload to reload.");
        }
    }
    
    public void unload() {
        MinecraftForge.EVENT_BUS.unregister((Object)this);
    }
    
    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketChatMessage) {
            final CPacketChatMessage packet = event.getPacket();
            if (packet.func_149439_c().startsWith(this.prefix) && packet.func_149439_c().contains("reload")) {
                esohack.load();
                event.setCanceled(true);
            }
        }
    }
}
