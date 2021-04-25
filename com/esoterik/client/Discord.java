// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client;

import club.minnced.discord.rpc.DiscordEventHandlers;
import com.esoterik.client.features.modules.misc.RPC;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class Discord
{
    public static DiscordRichPresence presence;
    private static final DiscordRPC rpc;
    private static RPC discordrpc;
    private static Thread thread;
    
    public static void start() {
        final DiscordEventHandlers handlers = new DiscordEventHandlers();
        Discord.rpc.Discord_Initialize("823184074369663016", handlers, true, "");
        Discord.presence.startTimestamp = System.currentTimeMillis() / 1000L;
        Discord.presence.details = esohack.getName() + " v" + "1.0.5";
        Discord.presence.state = "balling";
        Discord.presence.largeImageKey = "download";
        Discord.presence.largeImageText = "https://discord.gg/wJq5nMEdNT";
        Discord.rpc.Discord_UpdatePresence(Discord.presence);
        (Discord.thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Discord.rpc.Discord_RunCallbacks();
                Discord.presence.details = esohack.getName() + " v" + "1.0.5";
                Discord.presence.state = "balling";
                Discord.rpc.Discord_UpdatePresence(Discord.presence);
                try {
                    Thread.sleep(2000L);
                }
                catch (InterruptedException ex) {}
            }
        }, "RPC-Callback-Handler")).start();
    }
    
    public static void stop() {
        if (Discord.thread != null && !Discord.thread.isInterrupted()) {
            Discord.thread.interrupt();
        }
        Discord.rpc.Discord_Shutdown();
    }
    
    static {
        rpc = DiscordRPC.INSTANCE;
        Discord.presence = new DiscordRichPresence();
    }
}
