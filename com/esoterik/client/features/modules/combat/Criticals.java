// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.combat;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketUseEntity;
import com.esoterik.client.event.events.PacketEvent;
import com.esoterik.client.util.Timer;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Criticals extends Module
{
    private Setting<Mode> mode;
    public Setting<Boolean> noDesync;
    private Setting<Integer> packets;
    private Setting<Integer> desyncDelay;
    public Setting<Boolean> cancelFirst;
    public Setting<Integer> delay32k;
    private Timer timer;
    private Timer timer32k;
    private boolean firstCanceled;
    private boolean resetTimer;
    
    public Criticals() {
        super("Criticals", "Scores criticals for you", Category.COMBAT, true, false, false);
        this.mode = (Setting<Mode>)this.register(new Setting("Mode", (T)Mode.PACKET));
        this.noDesync = (Setting<Boolean>)this.register(new Setting("NoDesync", (T)true));
        this.packets = (Setting<Integer>)this.register(new Setting("Packets", (T)2, (T)1, (T)4, v -> this.mode.getValue() == Mode.PACKET, "Amount of packets you want to send."));
        this.desyncDelay = (Setting<Integer>)this.register(new Setting("DesyncDelay", (T)10, (T)0, (T)500, v -> this.mode.getValue() == Mode.PACKET, "Amount of packets you want to send."));
        this.cancelFirst = (Setting<Boolean>)this.register(new Setting("CancelFirst32k", (T)true));
        this.delay32k = (Setting<Integer>)this.register(new Setting("32kDelay", (T)25, (T)0, (T)500, v -> this.cancelFirst.getValue()));
        this.timer = new Timer();
        this.timer32k = new Timer();
        this.firstCanceled = false;
        this.resetTimer = false;
    }
    
    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (!this.cancelFirst.getValue()) {
            this.firstCanceled = false;
        }
        if (event.getPacket() instanceof CPacketUseEntity) {
            final CPacketUseEntity packet = event.getPacket();
            if (packet.func_149565_c() == CPacketUseEntity.Action.ATTACK) {
                if (this.firstCanceled) {
                    this.timer32k.reset();
                    this.resetTimer = true;
                    this.timer.setMs(this.desyncDelay.getValue() + 1);
                    this.firstCanceled = false;
                    return;
                }
                if (this.resetTimer && !this.timer32k.passedMs(this.delay32k.getValue())) {
                    return;
                }
                if (this.resetTimer && this.timer32k.passedMs(this.delay32k.getValue())) {
                    this.resetTimer = false;
                }
                if (!this.timer.passedMs(this.desyncDelay.getValue())) {
                    return;
                }
                if (Criticals.mc.field_71439_g.field_70122_E && !Criticals.mc.field_71474_y.field_74314_A.func_151470_d() && (packet.func_149564_a((World)Criticals.mc.field_71441_e) instanceof EntityLivingBase || !this.noDesync.getValue()) && !Criticals.mc.field_71439_g.func_70090_H() && !Criticals.mc.field_71439_g.func_180799_ab()) {
                    if (this.mode.getValue() == Mode.PACKET) {
                        switch (this.packets.getValue()) {
                            case 1: {
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u + 0.10000000149011612, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u, Criticals.mc.field_71439_g.field_70161_v, false));
                                break;
                            }
                            case 2: {
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u + 0.0625101, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u + 1.1E-5, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u, Criticals.mc.field_71439_g.field_70161_v, false));
                                break;
                            }
                            case 3: {
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u + 0.0625101, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u + 0.0125, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u, Criticals.mc.field_71439_g.field_70161_v, false));
                                break;
                            }
                            case 4: {
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u + 0.1625, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u + 4.0E-6, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u + 1.0E-6, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Criticals.mc.field_71439_g.field_70165_t, Criticals.mc.field_71439_g.field_70163_u, Criticals.mc.field_71439_g.field_70161_v, false));
                                Criticals.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer());
                                Criticals.mc.field_71439_g.func_71009_b((Entity)Objects.requireNonNull(packet.func_149564_a((World)Criticals.mc.field_71441_e)));
                                break;
                            }
                        }
                    }
                    else {
                        Criticals.mc.field_71439_g.func_70664_aZ();
                    }
                    this.timer.reset();
                }
            }
        }
    }
    
    @Override
    public String getDisplayInfo() {
        return this.mode.currentEnumName();
    }
    
    public enum Mode
    {
        JUMP, 
        PACKET;
    }
}
