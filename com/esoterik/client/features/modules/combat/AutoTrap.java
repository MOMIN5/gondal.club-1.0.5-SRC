// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.combat;

import net.minecraft.util.EnumHand;
import net.minecraft.block.BlockEnderChest;
import com.esoterik.client.util.MathUtil;
import com.esoterik.client.esohack;
import com.esoterik.client.features.command.Command;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.esoterik.client.util.InventoryUtil;
import net.minecraft.block.BlockObsidian;
import java.util.Iterator;
import com.esoterik.client.util.BlockUtil;
import java.util.Comparator;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import net.minecraft.entity.Entity;
import com.esoterik.client.util.EntityUtil;
import java.util.HashMap;
import net.minecraft.util.math.BlockPos;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import com.esoterik.client.util.Timer;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class AutoTrap extends Module
{
    private final Setting<Integer> delay;
    private final Setting<Integer> blocksPerPlace;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> raytrace;
    private final Setting<Boolean> antiScaffold;
    private final Setting<Boolean> antiStep;
    private final Timer timer;
    private boolean didPlace;
    private boolean switchedItem;
    public EntityPlayer target;
    private boolean isSneaking;
    private int lastHotbarSlot;
    private int placements;
    public static boolean isPlacing;
    private boolean smartRotate;
    private final Map<BlockPos, Integer> retries;
    private final Timer retryTimer;
    private BlockPos startPos;
    
    public AutoTrap() {
        super("AutoTrap", "Traps other players", Category.COMBAT, true, false, false);
        this.delay = (Setting<Integer>)this.register(new Setting("Delay", (T)50, (T)0, (T)250));
        this.blocksPerPlace = (Setting<Integer>)this.register(new Setting("BlocksPerTick", (T)8, (T)1, (T)30));
        this.rotate = (Setting<Boolean>)this.register(new Setting("Rotate", (T)true));
        this.raytrace = (Setting<Boolean>)this.register(new Setting("Raytrace", (T)false));
        this.antiScaffold = (Setting<Boolean>)this.register(new Setting("AntiScaffold", (T)false));
        this.antiStep = (Setting<Boolean>)this.register(new Setting("AntiStep", (T)false));
        this.timer = new Timer();
        this.didPlace = false;
        this.placements = 0;
        this.smartRotate = false;
        this.retries = new HashMap<BlockPos, Integer>();
        this.retryTimer = new Timer();
        this.startPos = null;
    }
    
    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        super.onEnable();
        this.startPos = EntityUtil.getRoundedBlockPos((Entity)AutoTrap.mc.field_71439_g);
        this.lastHotbarSlot = AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c;
        this.retries.clear();
    }
    
    @Override
    public void onTick() {
        if (fullNullCheck()) {
            return;
        }
        this.smartRotate = false;
        this.doTrap();
    }
    
    @Override
    public String getDisplayInfo() {
        if (this.target != null) {
            return this.target.func_70005_c_();
        }
        return null;
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        AutoTrap.isPlacing = false;
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
    }
    
    private void doTrap() {
        if (this.check()) {
            return;
        }
        this.doStaticTrap();
        if (this.didPlace) {
            this.timer.reset();
        }
    }
    
    private void doStaticTrap() {
        final List<Vec3d> placeTargets = EntityUtil.targets(this.target.func_174791_d(), this.antiScaffold.getValue(), this.antiStep.getValue(), false, false, false, this.raytrace.getValue(), true);
        this.placeList(placeTargets);
    }
    
    private void placeList(final List<Vec3d> list) {
        list.sort((vec3d, vec3d2) -> Double.compare(AutoTrap.mc.field_71439_g.func_70092_e(vec3d2.field_72450_a, vec3d2.field_72448_b, vec3d2.field_72449_c), AutoTrap.mc.field_71439_g.func_70092_e(vec3d.field_72450_a, vec3d.field_72448_b, vec3d.field_72449_c)));
        list.sort(Comparator.comparingDouble(vec3d -> vec3d.field_72448_b));
        for (final Vec3d vec3d3 : list) {
            final BlockPos position = new BlockPos(vec3d3);
            final int placeability = BlockUtil.isPositionPlaceable(position, this.raytrace.getValue());
            if (placeability == 1 && (this.retries.get(position) == null || this.retries.get(position) < 4)) {
                this.placeBlock(position);
                this.retries.put(position, (this.retries.get(position) == null) ? 1 : (this.retries.get(position) + 1));
                this.retryTimer.reset();
            }
            else {
                if (placeability != 3) {
                    continue;
                }
                this.placeBlock(position);
            }
        }
    }
    
    private boolean check() {
        AutoTrap.isPlacing = false;
        this.didPlace = false;
        this.placements = 0;
        final int obbySlot2 = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        if (obbySlot2 == -1) {
            this.toggle();
        }
        final int obbySlot3 = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        if (this.isOff()) {
            return true;
        }
        if (!this.startPos.equals((Object)EntityUtil.getRoundedBlockPos((Entity)AutoTrap.mc.field_71439_g))) {
            this.disable();
            return true;
        }
        if (this.retryTimer.passedMs(2000L)) {
            this.retries.clear();
            this.retryTimer.reset();
        }
        if (obbySlot3 == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + ChatFormatting.RED + "No Obsidian in hotbar disabling...");
            this.disable();
            return true;
        }
        if (AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c != this.lastHotbarSlot && AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c != obbySlot3) {
            this.lastHotbarSlot = AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c;
        }
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
        this.target = this.getTarget(10.0, true);
        return this.target == null || !this.timer.passedMs(this.delay.getValue());
    }
    
    private EntityPlayer getTarget(final double range, final boolean trapped) {
        EntityPlayer target = null;
        double distance = Math.pow(range, 2.0) + 1.0;
        for (final EntityPlayer player : AutoTrap.mc.field_71441_e.field_73010_i) {
            if (!EntityUtil.isntValid((Entity)player, range) && (!trapped || !EntityUtil.isTrapped(player, this.antiScaffold.getValue(), this.antiStep.getValue(), false, false, false, true))) {
                if (esohack.speedManager.getPlayerSpeed(player) > 10.0) {
                    continue;
                }
                if (target == null) {
                    target = player;
                    distance = AutoTrap.mc.field_71439_g.func_70068_e((Entity)player);
                }
                else {
                    if (AutoTrap.mc.field_71439_g.func_70068_e((Entity)player) >= distance) {
                        continue;
                    }
                    target = player;
                    distance = AutoTrap.mc.field_71439_g.func_70068_e((Entity)player);
                }
            }
        }
        return target;
    }
    
    private void placeBlock(final BlockPos pos) {
        if (this.placements < this.blocksPerPlace.getValue() && AutoTrap.mc.field_71439_g.func_174818_b(pos) <= MathUtil.square(5.0)) {
            AutoTrap.isPlacing = true;
            final int originalSlot = AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c;
            final int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            final int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle();
            }
            if (this.smartRotate) {
                AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c = ((obbySlot == -1) ? eChestSot : obbySlot);
                AutoTrap.mc.field_71442_b.func_78765_e();
                this.isSneaking = BlockUtil.placeBlockSmartRotate(pos, EnumHand.MAIN_HAND, true, true, this.isSneaking);
                AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c = originalSlot;
                AutoTrap.mc.field_71442_b.func_78765_e();
            }
            else {
                AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c = ((obbySlot == -1) ? eChestSot : obbySlot);
                AutoTrap.mc.field_71442_b.func_78765_e();
                this.isSneaking = BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, this.isSneaking);
                AutoTrap.mc.field_71439_g.field_71071_by.field_70461_c = originalSlot;
                AutoTrap.mc.field_71442_b.func_78765_e();
            }
            this.didPlace = true;
            ++this.placements;
        }
    }
    
    static {
        AutoTrap.isPlacing = false;
    }
}
