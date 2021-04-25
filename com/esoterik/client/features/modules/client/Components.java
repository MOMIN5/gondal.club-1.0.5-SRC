// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.client;

import net.minecraft.inventory.Slot;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import com.esoterik.client.util.MathUtil;
import net.minecraft.client.gui.ScaledResolution;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import com.esoterik.client.esohack;
import com.esoterik.client.util.EntityUtil;
import java.text.DecimalFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.GlStateManager;
import com.esoterik.client.util.RenderUtil;
import com.esoterik.client.util.ColorUtil;
import com.esoterik.client.event.events.Render2DEvent;
import com.esoterik.client.features.setting.Setting;
import net.minecraft.util.ResourceLocation;
import com.esoterik.client.features.modules.Module;

public class Components extends Module
{
    private static final ResourceLocation box;
    private ResourceLocation logo;
    private static final double HALF_PI = 1.5707963267948966;
    public Setting<Boolean> inventory;
    public Setting<Integer> invX;
    public Setting<Integer> invY;
    public Setting<Integer> fineinvX;
    public Setting<Integer> fineinvY;
    public Setting<Boolean> renderXCarry;
    public Setting<Integer> invH;
    public Setting<Boolean> holeHud;
    public Setting<Integer> holeX;
    public Setting<Integer> holeY;
    public Setting<Compass> compass;
    public Setting<Integer> compassX;
    public Setting<Integer> compassY;
    public Setting<Integer> scale;
    public Setting<Boolean> playerViewer;
    public Setting<Integer> playerViewerX;
    public Setting<Integer> playerViewerY;
    public Setting<Float> playerScale;
    public Setting<Boolean> imageLogo;
    public Setting<Integer> imageX;
    public Setting<Integer> imageY;
    public Setting<Integer> imageWidth;
    public Setting<Integer> imageHeight;
    public Setting<Boolean> targetHud;
    public Setting<Boolean> targetHudBackground;
    public Setting<Integer> targetHudX;
    public Setting<Integer> targetHudY;
    
    public Components() {
        super("Components", "HudComponents", Category.CLIENT, false, true, true);
        this.logo = new ResourceLocation("textures/client.png");
        this.inventory = (Setting<Boolean>)this.register(new Setting("Inventory", (T)false));
        this.invX = (Setting<Integer>)this.register(new Setting("InvX", (T)564, (T)0, (T)1000, v -> this.inventory.getValue()));
        this.invY = (Setting<Integer>)this.register(new Setting("InvY", (T)467, (T)0, (T)1000, v -> this.inventory.getValue()));
        this.fineinvX = (Setting<Integer>)this.register(new Setting("InvFineX", (T)0, v -> this.inventory.getValue()));
        this.fineinvY = (Setting<Integer>)this.register(new Setting("InvFineY", (T)0, v -> this.inventory.getValue()));
        this.renderXCarry = (Setting<Boolean>)this.register(new Setting("RenderXCarry", (T)false, v -> this.inventory.getValue()));
        this.invH = (Setting<Integer>)this.register(new Setting("InvH", (T)3, v -> this.inventory.getValue()));
        this.holeHud = (Setting<Boolean>)this.register(new Setting("HoleHUD", (T)false));
        this.holeX = (Setting<Integer>)this.register(new Setting("HoleX", (T)279, (T)0, (T)1000, v -> this.holeHud.getValue()));
        this.holeY = (Setting<Integer>)this.register(new Setting("HoleY", (T)485, (T)0, (T)1000, v -> this.holeHud.getValue()));
        this.compass = (Setting<Compass>)this.register(new Setting("Compass", (T)Compass.NONE));
        this.compassX = (Setting<Integer>)this.register(new Setting("CompX", (T)472, (T)0, (T)1000, v -> this.compass.getValue() != Compass.NONE));
        this.compassY = (Setting<Integer>)this.register(new Setting("CompY", (T)424, (T)0, (T)1000, v -> this.compass.getValue() != Compass.NONE));
        this.scale = (Setting<Integer>)this.register(new Setting("Scale", (T)3, (T)0, (T)10, v -> this.compass.getValue() != Compass.NONE));
        this.playerViewer = (Setting<Boolean>)this.register(new Setting("PlayerViewer", (T)false));
        this.playerViewerX = (Setting<Integer>)this.register(new Setting("PlayerX", (T)752, (T)0, (T)1000, v -> this.playerViewer.getValue()));
        this.playerViewerY = (Setting<Integer>)this.register(new Setting("PlayerY", (T)497, (T)0, (T)1000, v -> this.playerViewer.getValue()));
        this.playerScale = (Setting<Float>)this.register(new Setting("PlayerScale", (T)1.0f, (T)0.1f, (T)2.0f, v -> this.playerViewer.getValue()));
        this.imageLogo = (Setting<Boolean>)this.register(new Setting("ImageLogo", (T)false));
        this.imageX = (Setting<Integer>)this.register(new Setting("ImageX", (T)2, (T)0, (T)1000, v -> this.imageLogo.getValue()));
        this.imageY = (Setting<Integer>)this.register(new Setting("ImageY", (T)2, (T)0, (T)1000, v -> this.imageLogo.getValue()));
        this.imageWidth = (Setting<Integer>)this.register(new Setting("ImageWidth", (T)100, (T)0, (T)1000, v -> this.imageLogo.getValue()));
        this.imageHeight = (Setting<Integer>)this.register(new Setting("ImageHeight", (T)100, (T)0, (T)1000, v -> this.imageLogo.getValue()));
        this.targetHud = (Setting<Boolean>)this.register(new Setting("TargetHud", (T)false));
        this.targetHudBackground = (Setting<Boolean>)this.register(new Setting("TargetHudBackground", (T)true, v -> this.targetHud.getValue()));
        this.targetHudX = (Setting<Integer>)this.register(new Setting("TargetHudX", (T)2, (T)0, (T)1000, v -> this.targetHud.getValue()));
        this.targetHudY = (Setting<Integer>)this.register(new Setting("TargetHudY", (T)2, (T)0, (T)1000, v -> this.targetHud.getValue()));
    }
    
    @Override
    public void onRender2D(final Render2DEvent event) {
        if (fullNullCheck()) {
            return;
        }
        if (this.playerViewer.getValue()) {
            this.drawPlayer();
        }
        if (this.compass.getValue() != Compass.NONE) {
            this.drawCompass();
        }
        if (this.holeHud.getValue()) {
            this.drawOverlay(event.partialTicks);
        }
        if (this.inventory.getValue()) {
            this.renderInventory();
        }
        if (this.imageLogo.getValue()) {
            this.drawImageLogo();
        }
        if (this.targetHud.getValue()) {
            this.drawTargetHud(event.partialTicks);
        }
    }
    
    public void drawTargetHud(final float partialTicks) {
        final EntityPlayer target = getClosestEnemy();
        if (target == null) {
            return;
        }
        if (this.targetHudBackground.getValue()) {
            RenderUtil.drawRectangleCorrectly(this.targetHudX.getValue(), this.targetHudY.getValue(), 210, 100, ColorUtil.toRGBA(20, 20, 20, 160));
        }
        GlStateManager.func_179101_C();
        GlStateManager.func_179138_g(OpenGlHelper.field_77476_b);
        GlStateManager.func_179090_x();
        GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        try {
            GuiInventory.func_147046_a(this.targetHudX.getValue() + 30, this.targetHudY.getValue() + 90, 45, 0.0f, 0.0f, (EntityLivingBase)target);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        GlStateManager.func_179091_B();
        GlStateManager.func_179098_w();
        GlStateManager.func_179147_l();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        this.renderer.drawStringWithShadow(target.func_70005_c_(), (float)(this.targetHudX.getValue() + 60), (float)(this.targetHudY.getValue() + 10), ColorUtil.toRGBA(255, 0, 0, 255));
        final float health = target.func_110143_aJ() + target.func_110139_bj();
        int healthColor;
        if (health >= 16.0f) {
            healthColor = ColorUtil.toRGBA(0, 255, 0, 255);
        }
        else if (health >= 10.0f) {
            healthColor = ColorUtil.toRGBA(255, 255, 0, 255);
        }
        else {
            healthColor = ColorUtil.toRGBA(255, 0, 0, 255);
        }
        final DecimalFormat df = new DecimalFormat("##.#");
        this.renderer.drawStringWithShadow(df.format(target.func_110143_aJ() + target.func_110139_bj()), (float)(this.targetHudX.getValue() + 60 + this.renderer.getStringWidth(target.func_70005_c_() + "  ")), (float)(this.targetHudY.getValue() + 10), healthColor);
        final Integer ping = EntityUtil.isFakePlayer(target) ? 0 : ((Components.mc.func_147114_u().func_175102_a(target.func_110124_au()) == null) ? 0 : Components.mc.func_147114_u().func_175102_a(target.func_110124_au()).func_178853_c());
        int color;
        if (ping >= 100) {
            color = ColorUtil.toRGBA(0, 255, 0, 255);
        }
        else if (ping > 50) {
            color = ColorUtil.toRGBA(255, 255, 0, 255);
        }
        else {
            color = ColorUtil.toRGBA(255, 0, 0, 255);
        }
        this.renderer.drawStringWithShadow("Ping: " + ((ping == null) ? 0 : ping), (float)(this.targetHudX.getValue() + 60), (float)(this.targetHudY.getValue() + this.renderer.getFontHeight() + 20), color);
        this.renderer.drawStringWithShadow("Pops: " + esohack.totemPopManager.getTotemPops(target), (float)(this.targetHudX.getValue() + 60), (float)(this.targetHudY.getValue() + this.renderer.getFontHeight() * 2 + 30), ColorUtil.toRGBA(255, 0, 0, 255));
        GlStateManager.func_179098_w();
        int iteration = 0;
        final int i = this.targetHudX.getValue() + 50;
        final int y = this.targetHudY.getValue() + this.renderer.getFontHeight() * 3 + 44;
        for (final ItemStack is : target.field_71071_by.field_70460_b) {
            ++iteration;
            if (is.func_190926_b()) {
                continue;
            }
            final int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.func_179126_j();
            RenderUtil.itemRender.field_77023_b = 200.0f;
            RenderUtil.itemRender.func_180450_b(is, x, y);
            RenderUtil.itemRender.func_180453_a(Components.mc.field_71466_p, is, x, y, "");
            RenderUtil.itemRender.field_77023_b = 0.0f;
            GlStateManager.func_179098_w();
            GlStateManager.func_179140_f();
            GlStateManager.func_179097_i();
            final String s = (is.func_190916_E() > 1) ? (is.func_190916_E() + "") : "";
            this.renderer.drawStringWithShadow(s, (float)(x + 19 - 2 - this.renderer.getStringWidth(s)), (float)(y + 9), 16777215);
            int dmg = 0;
            final int itemDurability = is.func_77958_k() - is.func_77952_i();
            final float green = (is.func_77958_k() - (float)is.func_77952_i()) / is.func_77958_k();
            final float red = 1.0f - green;
            dmg = 100 - (int)(red * 100.0f);
            this.renderer.drawStringWithShadow(dmg + "", x + 8 - this.renderer.getStringWidth(dmg + "") / 2.0f, (float)(y - 5), ColorUtil.toRGBA((int)(red * 255.0f), (int)(green * 255.0f), 0));
        }
        this.drawOverlay(partialTicks, (Entity)target, this.targetHudX.getValue() + 150, this.targetHudY.getValue() + 6);
        this.renderer.drawStringWithShadow("Strength", (float)(this.targetHudX.getValue() + 150), (float)(this.targetHudY.getValue() + 60), target.func_70644_a(MobEffects.field_76420_g) ? ColorUtil.toRGBA(0, 255, 0, 255) : ColorUtil.toRGBA(255, 0, 0, 255));
        this.renderer.drawStringWithShadow("Weakness", (float)(this.targetHudX.getValue() + 150), (float)(this.targetHudY.getValue() + this.renderer.getFontHeight() + 70), target.func_70644_a(MobEffects.field_76437_t) ? ColorUtil.toRGBA(0, 255, 0, 255) : ColorUtil.toRGBA(255, 0, 0, 255));
    }
    
    public static EntityPlayer getClosestEnemy() {
        EntityPlayer closestPlayer = null;
        for (final EntityPlayer player : Components.mc.field_71441_e.field_73010_i) {
            if (player == Components.mc.field_71439_g) {
                continue;
            }
            if (esohack.friendManager.isFriend(player)) {
                continue;
            }
            if (closestPlayer == null) {
                closestPlayer = player;
            }
            else {
                if (Components.mc.field_71439_g.func_70068_e((Entity)player) >= Components.mc.field_71439_g.func_70068_e((Entity)closestPlayer)) {
                    continue;
                }
                closestPlayer = player;
            }
        }
        return closestPlayer;
    }
    
    public void drawImageLogo() {
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
        Components.mc.func_110434_K().func_110577_a(this.logo);
        drawCompleteImage(this.imageX.getValue(), this.imageY.getValue(), this.imageWidth.getValue(), this.imageHeight.getValue());
        Components.mc.func_110434_K().func_147645_c(this.logo);
        GlStateManager.func_179147_l();
        GlStateManager.func_179090_x();
    }
    
    public void drawCompass() {
        final ScaledResolution sr = new ScaledResolution(Components.mc);
        if (this.compass.getValue() == Compass.LINE) {
            final float playerYaw = Components.mc.field_71439_g.field_70177_z;
            final float rotationYaw = MathUtil.wrap(playerYaw);
            RenderUtil.drawRect(this.compassX.getValue(), this.compassY.getValue(), (float)(this.compassX.getValue() + 100), (float)(this.compassY.getValue() + this.renderer.getFontHeight()), 1963986960);
            RenderUtil.glScissor(this.compassX.getValue(), this.compassY.getValue(), (float)(this.compassX.getValue() + 100), (float)(this.compassY.getValue() + this.renderer.getFontHeight()), sr);
            GL11.glEnable(3089);
            final float zeroZeroYaw = MathUtil.wrap((float)(Math.atan2(0.0 - Components.mc.field_71439_g.field_70161_v, 0.0 - Components.mc.field_71439_g.field_70165_t) * 180.0 / 3.141592653589793) - 90.0f);
            RenderUtil.drawLine(this.compassX.getValue() - rotationYaw + 50.0f + zeroZeroYaw, (float)(this.compassY.getValue() + 2), this.compassX.getValue() - rotationYaw + 50.0f + zeroZeroYaw, (float)(this.compassY.getValue() + this.renderer.getFontHeight() - 2), 2.0f, -61424);
            RenderUtil.drawLine(this.compassX.getValue() - rotationYaw + 50.0f + 45.0f, (float)(this.compassY.getValue() + 2), this.compassX.getValue() - rotationYaw + 50.0f + 45.0f, (float)(this.compassY.getValue() + this.renderer.getFontHeight() - 2), 2.0f, -1);
            RenderUtil.drawLine(this.compassX.getValue() - rotationYaw + 50.0f - 45.0f, (float)(this.compassY.getValue() + 2), this.compassX.getValue() - rotationYaw + 50.0f - 45.0f, (float)(this.compassY.getValue() + this.renderer.getFontHeight() - 2), 2.0f, -1);
            RenderUtil.drawLine(this.compassX.getValue() - rotationYaw + 50.0f + 135.0f, (float)(this.compassY.getValue() + 2), this.compassX.getValue() - rotationYaw + 50.0f + 135.0f, (float)(this.compassY.getValue() + this.renderer.getFontHeight() - 2), 2.0f, -1);
            RenderUtil.drawLine(this.compassX.getValue() - rotationYaw + 50.0f - 135.0f, (float)(this.compassY.getValue() + 2), this.compassX.getValue() - rotationYaw + 50.0f - 135.0f, (float)(this.compassY.getValue() + this.renderer.getFontHeight() - 2), 2.0f, -1);
            this.renderer.drawStringWithShadow("n", this.compassX.getValue() - rotationYaw + 50.0f + 180.0f - this.renderer.getStringWidth("n") / 2.0f, this.compassY.getValue(), -1);
            this.renderer.drawStringWithShadow("n", this.compassX.getValue() - rotationYaw + 50.0f - 180.0f - this.renderer.getStringWidth("n") / 2.0f, this.compassY.getValue(), -1);
            this.renderer.drawStringWithShadow("e", this.compassX.getValue() - rotationYaw + 50.0f - 90.0f - this.renderer.getStringWidth("e") / 2.0f, this.compassY.getValue(), -1);
            this.renderer.drawStringWithShadow("s", this.compassX.getValue() - rotationYaw + 50.0f - this.renderer.getStringWidth("s") / 2.0f, this.compassY.getValue(), -1);
            this.renderer.drawStringWithShadow("w", this.compassX.getValue() - rotationYaw + 50.0f + 90.0f - this.renderer.getStringWidth("w") / 2.0f, this.compassY.getValue(), -1);
            RenderUtil.drawLine((float)(this.compassX.getValue() + 50), (float)(this.compassY.getValue() + 1), (float)(this.compassX.getValue() + 50), (float)(this.compassY.getValue() + this.renderer.getFontHeight() - 1), 2.0f, -7303024);
            GL11.glDisable(3089);
        }
        else {
            final double centerX = this.compassX.getValue();
            final double centerY = this.compassY.getValue();
            for (final Direction dir : Direction.values()) {
                final double rad = getPosOnCompass(dir);
                this.renderer.drawStringWithShadow(dir.name(), (float)(centerX + this.getX(rad)), (float)(centerY + this.getY(rad)), (dir == Direction.N) ? -65536 : -1);
            }
        }
    }
    
    public void drawPlayer(final EntityPlayer player, final int x, final int y) {
        final EntityPlayer ent = player;
        GlStateManager.func_179094_E();
        GlStateManager.func_179124_c(1.0f, 1.0f, 1.0f);
        RenderHelper.func_74519_b();
        GlStateManager.func_179141_d();
        GlStateManager.func_179103_j(7424);
        GlStateManager.func_179141_d();
        GlStateManager.func_179126_j();
        GlStateManager.func_179114_b(0.0f, 0.0f, 5.0f, 0.0f);
        GlStateManager.func_179142_g();
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)(this.playerViewerX.getValue() + 25), (float)(this.playerViewerY.getValue() + 25), 50.0f);
        GlStateManager.func_179152_a(-50.0f * this.playerScale.getValue(), 50.0f * this.playerScale.getValue(), 50.0f * this.playerScale.getValue());
        GlStateManager.func_179114_b(180.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.func_179114_b(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.func_74519_b();
        GlStateManager.func_179114_b(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.func_179114_b(-(float)Math.atan(this.playerViewerY.getValue() / 40.0f) * 20.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.func_179109_b(0.0f, 0.0f, 0.0f);
        final RenderManager rendermanager = Components.mc.func_175598_ae();
        rendermanager.func_178631_a(180.0f);
        rendermanager.func_178633_a(false);
        try {
            rendermanager.func_188391_a((Entity)ent, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
        }
        catch (Exception ex) {}
        rendermanager.func_178633_a(true);
        GlStateManager.func_179121_F();
        RenderHelper.func_74518_a();
        GlStateManager.func_179101_C();
        GlStateManager.func_179138_g(OpenGlHelper.field_77476_b);
        GlStateManager.func_179090_x();
        GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
        GlStateManager.func_179143_c(515);
        GlStateManager.func_179117_G();
        GlStateManager.func_179097_i();
        GlStateManager.func_179121_F();
    }
    
    public void drawPlayer() {
        final EntityPlayer ent = (EntityPlayer)Components.mc.field_71439_g;
        GlStateManager.func_179094_E();
        GlStateManager.func_179124_c(1.0f, 1.0f, 1.0f);
        RenderHelper.func_74519_b();
        GlStateManager.func_179141_d();
        GlStateManager.func_179103_j(7424);
        GlStateManager.func_179141_d();
        GlStateManager.func_179126_j();
        GlStateManager.func_179114_b(0.0f, 0.0f, 5.0f, 0.0f);
        GlStateManager.func_179142_g();
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)(this.playerViewerX.getValue() + 25), (float)(this.playerViewerY.getValue() + 25), 50.0f);
        GlStateManager.func_179152_a(-50.0f * this.playerScale.getValue(), 50.0f * this.playerScale.getValue(), 50.0f * this.playerScale.getValue());
        GlStateManager.func_179114_b(180.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.func_179114_b(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.func_74519_b();
        GlStateManager.func_179114_b(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.func_179114_b(-(float)Math.atan(this.playerViewerY.getValue() / 40.0f) * 20.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.func_179109_b(0.0f, 0.0f, 0.0f);
        final RenderManager rendermanager = Components.mc.func_175598_ae();
        rendermanager.func_178631_a(180.0f);
        rendermanager.func_178633_a(false);
        try {
            rendermanager.func_188391_a((Entity)ent, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
        }
        catch (Exception ex) {}
        rendermanager.func_178633_a(true);
        GlStateManager.func_179121_F();
        RenderHelper.func_74518_a();
        GlStateManager.func_179101_C();
        GlStateManager.func_179138_g(OpenGlHelper.field_77476_b);
        GlStateManager.func_179090_x();
        GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
        GlStateManager.func_179143_c(515);
        GlStateManager.func_179117_G();
        GlStateManager.func_179097_i();
        GlStateManager.func_179121_F();
    }
    
    private double getX(final double rad) {
        return Math.sin(rad) * (this.scale.getValue() * 10);
    }
    
    private double getY(final double rad) {
        final double epicPitch = MathHelper.func_76131_a(Components.mc.field_71439_g.field_70125_A + 30.0f, -90.0f, 90.0f);
        final double pitchRadians = Math.toRadians(epicPitch);
        return Math.cos(rad) * Math.sin(pitchRadians) * (this.scale.getValue() * 10);
    }
    
    private static double getPosOnCompass(final Direction dir) {
        final double yaw = Math.toRadians(MathHelper.func_76142_g(Components.mc.field_71439_g.field_70177_z));
        final int index = dir.ordinal();
        return yaw + index * 1.5707963267948966;
    }
    
    public void drawOverlay(final float partialTicks) {
        float yaw = 0.0f;
        final int dir = MathHelper.func_76128_c(Components.mc.field_71439_g.field_70177_z * 4.0f / 360.0f + 0.5) & 0x3;
        switch (dir) {
            case 1: {
                yaw = 90.0f;
                break;
            }
            case 2: {
                yaw = -180.0f;
                break;
            }
            case 3: {
                yaw = -90.0f;
                break;
            }
        }
        final BlockPos northPos = this.traceToBlock(partialTicks, yaw);
        final Block north = this.getBlock(northPos);
        if (north != null && north != Blocks.field_150350_a) {
            final int damage = this.getBlockDamage(northPos);
            if (damage != 0) {
                RenderUtil.drawRect((float)(this.holeX.getValue() + 16), this.holeY.getValue(), (float)(this.holeX.getValue() + 32), (float)(this.holeY.getValue() + 16), 1627324416);
            }
            this.drawBlock(north, (float)(this.holeX.getValue() + 16), this.holeY.getValue());
        }
        final BlockPos southPos = this.traceToBlock(partialTicks, yaw - 180.0f);
        final Block south = this.getBlock(southPos);
        if (south != null && south != Blocks.field_150350_a) {
            final int damage2 = this.getBlockDamage(southPos);
            if (damage2 != 0) {
                RenderUtil.drawRect((float)(this.holeX.getValue() + 16), (float)(this.holeY.getValue() + 32), (float)(this.holeX.getValue() + 32), (float)(this.holeY.getValue() + 48), 1627324416);
            }
            this.drawBlock(south, (float)(this.holeX.getValue() + 16), (float)(this.holeY.getValue() + 32));
        }
        final BlockPos eastPos = this.traceToBlock(partialTicks, yaw + 90.0f);
        final Block east = this.getBlock(eastPos);
        if (east != null && east != Blocks.field_150350_a) {
            final int damage3 = this.getBlockDamage(eastPos);
            if (damage3 != 0) {
                RenderUtil.drawRect((float)(this.holeX.getValue() + 32), (float)(this.holeY.getValue() + 16), (float)(this.holeX.getValue() + 48), (float)(this.holeY.getValue() + 32), 1627324416);
            }
            this.drawBlock(east, (float)(this.holeX.getValue() + 32), (float)(this.holeY.getValue() + 16));
        }
        final BlockPos westPos = this.traceToBlock(partialTicks, yaw - 90.0f);
        final Block west = this.getBlock(westPos);
        if (west != null && west != Blocks.field_150350_a) {
            final int damage4 = this.getBlockDamage(westPos);
            if (damage4 != 0) {
                RenderUtil.drawRect(this.holeX.getValue(), (float)(this.holeY.getValue() + 16), (float)(this.holeX.getValue() + 16), (float)(this.holeY.getValue() + 32), 1627324416);
            }
            this.drawBlock(west, this.holeX.getValue(), (float)(this.holeY.getValue() + 16));
        }
    }
    
    public void drawOverlay(final float partialTicks, final Entity player, final int x, final int y) {
        float yaw = 0.0f;
        final int dir = MathHelper.func_76128_c(player.field_70177_z * 4.0f / 360.0f + 0.5) & 0x3;
        switch (dir) {
            case 1: {
                yaw = 90.0f;
                break;
            }
            case 2: {
                yaw = -180.0f;
                break;
            }
            case 3: {
                yaw = -90.0f;
                break;
            }
        }
        final BlockPos northPos = this.traceToBlock(partialTicks, yaw, player);
        final Block north = this.getBlock(northPos);
        if (north != null && north != Blocks.field_150350_a) {
            final int damage = this.getBlockDamage(northPos);
            if (damage != 0) {
                RenderUtil.drawRect((float)(x + 16), (float)y, (float)(x + 32), (float)(y + 16), 1627324416);
            }
            this.drawBlock(north, (float)(x + 16), (float)y);
        }
        final BlockPos southPos = this.traceToBlock(partialTicks, yaw - 180.0f, player);
        final Block south = this.getBlock(southPos);
        if (south != null && south != Blocks.field_150350_a) {
            final int damage2 = this.getBlockDamage(southPos);
            if (damage2 != 0) {
                RenderUtil.drawRect((float)(x + 16), (float)(y + 32), (float)(x + 32), (float)(y + 48), 1627324416);
            }
            this.drawBlock(south, (float)(x + 16), (float)(y + 32));
        }
        final BlockPos eastPos = this.traceToBlock(partialTicks, yaw + 90.0f, player);
        final Block east = this.getBlock(eastPos);
        if (east != null && east != Blocks.field_150350_a) {
            final int damage3 = this.getBlockDamage(eastPos);
            if (damage3 != 0) {
                RenderUtil.drawRect((float)(x + 32), (float)(y + 16), (float)(x + 48), (float)(y + 32), 1627324416);
            }
            this.drawBlock(east, (float)(x + 32), (float)(y + 16));
        }
        final BlockPos westPos = this.traceToBlock(partialTicks, yaw - 90.0f, player);
        final Block west = this.getBlock(westPos);
        if (west != null && west != Blocks.field_150350_a) {
            final int damage4 = this.getBlockDamage(westPos);
            if (damage4 != 0) {
                RenderUtil.drawRect((float)x, (float)(y + 16), (float)(x + 16), (float)(y + 32), 1627324416);
            }
            this.drawBlock(west, (float)x, (float)(y + 16));
        }
    }
    
    private int getBlockDamage(final BlockPos pos) {
        for (final DestroyBlockProgress destBlockProgress : Components.mc.field_71438_f.field_72738_E.values()) {
            if (destBlockProgress.func_180246_b().func_177958_n() == pos.func_177958_n() && destBlockProgress.func_180246_b().func_177956_o() == pos.func_177956_o() && destBlockProgress.func_180246_b().func_177952_p() == pos.func_177952_p()) {
                return destBlockProgress.func_73106_e();
            }
        }
        return 0;
    }
    
    private BlockPos traceToBlock(final float partialTicks, final float yaw) {
        final Vec3d pos = EntityUtil.interpolateEntity((Entity)Components.mc.field_71439_g, partialTicks);
        final Vec3d dir = MathUtil.direction(yaw);
        return new BlockPos(pos.field_72450_a + dir.field_72450_a, pos.field_72448_b, pos.field_72449_c + dir.field_72449_c);
    }
    
    private BlockPos traceToBlock(final float partialTicks, final float yaw, final Entity player) {
        final Vec3d pos = EntityUtil.interpolateEntity(player, partialTicks);
        final Vec3d dir = MathUtil.direction(yaw);
        return new BlockPos(pos.field_72450_a + dir.field_72450_a, pos.field_72448_b, pos.field_72449_c + dir.field_72449_c);
    }
    
    private Block getBlock(final BlockPos pos) {
        final Block block = Components.mc.field_71441_e.func_180495_p(pos).func_177230_c();
        if (block == Blocks.field_150357_h || block == Blocks.field_150343_Z) {
            return block;
        }
        return Blocks.field_150350_a;
    }
    
    private void drawBlock(final Block block, final float x, final float y) {
        final ItemStack stack = new ItemStack(block);
        GlStateManager.func_179094_E();
        GlStateManager.func_179147_l();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        RenderHelper.func_74520_c();
        GlStateManager.func_179109_b(x, y, 0.0f);
        Components.mc.func_175599_af().field_77023_b = 501.0f;
        Components.mc.func_175599_af().func_180450_b(stack, 0, 0);
        Components.mc.func_175599_af().field_77023_b = 0.0f;
        RenderHelper.func_74518_a();
        GlStateManager.func_179084_k();
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179121_F();
    }
    
    public void renderInventory() {
        this.boxrender(this.invX.getValue() + this.fineinvX.getValue(), this.invY.getValue() + this.fineinvY.getValue());
        this.itemrender((NonNullList<ItemStack>)Components.mc.field_71439_g.field_71071_by.field_70462_a, this.invX.getValue() + this.fineinvX.getValue(), this.invY.getValue() + this.fineinvY.getValue());
    }
    
    private static void preboxrender() {
        GL11.glPushMatrix();
        GlStateManager.func_179094_E();
        GlStateManager.func_179118_c();
        GlStateManager.func_179086_m(256);
        GlStateManager.func_179147_l();
        GlStateManager.func_179131_c(255.0f, 255.0f, 255.0f, 255.0f);
    }
    
    private static void postboxrender() {
        GlStateManager.func_179084_k();
        GlStateManager.func_179097_i();
        GlStateManager.func_179140_f();
        GlStateManager.func_179126_j();
        GlStateManager.func_179141_d();
        GlStateManager.func_179121_F();
        GL11.glPopMatrix();
    }
    
    private static void preitemrender() {
        GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GlStateManager.func_179086_m(256);
        GlStateManager.func_179097_i();
        GlStateManager.func_179126_j();
        RenderHelper.func_74519_b();
        GlStateManager.func_179152_a(1.0f, 1.0f, 0.01f);
    }
    
    private static void postitemrender() {
        GlStateManager.func_179152_a(1.0f, 1.0f, 1.0f);
        RenderHelper.func_74518_a();
        GlStateManager.func_179141_d();
        GlStateManager.func_179084_k();
        GlStateManager.func_179140_f();
        GlStateManager.func_179139_a(0.5, 0.5, 0.5);
        GlStateManager.func_179097_i();
        GlStateManager.func_179126_j();
        GlStateManager.func_179152_a(2.0f, 2.0f, 2.0f);
        GL11.glPopMatrix();
    }
    
    private void boxrender(final int x, final int y) {
        preboxrender();
        Components.mc.field_71446_o.func_110577_a(Components.box);
        RenderUtil.drawTexturedRect(x, y, 0, 0, 176, 16, 500);
        RenderUtil.drawTexturedRect(x, y + 16, 0, 16, 176, 54 + this.invH.getValue(), 500);
        RenderUtil.drawTexturedRect(x, y + 16 + 54, 0, 160, 176, 8, 500);
        postboxrender();
    }
    
    private void itemrender(final NonNullList<ItemStack> items, final int x, final int y) {
        for (int i = 0; i < items.size() - 9; ++i) {
            final int iX = x + i % 9 * 18 + 8;
            final int iY = y + i / 9 * 18 + 18;
            final ItemStack itemStack = (ItemStack)items.get(i + 9);
            preitemrender();
            Components.mc.func_175599_af().field_77023_b = 501.0f;
            RenderUtil.itemRender.func_180450_b(itemStack, iX, iY);
            RenderUtil.itemRender.func_180453_a(Components.mc.field_71466_p, itemStack, iX, iY, (String)null);
            Components.mc.func_175599_af().field_77023_b = 0.0f;
            postitemrender();
        }
        if (this.renderXCarry.getValue()) {
            for (int i = 1; i < 5; ++i) {
                final int iX = x + (i + 4) % 9 * 18 + 8;
                final ItemStack itemStack2 = Components.mc.field_71439_g.field_71069_bz.field_75151_b.get(i).func_75211_c();
                if (itemStack2 != null && !itemStack2.field_190928_g) {
                    preitemrender();
                    Components.mc.func_175599_af().field_77023_b = 501.0f;
                    RenderUtil.itemRender.func_180450_b(itemStack2, iX, y + 1);
                    RenderUtil.itemRender.func_180453_a(Components.mc.field_71466_p, itemStack2, iX, y + 1, (String)null);
                    Components.mc.func_175599_af().field_77023_b = 0.0f;
                    postitemrender();
                }
            }
        }
    }
    
    public static void drawCompleteImage(final int posX, final int posY, final int width, final int height) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)posX, (float)posY, 0.0f);
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, (float)height, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f((float)width, (float)height, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f((float)width, 0.0f, 0.0f);
        GL11.glEnd();
        GL11.glPopMatrix();
    }
    
    static {
        box = new ResourceLocation("textures/gui/container/shulker_box.png");
    }
    
    private enum Direction
    {
        N, 
        W, 
        S, 
        E;
    }
    
    public enum Compass
    {
        NONE, 
        CIRCLE, 
        LINE;
    }
}
