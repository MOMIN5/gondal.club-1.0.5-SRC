// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemArmor;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;

public class ItemUtil implements Minecraftable
{
    public static int getItemFromHotbar(final Item item) {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack.func_77973_b() == item) {
                slot = i;
            }
        }
        return slot;
    }
    
    public static int getItemSlot(final Class clss) {
        int itemSlot = -1;
        for (int i = 45; i > 0; --i) {
            if (ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(i).func_77973_b().getClass() == clss) {
                itemSlot = i;
                break;
            }
        }
        return itemSlot;
    }
    
    public static int getItemSlot(final Item item) {
        int itemSlot = -1;
        for (int i = 45; i > 0; --i) {
            if (ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(i).func_77973_b().equals(item)) {
                itemSlot = i;
                break;
            }
        }
        return itemSlot;
    }
    
    public static int getItemCount(final Item item) {
        int count = 0;
        for (int size = ItemUtil.mc.field_71439_g.field_71071_by.field_70462_a.size(), i = 0; i < size; ++i) {
            final ItemStack itemStack = (ItemStack)ItemUtil.mc.field_71439_g.field_71071_by.field_70462_a.get(i);
            if (itemStack.func_77973_b() == item) {
                count += itemStack.func_190916_E();
            }
        }
        final ItemStack offhandStack = ItemUtil.mc.field_71439_g.func_184592_cb();
        if (offhandStack.func_77973_b() == item) {
            count += offhandStack.func_190916_E();
        }
        return count;
    }
    
    public static boolean isArmorLow(final EntityPlayer player, final int durability) {
        for (final ItemStack piece : player.field_71071_by.field_70460_b) {
            if (piece != null && getDamageInPercent(piece) >= durability) {
                continue;
            }
            return true;
        }
        return false;
    }
    
    public static int getItemDamage(final ItemStack stack) {
        return stack.func_77958_k() - stack.func_77952_i();
    }
    
    public static float getDamageInPercent(final ItemStack stack) {
        final float green = (stack.func_77958_k() - (float)stack.func_77952_i()) / stack.func_77958_k();
        final float red = 1.0f - green;
        return (float)(100 - (int)(red * 100.0f));
    }
    
    public static int getRoundedDamage(final ItemStack stack) {
        return (int)getDamageInPercent(stack);
    }
    
    public static boolean hasDurability(final ItemStack stack) {
        final Item item = stack.func_77973_b();
        return item instanceof ItemArmor || item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemShield;
    }
}
