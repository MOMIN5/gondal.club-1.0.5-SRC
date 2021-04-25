// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.combat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import net.minecraft.util.NonNullList;
import com.esoterik.client.util.EntityUtil;
import com.esoterik.client.util.RenderUtil;
import com.esoterik.client.features.modules.client.Colors;
import com.esoterik.client.event.events.Render3DEvent;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.EnumHand;
import com.esoterik.client.util.BlockUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import com.esoterik.client.esohack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import com.esoterik.client.event.events.PacketEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import com.esoterik.client.features.setting.Setting;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.features.modules.Module;

public class HoleFiller extends Module
{
    private static BlockPos PlayerPos;
    private Setting<Double> range;
    private Setting<Boolean> smart;
    private Setting<Integer> smartRange;
    private Setting<Boolean> announceUsage;
    private BlockPos render;
    private Entity renderEnt;
    private EntityPlayer closestTarget;
    private long systemTime;
    private static boolean togglePitch;
    private boolean switchCooldown;
    private boolean isAttacking;
    private boolean caOn;
    private int newSlot;
    double d;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    private static HoleFiller INSTANCE;
    
    public HoleFiller() {
        super("HoleFiller", "Fills holes around you.", Category.COMBAT, true, false, true);
        this.range = (Setting<Double>)this.register(new Setting("Range", (T)4.5, (T)0.1, (T)6));
        this.smart = (Setting<Boolean>)this.register(new Setting("Smart", (T)false));
        this.smartRange = (Setting<Integer>)this.register(new Setting("Smart Range", (T)4));
        this.announceUsage = (Setting<Boolean>)this.register(new Setting("Announce Usage", (T)false));
        this.systemTime = -1L;
        this.switchCooldown = false;
        this.isAttacking = false;
        this.setInstance();
    }
    
    public static HoleFiller getInstance() {
        if (HoleFiller.INSTANCE == null) {
            HoleFiller.INSTANCE = new HoleFiller();
        }
        return HoleFiller.INSTANCE;
    }
    
    private void setInstance() {
        HoleFiller.INSTANCE = this;
    }
    
    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        final Packet packet = event.getPacket();
        if (packet instanceof CPacketPlayer && HoleFiller.isSpoofingAngles) {
            ((CPacketPlayer)packet).field_149476_e = (float)HoleFiller.yaw;
            ((CPacketPlayer)packet).field_149473_f = (float)HoleFiller.pitch;
        }
    }
    
    @Override
    public void onEnable() {
        if (esohack.moduleManager.isModuleEnabled("AutoGondal")) {
            this.caOn = true;
        }
        super.onEnable();
    }
    
    @Override
    public void onUpdate() {
        if (HoleFiller.mc.field_71441_e == null) {
            return;
        }
        if (this.smart.getValue()) {
            this.findClosestTarget();
        }
        final List<BlockPos> blocks = this.findCrystalBlocks();
        BlockPos q = null;
        final double dist = 0.0;
        final double prevDist = 0.0;
        final int n;
        int obsidianSlot = n = ((HoleFiller.mc.field_71439_g.func_184614_ca().func_77973_b() == Item.func_150898_a(Blocks.field_150343_Z)) ? HoleFiller.mc.field_71439_g.field_71071_by.field_70461_c : -1);
        if (obsidianSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (HoleFiller.mc.field_71439_g.field_71071_by.func_70301_a(l).func_77973_b() == Item.func_150898_a(Blocks.field_150343_Z)) {
                    obsidianSlot = l;
                    break;
                }
            }
        }
        if (obsidianSlot == -1) {
            return;
        }
        for (final BlockPos blockPos : blocks) {
            if (!HoleFiller.mc.field_71441_e.func_72872_a((Class)Entity.class, new AxisAlignedBB(blockPos)).isEmpty()) {
                continue;
            }
            if (this.smart.getValue() && this.isInRange(blockPos)) {
                q = blockPos;
            }
            else {
                q = blockPos;
            }
        }
        this.render = q;
        if (q != null && HoleFiller.mc.field_71439_g.field_70122_E) {
            final int oldSlot = HoleFiller.mc.field_71439_g.field_71071_by.field_70461_c;
            if (HoleFiller.mc.field_71439_g.field_71071_by.field_70461_c != obsidianSlot) {
                HoleFiller.mc.field_71439_g.field_71071_by.field_70461_c = obsidianSlot;
            }
            this.lookAtPacket(q.func_177958_n() + 0.5, q.func_177956_o() - 0.5, q.func_177952_p() + 0.5, (EntityPlayer)HoleFiller.mc.field_71439_g);
            BlockUtil.placeBlockScaffold(this.render);
            HoleFiller.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
            HoleFiller.mc.field_71439_g.field_71071_by.field_70461_c = oldSlot;
            resetRotation();
        }
    }
    
    @Override
    public void onRender3D(final Render3DEvent event) {
        if (this.render != null) {
            RenderUtil.drawBoxESP(this.render, Colors.INSTANCE.getCurrentColor(), false, Colors.INSTANCE.getCurrentColor(), 2.0f, true, true, 150, true, -0.9, false, false, false, false, 255);
        }
    }
    
    private double getDistanceToBlockPos(final BlockPos pos1, final BlockPos pos2) {
        final double x = pos1.func_177958_n() - pos2.func_177958_n();
        final double y = pos1.func_177956_o() - pos2.func_177956_o();
        final double z = pos1.func_177952_p() - pos2.func_177952_p();
        return Math.sqrt(x * x + y * y + z * z);
    }
    
    private void lookAtPacket(final double px, final double py, final double pz, final EntityPlayer me) {
        final double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float)v[0], (float)v[1]);
    }
    
    private boolean IsHole(final BlockPos blockPos) {
        final BlockPos boost = blockPos.func_177982_a(0, 1, 0);
        final BlockPos boost2 = blockPos.func_177982_a(0, 0, 0);
        final BlockPos boost3 = blockPos.func_177982_a(0, 0, -1);
        final BlockPos boost4 = blockPos.func_177982_a(1, 0, 0);
        final BlockPos boost5 = blockPos.func_177982_a(-1, 0, 0);
        final BlockPos boost6 = blockPos.func_177982_a(0, 0, 1);
        final BlockPos boost7 = blockPos.func_177982_a(0, 2, 0);
        final BlockPos boost8 = blockPos.func_177963_a(0.5, 0.5, 0.5);
        final BlockPos boost9 = blockPos.func_177982_a(0, -1, 0);
        return HoleFiller.mc.field_71441_e.func_180495_p(boost).func_177230_c() == Blocks.field_150350_a && HoleFiller.mc.field_71441_e.func_180495_p(boost2).func_177230_c() == Blocks.field_150350_a && HoleFiller.mc.field_71441_e.func_180495_p(boost7).func_177230_c() == Blocks.field_150350_a && (HoleFiller.mc.field_71441_e.func_180495_p(boost3).func_177230_c() == Blocks.field_150343_Z || HoleFiller.mc.field_71441_e.func_180495_p(boost3).func_177230_c() == Blocks.field_150357_h) && (HoleFiller.mc.field_71441_e.func_180495_p(boost4).func_177230_c() == Blocks.field_150343_Z || HoleFiller.mc.field_71441_e.func_180495_p(boost4).func_177230_c() == Blocks.field_150357_h) && (HoleFiller.mc.field_71441_e.func_180495_p(boost5).func_177230_c() == Blocks.field_150343_Z || HoleFiller.mc.field_71441_e.func_180495_p(boost5).func_177230_c() == Blocks.field_150357_h) && (HoleFiller.mc.field_71441_e.func_180495_p(boost6).func_177230_c() == Blocks.field_150343_Z || HoleFiller.mc.field_71441_e.func_180495_p(boost6).func_177230_c() == Blocks.field_150357_h) && HoleFiller.mc.field_71441_e.func_180495_p(boost8).func_177230_c() == Blocks.field_150350_a && (HoleFiller.mc.field_71441_e.func_180495_p(boost9).func_177230_c() == Blocks.field_150343_Z || HoleFiller.mc.field_71441_e.func_180495_p(boost9).func_177230_c() == Blocks.field_150357_h);
    }
    
    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(HoleFiller.mc.field_71439_g.field_70165_t), Math.floor(HoleFiller.mc.field_71439_g.field_70163_u), Math.floor(HoleFiller.mc.field_71439_g.field_70161_v));
    }
    
    public BlockPos getClosestTargetPos() {
        if (this.closestTarget != null) {
            return new BlockPos(Math.floor(this.closestTarget.field_70165_t), Math.floor(this.closestTarget.field_70163_u), Math.floor(this.closestTarget.field_70161_v));
        }
        return null;
    }
    
    private void findClosestTarget() {
        final List<EntityPlayer> playerList = (List<EntityPlayer>)HoleFiller.mc.field_71441_e.field_73010_i;
        this.closestTarget = null;
        for (final EntityPlayer target : playerList) {
            if (target != HoleFiller.mc.field_71439_g && !esohack.friendManager.isFriend(target.func_70005_c_()) && EntityUtil.isLiving((Entity)target)) {
                if (target.func_110143_aJ() <= 0.0f) {
                    continue;
                }
                if (this.closestTarget == null) {
                    this.closestTarget = target;
                }
                else {
                    if (HoleFiller.mc.field_71439_g.func_70032_d((Entity)target) >= HoleFiller.mc.field_71439_g.func_70032_d((Entity)this.closestTarget)) {
                        continue;
                    }
                    this.closestTarget = target;
                }
            }
        }
    }
    
    private boolean isInRange(final BlockPos blockPos) {
        final NonNullList positions = NonNullList.func_191196_a();
        positions.addAll((Collection)this.getSphere(getPlayerPos(), this.range.getValue().floatValue(), this.range.getValue().intValue(), false, true, 0).stream().filter((Predicate<? super Object>)this::IsHole).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
        return positions.contains((Object)blockPos);
    }
    
    private List<BlockPos> findCrystalBlocks() {
        final NonNullList positions = NonNullList.func_191196_a();
        if (this.smart.getValue() && this.closestTarget != null) {
            positions.addAll((Collection)this.getSphere(this.getClosestTargetPos(), this.smartRange.getValue(), this.range.getValue().intValue(), false, true, 0).stream().filter((Predicate<? super Object>)this::IsHole).filter((Predicate<? super Object>)this::isInRange).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
        }
        else if (!this.smart.getValue()) {
            positions.addAll((Collection)this.getSphere(getPlayerPos(), this.range.getValue().floatValue(), this.range.getValue().intValue(), false, true, 0).stream().filter((Predicate<? super Object>)this::IsHole).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
        }
        return (List<BlockPos>)positions;
    }
    
    public List<BlockPos> getSphere(final BlockPos loc, final float r, final int h, final boolean hollow, final boolean sphere, final int plus_y) {
        final ArrayList<BlockPos> circleblocks = new ArrayList<BlockPos>();
        final int cx = loc.func_177958_n();
        final int cy = loc.func_177956_o();
        final int cz = loc.func_177952_p();
        for (int x = cx - (int)r; x <= cx + r; ++x) {
            for (int z = cz - (int)r; z <= cz + r; ++z) {
                int y = sphere ? (cy - (int)r) : cy;
                while (true) {
                    final float f = (float)y;
                    final float f2 = sphere ? (cy + r) : ((float)(cy + h));
                    if (f >= f2) {
                        break;
                    }
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                    ++y;
                }
            }
        }
        return circleblocks;
    }
    
    private static void setYawAndPitch(final float yaw1, final float pitch1) {
        HoleFiller.yaw = yaw1;
        HoleFiller.pitch = pitch1;
        HoleFiller.isSpoofingAngles = true;
    }
    
    private static void resetRotation() {
        if (HoleFiller.isSpoofingAngles) {
            HoleFiller.yaw = HoleFiller.mc.field_71439_g.field_70177_z;
            HoleFiller.pitch = HoleFiller.mc.field_71439_g.field_70125_A;
            HoleFiller.isSpoofingAngles = false;
        }
    }
    
    @Override
    public void onDisable() {
        this.closestTarget = null;
        this.render = null;
        resetRotation();
        super.onDisable();
    }
    
    static {
        HoleFiller.INSTANCE = new HoleFiller();
        HoleFiller.togglePitch = false;
    }
}
