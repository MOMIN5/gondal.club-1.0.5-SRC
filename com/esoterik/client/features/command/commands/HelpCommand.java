// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.command.commands;

import java.util.Iterator;
import com.esoterik.client.esohack;
import com.esoterik.client.features.command.Command;

public class HelpCommand extends Command
{
    public HelpCommand() {
        super("commands");
    }
    
    @Override
    public void execute(final String[] commands) {
        Command.sendMessage("You can use following commands: ");
        for (final Command command : esohack.commandManager.getCommands()) {
            Command.sendMessage(esohack.commandManager.getPrefix() + command.getName());
        }
    }
}
