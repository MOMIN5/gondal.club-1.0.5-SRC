// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.command.commands;

import java.util.Iterator;
import com.esoterik.client.features.Feature;
import com.esoterik.client.manager.ConfigManager;
import com.google.gson.JsonParser;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;
import com.esoterik.client.esohack;
import com.esoterik.client.features.command.Command;

public class ModuleCommand extends Command
{
    public ModuleCommand() {
        super("module", new String[] { "<module>", "<set/reset>", "<setting>", "<value>" });
    }
    
    @Override
    public void execute(final String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("Modules: ");
            for (final Module.Category category : esohack.moduleManager.getCategories()) {
                String modules = category.getName() + ": ";
                for (final Module module : esohack.moduleManager.getModulesByCategory(category)) {
                    modules = modules + (module.isEnabled() ? "§a" : "§c") + module.getName() + "§r" + ", ";
                }
                Command.sendMessage(modules);
            }
            return;
        }
        Module module2 = esohack.moduleManager.getModuleByDisplayName(commands[0]);
        if (module2 == null) {
            module2 = esohack.moduleManager.getModuleByName(commands[0]);
            if (module2 == null) {
                Command.sendMessage("§cThis module doesnt exist.");
                return;
            }
            Command.sendMessage("§c This is the original name of the module. Its current name is: " + module2.getDisplayName());
        }
        else {
            if (commands.length == 2) {
                Command.sendMessage(module2.getDisplayName() + " : " + module2.getDescription());
                for (final Setting setting : module2.getSettings()) {
                    Command.sendMessage(setting.getName() + " : " + setting.getValue() + ", " + setting.getDescription());
                }
                return;
            }
            if (commands.length == 3) {
                if (commands[1].equalsIgnoreCase("set")) {
                    Command.sendMessage("§cPlease specify a setting.");
                }
                else if (commands[1].equalsIgnoreCase("reset")) {
                    for (final Setting setting : module2.getSettings()) {
                        setting.setValue(setting.getDefaultValue());
                    }
                }
                else {
                    Command.sendMessage("§cThis command doesnt exist.");
                }
                return;
            }
            if (commands.length == 4) {
                Command.sendMessage("§cPlease specify a value.");
                return;
            }
            if (commands.length == 5) {
                final Setting setting2 = module2.getSettingByName(commands[2]);
                if (setting2 != null) {
                    final JsonParser jp = new JsonParser();
                    if (setting2.getType().equalsIgnoreCase("String")) {
                        setting2.setValue(commands[3]);
                        Command.sendMessage("§a" + module2.getName() + " " + setting2.getName() + " has been set to " + commands[3] + ".");
                        return;
                    }
                    try {
                        if (setting2.getName().equalsIgnoreCase("Enabled")) {
                            if (commands[3].equalsIgnoreCase("true")) {
                                module2.enable();
                            }
                            if (commands[3].equalsIgnoreCase("false")) {
                                module2.disable();
                            }
                        }
                        ConfigManager.setValueFromJson(module2, setting2, jp.parse(commands[3]));
                    }
                    catch (Exception e) {
                        Command.sendMessage("§cBad Value! This setting requires a: " + setting2.getType() + " value.");
                        return;
                    }
                    Command.sendMessage("§a" + module2.getName() + " " + setting2.getName() + " has been set to " + commands[3] + ".");
                }
            }
        }
    }
}
