// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.mixin.mixins;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.entity.RenderManager;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ RenderGlobal.class })
public abstract class MixinRenderGlobal
{
    @Redirect(method = { "setupTerrain" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ChunkRenderContainer;initialize(DDD)V"))
    public void initializeHook(final ChunkRenderContainer chunkRenderContainer, final double viewEntityXIn, final double viewEntityYIn, final double viewEntityZIn) {
        final double y = viewEntityYIn;
        chunkRenderContainer.initialize(viewEntityXIn, y, viewEntityZIn);
    }
    
    @Redirect(method = { "renderEntities" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;setRenderPosition(DDD)V"))
    public void setRenderPositionHook(final RenderManager renderManager, final double renderPosXIn, final double renderPosYIn, final double renderPosZIn) {
        final double y = renderPosYIn;
        renderManager.setRenderPosition(renderPosXIn, TileEntityRendererDispatcher.staticPlayerY = y, renderPosZIn);
    }
    
    @Redirect(method = { "drawSelectionBox" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/AxisAlignedBB;offset(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"))
    public AxisAlignedBB offsetHook(final AxisAlignedBB axisAlignedBB, final double x, final double y, final double z) {
        final double yIn = y;
        return axisAlignedBB.offset(x, y, z);
    }
}
