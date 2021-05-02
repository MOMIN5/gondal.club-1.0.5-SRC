// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.manager;

import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3i;
import net.minecraft.init.Blocks;
import com.esoterik.client.util.BlockUtil;
import net.minecraft.entity.player.EntityPlayer;
import com.esoterik.client.util.EntityUtil;
import java.util.Comparator;
import java.util.Iterator;
import com.esoterik.client.esohack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import com.esoterik.client.features.modules.combat.HoleFiller;
import com.esoterik.client.features.modules.render.HoleESP;
import com.esoterik.client.features.modules.client.Managers;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import com.esoterik.client.util.Timer;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.features.Feature;

public class HoleManager extends Feature implements Runnable
{
    private static final BlockPos[] surroundOffset;
    private final List<BlockPos> midSafety;
    private final Timer syncTimer;
    private final AtomicBoolean shouldInterrupt;
    private final Timer holeTimer;
    private List<BlockPos> holes;
    private ScheduledExecutorService executorService;
    private int lastUpdates;
    private Thread thread;
    
    public HoleManager() {
        this.midSafety = new ArrayList<BlockPos>();
        this.syncTimer = new Timer();
        this.shouldInterrupt = new AtomicBoolean(false);
        this.holeTimer = new Timer();
        this.holes = new ArrayList<BlockPos>();
        this.lastUpdates = 0;
    }
    
    public void update() {
        if (Managers.getInstance().holeThread.getValue() == Managers.ThreadMode.WHILE) {
            if (this.thread == null || this.thread.isInterrupted() || !this.thread.isAlive() || this.syncTimer.passedMs(Managers.getInstance().holeSync.getValue())) {
                if (this.thread == null) {
                    this.thread = new Thread(this);
                }
                else if (this.syncTimer.passedMs(Managers.getInstance().holeSync.getValue()) && !this.shouldInterrupt.get()) {
                    this.shouldInterrupt.set(true);
                    this.syncTimer.reset();
                    return;
                }
                if (this.thread != null && (this.thread.isInterrupted() || !this.thread.isAlive())) {
                    this.thread = new Thread(this);
                }
                if (this.thread != null && this.thread.getState() == Thread.State.NEW) {
                    try {
                        this.thread.start();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.syncTimer.reset();
                }
            }
        }
        else if (Managers.getInstance().holeThread.getValue() == Managers.ThreadMode.WHILE) {
            if (this.executorService == null || this.executorService.isTerminated() || this.executorService.isShutdown() || this.syncTimer.passedMs(10000L) || this.lastUpdates != Managers.getInstance().holeUpdates.getValue()) {
                this.lastUpdates = Managers.getInstance().holeUpdates.getValue();
                if (this.executorService != null) {
                    this.executorService.shutdown();
                }
                this.executorService = this.getExecutor();
            }
        }
        else if (this.holeTimer.passedMs(Managers.getInstance().holeUpdates.getValue()) && !Feature.fullNullCheck() && (HoleESP.getInstance().isOn() || HoleFiller.getInstance().isOn())) {
            this.holes = this.calcHoles();
            this.holeTimer.reset();
        }
    }
    
    public void settingChanged() {
        if (this.executorService != null) {
            this.executorService.shutdown();
        }
        if (this.thread != null) {
            this.shouldInterrupt.set(true);
        }
    }
    
    private ScheduledExecutorService getExecutor() {
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this, 0L, Managers.getInstance().holeUpdates.getValue(), TimeUnit.MILLISECONDS);
        return service;
    }
    
    public boolean isPlayerInHole() {
        for (final BlockPos pos : esohack.holeManager.getSortedHoles()) {
            if (pos.equals((Object)new BlockPos(HoleESP.mc.player.posX, HoleESP.mc.player.posY, HoleESP.mc.player.posZ))) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void run() {
        if (Managers.getInstance().holeThread.getValue() == Managers.ThreadMode.WHILE) {
            while (!this.shouldInterrupt.get()) {
                if (!Feature.fullNullCheck() && (HoleESP.getInstance().isOn() || HoleFiller.getInstance().isOn())) {
                    this.holes = this.calcHoles();
                }
                try {
                    Thread.sleep(Managers.getInstance().holeUpdates.getValue());
                }
                catch (InterruptedException e) {
                    this.thread.interrupt();
                    e.printStackTrace();
                }
            }
            this.shouldInterrupt.set(false);
            this.syncTimer.reset();
            Thread.currentThread().interrupt();
            return;
        }
        if (Managers.getInstance().holeThread.getValue() == Managers.ThreadMode.POOL && !Feature.fullNullCheck() && (HoleESP.getInstance().isOn() || HoleFiller.getInstance().isOn())) {
            this.holes = this.calcHoles();
        }
    }
    
    public List<BlockPos> getHoles() {
        return this.holes;
    }
    
    public List<BlockPos> getMidSafety() {
        return this.midSafety;
    }
    
    public List<BlockPos> getSortedHoles() {
        this.holes.sort(Comparator.comparingDouble(hole -> HoleManager.mc.player.getDistanceSq(hole)));
        return this.getHoles();
    }
    
    public List<BlockPos> calcHoles() {
        final ArrayList<BlockPos> safeSpots = new ArrayList<BlockPos>();
        this.midSafety.clear();
        final List<BlockPos> positions = BlockUtil.getSphere(EntityUtil.getPlayerPos((EntityPlayer)HoleManager.mc.player), Managers.getInstance().holeRange.getValue(), Managers.getInstance().holeRange.getValue().intValue(), false, true, 0);
        for (final BlockPos pos : positions) {
            if (HoleManager.mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) && HoleManager.mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                if (!HoleManager.mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                    continue;
                }
                boolean isSafe = true;
                boolean midSafe = true;
                for (final BlockPos offset : HoleManager.surroundOffset) {
                    final Block block = HoleManager.mc.world.getBlockState(pos.add((Vec3i)offset)).getBlock();
                    if (BlockUtil.isBlockUnSolid(block)) {
                        midSafe = false;
                    }
                    if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST) {
                        if (block != Blocks.ANVIL) {
                            isSafe = false;
                        }
                    }
                }
                if (isSafe) {
                    safeSpots.add(pos);
                }
                if (!midSafe) {
                    continue;
                }
                this.midSafety.add(pos);
            }
        }
        return safeSpots;
    }
    
    public boolean isSafe(final BlockPos pos) {
        boolean isSafe = true;
        for (final BlockPos offset : HoleManager.surroundOffset) {
            final Block block = HoleManager.mc.world.getBlockState(pos.add((Vec3i)offset)).getBlock();
            if (block != Blocks.BEDROCK) {
                isSafe = false;
                break;
            }
        }
        return isSafe;
    }
    
    static {
        surroundOffset = BlockUtil.toBlockPos(EntityUtil.getOffsets(0, true, true));
    }
}
