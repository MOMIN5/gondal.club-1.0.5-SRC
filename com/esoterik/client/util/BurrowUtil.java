// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import java.util.Iterator;
import net.minecraft.block.state.IBlockState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.network.Packet;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.Minecraft;

public class BurrowUtil implements Util
{
    public static final Minecraft mc;
    
    public static boolean placeBlock(final BlockPos pos, final EnumHand hand, final boolean rotate, final boolean packet, final boolean isSneaking) {
        boolean sneaking = false;
        final EnumFacing side = getFirstFacing(pos);
        if (side == null) {
            return isSneaking;
        }
        final BlockPos neighbour = pos.func_177972_a(side);
        final EnumFacing opposite = side.func_176734_d();
        final Vec3d hitVec = new Vec3d((Vec3i)neighbour).func_72441_c(0.5, 0.5, 0.5).func_178787_e(new Vec3d(opposite.func_176730_m()).func_186678_a(0.5));
        final Block neighbourBlock = BurrowUtil.mc.field_71441_e.func_180495_p(neighbour).func_177230_c();
        if (!BurrowUtil.mc.field_71439_g.func_70093_af()) {
            BurrowUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)BurrowUtil.mc.field_71439_g, CPacketEntityAction.Action.START_SNEAKING));
            BurrowUtil.mc.field_71439_g.func_70095_a(true);
            sneaking = true;
        }
        if (rotate) {
            faceVector(hitVec, true);
        }
        rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        BurrowUtil.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
        BurrowUtil.mc.field_71467_ac = 4;
        return sneaking || isSneaking;
    }
    
    public static List<EnumFacing> getPossibleSides(final BlockPos pos) {
        final List<EnumFacing> facings = new ArrayList<EnumFacing>();
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbour = pos.func_177972_a(side);
            if (BurrowUtil.mc.field_71441_e.func_180495_p(neighbour).func_177230_c().func_176209_a(BurrowUtil.mc.field_71441_e.func_180495_p(neighbour), false)) {
                final IBlockState blockState = BurrowUtil.mc.field_71441_e.func_180495_p(neighbour);
                if (!blockState.func_185904_a().func_76222_j()) {
                    facings.add(side);
                }
            }
        }
        return facings;
    }
    
    public static EnumFacing getFirstFacing(final BlockPos pos) {
        final Iterator<EnumFacing> iterator = getPossibleSides(pos).iterator();
        if (iterator.hasNext()) {
            final EnumFacing facing = iterator.next();
            return facing;
        }
        return null;
    }
    
    public static Vec3d getEyesPos() {
        return new Vec3d(BurrowUtil.mc.field_71439_g.field_70165_t, BurrowUtil.mc.field_71439_g.field_70163_u + BurrowUtil.mc.field_71439_g.func_70047_e(), BurrowUtil.mc.field_71439_g.field_70161_v);
    }
    
    public static float[] getLegitRotations(final Vec3d vec) {
        final Vec3d eyesPos = getEyesPos();
        final double diffX = vec.field_72450_a - eyesPos.field_72450_a;
        final double diffY = vec.field_72448_b - eyesPos.field_72448_b;
        final double diffZ = vec.field_72449_c - eyesPos.field_72449_c;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[] { BurrowUtil.mc.field_71439_g.field_70177_z + MathHelper.func_76142_g(yaw - BurrowUtil.mc.field_71439_g.field_70177_z), BurrowUtil.mc.field_71439_g.field_70125_A + MathHelper.func_76142_g(pitch - BurrowUtil.mc.field_71439_g.field_70125_A) };
    }
    
    public static void faceVector(final Vec3d vec, final boolean normalizeAngle) {
        final float[] rotations = getLegitRotations(vec);
        BurrowUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Rotation(rotations[0], normalizeAngle ? ((float)MathHelper.func_180184_b((int)rotations[1], 360)) : rotations[1], BurrowUtil.mc.field_71439_g.field_70122_E));
    }
    
    public static void rightClickBlock(final BlockPos pos, final Vec3d vec, final EnumHand hand, final EnumFacing direction, final boolean packet) {
        if (packet) {
            final float f = (float)(vec.field_72450_a - pos.func_177958_n());
            final float f2 = (float)(vec.field_72448_b - pos.func_177956_o());
            final float f3 = (float)(vec.field_72449_c - pos.func_177952_p());
            BurrowUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f2, f3));
        }
        else {
            BurrowUtil.mc.field_71442_b.func_187099_a(BurrowUtil.mc.field_71439_g, BurrowUtil.mc.field_71441_e, pos, direction, vec, hand);
        }
        BurrowUtil.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
        BurrowUtil.mc.field_71467_ac = 4;
    }
    
    public static int findHotbarBlock(final Class clazz) {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = BurrowUtil.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack != ItemStack.field_190927_a) {
                if (clazz.isInstance(stack.func_77973_b())) {
                    return i;
                }
                if (stack.func_77973_b() instanceof ItemBlock) {
                    final Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
                    if (clazz.isInstance(block)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    public static void switchToSlot(final int slot) {
        BurrowUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketHeldItemChange(slot));
        BurrowUtil.mc.field_71439_g.field_71071_by.field_70461_c = slot;
        BurrowUtil.mc.field_71442_b.func_78765_e();
    }
    
    static {
        mc = Minecraft.func_71410_x();
    }
}
