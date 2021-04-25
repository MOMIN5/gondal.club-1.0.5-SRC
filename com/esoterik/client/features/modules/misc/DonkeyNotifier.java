// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.misc;

import java.util.Iterator;
import com.esoterik.client.features.command.Command;
import net.minecraft.entity.passive.EntityDonkey;
import java.util.HashSet;
import net.minecraft.entity.Entity;
import java.util.Set;
import com.esoterik.client.features.modules.Module;

public class DonkeyNotifier extends Module
{
    private static DonkeyNotifier instance;
    private Set<Entity> entities;
    
    public DonkeyNotifier() {
        super("DonkeyNotifier", "Notifies you when a donkey is discovered", Category.MISC, true, false, false);
        this.entities = new HashSet<Entity>();
        DonkeyNotifier.instance = this;
    }
    
    @Override
    public void onEnable() {
        this.entities.clear();
    }
    
    @Override
    public void onUpdate() {
        for (final Entity entity : DonkeyNotifier.mc.field_71441_e.field_72996_f) {
            if (entity instanceof EntityDonkey) {
                if (this.entities.contains(entity)) {
                    continue;
                }
                Command.sendMessage("Donkey Detected at: " + entity.field_70165_t + "x, " + entity.field_70163_u + "y, " + entity.field_70161_v + "z.");
                this.entities.add(entity);
            }
        }
    }
}
