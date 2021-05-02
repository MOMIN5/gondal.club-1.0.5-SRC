// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.ITextComponent;
import com.esoterik.client.esohack;
import com.esoterik.client.features.Feature;

public abstract class Command extends Feature
{
    protected String name;
    protected String[] commands;
    
    public Command(final String name) {
        super(name);
        this.name = name;
        this.commands = new String[] { "" };
    }
    
    public Command(final String name, final String[] commands) {
        super(name);
        this.name = name;
        this.commands = commands;
    }
    
    public abstract void execute(final String[] p0);
    
    public static void sendMessage(final String message, final boolean notification) {
        sendSilentMessage(esohack.commandManager.getClientMessage() + " " + "§r" + message);
        if (notification) {
            esohack.notificationManager.addNotification(message, 3000L);
        }
    }
    
    public static void sendMessage(final String message) {
        sendSilentMessage(esohack.commandManager.getClientMessage() + " " + "§r" + message);
    }
    
    public static void sendSilentMessage(final String message) {
        if (nullCheck()) {
            return;
        }
        Command.mc.player.sendMessage((ITextComponent)new ChatMessage(message));
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public String[] getCommands() {
        return this.commands;
    }
    
    public static String getCommandPrefix() {
        return esohack.commandManager.getPrefix();
    }
    
    public static class ChatMessage extends TextComponentBase
    {
        private final String text;
        
        public ChatMessage(final String text) {
            final Pattern pattern = Pattern.compile("&[0123456789abcdefrlosmk]");
            final Matcher matcher = pattern.matcher(text);
            final StringBuffer stringBuffer = new StringBuffer();
            while (matcher.find()) {
                final String replacement = "§" + matcher.group().substring(1);
                matcher.appendReplacement(stringBuffer, replacement);
            }
            matcher.appendTail(stringBuffer);
            this.text = stringBuffer.toString();
        }
        
        public String getUnformattedComponentText() {
            return this.text;
        }
        
        public ITextComponent createCopy() {
            return (ITextComponent)new ChatMessage(this.text);
        }
    }
}
