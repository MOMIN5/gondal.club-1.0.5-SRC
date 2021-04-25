// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.CombatRules;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemArmor;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;

public class DamageUtil implements Util
{
    public static boolean isArmorLow(final EntityPlayer player, final int durability) {
        for (final ItemStack piece : player.field_71071_by.field_70460_b) {
            if (piece == null) {
                return true;
            }
            if (getItemDamage(piece) < durability) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isNaked(final EntityPlayer player) {
        for (final ItemStack piece : player.field_71071_by.field_70460_b) {
            if (piece != null) {
                if (piece.func_190926_b()) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }
    
    public static int getItemDamage(final ItemStack stack) {
        return stack.func_77958_k() - stack.func_77952_i();
    }
    
    public static float getDamageInPercent(final ItemStack stack) {
        return getItemDamage(stack) / (float)stack.func_77958_k() * 100.0f;
    }
    
    public static int getRoundedDamage(final ItemStack stack) {
        return (int)getDamageInPercent(stack);
    }
    
    public static boolean hasDurability(final ItemStack stack) {
        final Item item = stack.func_77973_b();
        return item instanceof ItemArmor || item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemShield;
    }
    
    public static boolean canBreakWeakness(final EntityPlayer player) {
        int strengthAmp = 0;
        final PotionEffect effect = DamageUtil.mc.field_71439_g.func_70660_b(MobEffects.field_76420_g);
        if (effect != null) {
            strengthAmp = effect.func_76458_c();
        }
        return !DamageUtil.mc.field_71439_g.func_70644_a(MobEffects.field_76437_t) || strengthAmp >= 1 || DamageUtil.mc.field_71439_g.func_184614_ca().func_77973_b() instanceof ItemSword || DamageUtil.mc.field_71439_g.func_184614_ca().func_77973_b() instanceof ItemPickaxe || DamageUtil.mc.field_71439_g.func_184614_ca().func_77973_b() instanceof ItemAxe || DamageUtil.mc.field_71439_g.func_184614_ca().func_77973_b() instanceof ItemSpade;
    }
    
    public static float calculateDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        final float doubleExplosionSize = 12.0f;
        final double distancedsize = entity.func_70011_f(posX, posY, posZ) / doubleExplosionSize;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = entity.field_70170_p.func_72842_a(vec3d, entity.func_174813_aQ());
        }
        catch (Exception ex) {}
        final double v = (1.0 - distancedsize) * blockDensity;
        final float damage = (float)(int)((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion((World)DamageUtil.mc.field_71441_e, (Entity)null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finald;
    }
    
    public static float getBlastReduction(final EntityLivingBase entity, final float damageI, final Explosion explosion) {
        float damage = damageI;
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer)entity;
            final DamageSource ds = DamageSource.func_94539_a(explosion);
            damage = CombatRules.func_189427_a(damage, (float)ep.func_70658_aO(), (float)ep.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
            int k = 0;
            try {
                k = EnchantmentHelper.func_77508_a(ep.func_184193_aE(), ds);
            }
            catch (Exception ex) {}
            final float f = MathHelper.func_76131_a((float)k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.func_70644_a(MobEffects.field_76429_m)) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.func_189427_a(damage, (float)entity.func_70658_aO(), (float)entity.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
        return damage;
    }
    
    public static float getDamageMultiplied(final float damage) {
        final int diff = DamageUtil.mc.field_71441_e.func_175659_aa().func_151525_a();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }
    
    public static float calculateDamage(final Entity crystal, final Entity entity) {
        return calculateDamage(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, entity);
    }
    
    public static float calculateDamageAlt(final Entity crystal, final Entity entity) {
        final BlockPos cPos = new BlockPos(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v);
        return calculateDamage(cPos.func_177958_n(), cPos.func_177956_o(), cPos.func_177952_p(), entity);
    }
    
    public static float calculateDamage(final BlockPos pos, final Entity entity) {
        return calculateDamage(pos.func_177958_n() + 0.5, pos.func_177956_o() + 1, pos.func_177952_p() + 0.5, entity);
    }
    
    public static boolean canTakeDamage(final boolean suicide) {
        return !DamageUtil.mc.field_71439_g.field_71075_bZ.field_75098_d && !suicide;
    }
    
    public static int getCooldownByWeapon(final EntityPlayer player) {
        final Item item = player.func_184614_ca().func_77973_b();
        if (item instanceof ItemSword) {
            return 600;
        }
        if (item instanceof ItemPickaxe) {
            return 850;
        }
        if (item == Items.field_151036_c) {
            return 1100;
        }
        if (item == Items.field_151018_J) {
            return 500;
        }
        if (item == Items.field_151019_K) {
            return 350;
        }
        if (item == Items.field_151053_p || item == Items.field_151049_t) {
            return 1250;
        }
        if (item instanceof ItemSpade || item == Items.field_151006_E || item == Items.field_151056_x || item == Items.field_151017_I || item == Items.field_151013_M) {
            return 1000;
        }
        return 250;
    }
}
