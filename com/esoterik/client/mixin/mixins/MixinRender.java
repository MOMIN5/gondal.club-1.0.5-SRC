// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.mixin.mixins;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.entity.Entity;

@Mixin({ Render.class })
public class MixinRender<T extends Entity>
{
    @Inject(method = { "shouldRender" }, at = { @At("HEAD") }, cancellable = true)
    public void shouldRender(final T livingEntity, final ICamera camera, final double camX, final double camY, final double camZ, final CallbackInfoReturnable<Boolean> info) {
        if (livingEntity == null || camera == null || livingEntity.func_184177_bl() == null) {
            info.setReturnValue(false);
        }
    }
}
