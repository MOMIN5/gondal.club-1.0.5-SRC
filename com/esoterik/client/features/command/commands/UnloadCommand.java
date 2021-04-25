// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.command.commands;

import com.esoterik.client.esohack;
import com.esoterik.client.features.command.Command;

public class UnloadCommand extends Command
{
    public UnloadCommand() {
        super("unload", new String[0]);
    }
    
    @Override
    public void execute(final String[] commands) {
        esohack.unload(true);
    }
}
