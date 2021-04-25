// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.event.events;

import net.minecraft.entity.MoverType;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import com.esoterik.client.event.EventStage;

@Cancelable
public class MoveEvent extends EventStage
{
    private MoverType type;
    private double x;
    private double y;
    private double z;
    
    public MoveEvent(final int stage, final MoverType type, final double x, final double y, final double z) {
        super(stage);
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public MoverType getType() {
        return this.type;
    }
    
    public void setType(final MoverType type) {
        this.type = type;
    }
    
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public double getZ() {
        return this.z;
    }
    
    public void setX(final double x) {
        this.x = x;
    }
    
    public void setY(final double y) {
        this.y = y;
    }
    
    public void setZ(final double z) {
        this.z = z;
    }
}
