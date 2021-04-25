// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.math.RoundingMode;
import java.math.BigDecimal;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import java.util.Random;

public class MathUtil implements Util
{
    private static final Random random;
    
    public static int getRandom(final int min, final int max) {
        return min + MathUtil.random.nextInt(max - min + 1);
    }
    
    public static double getRandom(final double min, final double max) {
        return MathHelper.func_151237_a(min + MathUtil.random.nextDouble() * max, min, max);
    }
    
    public static float getRandom(final float min, final float max) {
        return MathHelper.func_76131_a(min + MathUtil.random.nextFloat() * max, min, max);
    }
    
    public static int clamp(final int num, final int min, final int max) {
        return (num < min) ? min : Math.min(num, max);
    }
    
    public static float clamp(final float num, final float min, final float max) {
        return (num < min) ? min : Math.min(num, max);
    }
    
    public static double clamp(final double num, final double min, final double max) {
        return (num < min) ? min : Math.min(num, max);
    }
    
    public static float sin(final float value) {
        return MathHelper.func_76126_a(value);
    }
    
    public static float cos(final float value) {
        return MathHelper.func_76134_b(value);
    }
    
    public static float wrapDegrees(final float value) {
        return MathHelper.func_76142_g(value);
    }
    
    public static double wrapDegrees(final double value) {
        return MathHelper.func_76138_g(value);
    }
    
    public static Vec3d roundVec(final Vec3d vec3d, final int places) {
        return new Vec3d(round(vec3d.field_72450_a, places), round(vec3d.field_72448_b, places), round(vec3d.field_72449_c, places));
    }
    
    public static double square(final double input) {
        return input * input;
    }
    
    public static double round(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.doubleValue();
    }
    
    public static float wrap(final float valI) {
        float val = valI % 360.0f;
        if (val >= 180.0f) {
            val -= 360.0f;
        }
        if (val < -180.0f) {
            val += 360.0f;
        }
        return val;
    }
    
    public static Vec3d direction(final float yaw) {
        return new Vec3d(Math.cos(degToRad(yaw + 90.0f)), 0.0, Math.sin(degToRad(yaw + 90.0f)));
    }
    
    public static float round(final float value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.floatValue();
    }
    
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map, final boolean descending) {
        final List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        if (descending) {
            list.sort((Comparator<? super Map.Entry<K, V>>)Map.Entry.comparingByValue(Comparator.reverseOrder()));
        }
        else {
            list.sort((Comparator<? super Map.Entry<K, V>>)Map.Entry.comparingByValue());
        }
        final Map<K, V> result = new LinkedHashMap<K, V>();
        for (final Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    public static String getTimeOfDay() {
        final Calendar c = Calendar.getInstance();
        final int timeOfDay = c.get(11);
        if (timeOfDay < 12) {
            return "Good Morning ";
        }
        if (timeOfDay < 16) {
            return "Good Afternoon ";
        }
        if (timeOfDay < 21) {
            return "Good Evening ";
        }
        return "Good Night ";
    }
    
    public static double radToDeg(final double rad) {
        return rad * 57.295780181884766;
    }
    
    public static double degToRad(final double deg) {
        return deg * 0.01745329238474369;
    }
    
    public static double getIncremental(final double val, final double inc) {
        final double one = 1.0 / inc;
        return Math.round(val * one) / one;
    }
    
    public static double[] directionSpeed(final double speed) {
        float forward = MathUtil.mc.field_71439_g.field_71158_b.field_192832_b;
        float side = MathUtil.mc.field_71439_g.field_71158_b.field_78902_a;
        float yaw = MathUtil.mc.field_71439_g.field_70126_B + (MathUtil.mc.field_71439_g.field_70177_z - MathUtil.mc.field_71439_g.field_70126_B) * MathUtil.mc.func_184121_ak();
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
    
    public static List<Vec3d> getBlockBlocks(final Entity entity) {
        final List<Vec3d> vec3ds = new ArrayList<Vec3d>();
        final AxisAlignedBB bb = entity.func_174813_aQ();
        final double y = entity.field_70163_u;
        final double minX = round(bb.field_72340_a, 0);
        final double minZ = round(bb.field_72339_c, 0);
        final double maxX = round(bb.field_72336_d, 0);
        final double maxZ = round(bb.field_72334_f, 0);
        if (minX != maxX) {
            vec3ds.add(new Vec3d(minX, y, minZ));
            vec3ds.add(new Vec3d(maxX, y, minZ));
            if (minZ != maxZ) {
                vec3ds.add(new Vec3d(minX, y, maxZ));
                vec3ds.add(new Vec3d(maxX, y, maxZ));
                return vec3ds;
            }
        }
        else if (minZ != maxZ) {
            vec3ds.add(new Vec3d(minX, y, minZ));
            vec3ds.add(new Vec3d(minX, y, maxZ));
            return vec3ds;
        }
        vec3ds.add(entity.func_174791_d());
        return vec3ds;
    }
    
    public static boolean areVec3dsAligned(final Vec3d vec3d1, final Vec3d vec3d2) {
        return areVec3dsAlignedRetarded(vec3d1, vec3d2);
    }
    
    public static boolean areVec3dsAlignedRetarded(final Vec3d vec3d1, final Vec3d vec3d2) {
        final BlockPos pos1 = new BlockPos(vec3d1);
        final BlockPos pos2 = new BlockPos(vec3d2.field_72450_a, vec3d1.field_72448_b, vec3d2.field_72449_c);
        return pos1.equals((Object)pos2);
    }
    
    public static Vec3d calculateLine(final Vec3d x1, final Vec3d x2, final double distance) {
        final double length = Math.sqrt(multiply(x2.field_72450_a - x1.field_72450_a) + multiply(x2.field_72448_b - x1.field_72448_b) + multiply(x2.field_72449_c - x1.field_72449_c));
        final double unitSlopeX = (x2.field_72450_a - x1.field_72450_a) / length;
        final double unitSlopeY = (x2.field_72448_b - x1.field_72448_b) / length;
        final double unitSlopeZ = (x2.field_72449_c - x1.field_72449_c) / length;
        final double x3 = x1.field_72450_a + unitSlopeX * distance;
        final double y = x1.field_72448_b + unitSlopeY * distance;
        final double z = x1.field_72449_c + unitSlopeZ * distance;
        return new Vec3d(x3, y, z);
    }
    
    public static float[] calcAngle(final Vec3d from, final Vec3d to) {
        final double difX = to.field_72450_a - from.field_72450_a;
        final double difY = (to.field_72448_b - from.field_72448_b) * -1.0;
        final double difZ = to.field_72449_c - from.field_72449_c;
        final double dist = MathHelper.func_76133_a(difX * difX + difZ * difZ);
        return new float[] { (float)MathHelper.func_76138_g(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float)MathHelper.func_76138_g(Math.toDegrees(Math.atan2(difY, dist))) };
    }
    
    public static double multiply(final double one) {
        return one * one;
    }
    
    public static Vec3d extrapolatePlayerPosition(final EntityPlayer player, final int ticks) {
        final Vec3d lastPos = new Vec3d(player.field_70142_S, player.field_70137_T, player.field_70136_U);
        final Vec3d currentPos = new Vec3d(player.field_70165_t, player.field_70163_u, player.field_70161_v);
        final double distance = multiply(player.field_70159_w) + multiply(player.field_70181_x) + multiply(player.field_70179_y);
        final Vec3d tempVec = calculateLine(lastPos, currentPos, distance * ticks);
        return new Vec3d(tempVec.field_72450_a, player.field_70163_u, tempVec.field_72449_c);
    }
    
    static {
        random = new Random();
    }
}
