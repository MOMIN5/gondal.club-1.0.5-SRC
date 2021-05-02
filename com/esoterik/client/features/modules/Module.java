// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules;

import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import com.esoterik.client.esohack;
import net.minecraftforge.fml.common.eventhandler.Event;
import com.esoterik.client.event.events.ClientEvent;
import net.minecraftforge.common.MinecraftForge;
import com.esoterik.client.event.events.Render3DEvent;
import com.esoterik.client.event.events.Render2DEvent;
import com.esoterik.client.features.command.Command;
import com.esoterik.client.features.setting.Bind;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.Feature;

public class Module extends Feature
{
    private final String description;
    private final Category category;
    public Setting<Boolean> enabled;
    public Setting<Boolean> drawn;
    public Setting<Bind> bind;
    public Setting<String> displayName;
    public boolean hasListener;
    public boolean alwaysListening;
    public boolean hidden;
    
    public Module(final String name, final String description, final Category category, final boolean hasListener, final boolean hidden, final boolean alwaysListening) {
        super(name);
        this.enabled = (Setting<Boolean>)this.register(new Setting("Enabled", (T)false));
        this.drawn = (Setting<Boolean>)this.register(new Setting("Drawn", (T)true));
        this.bind = (Setting<Bind>)this.register(new Setting("Bind", (T)new Bind(-1)));
        this.displayName = (Setting<String>)this.register(new Setting("DisplayName", (T)name));
        this.description = description;
        this.category = category;
        this.hasListener = hasListener;
        this.hidden = hidden;
        this.alwaysListening = alwaysListening;
    }
    
    public void onEnable() {
        Command.sendMessage("§a" + this.getDisplayName() + " enabled.");
    }
    
    public void onDisable() {
        Command.sendMessage("§c" + this.getDisplayName() + " disabled.");
    }
    
    public void onToggle() {
    }
    
    public void onLoad() {
    }
    
    public void onTick() {
    }
    
    public void onLogin() {
    }
    
    public void onLogout() {
    }
    
    public void onUpdate() {
    }
    
    public void onRender2D(final Render2DEvent event) {
    }
    
    public void onRender3D(final Render3DEvent event) {
    }
    
    public void onUnload() {
    }
    
    public void onServerUpdate() {
    }
    
    public String getDisplayInfo() {
        return null;
    }
    
    public boolean isOn() {
        return this.enabled.getValue();
    }
    
    public boolean isOff() {
        return !this.enabled.getValue();
    }
    
    public void setEnabled(final boolean enabled) {
        if (enabled) {
            this.enable();
        }
        else {
            this.disable();
        }
    }
    
    public void enable() {
        this.enabled.setValue(true);
        this.onToggle();
        this.onEnable();
        if (this.isOn() && this.hasListener && !this.alwaysListening) {
            MinecraftForge.EVENT_BUS.register((Object)this);
        }
    }
    
    public void disable() {
        if (this.hasListener && !this.alwaysListening) {
            MinecraftForge.EVENT_BUS.unregister((Object)this);
        }
        this.enabled.setValue(false);
        this.onToggle();
        this.onDisable();
    }
    
    public void toggle() {
        final ClientEvent event = new ClientEvent(this.isEnabled() ? 0 : 1, this);
        MinecraftForge.EVENT_BUS.post((Event)event);
        if (!event.isCanceled()) {
            this.setEnabled(!this.isEnabled());
        }
    }
    
    public String getDisplayName() {
        return this.displayName.getValue();
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDisplayName(final String name) {
        final Module module = esohack.moduleManager.getModuleByDisplayName(name);
        final Module originalModule = esohack.moduleManager.getModuleByName(name);
        if (module == null && originalModule == null) {
            Command.sendMessage(this.getDisplayName() + ", Original name: " + this.getName() + ", has been renamed to: " + name);
            this.displayName.setValue(name);
            return;
        }
        Command.sendMessage("§cA module of this name already exists.");
    }
    
    public boolean isDrawn() {
        return this.drawn.getValue();
    }
    
    public void setDrawn(final boolean drawn) {
        this.drawn.setValue(drawn);
    }
    
    public Category getCategory() {
        return this.category;
    }
    
    public String getInfo() {
        return null;
    }
    
    public Bind getBind() {
        return this.bind.getValue();
    }
    
    public void setBind(final int key) {
        this.bind.setValue(new Bind(key));
    }
    
    public boolean listening() {
        return (this.hasListener && this.isOn()) || this.alwaysListening;
    }
    
    public Vec3d process(final Entity entity, final double x, final double y, final double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z);
    }
    
    public String getFullArrayString() {
        return this.getDisplayName() + "§8" + ((this.getDisplayInfo() != null) ? (" [§r" + this.getDisplayInfo() + "§8" + "]") : "");
    }
    
    public enum Category
    {
        COMBAT("Combat"), 
        MISC("Misc"), 
        RENDER("Render"), 
        MOVEMENT("Movement"), 
        PLAYER("Player"), 
        CLIENT("Client");
        
        private final String name;
        
        private Category(final String name) {
            this.name = name;
        }
        
        public String getName() {
            return this.name;
        }
    }
}
