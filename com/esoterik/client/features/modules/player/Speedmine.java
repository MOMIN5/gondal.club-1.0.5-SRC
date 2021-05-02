// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.player;

import net.minecraft.util.EnumHand;
import com.esoterik.client.util.BlockUtil;
import com.esoterik.client.event.events.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Iterator;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.network.play.client.CPacketAnimation;
import com.esoterik.client.event.events.PacketEvent;
import com.esoterik.client.util.RenderUtil;
import com.esoterik.client.esohack;
import com.esoterik.client.features.modules.client.Colors;
import java.awt.Color;
import com.esoterik.client.event.events.Render3DEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import com.esoterik.client.util.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Speedmine extends Module
{
    public Setting<Boolean> tweaks;
    public Setting<Boolean> reset;
    public Setting<Boolean> noBreakAnim;
    public Setting<Boolean> noDelay;
    public Setting<Boolean> noSwing;
    public Setting<Boolean> noTrace;
    public Setting<Boolean> allow;
    public Setting<Boolean> pickaxe;
    public Setting<Boolean> doubleBreak;
    public Setting<Boolean> render;
    public Setting<Boolean> box;
    public Setting<Boolean> outline;
    private final Setting<Integer> boxAlpha;
    private final Setting<Float> lineWidth;
    private static Speedmine INSTANCE;
    public BlockPos currentPos;
    public IBlockState currentBlockState;
    private final Timer timer;
    private boolean isMining;
    private BlockPos lastPos;
    private EnumFacing lastFacing;
    
    public Speedmine() {
        super("Speedmine", "Speeds up mining.", Category.PLAYER, true, false, false);
        this.tweaks = (Setting<Boolean>)this.register(new Setting("Tweaks", (T)true));
        this.reset = (Setting<Boolean>)this.register(new Setting("Reset", (T)true));
        this.noBreakAnim = (Setting<Boolean>)this.register(new Setting("NoBreakAnim", (T)false));
        this.noDelay = (Setting<Boolean>)this.register(new Setting("NoDelay", (T)false));
        this.noSwing = (Setting<Boolean>)this.register(new Setting("NoSwing", (T)false));
        this.noTrace = (Setting<Boolean>)this.register(new Setting("NoTrace", (T)false));
        this.allow = (Setting<Boolean>)this.register(new Setting("AllowMultiTask", (T)false));
        this.pickaxe = (Setting<Boolean>)this.register(new Setting("Pickaxe", (T)true, v -> this.noTrace.getValue()));
        this.doubleBreak = (Setting<Boolean>)this.register(new Setting("DoubleBreak", (T)false));
        this.render = (Setting<Boolean>)this.register(new Setting("Render", (T)false));
        this.box = (Setting<Boolean>)this.register(new Setting("Box", (T)false, v -> this.render.getValue()));
        this.outline = (Setting<Boolean>)this.register(new Setting("Outline", (T)true, v -> this.render.getValue()));
        this.boxAlpha = (Setting<Integer>)this.register(new Setting("BoxAlpha", (T)85, (T)0, (T)255, v -> this.box.getValue() && this.render.getValue()));
        this.lineWidth = (Setting<Float>)this.register(new Setting("LineWidth", (T)1.0f, (T)0.1f, (T)5.0f, v -> this.outline.getValue() && this.render.getValue()));
        this.timer = new Timer();
        this.isMining = false;
        this.lastPos = null;
        this.lastFacing = null;
        this.setInstance();
    }
    
    private void setInstance() {
        Speedmine.INSTANCE = this;
    }
    
    public static Speedmine getInstance() {
        if (Speedmine.INSTANCE == null) {
            Speedmine.INSTANCE = new Speedmine();
        }
        return Speedmine.INSTANCE;
    }
    
    @Override
    public void onTick() {
        if (this.currentPos != null && (!Speedmine.mc.world.getBlockState(this.currentPos).equals(this.currentBlockState) || Speedmine.mc.world.getBlockState(this.currentPos).getBlock() == Blocks.AIR)) {
            this.currentPos = null;
            this.currentBlockState = null;
        }
    }
    
    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (this.noDelay.getValue()) {
            Speedmine.mc.playerController.blockHitDelay = 0;
        }
        if (this.isMining && this.lastPos != null && this.lastFacing != null && this.noBreakAnim.getValue()) {
            Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.lastPos, this.lastFacing));
        }
        if (this.reset.getValue() && Speedmine.mc.gameSettings.keyBindUseItem.isKeyDown() && !this.allow.getValue()) {
            Speedmine.mc.playerController.isHittingBlock = false;
        }
    }
    
    @Override
    public void onRender3D(final Render3DEvent event) {
        if (this.render.getValue() && this.currentPos != null) {
            final Color color = new Color(255, 255, 255, 255);
            final Color readyColor = Colors.INSTANCE.isEnabled() ? Colors.INSTANCE.getCurrentColor() : new Color(125, 105, 255, 255);
            RenderUtil.drawBoxESP(this.currentPos, this.timer.passedMs((int)(2000.0f * esohack.serverManager.getTpsFactor())) ? readyColor : color, false, color, this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), false);
        }
    }
    
    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0) {
            if (this.noSwing.getValue() && event.getPacket() instanceof CPacketAnimation) {
                event.setCanceled(true);
            }
            if (this.noBreakAnim.getValue() && event.getPacket() instanceof CPacketPlayerDigging) {
                final CPacketPlayerDigging packet = event.getPacket();
                if (packet != null && packet.getPosition() != null) {
                    try {
                        for (final Entity entity : Speedmine.mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(packet.getPosition()))) {
                            if (entity instanceof EntityEnderCrystal) {
                                this.showAnimation();
                                return;
                            }
                        }
                    }
                    catch (Exception ex) {}
                    if (packet.getAction().equals((Object)CPacketPlayerDigging.Action.START_DESTROY_BLOCK)) {
                        this.showAnimation(true, packet.getPosition(), packet.getFacing());
                    }
                    if (packet.getAction().equals((Object)CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                        this.showAnimation();
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onBlockEvent(final BlockEvent event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getStage() == 3 && this.reset.getValue() && Speedmine.mc.playerController.curBlockDamageMP > 0.1f) {
            Speedmine.mc.playerController.isHittingBlock = true;
        }
        if (event.getStage() == 4 && this.tweaks.getValue()) {
            if (BlockUtil.canBreak(event.pos)) {
                if (this.reset.getValue()) {
                    Speedmine.mc.playerController.isHittingBlock = false;
                }
                if (this.currentPos == null) {
                    this.currentPos = event.pos;
                    this.currentBlockState = Speedmine.mc.world.getBlockState(this.currentPos);
                    this.timer.reset();
                }
                Speedmine.mc.player.swingArm(EnumHand.MAIN_HAND);
                Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.pos, event.facing));
                Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.pos, event.facing));
                event.setCanceled(true);
            }
            if (this.doubleBreak.getValue()) {
                final BlockPos above = event.pos.add(0, 1, 0);
                if (BlockUtil.canBreak(above) && Speedmine.mc.player.getDistance((double)above.getX(), (double)above.getY(), (double)above.getZ()) <= 5.0) {
                    Speedmine.mc.player.swingArm(EnumHand.MAIN_HAND);
                    Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, above, event.facing));
                    Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, above, event.facing));
                    Speedmine.mc.playerController.onPlayerDestroyBlock(above);
                    Speedmine.mc.world.setBlockToAir(above);
                }
            }
        }
    }
    
    private void showAnimation(final boolean isMining, final BlockPos lastPos, final EnumFacing lastFacing) {
        this.isMining = isMining;
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }
    
    public void showAnimation() {
        this.showAnimation(false, null, null);
    }
    
    @Override
    public String getDisplayInfo() {
        return "Packet";
    }
    
    static {
        Speedmine.INSTANCE = new Speedmine();
    }
}
