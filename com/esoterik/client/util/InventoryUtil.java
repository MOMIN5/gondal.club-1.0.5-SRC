// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EntityEquipmentSlot;
import com.esoterik.client.esohack;
import java.util.HashMap;
import net.minecraft.inventory.Slot;
import net.minecraft.init.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;

public class InventoryUtil implements Util
{
    public static void switchToHotbarSlot(final int slot, final boolean silent) {
        if (InventoryUtil.mc.field_71439_g.field_71071_by.field_70461_c == slot || slot < 0) {
            return;
        }
        if (silent) {
            InventoryUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketHeldItemChange(slot));
            InventoryUtil.mc.field_71442_b.func_78765_e();
        }
        else {
            InventoryUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketHeldItemChange(slot));
            InventoryUtil.mc.field_71439_g.field_71071_by.field_70461_c = slot;
            InventoryUtil.mc.field_71442_b.func_78765_e();
        }
    }
    
    public static void switchToHotbarSlot(final Class clazz, final boolean silent) {
        final int slot = findHotbarBlock(clazz);
        if (slot > -1) {
            switchToHotbarSlot(slot, silent);
        }
    }
    
    public static boolean isNull(final ItemStack stack) {
        return stack == null || stack.func_77973_b() instanceof ItemAir;
    }
    
    public static int findHotbarBlock(final Class clazz) {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = InventoryUtil.mc.field_71439_g.field_71071_by.func_70301_a(i);
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
    
    public static int findHotbarBlock(final Block blockIn) {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = InventoryUtil.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack != ItemStack.field_190927_a) {
                if (stack.func_77973_b() instanceof ItemBlock) {
                    final Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
                    if (block == blockIn) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    public static int getItemHotbar(final Item input) {
        for (int i = 0; i < 9; ++i) {
            final Item item = InventoryUtil.mc.field_71439_g.field_71071_by.func_70301_a(i).func_77973_b();
            if (Item.func_150891_b(item) == Item.func_150891_b(input)) {
                return i;
            }
        }
        return -1;
    }
    
    public static int findStackInventory(final Item input) {
        return findStackInventory(input, false);
    }
    
    public static int findStackInventory(final Item input, final boolean withHotbar) {
        for (int i = withHotbar ? 0 : 9; i < 36; ++i) {
            final Item item = InventoryUtil.mc.field_71439_g.field_71071_by.func_70301_a(i).func_77973_b();
            if (Item.func_150891_b(input) == Item.func_150891_b(item)) {
                return i + ((i < 9) ? 36 : 0);
            }
        }
        return -1;
    }
    
    public static int findItemInventorySlot(final Item item, final boolean offHand) {
        final AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        for (final Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().func_77973_b() == item) {
                if (entry.getKey() == 45 && !offHand) {
                    continue;
                }
                slot.set(entry.getKey());
                return slot.get();
            }
        }
        return slot.get();
    }
    
    public static List<Integer> findEmptySlots(final boolean withXCarry) {
        final List<Integer> outPut = new ArrayList<Integer>();
        for (final Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().field_190928_g || entry.getValue().func_77973_b() == Items.field_190931_a) {
                outPut.add(entry.getKey());
            }
        }
        if (withXCarry) {
            for (int i = 1; i < 5; ++i) {
                final Slot craftingSlot = InventoryUtil.mc.field_71439_g.field_71069_bz.field_75151_b.get(i);
                final ItemStack craftingStack = craftingSlot.func_75211_c();
                if (craftingStack.func_190926_b() || craftingStack.func_77973_b() == Items.field_190931_a) {
                    outPut.add(i);
                }
            }
        }
        return outPut;
    }
    
    public static int findInventoryBlock(final Class clazz, final boolean offHand) {
        final AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        for (final Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (isBlock(entry.getValue().func_77973_b(), clazz)) {
                if (entry.getKey() == 45 && !offHand) {
                    continue;
                }
                slot.set(entry.getKey());
                return slot.get();
            }
        }
        return slot.get();
    }
    
    public static boolean isBlock(final Item item, final Class clazz) {
        if (item instanceof ItemBlock) {
            final Block block = ((ItemBlock)item).func_179223_d();
            return clazz.isInstance(block);
        }
        return false;
    }
    
    public static void confirmSlot(final int slot) {
        InventoryUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketHeldItemChange(slot));
        InventoryUtil.mc.field_71439_g.field_71071_by.field_70461_c = slot;
        InventoryUtil.mc.field_71442_b.func_78765_e();
    }
    
    public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        return getInventorySlots(9, 44);
    }
    
    private static Map<Integer, ItemStack> getInventorySlots(final int currentI, final int last) {
        int current = currentI;
        final Map<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        while (current <= last) {
            fullInventorySlots.put(current, (ItemStack)InventoryUtil.mc.field_71439_g.field_71069_bz.func_75138_a().get(current));
            ++current;
        }
        return fullInventorySlots;
    }
    
    public static boolean[] switchItem(final boolean back, final int lastHotbarSlot, final boolean switchedItem, final Switch mode, final Class clazz) {
        final boolean[] switchedItemSwitched = { switchedItem, false };
        switch (mode) {
            case NORMAL: {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(findHotbarBlock(clazz), false);
                    switchedItemSwitched[0] = true;
                }
                else if (back && switchedItem) {
                    switchToHotbarSlot(lastHotbarSlot, false);
                    switchedItemSwitched[0] = false;
                }
                switchedItemSwitched[1] = true;
                break;
            }
            case SILENT: {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(findHotbarBlock(clazz), true);
                    switchedItemSwitched[0] = true;
                }
                else if (back && switchedItem) {
                    switchedItemSwitched[0] = false;
                    esohack.inventoryManager.recoverSilent(lastHotbarSlot);
                }
                switchedItemSwitched[1] = true;
                break;
            }
            case NONE: {
                if (back) {
                    switchedItemSwitched[1] = true;
                    break;
                }
                switchedItemSwitched[1] = (InventoryUtil.mc.field_71439_g.field_71071_by.field_70461_c == findHotbarBlock(clazz));
                break;
            }
        }
        return switchedItemSwitched;
    }
    
    public static boolean[] switchItemToItem(final boolean back, final int lastHotbarSlot, final boolean switchedItem, final Switch mode, final Item item) {
        final boolean[] switchedItemSwitched = { switchedItem, false };
        switch (mode) {
            case NORMAL: {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(getItemHotbar(item), false);
                    switchedItemSwitched[0] = true;
                }
                else if (back && switchedItem) {
                    switchToHotbarSlot(lastHotbarSlot, false);
                    switchedItemSwitched[0] = false;
                }
                switchedItemSwitched[1] = true;
                break;
            }
            case SILENT: {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(getItemHotbar(item), true);
                    switchedItemSwitched[0] = true;
                }
                else if (back && switchedItem) {
                    switchedItemSwitched[0] = false;
                    esohack.inventoryManager.recoverSilent(lastHotbarSlot);
                }
                switchedItemSwitched[1] = true;
                break;
            }
            case NONE: {
                if (back) {
                    switchedItemSwitched[1] = true;
                    break;
                }
                switchedItemSwitched[1] = (InventoryUtil.mc.field_71439_g.field_71071_by.field_70461_c == getItemHotbar(item));
                break;
            }
        }
        return switchedItemSwitched;
    }
    
    public static boolean holdingItem(final Class clazz) {
        boolean result = false;
        final ItemStack stack = InventoryUtil.mc.field_71439_g.func_184614_ca();
        result = isInstanceOf(stack, clazz);
        if (!result) {
            final ItemStack offhand = InventoryUtil.mc.field_71439_g.func_184592_cb();
            result = isInstanceOf(stack, clazz);
        }
        return result;
    }
    
    public static boolean isInstanceOf(final ItemStack stack, final Class clazz) {
        if (stack == null) {
            return false;
        }
        final Item item = stack.func_77973_b();
        if (clazz.isInstance(item)) {
            return true;
        }
        if (item instanceof ItemBlock) {
            final Block block = Block.func_149634_a(item);
            return clazz.isInstance(block);
        }
        return false;
    }
    
    public static int getEmptyXCarry() {
        for (int i = 1; i < 5; ++i) {
            final Slot craftingSlot = InventoryUtil.mc.field_71439_g.field_71069_bz.field_75151_b.get(i);
            final ItemStack craftingStack = craftingSlot.func_75211_c();
            if (craftingStack.func_190926_b() || craftingStack.func_77973_b() == Items.field_190931_a) {
                return i;
            }
        }
        return -1;
    }
    
    public static boolean isSlotEmpty(final int i) {
        final Slot slot = InventoryUtil.mc.field_71439_g.field_71069_bz.field_75151_b.get(i);
        final ItemStack stack = slot.func_75211_c();
        return stack.func_190926_b();
    }
    
    public static int convertHotbarToInv(final int input) {
        return 36 + input;
    }
    
    public static boolean areStacksCompatible(final ItemStack stack1, final ItemStack stack2) {
        if (!stack1.func_77973_b().equals(stack2.func_77973_b())) {
            return false;
        }
        if (stack1.func_77973_b() instanceof ItemBlock && stack2.func_77973_b() instanceof ItemBlock) {
            final Block block1 = ((ItemBlock)stack1.func_77973_b()).func_179223_d();
            final Block block2 = ((ItemBlock)stack2.func_77973_b()).func_179223_d();
            if (!block1.field_149764_J.equals(block2.field_149764_J)) {
                return false;
            }
        }
        return stack1.func_82833_r().equals(stack2.func_82833_r()) && stack1.func_77952_i() == stack2.func_77952_i();
    }
    
    public static EntityEquipmentSlot getEquipmentFromSlot(final int slot) {
        if (slot == 5) {
            return EntityEquipmentSlot.HEAD;
        }
        if (slot == 6) {
            return EntityEquipmentSlot.CHEST;
        }
        if (slot == 7) {
            return EntityEquipmentSlot.LEGS;
        }
        return EntityEquipmentSlot.FEET;
    }
    
    public static int findArmorSlot(final EntityEquipmentSlot type, final boolean binding) {
        int slot = -1;
        float damage = 0.0f;
        for (int i = 9; i < 45; ++i) {
            final ItemStack s = Minecraft.func_71410_x().field_71439_g.field_71069_bz.func_75139_a(i).func_75211_c();
            if (s.func_77973_b() != Items.field_190931_a && s.func_77973_b() instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor)s.func_77973_b();
                if (armor.field_77881_a == type) {
                    final float currentDamage = (float)(armor.field_77879_b + EnchantmentHelper.func_77506_a(Enchantments.field_180310_c, s));
                    final boolean cursed = binding && EnchantmentHelper.func_190938_b(s);
                    if (currentDamage > damage && !cursed) {
                        damage = currentDamage;
                        slot = i;
                    }
                }
            }
        }
        return slot;
    }
    
    public static int findArmorSlot(final EntityEquipmentSlot type, final boolean binding, final boolean withXCarry) {
        int slot = findArmorSlot(type, binding);
        if (slot == -1 && withXCarry) {
            float damage = 0.0f;
            for (int i = 1; i < 5; ++i) {
                final Slot craftingSlot = InventoryUtil.mc.field_71439_g.field_71069_bz.field_75151_b.get(i);
                final ItemStack craftingStack = craftingSlot.func_75211_c();
                if (craftingStack.func_77973_b() != Items.field_190931_a && craftingStack.func_77973_b() instanceof ItemArmor) {
                    final ItemArmor armor = (ItemArmor)craftingStack.func_77973_b();
                    if (armor.field_77881_a == type) {
                        final float currentDamage = (float)(armor.field_77879_b + EnchantmentHelper.func_77506_a(Enchantments.field_180310_c, craftingStack));
                        final boolean cursed = binding && EnchantmentHelper.func_190938_b(craftingStack);
                        if (currentDamage > damage && !cursed) {
                            damage = currentDamage;
                            slot = i;
                        }
                    }
                }
            }
        }
        return slot;
    }
    
    public static int findItemInventorySlot(final Item item, final boolean offHand, final boolean withXCarry) {
        int slot = findItemInventorySlot(item, offHand);
        if (slot == -1 && withXCarry) {
            for (int i = 1; i < 5; ++i) {
                final Slot craftingSlot = InventoryUtil.mc.field_71439_g.field_71069_bz.field_75151_b.get(i);
                final ItemStack craftingStack = craftingSlot.func_75211_c();
                if (craftingStack.func_77973_b() != Items.field_190931_a) {
                    final Item craftingStackItem = craftingStack.func_77973_b();
                    if (craftingStackItem == item) {
                        slot = i;
                    }
                }
            }
        }
        return slot;
    }
    
    public static int findBlockSlotInventory(final Class clazz, final boolean offHand, final boolean withXCarry) {
        int slot = findInventoryBlock(clazz, offHand);
        if (slot == -1 && withXCarry) {
            for (int i = 1; i < 5; ++i) {
                final Slot craftingSlot = InventoryUtil.mc.field_71439_g.field_71069_bz.field_75151_b.get(i);
                final ItemStack craftingStack = craftingSlot.func_75211_c();
                if (craftingStack.func_77973_b() != Items.field_190931_a) {
                    final Item craftingStackItem = craftingStack.func_77973_b();
                    if (clazz.isInstance(craftingStackItem)) {
                        slot = i;
                    }
                    else if (craftingStackItem instanceof ItemBlock) {
                        final Block block = ((ItemBlock)craftingStackItem).func_179223_d();
                        if (clazz.isInstance(block)) {
                            slot = i;
                        }
                    }
                }
            }
        }
        return slot;
    }
    
    public enum Switch
    {
        NORMAL, 
        SILENT, 
        NONE;
    }
    
    public static class Task
    {
        private final int slot;
        private final boolean update;
        private final boolean quickClick;
        
        public Task() {
            this.update = true;
            this.slot = -1;
            this.quickClick = false;
        }
        
        public Task(final int slot) {
            this.slot = slot;
            this.quickClick = false;
            this.update = false;
        }
        
        public Task(final int slot, final boolean quickClick) {
            this.slot = slot;
            this.quickClick = quickClick;
            this.update = false;
        }
        
        public void run() {
            if (this.update) {
                Util.mc.field_71442_b.func_78765_e();
            }
            if (this.slot != -1) {
                Util.mc.field_71442_b.func_187098_a(0, this.slot, 0, this.quickClick ? ClickType.QUICK_MOVE : ClickType.PICKUP, (EntityPlayer)Util.mc.field_71439_g);
            }
        }
        
        public boolean isSwitching() {
            return !this.update;
        }
    }
}
