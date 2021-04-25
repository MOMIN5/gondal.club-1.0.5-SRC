// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import com.esoterik.client.features.modules.client.ClickGui;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RotationUtil implements Util
{
    public static Vec3d getEyesPos() {
        return new Vec3d(RotationUtil.mc.field_71439_g.field_70165_t, RotationUtil.mc.field_71439_g.field_70163_u + RotationUtil.mc.field_71439_g.func_70047_e(), RotationUtil.mc.field_71439_g.field_70161_v);
    }
    
    public static double yawDist(final BlockPos pos) {
        if (pos != null) {
            final Vec3d difference = new Vec3d((Vec3i)pos).func_178788_d(RotationUtil.mc.field_71439_g.func_174824_e(RotationUtil.mc.func_184121_ak()));
            final double d = Math.abs(RotationUtil.mc.field_71439_g.field_70177_z - (Math.toDegrees(Math.atan2(difference.field_72449_c, difference.field_72450_a)) - 90.0)) % 360.0;
            return (d > 180.0) ? (360.0 - d) : d;
        }
        return 0.0;
    }
    
    public static double yawDist(final Entity e) {
        if (e != null) {
            final Vec3d difference = e.func_174791_d().func_72441_c(0.0, (double)(e.func_70047_e() / 2.0f), 0.0).func_178788_d(RotationUtil.mc.field_71439_g.func_174824_e(RotationUtil.mc.func_184121_ak()));
            final double d = Math.abs(RotationUtil.mc.field_71439_g.field_70177_z - (Math.toDegrees(Math.atan2(difference.field_72449_c, difference.field_72450_a)) - 90.0)) % 360.0;
            return (d > 180.0) ? (360.0 - d) : d;
        }
        return 0.0;
    }
    
    public static float getFov() {
        return ClickGui.getInstance().customFov.getValue() ? ClickGui.getInstance().fov.getValue() : RotationUtil.mc.field_71474_y.field_74334_X;
    }
    
    public static float getHalvedfov() {
        return getFov() / 2.0f;
    }
    
    public static boolean isInFov(final BlockPos pos) {
        return pos != null && (RotationUtil.mc.field_71439_g.func_174818_b(pos) < 4.0 || yawDist(pos) < getHalvedfov() + 2.0f);
    }
    
    public static boolean isInFov(final Entity entity) {
        return entity != null && (RotationUtil.mc.field_71439_g.func_70068_e(entity) < 4.0 || yawDist(entity) < getHalvedfov() + 2.0f);
    }
    
    public static float transformYaw() {
        float yaw = RotationUtil.mc.field_71439_g.field_70177_z % 360.0f;
        if (RotationUtil.mc.field_71439_g.field_70177_z > 0.0f) {
            if (yaw > 180.0f) {
                yaw = -180.0f + (yaw - 180.0f);
            }
        }
        else if (yaw < -180.0f) {
            yaw = 180.0f + (yaw + 180.0f);
        }
        if (yaw < 0.0f) {
            return 180.0f + yaw;
        }
        return -180.0f + yaw;
    }
    
    public static float[] calcAngleNoY(final Vec3d from, final Vec3d to) {
        final double difX = to.field_72450_a - from.field_72450_a;
        final double difZ = to.field_72449_c - from.field_72449_c;
        return new float[] { (float)MathHelper.func_76138_g(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0) };
    }
    
    public static boolean isInFov(final Vec3d vec3d, final Vec3d other) {
        Label_0069: {
            if (RotationUtil.mc.field_71439_g.field_70125_A > 30.0f) {
                if (other.field_72448_b <= RotationUtil.mc.field_71439_g.field_70163_u) {
                    break Label_0069;
                }
            }
            else if (RotationUtil.mc.field_71439_g.field_70125_A >= -30.0f || other.field_72448_b >= RotationUtil.mc.field_71439_g.field_70163_u) {
                break Label_0069;
            }
            return true;
        }
        final float angle = calcAngleNoY(vec3d, other)[0] - transformYaw();
        if (angle < -270.0f) {
            return true;
        }
        final float fov = (ClickGui.getInstance().customFov.getValue() ? ClickGui.getInstance().fov.getValue() : RotationUtil.mc.field_71474_y.field_74334_X) / 2.0f;
        return angle < fov + 10.0f && angle > -fov - 10.0f;
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
    
    public static float[] getLegitRotations(final Vec3d vec) {
        final Vec3d eyesPos = getEyesPos();
        final double diffX = vec.field_72450_a - eyesPos.field_72450_a;
        final double diffY = vec.field_72448_b - eyesPos.field_72448_b;
        final double diffZ = vec.field_72449_c - eyesPos.field_72449_c;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[] { RotationUtil.mc.field_71439_g.field_70177_z + MathHelper.func_76142_g(yaw - RotationUtil.mc.field_71439_g.field_70177_z), RotationUtil.mc.field_71439_g.field_70125_A + MathHelper.func_76142_g(pitch - RotationUtil.mc.field_71439_g.field_70125_A) };
    }
    
    public static void faceYawAndPitch(final float yaw, final float pitch) {
        RotationUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Rotation(yaw, pitch, RotationUtil.mc.field_71439_g.field_70122_E));
    }
    
    public static void faceVector(final Vec3d vec, final boolean normalizeAngle) {
        final float[] rotations = getLegitRotations(vec);
        RotationUtil.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketPlayer.Rotation(rotations[0], normalizeAngle ? ((float)MathHelper.func_180184_b((int)rotations[1], 360)) : rotations[1], RotationUtil.mc.field_71439_g.field_70122_E));
    }
    
    public static void faceEntity(final Entity entity) {
        final float[] angle = MathUtil.calcAngle(RotationUtil.mc.field_71439_g.func_174824_e(RotationUtil.mc.func_184121_ak()), entity.func_174824_e(RotationUtil.mc.func_184121_ak()));
        faceYawAndPitch(angle[0], angle[1]);
    }
    
    public static float[] getAngle(final Entity entity) {
        return MathUtil.calcAngle(RotationUtil.mc.field_71439_g.func_174824_e(RotationUtil.mc.func_184121_ak()), entity.func_174824_e(RotationUtil.mc.func_184121_ak()));
    }
    
    public static int getDirection4D() {
        return MathHelper.func_76128_c(RotationUtil.mc.field_71439_g.field_70177_z * 4.0f / 360.0f + 0.5) & 0x3;
    }
    
    public static String getDirection4D(final boolean northRed) {
        final int dirnumber = getDirection4D();
        if (dirnumber == 0) {
            return "South (+Z)";
        }
        if (dirnumber == 1) {
            return "West (-X)";
        }
        if (dirnumber == 2) {
            return (northRed ? "§c" : "") + "North (-Z)";
        }
        if (dirnumber == 3) {
            return "East (+X)";
        }
        return "Loading...";
    }
}
