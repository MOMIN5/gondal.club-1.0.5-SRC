// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.manager;

import com.esoterik.client.util.RotationUtil;
import net.minecraft.entity.Entity;
import com.esoterik.client.util.MathUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.features.Feature;

public class RotationManager extends Feature
{
    private float yaw;
    private float pitch;
    
    public void updateRotations() {
        this.yaw = RotationManager.mc.field_71439_g.field_70177_z;
        this.pitch = RotationManager.mc.field_71439_g.field_70125_A;
    }
    
    public void restoreRotations() {
        RotationManager.mc.field_71439_g.field_70177_z = this.yaw;
        RotationManager.mc.field_71439_g.field_70759_as = this.yaw;
        RotationManager.mc.field_71439_g.field_70125_A = this.pitch;
    }
    
    public void setPlayerRotations(final float yaw, final float pitch) {
        RotationManager.mc.field_71439_g.field_70177_z = yaw;
        RotationManager.mc.field_71439_g.field_70759_as = yaw;
        RotationManager.mc.field_71439_g.field_70125_A = pitch;
    }
    
    public void setPlayerYaw(final float yaw) {
        RotationManager.mc.field_71439_g.field_70177_z = yaw;
        RotationManager.mc.field_71439_g.field_70759_as = yaw;
    }
    
    public void lookAtPos(final BlockPos pos) {
        final float[] angle = MathUtil.calcAngle(RotationManager.mc.field_71439_g.func_174824_e(RotationManager.mc.func_184121_ak()), new Vec3d((double)(pos.func_177958_n() + 0.5f), (double)(pos.func_177956_o() + 0.5f), (double)(pos.func_177952_p() + 0.5f)));
        this.setPlayerRotations(angle[0], angle[1]);
    }
    
    public void lookAtVec3d(final Vec3d vec3d) {
        final float[] angle = MathUtil.calcAngle(RotationManager.mc.field_71439_g.func_174824_e(RotationManager.mc.func_184121_ak()), new Vec3d(vec3d.field_72450_a, vec3d.field_72448_b, vec3d.field_72449_c));
        this.setPlayerRotations(angle[0], angle[1]);
    }
    
    public void lookAtVec3d(final double x, final double y, final double z) {
        final Vec3d vec3d = new Vec3d(x, y, z);
        this.lookAtVec3d(vec3d);
    }
    
    public void lookAtEntity(final Entity entity) {
        final float[] angle = MathUtil.calcAngle(RotationManager.mc.field_71439_g.func_174824_e(RotationManager.mc.func_184121_ak()), entity.func_174824_e(RotationManager.mc.func_184121_ak()));
        this.setPlayerRotations(angle[0], angle[1]);
    }
    
    public void setPlayerPitch(final float pitch) {
        RotationManager.mc.field_71439_g.field_70125_A = pitch;
    }
    
    public float getYaw() {
        return this.yaw;
    }
    
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
    
    public float getPitch() {
        return this.pitch;
    }
    
    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }
    
    public int getDirection4D() {
        return RotationUtil.getDirection4D();
    }
    
    public String getDirection4D(final boolean northRed) {
        return RotationUtil.getDirection4D(northRed);
    }
}
