// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.render;

import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import java.awt.Color;
import net.minecraft.client.model.ModelBiped;
import com.esoterik.client.event.events.RenderEntityModelEvent;
import java.util.Iterator;
import com.esoterik.client.util.EntityUtil;
import net.minecraft.entity.Entity;
import com.esoterik.client.util.RenderUtil;
import com.esoterik.client.event.events.Render3DEvent;
import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Map;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Skeleton extends Module
{
    private final Setting<Integer> red;
    private final Setting<Integer> green;
    private final Setting<Integer> blue;
    private final Setting<Integer> alpha;
    private final Setting<Float> lineWidth;
    private final Setting<Boolean> colorFriends;
    private final Setting<Boolean> invisibles;
    private static Skeleton INSTANCE;
    private final Map<EntityPlayer, float[][]> rotationList;
    
    public Skeleton() {
        super("Skeleton", "Draws a nice Skeleton.", Category.RENDER, false, false, false);
        this.red = (Setting<Integer>)this.register(new Setting("Red", (T)255, (T)0, (T)255));
        this.green = (Setting<Integer>)this.register(new Setting("Green", (T)255, (T)0, (T)255));
        this.blue = (Setting<Integer>)this.register(new Setting("Blue", (T)255, (T)0, (T)255));
        this.alpha = (Setting<Integer>)this.register(new Setting("Alpha", (T)255, (T)0, (T)255));
        this.lineWidth = (Setting<Float>)this.register(new Setting("LineWidth", (T)1.5f, (T)0.1f, (T)5.0f));
        this.colorFriends = (Setting<Boolean>)this.register(new Setting("Friends", (T)true));
        this.invisibles = (Setting<Boolean>)this.register(new Setting("Invisibles", (T)false));
        this.rotationList = new HashMap<EntityPlayer, float[][]>();
        this.setInstance();
    }
    
    private void setInstance() {
        Skeleton.INSTANCE = this;
    }
    
    public static Skeleton getInstance() {
        if (Skeleton.INSTANCE == null) {
            Skeleton.INSTANCE = new Skeleton();
        }
        return Skeleton.INSTANCE;
    }
    
    @Override
    public void onRender3D(final Render3DEvent event) {
        RenderUtil.GLPre(this.lineWidth.getValue());
        for (final EntityPlayer player : Skeleton.mc.field_71441_e.field_73010_i) {
            if (player != null && player != Skeleton.mc.func_175606_aa() && player.func_70089_S() && !player.func_70608_bn() && (!player.func_82150_aj() || this.invisibles.getValue()) && this.rotationList.get(player) != null && Skeleton.mc.field_71439_g.func_70068_e((Entity)player) < 2500.0) {
                this.renderSkeleton(player, this.rotationList.get(player), EntityUtil.getColor((Entity)player, this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue(), this.colorFriends.getValue()));
            }
        }
        RenderUtil.GlPost();
    }
    
    public void onRenderModel(final RenderEntityModelEvent event) {
        if (event.getStage() == 0 && event.entity instanceof EntityPlayer && event.modelBase instanceof ModelBiped) {
            final ModelBiped biped = (ModelBiped)event.modelBase;
            final float[][] rotations = RenderUtil.getBipedRotations(biped);
            final EntityPlayer player = (EntityPlayer)event.entity;
            this.rotationList.put(player, rotations);
        }
    }
    
    private void renderSkeleton(final EntityPlayer player, final float[][] rotations, final Color color) {
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179094_E();
        GlStateManager.func_179131_c(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        final Vec3d interp = EntityUtil.getInterpolatedRenderPos((Entity)player, Skeleton.mc.func_184121_ak());
        final double pX = interp.field_72450_a;
        final double pY = interp.field_72448_b;
        final double pZ = interp.field_72449_c;
        GlStateManager.func_179137_b(pX, pY, pZ);
        GlStateManager.func_179114_b(-player.field_70761_aq, 0.0f, 1.0f, 0.0f);
        GlStateManager.func_179137_b(0.0, 0.0, player.func_70093_af() ? -0.235 : 0.0);
        final float sneak = player.func_70093_af() ? 0.6f : 0.75f;
        GlStateManager.func_179094_E();
        GlStateManager.func_179137_b(-0.125, (double)sneak, 0.0);
        if (rotations[3][0] != 0.0f) {
            GlStateManager.func_179114_b(rotations[3][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (rotations[3][1] != 0.0f) {
            GlStateManager.func_179114_b(rotations[3][1] * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (rotations[3][2] != 0.0f) {
            GlStateManager.func_179114_b(rotations[3][2] * 57.295776f, 0.0f, 0.0f, 1.0f);
        }
        GlStateManager.func_187447_r(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, (double)(-sneak), 0.0);
        GlStateManager.func_187437_J();
        GlStateManager.func_179121_F();
        GlStateManager.func_179094_E();
        GlStateManager.func_179137_b(0.125, (double)sneak, 0.0);
        if (rotations[4][0] != 0.0f) {
            GlStateManager.func_179114_b(rotations[4][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (rotations[4][1] != 0.0f) {
            GlStateManager.func_179114_b(rotations[4][1] * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (rotations[4][2] != 0.0f) {
            GlStateManager.func_179114_b(rotations[4][2] * 57.295776f, 0.0f, 0.0f, 1.0f);
        }
        GlStateManager.func_187447_r(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, (double)(-sneak), 0.0);
        GlStateManager.func_187437_J();
        GlStateManager.func_179121_F();
        GlStateManager.func_179137_b(0.0, 0.0, player.func_70093_af() ? 0.25 : 0.0);
        GlStateManager.func_179094_E();
        double sneakOffset = 0.0;
        if (player.func_70093_af()) {
            sneakOffset = -0.05;
        }
        GlStateManager.func_179137_b(0.0, sneakOffset, player.func_70093_af() ? -0.01725 : 0.0);
        GlStateManager.func_179094_E();
        GlStateManager.func_179137_b(-0.375, sneak + 0.55, 0.0);
        if (rotations[1][0] != 0.0f) {
            GlStateManager.func_179114_b(rotations[1][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (rotations[1][1] != 0.0f) {
            GlStateManager.func_179114_b(rotations[1][1] * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (rotations[1][2] != 0.0f) {
            GlStateManager.func_179114_b(-rotations[1][2] * 57.295776f, 0.0f, 0.0f, 1.0f);
        }
        GlStateManager.func_187447_r(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, -0.5, 0.0);
        GlStateManager.func_187437_J();
        GlStateManager.func_179121_F();
        GlStateManager.func_179094_E();
        GlStateManager.func_179137_b(0.375, sneak + 0.55, 0.0);
        if (rotations[2][0] != 0.0f) {
            GlStateManager.func_179114_b(rotations[2][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (rotations[2][1] != 0.0f) {
            GlStateManager.func_179114_b(rotations[2][1] * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (rotations[2][2] != 0.0f) {
            GlStateManager.func_179114_b(-rotations[2][2] * 57.295776f, 0.0f, 0.0f, 1.0f);
        }
        GlStateManager.func_187447_r(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, -0.5, 0.0);
        GlStateManager.func_187437_J();
        GlStateManager.func_179121_F();
        GlStateManager.func_179094_E();
        GlStateManager.func_179137_b(0.0, sneak + 0.55, 0.0);
        if (rotations[0][0] != 0.0f) {
            GlStateManager.func_179114_b(rotations[0][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        GlStateManager.func_187447_r(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.3, 0.0);
        GlStateManager.func_187437_J();
        GlStateManager.func_179121_F();
        GlStateManager.func_179121_F();
        GlStateManager.func_179114_b(player.func_70093_af() ? 25.0f : 0.0f, 1.0f, 0.0f, 0.0f);
        if (player.func_70093_af()) {
            sneakOffset = -0.16175;
        }
        GlStateManager.func_179137_b(0.0, sneakOffset, player.func_70093_af() ? -0.48025 : 0.0);
        GlStateManager.func_179094_E();
        GlStateManager.func_179137_b(0.0, (double)sneak, 0.0);
        GlStateManager.func_187447_r(3);
        GL11.glVertex3d(-0.125, 0.0, 0.0);
        GL11.glVertex3d(0.125, 0.0, 0.0);
        GlStateManager.func_187437_J();
        GlStateManager.func_179121_F();
        GlStateManager.func_179094_E();
        GlStateManager.func_179137_b(0.0, (double)sneak, 0.0);
        GlStateManager.func_187447_r(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.55, 0.0);
        GlStateManager.func_187437_J();
        GlStateManager.func_179121_F();
        GlStateManager.func_179094_E();
        GlStateManager.func_179137_b(0.0, sneak + 0.55, 0.0);
        GlStateManager.func_187447_r(3);
        GL11.glVertex3d(-0.375, 0.0, 0.0);
        GL11.glVertex3d(0.375, 0.0, 0.0);
        GlStateManager.func_187437_J();
        GlStateManager.func_179121_F();
        GlStateManager.func_179121_F();
    }
    
    static {
        Skeleton.INSTANCE = new Skeleton();
    }
}
