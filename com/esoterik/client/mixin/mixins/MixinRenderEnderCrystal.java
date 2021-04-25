// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.mixin.mixins;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.esoterik.client.util.EntityUtil;
import java.awt.Color;
import com.esoterik.client.util.RenderUtil;
import com.esoterik.client.features.modules.client.Colors;
import org.lwjgl.opengl.GL11;
import com.esoterik.client.event.events.RenderEntityModelEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityEnderCrystal;
import com.esoterik.client.features.modules.render.CrystalChams;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ RenderEnderCrystal.class })
public class MixinRenderEnderCrystal
{
    @Shadow
    @Final
    private static ResourceLocation field_110787_a;
    private static ResourceLocation glint;
    
    @Redirect(method = { "doRender" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    public void renderModelBaseHook(final ModelBase model, final Entity entity, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        if (CrystalChams.INSTANCE.isEnabled()) {
            if (CrystalChams.INSTANCE.scaleMap.containsKey(entity)) {
                GlStateManager.func_179152_a((float)CrystalChams.INSTANCE.scaleMap.get(entity), (float)CrystalChams.INSTANCE.scaleMap.get(entity), (float)CrystalChams.INSTANCE.scaleMap.get(entity));
            }
            else {
                GlStateManager.func_179152_a((float)CrystalChams.INSTANCE.scale.getValue(), (float)CrystalChams.INSTANCE.scale.getValue(), (float)CrystalChams.INSTANCE.scale.getValue());
            }
            if (CrystalChams.INSTANCE.wireframe.getValue()) {
                final RenderEntityModelEvent event = new RenderEntityModelEvent(0, model, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                CrystalChams.INSTANCE.onRenderModel(event);
            }
        }
        if (CrystalChams.INSTANCE.isEnabled()) {
            GL11.glPushAttrib(1048575);
            GL11.glDisable(3008);
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glLineWidth(1.5f);
            GL11.glEnable(2960);
            if (CrystalChams.INSTANCE.rainbow.getValue()) {
                final Color rainbowColor1 = CrystalChams.INSTANCE.colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : new Color(RenderUtil.getRainbow(CrystalChams.INSTANCE.speed.getValue() * 100, 0, CrystalChams.INSTANCE.saturation.getValue() / 100.0f, CrystalChams.INSTANCE.brightness.getValue() / 100.0f));
                final Color rainbowColor2 = EntityUtil.getColor(entity, rainbowColor1.getRed(), rainbowColor1.getGreen(), rainbowColor1.getBlue(), CrystalChams.INSTANCE.alpha.getValue(), true);
                if (CrystalChams.INSTANCE.throughWalls.getValue()) {
                    GL11.glDisable(2929);
                    GL11.glDepthMask(false);
                }
                GL11.glEnable(10754);
                GL11.glColor4f(rainbowColor2.getRed() / 255.0f, rainbowColor2.getGreen() / 255.0f, rainbowColor2.getBlue() / 255.0f, CrystalChams.INSTANCE.alpha.getValue() / 255.0f);
                model.func_78088_a(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                if (CrystalChams.INSTANCE.throughWalls.getValue()) {
                    GL11.glEnable(2929);
                    GL11.glDepthMask(true);
                }
            }
            else if (CrystalChams.INSTANCE.xqz.getValue() && CrystalChams.INSTANCE.throughWalls.getValue()) {
                final Color hiddenColor = CrystalChams.INSTANCE.colorSync.getValue() ? EntityUtil.getColor(entity, CrystalChams.INSTANCE.hiddenRed.getValue(), CrystalChams.INSTANCE.hiddenGreen.getValue(), CrystalChams.INSTANCE.hiddenBlue.getValue(), CrystalChams.INSTANCE.hiddenAlpha.getValue(), true) : EntityUtil.getColor(entity, CrystalChams.INSTANCE.hiddenRed.getValue(), CrystalChams.INSTANCE.hiddenGreen.getValue(), CrystalChams.INSTANCE.hiddenBlue.getValue(), CrystalChams.INSTANCE.hiddenAlpha.getValue(), true);
                final Color color;
                final Color visibleColor = color = (CrystalChams.INSTANCE.colorSync.getValue() ? EntityUtil.getColor(entity, CrystalChams.INSTANCE.red.getValue(), CrystalChams.INSTANCE.green.getValue(), CrystalChams.INSTANCE.blue.getValue(), CrystalChams.INSTANCE.alpha.getValue(), true) : EntityUtil.getColor(entity, CrystalChams.INSTANCE.red.getValue(), CrystalChams.INSTANCE.green.getValue(), CrystalChams.INSTANCE.blue.getValue(), CrystalChams.INSTANCE.alpha.getValue(), true));
                if (CrystalChams.INSTANCE.throughWalls.getValue()) {
                    GL11.glDisable(2929);
                    GL11.glDepthMask(false);
                }
                GL11.glEnable(10754);
                GL11.glColor4f(hiddenColor.getRed() / 255.0f, hiddenColor.getGreen() / 255.0f, hiddenColor.getBlue() / 255.0f, CrystalChams.INSTANCE.alpha.getValue() / 255.0f);
                model.func_78088_a(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                if (CrystalChams.INSTANCE.throughWalls.getValue()) {
                    GL11.glEnable(2929);
                    GL11.glDepthMask(true);
                }
                GL11.glColor4f(visibleColor.getRed() / 255.0f, visibleColor.getGreen() / 255.0f, visibleColor.getBlue() / 255.0f, CrystalChams.INSTANCE.alpha.getValue() / 255.0f);
                model.func_78088_a(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
            else {
                final Color color2;
                final Color visibleColor = color2 = (CrystalChams.INSTANCE.colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : EntityUtil.getColor(entity, CrystalChams.INSTANCE.red.getValue(), CrystalChams.INSTANCE.green.getValue(), CrystalChams.INSTANCE.blue.getValue(), CrystalChams.INSTANCE.alpha.getValue(), true));
                if (CrystalChams.INSTANCE.throughWalls.getValue()) {
                    GL11.glDisable(2929);
                    GL11.glDepthMask(false);
                }
                GL11.glEnable(10754);
                GL11.glColor4f(visibleColor.getRed() / 255.0f, visibleColor.getGreen() / 255.0f, visibleColor.getBlue() / 255.0f, CrystalChams.INSTANCE.alpha.getValue() / 255.0f);
                model.func_78088_a(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                if (CrystalChams.INSTANCE.throughWalls.getValue()) {
                    GL11.glEnable(2929);
                    GL11.glDepthMask(true);
                }
            }
            GL11.glEnable(3042);
            GL11.glEnable(2896);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GL11.glPopAttrib();
        }
        else {
            model.func_78088_a(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
        if (CrystalChams.INSTANCE.scaleMap.containsKey(entity)) {
            GlStateManager.func_179152_a(1.0f / CrystalChams.INSTANCE.scaleMap.get(entity), 1.0f / CrystalChams.INSTANCE.scaleMap.get(entity), 1.0f / CrystalChams.INSTANCE.scaleMap.get(entity));
        }
        else {
            GlStateManager.func_179152_a(1.0f / CrystalChams.INSTANCE.scale.getValue(), 1.0f / CrystalChams.INSTANCE.scale.getValue(), 1.0f / CrystalChams.INSTANCE.scale.getValue());
        }
    }
}
