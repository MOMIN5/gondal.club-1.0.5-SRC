// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import com.esoterik.client.features.modules.client.Managers;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.AxisAlignedBB;
import com.esoterik.client.features.modules.player.FakePlayer;
import com.esoterik.client.features.modules.player.Freecam;
import java.awt.Color;
import net.minecraft.util.MovementInput;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Objects;
import net.minecraft.potion.Potion;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;
import java.util.Collections;
import net.minecraft.util.math.MathHelper;
import java.util.Collection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockAir;
import java.util.ArrayList;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityPigZombie;
import java.util.List;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.EnumHand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.util.CombatRules;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.DamageSource;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.esohack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.math.Vec3d;

public class EntityUtil implements Util
{
    public static final Vec3d[] antiDropOffsetList;
    public static final Vec3d[] platformOffsetList;
    public static final Vec3d[] legOffsetList;
    public static final Vec3d[] doubleLegOffsetList;
    public static final Vec3d[] OffsetList;
    public static final Vec3d[] headpiece;
    public static final Vec3d[] offsetsNoHead;
    public static final Vec3d[] antiStepOffsetList;
    public static final Vec3d[] antiScaffoldOffsetList;
    
    public static boolean isCrystalAtFeet(final EntityEnderCrystal crystal, final double range) {
        for (final EntityPlayer player : EntityUtil.mc.field_71441_e.field_73010_i) {
            if (EntityUtil.mc.field_71439_g.func_70068_e((Entity)player) > range * range) {
                continue;
            }
            if (esohack.friendManager.isFriend(player)) {
                continue;
            }
            for (final Vec3d vec : EntityUtil.doubleLegOffsetList) {
                if (new BlockPos(player.func_174791_d()).func_177963_a(vec.field_72450_a, vec.field_72448_b, vec.field_72449_c) == crystal.func_180425_c()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static float getDifficultyMultiplier(final float distance) {
        switch (EntityUtil.mc.field_71441_e.func_175659_aa()) {
            case HARD: {
                return distance * 3.0f / 2.0f;
            }
            case PEACEFUL: {
                return 0.0f;
            }
            case EASY: {
                return Math.min(distance / 2.0f + 1.0f, distance);
            }
            default: {
                return distance;
            }
        }
    }
    
    public static int getEnchantmentModifierDamage(final Iterable<ItemStack> stacks, final DamageSource source) {
        int modifier = 0;
        for (final ItemStack stack : stacks) {
            final NBTTagList nbttaglist = stack.func_77986_q();
            for (int i = 0; i < nbttaglist.func_74745_c(); ++i) {
                final Enchantment enchantment = Enchantment.func_185262_c((int)nbttaglist.func_150305_b(i).func_74765_d("id"));
                if (enchantment != null) {
                    if (enchantment instanceof EnchantmentProtection) {
                        modifier += enchantment.func_77318_a((int)nbttaglist.func_150305_b(i).func_74765_d("lvl"), source);
                    }
                }
            }
        }
        return modifier;
    }
    
    public static float calculate(final double x, final double y, final double z, final EntityLivingBase base) {
        double distance = base.func_70092_e(x, y, z) / 144.0;
        if (distance > 1.0) {
            return 0.0f;
        }
        final double densityDistance;
        distance = (densityDistance = (1.0 - distance) * EntityUtil.mc.field_71441_e.func_72842_a(new Vec3d(x, y, z), base.func_174813_aQ()));
        float damage = getDifficultyMultiplier((float)((densityDistance * densityDistance + distance) / 2.0 * 7.0 * 12.0 + 1.0));
        final DamageSource damageSource = DamageSource.func_94539_a(new Explosion((World)EntityUtil.mc.field_71441_e, (Entity)EntityUtil.mc.field_71439_g, x, y, z, 6.0f, false, true));
        damage = CombatRules.func_189427_a(damage, (float)base.func_70658_aO(), (float)base.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
        final int modifierDamage = getEnchantmentModifierDamage(base.func_184193_aE(), damageSource);
        if (modifierDamage > 0) {
            damage = CombatRules.func_188401_b(damage, (float)modifierDamage);
        }
        final PotionEffect resistance;
        if ((resistance = base.func_70660_b(MobEffects.field_76429_m)) != null) {
            damage = damage * (25 - (resistance.func_76458_c() + 1) * 5) / 25.0f;
        }
        return Math.max(damage, 0.0f);
    }
    
    public static void attackEntity(final Entity entity, final boolean packet, final boolean swingArm) {
        if (packet) {
            EntityUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketUseEntity(entity));
        }
        else {
            EntityUtil.mc.field_71442_b.func_78764_a((EntityPlayer)EntityUtil.mc.field_71439_g, entity);
        }
        if (swingArm) {
            EntityUtil.mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
        }
    }
    
    public static double[] calculateLookAt(final double px, final double py, final double pz, final EntityPlayer me) {
        double dirx = me.field_70165_t - px;
        double diry = me.field_70163_u - py;
        double dirz = me.field_70161_v - pz;
        final double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        pitch = pitch * 180.0 / 3.141592653589793;
        yaw = yaw * 180.0 / 3.141592653589793;
        yaw += 90.0;
        return new double[] { yaw, pitch };
    }
    
    public static Vec3d interpolateEntity(final Entity entity, final float time) {
        return new Vec3d(entity.field_70142_S + (entity.field_70165_t - entity.field_70142_S) * time, entity.field_70137_T + (entity.field_70163_u - entity.field_70137_T) * time, entity.field_70136_U + (entity.field_70161_v - entity.field_70136_U) * time);
    }
    
    public static Vec3d getInterpolatedPos(final Entity entity, final float partialTicks) {
        return new Vec3d(entity.field_70142_S, entity.field_70137_T, entity.field_70136_U).func_178787_e(getInterpolatedAmount(entity, partialTicks));
    }
    
    public static Vec3d getInterpolatedRenderPos(final Entity entity, final float partialTicks) {
        return getInterpolatedPos(entity, partialTicks).func_178786_a(EntityUtil.mc.func_175598_ae().field_78725_b, EntityUtil.mc.func_175598_ae().field_78726_c, EntityUtil.mc.func_175598_ae().field_78723_d);
    }
    
    public static Vec3d getInterpolatedRenderPos(final Vec3d vec) {
        return new Vec3d(vec.field_72450_a, vec.field_72448_b, vec.field_72449_c).func_178786_a(EntityUtil.mc.func_175598_ae().field_78725_b, EntityUtil.mc.func_175598_ae().field_78726_c, EntityUtil.mc.func_175598_ae().field_78723_d);
    }
    
    public static Vec3d getInterpolatedAmount(final Entity entity, final double x, final double y, final double z) {
        return new Vec3d((entity.field_70165_t - entity.field_70142_S) * x, (entity.field_70163_u - entity.field_70137_T) * y, (entity.field_70161_v - entity.field_70136_U) * z);
    }
    
    public static Vec3d getInterpolatedAmount(final Entity entity, final Vec3d vec) {
        return getInterpolatedAmount(entity, vec.field_72450_a, vec.field_72448_b, vec.field_72449_c);
    }
    
    public static Vec3d getInterpolatedAmount(final Entity entity, final float partialTicks) {
        return getInterpolatedAmount(entity, partialTicks, partialTicks, partialTicks);
    }
    
    public static boolean isPassive(final Entity entity) {
        return (!(entity instanceof EntityWolf) || !((EntityWolf)entity).func_70919_bu()) && (entity instanceof EntityAgeable || entity instanceof EntityAmbientCreature || entity instanceof EntitySquid || (entity instanceof EntityIronGolem && ((EntityIronGolem)entity).func_70643_av() == null));
    }
    
    public static boolean isSafe(final Entity entity, final int height, final boolean floor, final boolean face) {
        return getUnsafeBlocks(entity, height, floor, face).size() == 0;
    }
    
    public static boolean isSafe(final Entity entity, final int height, final boolean floor) {
        return getUnsafeBlocks(entity, height, floor).size() == 0;
    }
    
    public static boolean stopSneaking(final boolean isSneaking) {
        if (isSneaking && EntityUtil.mc.field_71439_g != null) {
            EntityUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketEntityAction((Entity)EntityUtil.mc.field_71439_g, CPacketEntityAction.Action.STOP_SNEAKING));
        }
        return false;
    }
    
    public static boolean isSafe(final Entity entity) {
        return isSafe(entity, 0, false, true);
    }
    
    public static BlockPos getPlayerPos(final EntityPlayer player) {
        return new BlockPos(Math.floor(player.field_70165_t), Math.floor(player.field_70163_u), Math.floor(player.field_70161_v));
    }
    
    public static List<Vec3d> getUnsafeBlocks(final Entity entity, final int height, final boolean floor, final boolean face) {
        return getUnsafeBlocksFromVec3d(entity.func_174791_d(), height, floor, face);
    }
    
    public static List<Vec3d> getUnsafeBlocks(final Entity entity, final int height, final boolean floor) {
        return getUnsafeBlocksFromVec3d(entity.func_174791_d(), height, floor);
    }
    
    public static boolean isMobAggressive(final Entity entity) {
        if (entity instanceof EntityPigZombie) {
            if (((EntityPigZombie)entity).func_184734_db() || ((EntityPigZombie)entity).func_175457_ck()) {
                return true;
            }
        }
        else {
            if (entity instanceof EntityWolf) {
                return ((EntityWolf)entity).func_70919_bu() && !EntityUtil.mc.field_71439_g.equals((Object)((EntityWolf)entity).func_70902_q());
            }
            if (entity instanceof EntityEnderman) {
                return ((EntityEnderman)entity).func_70823_r();
            }
        }
        return isHostileMob(entity);
    }
    
    public static boolean isNeutralMob(final Entity entity) {
        return entity instanceof EntityPigZombie || entity instanceof EntityWolf || entity instanceof EntityEnderman;
    }
    
    public static boolean isProjectile(final Entity entity) {
        return entity instanceof EntityShulkerBullet || entity instanceof EntityFireball;
    }
    
    public static boolean isVehicle(final Entity entity) {
        return entity instanceof EntityBoat || entity instanceof EntityMinecart;
    }
    
    public static boolean isFriendlyMob(final Entity entity) {
        return (entity.isCreatureType(EnumCreatureType.CREATURE, false) && !isNeutralMob(entity)) || entity.isCreatureType(EnumCreatureType.AMBIENT, false) || entity instanceof EntityVillager || entity instanceof EntityIronGolem || (isNeutralMob(entity) && !isMobAggressive(entity));
    }
    
    public static boolean isHostileMob(final Entity entity) {
        return entity.isCreatureType(EnumCreatureType.MONSTER, false) && !isNeutralMob(entity);
    }
    
    public static List<Vec3d> getUnsafeBlocksFromVec3d(final Vec3d pos, final int height, final boolean floor) {
        final List<Vec3d> vec3ds = new ArrayList<Vec3d>();
        for (final Vec3d vector : getOffsets(height, floor)) {
            final BlockPos targetPos = new BlockPos(pos).func_177963_a(vector.field_72450_a, vector.field_72448_b, vector.field_72449_c);
            final Block block = EntityUtil.mc.field_71441_e.func_180495_p(targetPos).func_177230_c();
            if (block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow) {
                vec3ds.add(vector);
            }
        }
        return vec3ds;
    }
    
    public static List<Vec3d> getUnsafeBlocksFromVec3d(final Vec3d pos, final int height, final boolean floor, final boolean face) {
        final List<Vec3d> vec3ds = new ArrayList<Vec3d>();
        for (final Vec3d vector : getOffsets(height, floor, face)) {
            final BlockPos targetPos = new BlockPos(pos).func_177963_a(vector.field_72450_a, vector.field_72448_b, vector.field_72449_c);
            final Block block = EntityUtil.mc.field_71441_e.func_180495_p(targetPos).func_177230_c();
            if (block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow) {
                vec3ds.add(vector);
            }
        }
        return vec3ds;
    }
    
    public static boolean isInHole(final Entity entity) {
        return isBlockValid(new BlockPos(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v));
    }
    
    public static boolean isBlockValid(final BlockPos blockPos) {
        return isBedrockHole(blockPos) || isObbyHole(blockPos) || isBothHole(blockPos);
    }
    
    public static void swingArmNoPacket(final EnumHand hand, final EntityLivingBase entity) {
        final ItemStack stack = entity.func_184586_b(hand);
        if (!stack.func_190926_b() && stack.func_77973_b().onEntitySwing(entity, stack)) {
            return;
        }
        if (!entity.field_82175_bq || entity.field_110158_av >= getArmSwingAnimationEnd(entity) / 2 || entity.field_110158_av < 0) {
            entity.field_110158_av = -1;
            entity.field_82175_bq = true;
            entity.field_184622_au = hand;
        }
    }
    
    public static int getArmSwingAnimationEnd(final EntityLivingBase entity) {
        if (entity.func_70644_a(MobEffects.field_76422_e)) {
            return 6 - (1 + entity.func_70660_b(MobEffects.field_76422_e).func_76458_c());
        }
        return entity.func_70644_a(MobEffects.field_76419_f) ? (6 + (1 + entity.func_70660_b(MobEffects.field_76419_f).func_76458_c()) * 2) : 6;
    }
    
    public static boolean isObbyHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[] { blockPos.func_177978_c(), blockPos.func_177968_d(), blockPos.func_177974_f(), blockPos.func_177976_e(), blockPos.func_177977_b() };
        for (final BlockPos pos : array) {
            final IBlockState touchingState = EntityUtil.mc.field_71441_e.func_180495_p(pos);
            if (touchingState.func_177230_c() == Blocks.field_150350_a || touchingState.func_177230_c() != Blocks.field_150343_Z) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isBedrockHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[] { blockPos.func_177978_c(), blockPos.func_177968_d(), blockPos.func_177974_f(), blockPos.func_177976_e(), blockPos.func_177977_b() };
        for (final BlockPos pos : array) {
            final IBlockState touchingState = EntityUtil.mc.field_71441_e.func_180495_p(pos);
            if (touchingState.func_177230_c() == Blocks.field_150350_a || touchingState.func_177230_c() != Blocks.field_150357_h) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isBothHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[] { blockPos.func_177978_c(), blockPos.func_177968_d(), blockPos.func_177974_f(), blockPos.func_177976_e(), blockPos.func_177977_b() };
        for (final BlockPos pos : array) {
            final IBlockState touchingState = EntityUtil.mc.field_71441_e.func_180495_p(pos);
            if (touchingState.func_177230_c() == Blocks.field_150350_a || (touchingState.func_177230_c() != Blocks.field_150357_h && touchingState.func_177230_c() != Blocks.field_150343_Z)) {
                return false;
            }
        }
        return true;
    }
    
    public static Vec3d[] getUnsafeBlockArray(final Entity entity, final int height, final boolean floor, final boolean face) {
        final List<Vec3d> list = getUnsafeBlocks(entity, height, floor, face);
        final Vec3d[] array = new Vec3d[list.size()];
        return list.toArray(array);
    }
    
    public static Vec3d[] getUnsafeBlockArray(final Entity entity, final int height, final boolean floor) {
        final List<Vec3d> list = getUnsafeBlocks(entity, height, floor);
        final Vec3d[] array = new Vec3d[list.size()];
        return list.toArray(array);
    }
    
    public static Vec3d[] getUnsafeBlockArrayFromVec3d(final Vec3d pos, final int height, final boolean floor, final boolean face) {
        final List<Vec3d> list = getUnsafeBlocksFromVec3d(pos, height, floor, face);
        final Vec3d[] array = new Vec3d[list.size()];
        return list.toArray(array);
    }
    
    public static Vec3d[] getUnsafeBlockArrayFromVec3d(final Vec3d pos, final int height, final boolean floor) {
        final List<Vec3d> list = getUnsafeBlocksFromVec3d(pos, height, floor);
        final Vec3d[] array = new Vec3d[list.size()];
        return list.toArray(array);
    }
    
    public static double getDst(final Vec3d vec) {
        return EntityUtil.mc.field_71439_g.func_174791_d().func_72438_d(vec);
    }
    
    public static boolean isTrapped(final EntityPlayer player, final boolean antiScaffold, final boolean antiStep, final boolean legs, final boolean platform, final boolean antiDrop, final boolean face) {
        return getUntrappedBlocks(player, antiScaffold, antiStep, legs, platform, antiDrop, face).size() == 0;
    }
    
    public static boolean isTrappedExtended(final int extension, final EntityPlayer player, final boolean antiScaffold, final boolean antiStep, final boolean legs, final boolean platform, final boolean antiDrop, final boolean raytrace, final boolean noScaffoldExtend, final boolean face) {
        return getUntrappedBlocksExtended(extension, player, antiScaffold, antiStep, legs, platform, antiDrop, raytrace, noScaffoldExtend, face).size() == 0;
    }
    
    public static List<Vec3d> getUntrappedBlocks(final EntityPlayer player, final boolean antiScaffold, final boolean antiStep, final boolean legs, final boolean platform, final boolean antiDrop, final boolean face) {
        final List<Vec3d> vec3ds = new ArrayList<Vec3d>();
        if (!antiStep && getUnsafeBlocks((Entity)player, 2, false, face).size() == 4) {
            vec3ds.addAll(getUnsafeBlocks((Entity)player, 2, false, face));
        }
        for (int i = 0; i < getTrapOffsets(antiScaffold, antiStep, legs, platform, antiDrop, face).length; ++i) {
            final Vec3d vector = getTrapOffsets(antiScaffold, antiStep, legs, platform, antiDrop, face)[i];
            final BlockPos targetPos = new BlockPos(player.func_174791_d()).func_177963_a(vector.field_72450_a, vector.field_72448_b, vector.field_72449_c);
            final Block block = EntityUtil.mc.field_71441_e.func_180495_p(targetPos).func_177230_c();
            if (block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow) {
                vec3ds.add(vector);
            }
        }
        return vec3ds;
    }
    
    public static boolean isInWater(final Entity entity) {
        if (entity == null) {
            return false;
        }
        final double y = entity.field_70163_u + 0.01;
        for (int x = MathHelper.func_76128_c(entity.field_70165_t); x < MathHelper.func_76143_f(entity.field_70165_t); ++x) {
            for (int z = MathHelper.func_76128_c(entity.field_70161_v); z < MathHelper.func_76143_f(entity.field_70161_v); ++z) {
                final BlockPos pos = new BlockPos(x, (int)y, z);
                if (EntityUtil.mc.field_71441_e.func_180495_p(pos).func_177230_c() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean isDrivenByPlayer(final Entity entityIn) {
        return EntityUtil.mc.field_71439_g != null && entityIn != null && entityIn.equals((Object)EntityUtil.mc.field_71439_g.func_184187_bx());
    }
    
    public static boolean isPlayer(final Entity entity) {
        return entity instanceof EntityPlayer;
    }
    
    public static boolean isAboveWater(final Entity entity) {
        return isAboveWater(entity, false);
    }
    
    public static boolean isAboveWater(final Entity entity, final boolean packet) {
        if (entity == null) {
            return false;
        }
        final double y = entity.field_70163_u - (packet ? 0.03 : (isPlayer(entity) ? 0.2 : 0.5));
        for (int x = MathHelper.func_76128_c(entity.field_70165_t); x < MathHelper.func_76143_f(entity.field_70165_t); ++x) {
            for (int z = MathHelper.func_76128_c(entity.field_70161_v); z < MathHelper.func_76143_f(entity.field_70161_v); ++z) {
                final BlockPos pos = new BlockPos(x, MathHelper.func_76128_c(y), z);
                if (EntityUtil.mc.field_71441_e.func_180495_p(pos).func_177230_c() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static List<Vec3d> getUntrappedBlocksExtended(final int extension, final EntityPlayer player, final boolean antiScaffold, final boolean antiStep, final boolean legs, final boolean platform, final boolean antiDrop, final boolean raytrace, final boolean noScaffoldExtend, final boolean face) {
        final List<Vec3d> placeTargets = new ArrayList<Vec3d>();
        if (extension == 1) {
            placeTargets.addAll(targets(player.func_174791_d(), antiScaffold, antiStep, legs, platform, antiDrop, raytrace, face));
        }
        else {
            int extend = 1;
            for (final Vec3d vec3d : MathUtil.getBlockBlocks((Entity)player)) {
                if (extend > extension) {
                    break;
                }
                placeTargets.addAll(targets(vec3d, !noScaffoldExtend, antiStep, legs, platform, antiDrop, raytrace, face));
                ++extend;
            }
        }
        final List<Vec3d> removeList = new ArrayList<Vec3d>();
        for (final Vec3d vec3d : placeTargets) {
            final BlockPos pos = new BlockPos(vec3d);
            if (BlockUtil.isPositionPlaceable(pos, raytrace) == -1) {
                removeList.add(vec3d);
            }
        }
        for (final Vec3d vec3d : removeList) {
            placeTargets.remove(vec3d);
        }
        return placeTargets;
    }
    
    public static List<Vec3d> targets(final Vec3d vec3d, final boolean antiScaffold, final boolean antiStep, final boolean legs, final boolean platform, final boolean antiDrop, final boolean raytrace) {
        final List<Vec3d> placeTargets = new ArrayList<Vec3d>();
        if (antiDrop) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.antiDropOffsetList));
        }
        if (platform) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.platformOffsetList));
        }
        if (legs) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.legOffsetList));
        }
        Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.OffsetList));
        if (antiStep) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.antiStepOffsetList));
        }
        else {
            final List<Vec3d> vec3ds = getUnsafeBlocksFromVec3d(vec3d, 2, false);
            if (vec3ds.size() == 4) {
                for (final Vec3d vector : vec3ds) {
                    final BlockPos position = new BlockPos(vec3d).func_177963_a(vector.field_72450_a, vector.field_72448_b, vector.field_72449_c);
                    switch (BlockUtil.isPositionPlaceable(position, raytrace)) {
                        case -1:
                        case 1:
                        case 2: {
                            continue;
                        }
                        case 3: {
                            placeTargets.add(vec3d.func_178787_e(vector));
                            break;
                        }
                    }
                    break;
                }
            }
        }
        if (antiScaffold) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.antiScaffoldOffsetList));
        }
        return placeTargets;
    }
    
    public static List<Vec3d> targets(final Vec3d vec3d, final boolean antiScaffold, final boolean antiStep, final boolean legs, final boolean platform, final boolean antiDrop, final boolean raytrace, final boolean face) {
        final List<Vec3d> placeTargets = new ArrayList<Vec3d>();
        if (antiDrop) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.antiDropOffsetList));
        }
        if (platform) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.platformOffsetList));
        }
        if (legs) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.legOffsetList));
        }
        Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.OffsetList));
        if (antiStep) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.antiStepOffsetList));
        }
        else {
            final List<Vec3d> vec3ds = getUnsafeBlocksFromVec3d(vec3d, 2, false, face);
            if (vec3ds.size() == 4) {
                for (final Vec3d vector : vec3ds) {
                    final BlockPos position = new BlockPos(vec3d).func_177963_a(vector.field_72450_a, vector.field_72448_b, vector.field_72449_c);
                    switch (BlockUtil.isPositionPlaceable(position, raytrace)) {
                        case -1:
                        case 1:
                        case 2: {
                            continue;
                        }
                        case 3: {
                            placeTargets.add(vec3d.func_178787_e(vector));
                            break;
                        }
                    }
                    break;
                }
            }
        }
        if (antiScaffold) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, EntityUtil.antiScaffoldOffsetList));
        }
        return placeTargets;
    }
    
    public static List<Vec3d> getOffsetList(final int y, final boolean floor) {
        final List<Vec3d> offsets = new ArrayList<Vec3d>();
        offsets.add(new Vec3d(-1.0, (double)y, 0.0));
        offsets.add(new Vec3d(1.0, (double)y, 0.0));
        offsets.add(new Vec3d(0.0, (double)y, -1.0));
        offsets.add(new Vec3d(0.0, (double)y, 1.0));
        if (floor) {
            offsets.add(new Vec3d(0.0, (double)(y - 1), 0.0));
        }
        return offsets;
    }
    
    public static List<Vec3d> getOffsetList(final int y, final boolean floor, final boolean face) {
        final List<Vec3d> offsets = new ArrayList<Vec3d>();
        if (face) {
            offsets.add(new Vec3d(-1.0, (double)y, 0.0));
            offsets.add(new Vec3d(1.0, (double)y, 0.0));
            offsets.add(new Vec3d(0.0, (double)y, -1.0));
            offsets.add(new Vec3d(0.0, (double)y, 1.0));
        }
        else {
            offsets.add(new Vec3d(-1.0, (double)y, 0.0));
        }
        if (floor) {
            offsets.add(new Vec3d(0.0, (double)(y - 1), 0.0));
        }
        return offsets;
    }
    
    public static Vec3d[] getOffsets(final int y, final boolean floor) {
        final List<Vec3d> offsets = getOffsetList(y, floor);
        final Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }
    
    public static Vec3d[] getOffsets(final int y, final boolean floor, final boolean face) {
        final List<Vec3d> offsets = getOffsetList(y, floor, face);
        final Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }
    
    public static Vec3d[] getTrapOffsets(final boolean antiScaffold, final boolean antiStep, final boolean legs, final boolean platform, final boolean antiDrop, final boolean face) {
        final List<Vec3d> offsets = getTrapOffsetsList(antiScaffold, antiStep, legs, platform, antiDrop, face);
        final Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }
    
    public static List<Vec3d> getTrapOffsetsList(final boolean antiScaffold, final boolean antiStep, final boolean legs, final boolean platform, final boolean antiDrop, final boolean face) {
        final List<Vec3d> offsets = new ArrayList<Vec3d>(getOffsetList(1, false, face));
        offsets.add(new Vec3d(0.0, 2.0, 0.0));
        if (antiScaffold) {
            offsets.add(new Vec3d(0.0, 3.0, 0.0));
        }
        if (antiStep) {
            offsets.addAll(getOffsetList(2, false, face));
        }
        if (legs) {
            offsets.addAll(getOffsetList(0, false, face));
        }
        if (platform) {
            offsets.addAll(getOffsetList(-1, false, face));
            offsets.add(new Vec3d(0.0, -1.0, 0.0));
        }
        if (antiDrop) {
            offsets.add(new Vec3d(0.0, -2.0, 0.0));
        }
        return offsets;
    }
    
    public static Vec3d[] getHeightOffsets(final int min, final int max) {
        final List<Vec3d> offsets = new ArrayList<Vec3d>();
        for (int i = min; i <= max; ++i) {
            offsets.add(new Vec3d(0.0, (double)i, 0.0));
        }
        final Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }
    
    public static BlockPos getRoundedBlockPos(final Entity entity) {
        return new BlockPos(MathUtil.roundVec(entity.func_174791_d(), 0));
    }
    
    public static boolean isLiving(final Entity entity) {
        return entity instanceof EntityLivingBase;
    }
    
    public static boolean isAlive(final Entity entity) {
        return isLiving(entity) && !entity.field_70128_L && ((EntityLivingBase)entity).func_110143_aJ() > 0.0f;
    }
    
    public static boolean isDead(final Entity entity) {
        return !isAlive(entity);
    }
    
    public static float getHealth(final Entity entity) {
        if (isLiving(entity)) {
            final EntityLivingBase livingBase = (EntityLivingBase)entity;
            return livingBase.func_110143_aJ() + livingBase.func_110139_bj();
        }
        return 0.0f;
    }
    
    public static float getHealth(final Entity entity, final boolean absorption) {
        if (isLiving(entity)) {
            final EntityLivingBase livingBase = (EntityLivingBase)entity;
            return livingBase.func_110143_aJ() + (absorption ? livingBase.func_110139_bj() : 0.0f);
        }
        return 0.0f;
    }
    
    public static boolean canEntityFeetBeSeen(final Entity entityIn) {
        return EntityUtil.mc.field_71441_e.func_147447_a(new Vec3d(EntityUtil.mc.field_71439_g.field_70165_t, EntityUtil.mc.field_71439_g.field_70165_t + EntityUtil.mc.field_71439_g.func_70047_e(), EntityUtil.mc.field_71439_g.field_70161_v), new Vec3d(entityIn.field_70165_t, entityIn.field_70163_u, entityIn.field_70161_v), false, true, false) == null;
    }
    
    public static boolean isntValid(final Entity entity, final double range) {
        return entity == null || isDead(entity) || entity.equals((Object)EntityUtil.mc.field_71439_g) || (entity instanceof EntityPlayer && esohack.friendManager.isFriend(entity.func_70005_c_())) || EntityUtil.mc.field_71439_g.func_70068_e(entity) > MathUtil.square(range);
    }
    
    public static boolean isValid(final Entity entity, final double range) {
        return !isntValid(entity, range);
    }
    
    public static boolean holdingWeapon(final EntityPlayer player) {
        return player.func_184614_ca().func_77973_b() instanceof ItemSword || player.func_184614_ca().func_77973_b() instanceof ItemAxe;
    }
    
    public static double getMaxSpeed() {
        double maxModifier = 0.2873;
        if (EntityUtil.mc.field_71439_g.func_70644_a((Potion)Objects.requireNonNull(Potion.func_188412_a(1)))) {
            maxModifier *= 1.0 + 0.2 * (Objects.requireNonNull(EntityUtil.mc.field_71439_g.func_70660_b((Potion)Objects.requireNonNull(Potion.func_188412_a(1)))).func_76458_c() + 1);
        }
        return maxModifier;
    }
    
    public static void mutliplyEntitySpeed(final Entity entity, final double multiplier) {
        if (entity != null) {
            entity.field_70159_w *= multiplier;
            entity.field_70179_y *= multiplier;
        }
    }
    
    public static boolean isEntityMoving(final Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof EntityPlayer) {
            return EntityUtil.mc.field_71474_y.field_74351_w.func_151470_d() || EntityUtil.mc.field_71474_y.field_74368_y.func_151470_d() || EntityUtil.mc.field_71474_y.field_74370_x.func_151470_d() || EntityUtil.mc.field_71474_y.field_74366_z.func_151470_d();
        }
        return entity.field_70159_w != 0.0 || entity.field_70181_x != 0.0 || entity.field_70179_y != 0.0;
    }
    
    public static double getEntitySpeed(final Entity entity) {
        if (entity != null) {
            final double distTraveledLastTickX = entity.field_70165_t - entity.field_70169_q;
            final double distTraveledLastTickZ = entity.field_70161_v - entity.field_70166_s;
            final double speed = MathHelper.func_76133_a(distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ);
            return speed * 20.0;
        }
        return 0.0;
    }
    
    public static boolean holding32k(final EntityPlayer player) {
        return is32k(player.func_184614_ca());
    }
    
    public static boolean is32k(final ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.func_77978_p() == null) {
            return false;
        }
        final NBTTagList enchants = (NBTTagList)stack.func_77978_p().func_74781_a("ench");
        if (enchants == null) {
            return false;
        }
        int i = 0;
        while (i < enchants.func_74745_c()) {
            final NBTTagCompound enchant = enchants.func_150305_b(i);
            if (enchant.func_74762_e("id") == 16) {
                final int lvl = enchant.func_74762_e("lvl");
                if (lvl >= 42) {
                    return true;
                }
                break;
            }
            else {
                ++i;
            }
        }
        return false;
    }
    
    public static boolean simpleIs32k(final ItemStack stack) {
        return EnchantmentHelper.func_77506_a(Enchantments.field_185302_k, stack) >= 1000;
    }
    
    public static void moveEntityStrafe(final double speed, final Entity entity) {
        if (entity != null) {
            final MovementInput movementInput = EntityUtil.mc.field_71439_g.field_71158_b;
            double forward = movementInput.field_192832_b;
            double strafe = movementInput.field_78902_a;
            float yaw = EntityUtil.mc.field_71439_g.field_70177_z;
            if (forward == 0.0 && strafe == 0.0) {
                entity.field_70159_w = 0.0;
                entity.field_70179_y = 0.0;
            }
            else {
                if (forward != 0.0) {
                    if (strafe > 0.0) {
                        yaw += ((forward > 0.0) ? -45 : 45);
                    }
                    else if (strafe < 0.0) {
                        yaw += ((forward > 0.0) ? 45 : -45);
                    }
                    strafe = 0.0;
                    if (forward > 0.0) {
                        forward = 1.0;
                    }
                    else if (forward < 0.0) {
                        forward = -1.0;
                    }
                }
                entity.field_70159_w = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f));
                entity.field_70179_y = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f));
            }
        }
    }
    
    public static boolean rayTraceHitCheck(final Entity entity, final boolean shouldCheck) {
        return !shouldCheck || EntityUtil.mc.field_71439_g.func_70685_l(entity);
    }
    
    public static Color getColor(final Entity entity, final int red, final int green, final int blue, final int alpha, final boolean colorFriends) {
        Color color = new Color(red / 255.0f, green / 255.0f, blue / 255.0f, alpha / 255.0f);
        if (entity instanceof EntityPlayer && colorFriends && esohack.friendManager.isFriend((EntityPlayer)entity)) {
            color = new Color(0.33333334f, 1.0f, 1.0f, alpha / 255.0f);
        }
        return color;
    }
    
    public static boolean isFakePlayer(final EntityPlayer player) {
        final Freecam freecam = Freecam.getInstance();
        final FakePlayer fakePlayer = FakePlayer.getInstance();
        final int playerID = player.func_145782_y();
        if (freecam.isOn() && playerID == 69420) {
            return true;
        }
        if (fakePlayer.isOn()) {
            for (final int id : fakePlayer.fakePlayerIdList) {
                if (id == playerID) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean isMoving() {
        return EntityUtil.mc.field_71439_g.field_191988_bg != 0.0 || EntityUtil.mc.field_71439_g.field_70702_br != 0.0;
    }
    
    public static EntityPlayer getClosestEnemy(final double distance) {
        EntityPlayer closest = null;
        for (final EntityPlayer player : EntityUtil.mc.field_71441_e.field_73010_i) {
            if (isntValid((Entity)player, distance)) {
                continue;
            }
            if (closest == null) {
                closest = player;
            }
            else {
                if (EntityUtil.mc.field_71439_g.func_70068_e((Entity)player) >= EntityUtil.mc.field_71439_g.func_70068_e((Entity)closest)) {
                    continue;
                }
                closest = player;
            }
        }
        return closest;
    }
    
    public static boolean checkCollide() {
        return !EntityUtil.mc.field_71439_g.func_70093_af() && (EntityUtil.mc.field_71439_g.func_184187_bx() == null || EntityUtil.mc.field_71439_g.func_184187_bx().field_70143_R < 3.0f) && EntityUtil.mc.field_71439_g.field_70143_R < 3.0f;
    }
    
    public static boolean isInLiquid() {
        if (EntityUtil.mc.field_71439_g.field_70143_R >= 3.0f) {
            return false;
        }
        boolean inLiquid = false;
        final AxisAlignedBB bb = (EntityUtil.mc.field_71439_g.func_184187_bx() != null) ? EntityUtil.mc.field_71439_g.func_184187_bx().func_174813_aQ() : EntityUtil.mc.field_71439_g.func_174813_aQ();
        final int y = (int)bb.field_72338_b;
        for (int x = MathHelper.func_76128_c(bb.field_72340_a); x < MathHelper.func_76128_c(bb.field_72336_d) + 1; ++x) {
            for (int z = MathHelper.func_76128_c(bb.field_72339_c); z < MathHelper.func_76128_c(bb.field_72334_f) + 1; ++z) {
                final Block block = EntityUtil.mc.field_71441_e.func_180495_p(new BlockPos(x, y, z)).func_177230_c();
                if (!(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    inLiquid = true;
                }
            }
        }
        return inLiquid;
    }
    
    public static boolean isOnLiquid(final double offset) {
        if (EntityUtil.mc.field_71439_g.field_70143_R >= 3.0f) {
            return false;
        }
        final AxisAlignedBB bb = (EntityUtil.mc.field_71439_g.func_184187_bx() != null) ? EntityUtil.mc.field_71439_g.func_184187_bx().func_174813_aQ().func_191195_a(0.0, 0.0, 0.0).func_72317_d(0.0, -offset, 0.0) : EntityUtil.mc.field_71439_g.func_174813_aQ().func_191195_a(0.0, 0.0, 0.0).func_72317_d(0.0, -offset, 0.0);
        boolean onLiquid = false;
        final int y = (int)bb.field_72338_b;
        for (int x = MathHelper.func_76128_c(bb.field_72340_a); x < MathHelper.func_76128_c(bb.field_72336_d + 1.0); ++x) {
            for (int z = MathHelper.func_76128_c(bb.field_72339_c); z < MathHelper.func_76128_c(bb.field_72334_f + 1.0); ++z) {
                final Block block = EntityUtil.mc.field_71441_e.func_180495_p(new BlockPos(x, y, z)).func_177230_c();
                if (block != Blocks.field_150350_a) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }
    
    public static boolean isAboveLiquid(final Entity entity) {
        if (entity == null) {
            return false;
        }
        final double n = entity.field_70163_u + 0.01;
        for (int i = MathHelper.func_76128_c(entity.field_70165_t); i < MathHelper.func_76143_f(entity.field_70165_t); ++i) {
            for (int j = MathHelper.func_76128_c(entity.field_70161_v); j < MathHelper.func_76143_f(entity.field_70161_v); ++j) {
                if (EntityUtil.mc.field_71441_e.func_180495_p(new BlockPos(i, (int)n, j)).func_177230_c() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static BlockPos getPlayerPosWithEntity() {
        return new BlockPos((EntityUtil.mc.field_71439_g.func_184187_bx() != null) ? EntityUtil.mc.field_71439_g.func_184187_bx().field_70165_t : EntityUtil.mc.field_71439_g.field_70165_t, (EntityUtil.mc.field_71439_g.func_184187_bx() != null) ? EntityUtil.mc.field_71439_g.func_184187_bx().field_70163_u : EntityUtil.mc.field_71439_g.field_70163_u, (EntityUtil.mc.field_71439_g.func_184187_bx() != null) ? EntityUtil.mc.field_71439_g.func_184187_bx().field_70161_v : EntityUtil.mc.field_71439_g.field_70161_v);
    }
    
    public static boolean checkForLiquid(final Entity entity, final boolean b) {
        if (entity == null) {
            return false;
        }
        final double posY = entity.field_70163_u;
        double n;
        if (b) {
            n = 0.03;
        }
        else if (entity instanceof EntityPlayer) {
            n = 0.2;
        }
        else {
            n = 0.5;
        }
        final double n2 = posY - n;
        for (int i = MathHelper.func_76128_c(entity.field_70165_t); i < MathHelper.func_76143_f(entity.field_70165_t); ++i) {
            for (int j = MathHelper.func_76128_c(entity.field_70161_v); j < MathHelper.func_76143_f(entity.field_70161_v); ++j) {
                if (EntityUtil.mc.field_71441_e.func_180495_p(new BlockPos(i, MathHelper.func_76128_c(n2), j)).func_177230_c() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean isOnLiquid() {
        final double y = EntityUtil.mc.field_71439_g.field_70163_u - 0.03;
        for (int x = MathHelper.func_76128_c(EntityUtil.mc.field_71439_g.field_70165_t); x < MathHelper.func_76143_f(EntityUtil.mc.field_71439_g.field_70165_t); ++x) {
            for (int z = MathHelper.func_76128_c(EntityUtil.mc.field_71439_g.field_70161_v); z < MathHelper.func_76143_f(EntityUtil.mc.field_71439_g.field_70161_v); ++z) {
                final BlockPos pos = new BlockPos(x, MathHelper.func_76128_c(y), z);
                if (EntityUtil.mc.field_71441_e.func_180495_p(pos).func_177230_c() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static double[] forward(final double speed) {
        float forward = EntityUtil.mc.field_71439_g.field_71158_b.field_192832_b;
        float side = EntityUtil.mc.field_71439_g.field_71158_b.field_78902_a;
        float yaw = EntityUtil.mc.field_71439_g.field_70126_B + (EntityUtil.mc.field_71439_g.field_70177_z - EntityUtil.mc.field_71439_g.field_70126_B) * EntityUtil.mc.func_184121_ak();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            }
            else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            }
            else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[] { posX, posZ };
    }
    
    public static Map<String, Integer> getTextRadarPlayers() {
        Map<String, Integer> output = new HashMap<String, Integer>();
        final DecimalFormat dfHealth = new DecimalFormat("#.#");
        dfHealth.setRoundingMode(RoundingMode.CEILING);
        final DecimalFormat dfDistance = new DecimalFormat("#.#");
        dfDistance.setRoundingMode(RoundingMode.CEILING);
        final StringBuilder healthSB = new StringBuilder();
        final StringBuilder distanceSB = new StringBuilder();
        for (final EntityPlayer player : EntityUtil.mc.field_71441_e.field_73010_i) {
            if (player.func_82150_aj() && !Managers.getInstance().tRadarInv.getValue()) {
                continue;
            }
            if (player.func_70005_c_().equals(EntityUtil.mc.field_71439_g.func_70005_c_())) {
                continue;
            }
            final int hpRaw = (int)getHealth((Entity)player);
            final String hp = dfHealth.format(hpRaw);
            healthSB.append("§");
            if (hpRaw >= 20) {
                healthSB.append("a");
            }
            else if (hpRaw >= 10) {
                healthSB.append("e");
            }
            else if (hpRaw >= 5) {
                healthSB.append("6");
            }
            else {
                healthSB.append("c");
            }
            healthSB.append(hp);
            final int distanceInt = (int)EntityUtil.mc.field_71439_g.func_70032_d((Entity)player);
            final String distance = dfDistance.format(distanceInt);
            distanceSB.append("§");
            if (distanceInt >= 25) {
                distanceSB.append("a");
            }
            else if (distanceInt > 10) {
                distanceSB.append("6");
            }
            else if (distanceInt >= 50) {
                distanceSB.append("7");
            }
            else {
                distanceSB.append("c");
            }
            distanceSB.append(distance);
            output.put(healthSB.toString() + " " + (esohack.friendManager.isFriend(player) ? "§b" : "§r") + player.func_70005_c_() + " " + distanceSB.toString() + " " + "§f" + esohack.totemPopManager.getTotemPopString(player) + esohack.potionManager.getTextRadarPotion(player), (int)EntityUtil.mc.field_71439_g.func_70032_d((Entity)player));
            healthSB.setLength(0);
            distanceSB.setLength(0);
        }
        if (!output.isEmpty()) {
            output = MathUtil.sortByValue(output, false);
        }
        return output;
    }
    
    public static boolean isAboveBlock(final Entity entity, final BlockPos blockPos) {
        return entity.field_70163_u >= blockPos.func_177956_o();
    }
    
    static {
        antiDropOffsetList = new Vec3d[] { new Vec3d(0.0, -2.0, 0.0) };
        platformOffsetList = new Vec3d[] { new Vec3d(0.0, -1.0, 0.0), new Vec3d(0.0, -1.0, -1.0), new Vec3d(0.0, -1.0, 1.0), new Vec3d(-1.0, -1.0, 0.0), new Vec3d(1.0, -1.0, 0.0) };
        legOffsetList = new Vec3d[] { new Vec3d(-1.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 0.0, 1.0) };
        doubleLegOffsetList = new Vec3d[] { new Vec3d(-1.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-2.0, 0.0, 0.0), new Vec3d(2.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -2.0), new Vec3d(0.0, 0.0, 2.0) };
        OffsetList = new Vec3d[] { new Vec3d(1.0, 1.0, 0.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(0.0, 2.0, 0.0) };
        headpiece = new Vec3d[] { new Vec3d(0.0, 2.0, 0.0) };
        offsetsNoHead = new Vec3d[] { new Vec3d(1.0, 1.0, 0.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(0.0, 1.0, -1.0) };
        antiStepOffsetList = new Vec3d[] { new Vec3d(-1.0, 2.0, 0.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(0.0, 2.0, -1.0) };
        antiScaffoldOffsetList = new Vec3d[] { new Vec3d(0.0, 3.0, 0.0) };
    }
}
