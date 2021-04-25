// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.manager;

import net.minecraft.util.math.MathHelper;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import com.esoterik.client.features.modules.client.Managers;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.HashMap;
import com.esoterik.client.features.Feature;

public class SpeedManager extends Feature
{
    public double firstJumpSpeed;
    public double lastJumpSpeed;
    public double percentJumpSpeedChanged;
    public double jumpSpeedChanged;
    public static boolean didJumpThisTick;
    public static boolean isJumping;
    public boolean didJumpLastTick;
    public long jumpInfoStartTime;
    public boolean wasFirstJump;
    public static final double LAST_JUMP_INFO_DURATION_DEFAULT = 3.0;
    public double speedometerCurrentSpeed;
    public HashMap<EntityPlayer, Double> playerSpeeds;
    private int distancer;
    
    public SpeedManager() {
        this.firstJumpSpeed = 0.0;
        this.lastJumpSpeed = 0.0;
        this.percentJumpSpeedChanged = 0.0;
        this.jumpSpeedChanged = 0.0;
        this.didJumpLastTick = false;
        this.jumpInfoStartTime = 0L;
        this.wasFirstJump = true;
        this.speedometerCurrentSpeed = 0.0;
        this.playerSpeeds = new HashMap<EntityPlayer, Double>();
        this.distancer = 20;
    }
    
    public static void setDidJumpThisTick(final boolean val) {
        SpeedManager.didJumpThisTick = val;
    }
    
    public static void setIsJumping(final boolean val) {
        SpeedManager.isJumping = val;
    }
    
    public float lastJumpInfoTimeRemaining() {
        return (Minecraft.func_71386_F() - this.jumpInfoStartTime) / 1000.0f;
    }
    
    public void updateValues() {
        final double distTraveledLastTickX = SpeedManager.mc.field_71439_g.field_70165_t - SpeedManager.mc.field_71439_g.field_70169_q;
        final double distTraveledLastTickZ = SpeedManager.mc.field_71439_g.field_70161_v - SpeedManager.mc.field_71439_g.field_70166_s;
        this.speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
        if (SpeedManager.didJumpThisTick && (!SpeedManager.mc.field_71439_g.field_70122_E || SpeedManager.isJumping)) {
            if (SpeedManager.didJumpThisTick && !this.didJumpLastTick) {
                this.wasFirstJump = (this.lastJumpSpeed == 0.0);
                this.percentJumpSpeedChanged = ((this.speedometerCurrentSpeed != 0.0) ? (this.speedometerCurrentSpeed / this.lastJumpSpeed - 1.0) : -1.0);
                this.jumpSpeedChanged = this.speedometerCurrentSpeed - this.lastJumpSpeed;
                this.jumpInfoStartTime = Minecraft.func_71386_F();
                this.lastJumpSpeed = this.speedometerCurrentSpeed;
                this.firstJumpSpeed = (this.wasFirstJump ? this.lastJumpSpeed : 0.0);
            }
            this.didJumpLastTick = SpeedManager.didJumpThisTick;
        }
        else {
            this.didJumpLastTick = false;
            this.lastJumpSpeed = 0.0;
        }
        if (Managers.getInstance().speed.getValue()) {
            this.updatePlayers();
        }
    }
    
    public void updatePlayers() {
        for (final EntityPlayer player : SpeedManager.mc.field_71441_e.field_73010_i) {
            if (SpeedManager.mc.field_71439_g.func_70068_e((Entity)player) < this.distancer * this.distancer) {
                final double distTraveledLastTickX = player.field_70165_t - player.field_70169_q;
                final double distTraveledLastTickZ = player.field_70161_v - player.field_70166_s;
                final double playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
                this.playerSpeeds.put(player, playerSpeed);
            }
        }
    }
    
    public double getPlayerSpeed(final EntityPlayer player) {
        if (this.playerSpeeds.get(player) == null) {
            return 0.0;
        }
        return this.turnIntoKpH(this.playerSpeeds.get(player));
    }
    
    public double turnIntoKpH(final double input) {
        return MathHelper.func_76133_a(input) * 71.2729367892;
    }
    
    public double getSpeedKpH() {
        double speedometerkphdouble = this.turnIntoKpH(this.speedometerCurrentSpeed);
        speedometerkphdouble = Math.round(10.0 * speedometerkphdouble) / 10.0;
        return speedometerkphdouble;
    }
    
    public double getSpeedMpS() {
        double speedometerMpsdouble = this.turnIntoKpH(this.speedometerCurrentSpeed) / 3.6;
        speedometerMpsdouble = Math.round(10.0 * speedometerMpsdouble) / 10.0;
        return speedometerMpsdouble;
    }
    
    static {
        SpeedManager.didJumpThisTick = false;
        SpeedManager.isJumping = false;
    }
}
