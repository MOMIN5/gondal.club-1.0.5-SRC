// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import java.util.Arrays;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.entity.Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import java.util.List;

public class HoleFillUtil
{
    public static final List<Block> blackList;
    public static final List<Block> shulkerList;
    private static final Minecraft mc;
    private static Entity player;
    public static FMLCommonHandler fmlHandler;
    
    public static void placeBlockScaffold(final BlockPos pos) {
        final Vec3d eyesPos = new Vec3d(HoleFillUtil.player.field_70165_t, HoleFillUtil.player.field_70163_u + HoleFillUtil.player.func_70047_e(), HoleFillUtil.player.field_70161_v);
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbor = pos.func_177972_a(side);
            final EnumFacing side2 = side.func_176734_d();
            if (canBeClicked(neighbor)) {
                final Vec3d hitVec = new Vec3d((Vec3i)neighbor).func_72441_c(0.5, 0.5, 0.5).func_178787_e(new Vec3d(side2.func_176730_m()).func_186678_a(0.5));
                if (eyesPos.func_72436_e(hitVec) <= 18.0625) {
                    faceVectorPacketInstant(hitVec);
                    processRightClickBlock(neighbor, side2, hitVec);
                    HoleFillUtil.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                    HoleFillUtil.mc.field_71467_ac = 4;
                    return;
                }
            }
        }
    }
    
    private static float[] getLegitRotations(final Vec3d vec) {
        final Vec3d eyesPos = getEyesPos();
        final double diffX = vec.field_72450_a - eyesPos.field_72450_a;
        final double diffY = vec.field_72448_b - eyesPos.field_72448_b;
        final double diffZ = vec.field_72449_c - eyesPos.field_72449_c;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[] { HoleFillUtil.mc.field_71439_g.field_70177_z + MathHelper.func_76142_g(yaw - HoleFillUtil.mc.field_71439_g.field_70177_z), HoleFillUtil.mc.field_71439_g.field_70125_A + MathHelper.func_76142_g(pitch - HoleFillUtil.mc.field_71439_g.field_70125_A) };
    }
    
    private static Vec3d getEyesPos() {
        return new Vec3d(HoleFillUtil.mc.field_71439_g.field_70165_t, HoleFillUtil.mc.field_71439_g.field_70163_u + HoleFillUtil.mc.field_71439_g.func_70047_e(), HoleFillUtil.mc.field_71439_g.field_70161_v);
    }
    
    public static void faceVectorPacketInstant(final Vec3d vec) {
        final float[] rotations = getLegitRotations(vec);
        HoleFillUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Rotation(rotations[0], rotations[1], HoleFillUtil.mc.field_71439_g.field_70122_E));
    }
    
    private static void processRightClickBlock(final BlockPos pos, final EnumFacing side, final Vec3d hitVec) {
        getPlayerController().func_187099_a(HoleFillUtil.mc.field_71439_g, HoleFillUtil.mc.field_71441_e, pos, side, hitVec, EnumHand.MAIN_HAND);
    }
    
    public static boolean canBeClicked(final BlockPos pos) {
        return getBlock(pos).func_176209_a(getState(pos), false);
    }
    
    private static Block getBlock(final BlockPos pos) {
        return getState(pos).func_177230_c();
    }
    
    private static PlayerControllerMP getPlayerController() {
        return Minecraft.func_71410_x().field_71442_b;
    }
    
    private static IBlockState getState(final BlockPos pos) {
        return HoleFillUtil.mc.field_71441_e.func_180495_p(pos);
    }
    
    public static boolean checkForNeighbours(final BlockPos blockPos) {
        if (!hasNeighbour(blockPos)) {
            for (final EnumFacing side : EnumFacing.values()) {
                final BlockPos neighbour = blockPos.func_177972_a(side);
                if (hasNeighbour(neighbour)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    public static EnumFacing getPlaceableSide(final BlockPos pos) {
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbour = pos.func_177972_a(side);
            if (HoleFillUtil.mc.field_71441_e.func_180495_p(neighbour).func_177230_c().func_176209_a(HoleFillUtil.mc.field_71441_e.func_180495_p(neighbour), false)) {
                final IBlockState blockState = HoleFillUtil.mc.field_71441_e.func_180495_p(neighbour);
                if (!blockState.func_185904_a().func_76222_j()) {
                    return side;
                }
            }
        }
        return null;
    }
    
    public static boolean hasNeighbour(final BlockPos blockPos) {
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbour = blockPos.func_177972_a(side);
            if (!HoleFillUtil.mc.field_71441_e.func_180495_p(neighbour).func_185904_a().func_76222_j()) {
                return true;
            }
        }
        return false;
    }
    
    static {
        blackList = Arrays.asList(Blocks.field_150477_bB, (Block)Blocks.field_150486_ae, Blocks.field_150447_bR, Blocks.field_150462_ai, Blocks.field_150467_bQ, Blocks.field_150382_bo, (Block)Blocks.field_150438_bZ, Blocks.field_150409_cd, Blocks.field_150367_z, Blocks.field_150415_aT, Blocks.field_150381_bn);
        shulkerList = Arrays.asList(Blocks.field_190977_dl, Blocks.field_190978_dm, Blocks.field_190979_dn, Blocks.field_190980_do, Blocks.field_190981_dp, Blocks.field_190982_dq, Blocks.field_190983_dr, Blocks.field_190984_ds, Blocks.field_190985_dt, Blocks.field_190986_du, Blocks.field_190987_dv, Blocks.field_190988_dw, Blocks.field_190989_dx, Blocks.field_190990_dy, Blocks.field_190991_dz, Blocks.field_190975_dA);
        mc = Minecraft.func_71410_x();
        HoleFillUtil.player = (Entity)HoleFillUtil.mc.field_71439_g;
        HoleFillUtil.fmlHandler = FMLCommonHandler.instance();
    }
}
