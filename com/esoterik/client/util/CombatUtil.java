// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class CombatUtil implements Minecraftable
{
    public static EntityPlayer getTarget(final float range) {
        EntityPlayer currentTarget = null;
        for (int size = CombatUtil.mc.field_71441_e.field_73010_i.size(), i = 0; i < size; ++i) {
            final EntityPlayer player = CombatUtil.mc.field_71441_e.field_73010_i.get(i);
            if (!EntityUtil.isntValid((Entity)player, range)) {
                if (currentTarget == null) {
                    currentTarget = player;
                }
                else if (CombatUtil.mc.field_71439_g.func_70068_e((Entity)player) < CombatUtil.mc.field_71439_g.func_70068_e((Entity)currentTarget)) {
                    currentTarget = player;
                }
            }
        }
        return currentTarget;
    }
    
    public static boolean isInHole(final EntityPlayer entity) {
        return isBlockValid(new BlockPos(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v));
    }
    
    public static boolean isBlockValid(final BlockPos blockPos) {
        return isBedrockHole(blockPos) || isObbyHole(blockPos) || isBothHole(blockPos);
    }
    
    public static int isInHoleInt(final EntityPlayer entity) {
        final BlockPos playerPos = new BlockPos(entity.func_174791_d());
        if (isBedrockHole(playerPos)) {
            return 1;
        }
        if (isObbyHole(playerPos) || isBothHole(playerPos)) {
            return 2;
        }
        return 0;
    }
    
    public static boolean isObbyHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[] { blockPos.func_177978_c(), blockPos.func_177968_d(), blockPos.func_177974_f(), blockPos.func_177976_e(), blockPos.func_177977_b() };
        for (final BlockPos pos : array) {
            final IBlockState touchingState = CombatUtil.mc.field_71441_e.func_180495_p(pos);
            if (touchingState.func_177230_c() != Blocks.field_150343_Z) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isBedrockHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[] { blockPos.func_177978_c(), blockPos.func_177968_d(), blockPos.func_177974_f(), blockPos.func_177976_e(), blockPos.func_177977_b() };
        for (final BlockPos pos : array) {
            final IBlockState touchingState = CombatUtil.mc.field_71441_e.func_180495_p(pos);
            if (touchingState.func_177230_c() != Blocks.field_150357_h) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isBothHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[] { blockPos.func_177978_c(), blockPos.func_177968_d(), blockPos.func_177974_f(), blockPos.func_177976_e(), blockPos.func_177977_b() };
        for (final BlockPos pos : array) {
            final IBlockState touchingState = CombatUtil.mc.field_71441_e.func_180495_p(pos);
            if (touchingState.func_177230_c() != Blocks.field_150357_h && touchingState.func_177230_c() != Blocks.field_150343_Z) {
                return false;
            }
        }
        return true;
    }
}
