// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.event.events;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import com.esoterik.client.event.EventStage;

@Cancelable
public class JesusEvent extends EventStage
{
    private BlockPos pos;
    private AxisAlignedBB boundingBox;
    
    public JesusEvent(final int stage, final BlockPos pos) {
        super(stage);
        this.pos = pos;
    }
    
    public void setBoundingBox(final AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }
    
    public void setPos(final BlockPos pos) {
        this.pos = pos;
    }
    
    public BlockPos getPos() {
        return this.pos;
    }
    
    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }
}
