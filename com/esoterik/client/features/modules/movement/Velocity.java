// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.movement;

import com.esoterik.client.event.events.PushEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.world.World;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import com.esoterik.client.event.events.PacketEvent;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Velocity extends Module
{
    public Setting<Boolean> noPush;
    public Setting<Float> horizontal;
    public Setting<Float> vertical;
    public Setting<Boolean> explosions;
    public Setting<Boolean> bobbers;
    public Setting<Boolean> water;
    public Setting<Boolean> blocks;
    public Setting<Boolean> ice;
    private static Velocity INSTANCE;
    
    public Velocity() {
        super("Velocity", "Allows you to control your velocity", Category.MOVEMENT, true, false, false);
        this.noPush = (Setting<Boolean>)this.register(new Setting("NoPush", (T)true));
        this.horizontal = (Setting<Float>)this.register(new Setting("Horizontal", (T)0.0f, (T)0.0f, (T)100.0f));
        this.vertical = (Setting<Float>)this.register(new Setting("Vertical", (T)0.0f, (T)0.0f, (T)100.0f));
        this.explosions = (Setting<Boolean>)this.register(new Setting("Explosions", (T)true));
        this.bobbers = (Setting<Boolean>)this.register(new Setting("Bobbers", (T)true));
        this.water = (Setting<Boolean>)this.register(new Setting("Water", (T)false));
        this.blocks = (Setting<Boolean>)this.register(new Setting("Blocks", (T)false));
        this.ice = (Setting<Boolean>)this.register(new Setting("Ice", (T)false));
        this.setInstance();
    }
    
    private void setInstance() {
        Velocity.INSTANCE = this;
    }
    
    public static Velocity getINSTANCE() {
        if (Velocity.INSTANCE == null) {
            Velocity.INSTANCE = new Velocity();
        }
        return Velocity.INSTANCE;
    }
    
    @Override
    public void onUpdate() {
    }
    
    @Override
    public void onDisable() {
    }
    
    @SubscribeEvent
    public void onPacketReceived(final PacketEvent.Receive event) {
        if (event.getStage() == 0 && Velocity.mc.field_71439_g != null) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                final SPacketEntityVelocity velocity = event.getPacket();
                if (velocity.func_149412_c() == Velocity.mc.field_71439_g.field_145783_c) {
                    if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                        event.setCanceled(true);
                        return;
                    }
                    final SPacketEntityVelocity sPacketEntityVelocity = velocity;
                    sPacketEntityVelocity.field_149415_b *= (int)(Object)this.horizontal.getValue();
                    final SPacketEntityVelocity sPacketEntityVelocity2 = velocity;
                    sPacketEntityVelocity2.field_149416_c *= (int)(Object)this.vertical.getValue();
                    final SPacketEntityVelocity sPacketEntityVelocity3 = velocity;
                    sPacketEntityVelocity3.field_149414_d *= (int)(Object)this.horizontal.getValue();
                }
            }
            if (event.getPacket() instanceof SPacketEntityStatus && this.bobbers.getValue()) {
                final SPacketEntityStatus packet = event.getPacket();
                if (packet.func_149160_c() == 31) {
                    final Entity entity = packet.func_149161_a((World)Velocity.mc.field_71441_e);
                    if (entity instanceof EntityFishHook) {
                        final EntityFishHook fishHook = (EntityFishHook)entity;
                        if (fishHook.field_146043_c == Velocity.mc.field_71439_g) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
            if (this.explosions.getValue() && event.getPacket() instanceof SPacketExplosion) {
                if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                    event.setCanceled(true);
                    return;
                }
                final SPacketExplosion sPacketExplosion;
                final SPacketExplosion velocity2 = sPacketExplosion = event.getPacket();
                sPacketExplosion.field_149152_f *= this.horizontal.getValue();
                final SPacketExplosion sPacketExplosion2 = velocity2;
                sPacketExplosion2.field_149153_g *= this.vertical.getValue();
                final SPacketExplosion sPacketExplosion3 = velocity2;
                sPacketExplosion3.field_149159_h *= this.horizontal.getValue();
            }
        }
    }
    
    @SubscribeEvent
    public void onPush(final PushEvent event) {
        if (event.getStage() == 0 && this.noPush.getValue() && event.entity.equals((Object)Velocity.mc.field_71439_g)) {
            if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                event.setCanceled(true);
                return;
            }
            event.x = -event.x * this.horizontal.getValue();
            event.y = -event.y * this.vertical.getValue();
            event.z = -event.z * this.horizontal.getValue();
        }
        else if (event.getStage() == 1 && this.blocks.getValue()) {
            event.setCanceled(true);
        }
        else if (event.getStage() == 2 && this.water.getValue() && Velocity.mc.field_71439_g != null && Velocity.mc.field_71439_g.equals((Object)event.entity)) {
            event.setCanceled(true);
        }
    }
    
    static {
        Velocity.INSTANCE = new Velocity();
    }
}
