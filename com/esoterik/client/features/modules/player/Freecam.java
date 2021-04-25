// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.player;

import com.esoterik.client.event.events.PushEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import com.esoterik.client.event.events.PacketEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import com.esoterik.client.util.MathUtil;
import net.minecraft.world.World;
import com.esoterik.client.features.Feature;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Freecam extends Module
{
    public Setting<Double> speed;
    public Setting<Boolean> view;
    public Setting<Boolean> packet;
    public Setting<Boolean> disable;
    private static Freecam INSTANCE;
    private AxisAlignedBB oldBoundingBox;
    private EntityOtherPlayerMP entity;
    private Vec3d position;
    private Entity riding;
    private float yaw;
    private float pitch;
    
    public Freecam() {
        super("Freecam", "Look around freely.", Category.PLAYER, true, false, false);
        this.speed = (Setting<Double>)this.register(new Setting("Speed", (T)0.5, (T)0.1, (T)5.0));
        this.view = (Setting<Boolean>)this.register(new Setting("3D", (T)false));
        this.packet = (Setting<Boolean>)this.register(new Setting("Packet", (T)true));
        this.disable = (Setting<Boolean>)this.register(new Setting("Logout/Off", (T)true));
        this.setInstance();
    }
    
    private void setInstance() {
        Freecam.INSTANCE = this;
    }
    
    public static Freecam getInstance() {
        if (Freecam.INSTANCE == null) {
            Freecam.INSTANCE = new Freecam();
        }
        return Freecam.INSTANCE;
    }
    
    @Override
    public void onEnable() {
        if (!Feature.fullNullCheck()) {
            this.oldBoundingBox = Freecam.mc.field_71439_g.func_174813_aQ();
            Freecam.mc.field_71439_g.func_174826_a(new AxisAlignedBB(Freecam.mc.field_71439_g.field_70165_t, Freecam.mc.field_71439_g.field_70163_u, Freecam.mc.field_71439_g.field_70161_v, Freecam.mc.field_71439_g.field_70165_t, Freecam.mc.field_71439_g.field_70163_u, Freecam.mc.field_71439_g.field_70161_v));
            if (Freecam.mc.field_71439_g.func_184187_bx() != null) {
                this.riding = Freecam.mc.field_71439_g.func_184187_bx();
                Freecam.mc.field_71439_g.func_184210_p();
            }
            (this.entity = new EntityOtherPlayerMP((World)Freecam.mc.field_71441_e, Freecam.mc.field_71449_j.func_148256_e())).func_82149_j((Entity)Freecam.mc.field_71439_g);
            this.entity.field_70177_z = Freecam.mc.field_71439_g.field_70177_z;
            this.entity.field_70759_as = Freecam.mc.field_71439_g.field_70759_as;
            this.entity.field_71071_by.func_70455_b(Freecam.mc.field_71439_g.field_71071_by);
            Freecam.mc.field_71441_e.func_73027_a(69420, (Entity)this.entity);
            this.position = Freecam.mc.field_71439_g.func_174791_d();
            this.yaw = Freecam.mc.field_71439_g.field_70177_z;
            this.pitch = Freecam.mc.field_71439_g.field_70125_A;
            Freecam.mc.field_71439_g.field_70145_X = true;
        }
    }
    
    @Override
    public void onDisable() {
        if (!Feature.fullNullCheck()) {
            Freecam.mc.field_71439_g.func_174826_a(this.oldBoundingBox);
            if (this.riding != null) {
                Freecam.mc.field_71439_g.func_184205_a(this.riding, true);
            }
            if (this.entity != null) {
                Freecam.mc.field_71441_e.func_72900_e((Entity)this.entity);
            }
            if (this.position != null) {
                Freecam.mc.field_71439_g.func_70107_b(this.position.field_72450_a, this.position.field_72448_b, this.position.field_72449_c);
            }
            Freecam.mc.field_71439_g.field_70177_z = this.yaw;
            Freecam.mc.field_71439_g.field_70125_A = this.pitch;
            Freecam.mc.field_71439_g.field_70145_X = false;
        }
    }
    
    @Override
    public void onUpdate() {
        Freecam.mc.field_71439_g.field_70145_X = true;
        Freecam.mc.field_71439_g.func_70016_h(0.0, 0.0, 0.0);
        Freecam.mc.field_71439_g.field_70747_aH = this.speed.getValue().floatValue();
        final double[] dir = MathUtil.directionSpeed(this.speed.getValue());
        if (Freecam.mc.field_71439_g.field_71158_b.field_78902_a != 0.0f || Freecam.mc.field_71439_g.field_71158_b.field_192832_b != 0.0f) {
            Freecam.mc.field_71439_g.field_70159_w = dir[0];
            Freecam.mc.field_71439_g.field_70179_y = dir[1];
        }
        else {
            Freecam.mc.field_71439_g.field_70159_w = 0.0;
            Freecam.mc.field_71439_g.field_70179_y = 0.0;
        }
        Freecam.mc.field_71439_g.func_70031_b(false);
        if (this.view.getValue() && !Freecam.mc.field_71474_y.field_74311_E.func_151470_d() && !Freecam.mc.field_71474_y.field_74314_A.func_151470_d()) {
            Freecam.mc.field_71439_g.field_70181_x = this.speed.getValue() * -MathUtil.degToRad(Freecam.mc.field_71439_g.field_70125_A) * Freecam.mc.field_71439_g.field_71158_b.field_192832_b;
        }
        if (Freecam.mc.field_71474_y.field_74314_A.func_151470_d()) {
            final EntityPlayerSP field_71439_g = Freecam.mc.field_71439_g;
            field_71439_g.field_70181_x += this.speed.getValue();
        }
        if (Freecam.mc.field_71474_y.field_74311_E.func_151470_d()) {
            final EntityPlayerSP field_71439_g2 = Freecam.mc.field_71439_g;
            field_71439_g2.field_70181_x -= this.speed.getValue();
        }
    }
    
    @Override
    public void onLogout() {
        if (this.disable.getValue()) {
            this.disable();
        }
    }
    
    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (event.getStage() == 0 && (event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketInput)) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onPush(final PushEvent event) {
        if (event.getStage() == 1) {
            event.setCanceled(true);
        }
    }
    
    static {
        Freecam.INSTANCE = new Freecam();
    }
}
