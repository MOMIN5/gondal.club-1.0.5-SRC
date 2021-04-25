// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.player;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEndCrystal;
import com.esoterik.client.util.InventoryUtil;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class FastPlace extends Module
{
    private final Setting<Boolean> all;
    private BlockPos mousePos;
    
    public FastPlace() {
        super("FastPlace", "Fast everything.", Category.PLAYER, true, false, false);
        this.all = (Setting<Boolean>)this.register(new Setting("AllItems", (T)false));
        this.mousePos = null;
    }
    
    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (this.all.getValue()) {
            FastPlace.mc.field_71467_ac = 0;
        }
        if (InventoryUtil.holdingItem(ItemExpBottle.class)) {
            FastPlace.mc.field_71467_ac = 0;
        }
        if (InventoryUtil.holdingItem(ItemEndCrystal.class)) {
            FastPlace.mc.field_71467_ac = 0;
        }
        if (FastPlace.mc.field_71474_y.field_74313_G.func_151470_d()) {
            final boolean bl;
            final boolean offhand = bl = (FastPlace.mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_185158_cP);
            if (offhand || FastPlace.mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_185158_cP) {
                final RayTraceResult result = FastPlace.mc.field_71476_x;
                if (result == null) {
                    return;
                }
                switch (result.field_72313_a) {
                    case MISS: {
                        this.mousePos = null;
                        break;
                    }
                    case BLOCK: {
                        this.mousePos = FastPlace.mc.field_71476_x.func_178782_a();
                        break;
                    }
                    case ENTITY: {
                        final Entity entity;
                        if (this.mousePos == null || (entity = result.field_72308_g) == null) {
                            break;
                        }
                        if (!this.mousePos.equals((Object)new BlockPos(entity.field_70165_t, entity.field_70163_u - 1.0, entity.field_70161_v))) {
                            break;
                        }
                        FastPlace.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayerTryUseItemOnBlock(this.mousePos, EnumFacing.DOWN, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                        break;
                    }
                }
            }
        }
    }
}
