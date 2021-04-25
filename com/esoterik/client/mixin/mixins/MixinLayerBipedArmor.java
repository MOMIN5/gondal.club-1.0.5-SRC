// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.mixin.mixins;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.esoterik.client.features.modules.render.NoRender;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;

@Mixin({ LayerBipedArmor.class })
public abstract class MixinLayerBipedArmor extends LayerArmorBase<ModelBiped>
{
    public MixinLayerBipedArmor(final RenderLivingBase<?> rendererIn) {
        super((RenderLivingBase)rendererIn);
    }
    
    @Inject(method = { "setModelSlotVisible" }, at = { @At("HEAD") }, cancellable = true)
    protected void setModelSlotVisible(final ModelBiped model, final EntityEquipmentSlot slotIn, final CallbackInfo info) {
        final NoRender noArmor = NoRender.getInstance();
        if (noArmor.isOn() && noArmor.noArmor.getValue() != NoRender.NoArmor.NONE) {
            info.cancel();
            switch (slotIn) {
                case HEAD: {
                    model.field_78116_c.field_78806_j = false;
                    model.field_178720_f.field_78806_j = false;
                    break;
                }
                case CHEST: {
                    model.field_78115_e.field_78806_j = (noArmor.noArmor.getValue() != NoRender.NoArmor.ALL);
                    model.field_178723_h.field_78806_j = (noArmor.noArmor.getValue() != NoRender.NoArmor.ALL);
                    model.field_178724_i.field_78806_j = (noArmor.noArmor.getValue() != NoRender.NoArmor.ALL);
                    break;
                }
                case LEGS: {
                    model.field_78115_e.field_78806_j = (noArmor.noArmor.getValue() != NoRender.NoArmor.ALL);
                    model.field_178721_j.field_78806_j = (noArmor.noArmor.getValue() != NoRender.NoArmor.ALL);
                    model.field_178722_k.field_78806_j = (noArmor.noArmor.getValue() != NoRender.NoArmor.ALL);
                    break;
                }
                case FEET: {
                    model.field_178721_j.field_78806_j = (noArmor.noArmor.getValue() != NoRender.NoArmor.ALL);
                    model.field_178722_k.field_78806_j = (noArmor.noArmor.getValue() != NoRender.NoArmor.ALL);
                    break;
                }
            }
        }
    }
}
