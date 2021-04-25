// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.mixin.mixins;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.esoterik.client.util.Util;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.entity.Entity;

@Mixin({ EntityLivingBase.class })
public abstract class MixinEntityLivingBase extends Entity
{
    public MixinEntityLivingBase(final World worldIn) {
        super(worldIn);
    }
    
    @Inject(method = { "isElytraFlying" }, at = { @At("HEAD") }, cancellable = true)
    private void isElytraFlyingHook(final CallbackInfoReturnable<Boolean> info) {
        if (Util.mc.field_71439_g == null || Util.mc.field_71439_g.equals((Object)this)) {}
    }
}
