// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.player;

import java.util.Iterator;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumHand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import com.esoterik.client.features.command.Command;
import com.esoterik.client.util.BurrowUtil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Burrow extends Module
{
    private final Setting<Integer> offset;
    private final Setting<Boolean> rotate;
    private final Setting<Mode> mode;
    private BlockPos originalPos;
    private int oldSlot;
    Block returnBlock;
    
    public Burrow() {
        super("Cripple", "TPs you into a block", Category.PLAYER, true, false, false);
        this.offset = (Setting<Integer>)this.register(new Setting("Offset", (T)3, (T)(-5), (T)5));
        this.rotate = (Setting<Boolean>)this.register(new Setting("Rotate", (T)false));
        this.mode = (Setting<Mode>)this.register(new Setting("Mode", (T)Mode.OBBY));
        this.oldSlot = -1;
        this.returnBlock = null;
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        this.originalPos = new BlockPos(Burrow.mc.field_71439_g.field_70165_t, Burrow.mc.field_71439_g.field_70163_u, Burrow.mc.field_71439_g.field_70161_v);
        switch (this.mode.getValue()) {
            case OBBY: {
                this.returnBlock = Blocks.field_150343_Z;
                break;
            }
            case ECHEST: {
                this.returnBlock = Blocks.field_150477_bB;
                break;
            }
            case CHEST: {
                this.returnBlock = (Block)Blocks.field_150486_ae;
                break;
            }
        }
        if (Burrow.mc.field_71441_e.func_180495_p(new BlockPos(Burrow.mc.field_71439_g.field_70165_t, Burrow.mc.field_71439_g.field_70163_u, Burrow.mc.field_71439_g.field_70161_v)).func_177230_c().equals(this.returnBlock) || this.intersectsWithEntity(this.originalPos)) {
            this.toggle();
            return;
        }
        this.oldSlot = Burrow.mc.field_71439_g.field_71071_by.field_70461_c;
    }
    
    @Override
    public void onUpdate() {
        switch (this.mode.getValue()) {
            case OBBY: {
                if (BurrowUtil.findHotbarBlock(BlockObsidian.class) == -1) {
                    Command.sendMessage("Can't find obby in hotbar!");
                    this.toggle();
                    break;
                }
                break;
            }
            case ECHEST: {
                if (BurrowUtil.findHotbarBlock(BlockEnderChest.class) == -1) {
                    Command.sendMessage("Can't find echest in hotbar!");
                    this.toggle();
                    break;
                }
                break;
            }
            case CHEST: {
                if (BurrowUtil.findHotbarBlock(BlockChest.class) == -1) {
                    Command.sendMessage("Can't find chest in hotbar!");
                    this.toggle();
                    break;
                }
                break;
            }
        }
        switch (this.mode.getValue()) {
            case OBBY: {
                BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockObsidian.class));
                break;
            }
            case ECHEST: {
                BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockEnderChest.class));
                break;
            }
            case CHEST: {
                BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockChest.class));
                break;
            }
        }
        Burrow.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Burrow.mc.field_71439_g.field_70165_t, Burrow.mc.field_71439_g.field_70163_u + 0.41999998688698, Burrow.mc.field_71439_g.field_70161_v, true));
        Burrow.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Burrow.mc.field_71439_g.field_70165_t, Burrow.mc.field_71439_g.field_70163_u + 0.7531999805211997, Burrow.mc.field_71439_g.field_70161_v, true));
        Burrow.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Burrow.mc.field_71439_g.field_70165_t, Burrow.mc.field_71439_g.field_70163_u + 1.00133597911214, Burrow.mc.field_71439_g.field_70161_v, true));
        Burrow.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Burrow.mc.field_71439_g.field_70165_t, Burrow.mc.field_71439_g.field_70163_u + 1.16610926093821, Burrow.mc.field_71439_g.field_70161_v, true));
        BurrowUtil.placeBlock(this.originalPos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, false);
        Burrow.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Position(Burrow.mc.field_71439_g.field_70165_t, Burrow.mc.field_71439_g.field_70163_u + this.offset.getValue(), Burrow.mc.field_71439_g.field_70161_v, false));
        Burrow.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)Burrow.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
        Burrow.mc.field_71439_g.func_70095_a(false);
        BurrowUtil.switchToSlot(this.oldSlot);
        this.toggle();
    }
    
    private boolean intersectsWithEntity(final BlockPos pos) {
        for (final Entity entity : Burrow.mc.field_71441_e.field_72996_f) {
            if (entity.equals((Object)Burrow.mc.field_71439_g)) {
                continue;
            }
            if (entity instanceof EntityItem) {
                continue;
            }
            if (new AxisAlignedBB(pos).func_72326_a(entity.func_174813_aQ())) {
                return true;
            }
        }
        return false;
    }
    
    public enum Mode
    {
        OBBY, 
        ECHEST, 
        CHEST;
    }
}
