// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.render;

import com.esoterik.client.esohack;
import java.util.Objects;
import net.minecraft.client.network.NetHandlerPlayClient;
import com.esoterik.client.util.EntityUtil;
import net.minecraft.nbt.NBTTagList;
import com.esoterik.client.util.DamageUtil;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemTool;
import com.esoterik.client.util.RenderUtil;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import com.esoterik.client.event.events.Render3DEvent;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class Nametags extends Module
{
    private final Setting<Boolean> health;
    private final Setting<Boolean> armor;
    private final Setting<Float> scaling;
    private final Setting<Boolean> invisibles;
    private final Setting<Boolean> ping;
    private final Setting<Boolean> totemPops;
    private final Setting<Boolean> gamemode;
    private final Setting<Boolean> entityID;
    private final Setting<Boolean> rect;
    private final Setting<Boolean> sneak;
    private final Setting<Boolean> heldStackName;
    private final Setting<Boolean> whiter;
    private final Setting<Boolean> scaleing;
    private final Setting<Float> factor;
    private final Setting<Boolean> smartScale;
    private static Nametags INSTANCE;
    
    public Nametags() {
        super("Nametags", "Better Nametags", Category.RENDER, false, false, false);
        this.health = (Setting<Boolean>)this.register(new Setting("Health", (T)true));
        this.armor = (Setting<Boolean>)this.register(new Setting("Armor", (T)true));
        this.scaling = (Setting<Float>)this.register(new Setting("Size", (T)0.3f, (T)0.1f, (T)20.0f));
        this.invisibles = (Setting<Boolean>)this.register(new Setting("Invisibles", (T)false));
        this.ping = (Setting<Boolean>)this.register(new Setting("Ping", (T)true));
        this.totemPops = (Setting<Boolean>)this.register(new Setting("TotemPops", (T)true));
        this.gamemode = (Setting<Boolean>)this.register(new Setting("Gamemode", (T)false));
        this.entityID = (Setting<Boolean>)this.register(new Setting("ID", (T)false));
        this.rect = (Setting<Boolean>)this.register(new Setting("Rectangle", (T)true));
        this.sneak = (Setting<Boolean>)this.register(new Setting("SneakColor", (T)false));
        this.heldStackName = (Setting<Boolean>)this.register(new Setting("StackName", (T)false));
        this.whiter = (Setting<Boolean>)this.register(new Setting("White", (T)false));
        this.scaleing = (Setting<Boolean>)this.register(new Setting("Scale", (T)false));
        this.factor = (Setting<Float>)this.register(new Setting("Factor", (T)0.3f, (T)0.1f, (T)1.0f, v -> this.scaleing.getValue()));
        this.smartScale = (Setting<Boolean>)this.register(new Setting("SmartScale", (T)false, v -> this.scaleing.getValue()));
        this.setInstance();
    }
    
    private void setInstance() {
        Nametags.INSTANCE = this;
    }
    
    public static Nametags getInstance() {
        if (Nametags.INSTANCE == null) {
            Nametags.INSTANCE = new Nametags();
        }
        return Nametags.INSTANCE;
    }
    
    @Override
    public void onRender3D(final Render3DEvent event) {
        for (final EntityPlayer player : Nametags.mc.field_71441_e.field_73010_i) {
            if (player != null && !player.equals((Object)Nametags.mc.field_71439_g) && player.func_70089_S() && (!player.func_82150_aj() || this.invisibles.getValue())) {
                final double x = this.interpolate(player.field_70142_S, player.field_70165_t, event.getPartialTicks()) - Nametags.mc.func_175598_ae().field_78725_b;
                final double y = this.interpolate(player.field_70137_T, player.field_70163_u, event.getPartialTicks()) - Nametags.mc.func_175598_ae().field_78726_c;
                final double z = this.interpolate(player.field_70136_U, player.field_70161_v, event.getPartialTicks()) - Nametags.mc.func_175598_ae().field_78723_d;
                this.renderNameTag(player, x, y, z, event.getPartialTicks());
            }
        }
    }
    
    private void renderNameTag(final EntityPlayer player, final double x, final double y, final double z, final float delta) {
        double tempY = y;
        tempY += (player.func_70093_af() ? 0.5 : 0.7);
        final Entity camera = Nametags.mc.func_175606_aa();
        assert camera != null;
        final double originalPositionX = camera.field_70165_t;
        final double originalPositionY = camera.field_70163_u;
        final double originalPositionZ = camera.field_70161_v;
        camera.field_70165_t = this.interpolate(camera.field_70169_q, camera.field_70165_t, delta);
        camera.field_70163_u = this.interpolate(camera.field_70167_r, camera.field_70163_u, delta);
        camera.field_70161_v = this.interpolate(camera.field_70166_s, camera.field_70161_v, delta);
        final String displayTag = this.getDisplayTag(player);
        final double distance = camera.func_70011_f(x + Nametags.mc.func_175598_ae().field_78730_l, y + Nametags.mc.func_175598_ae().field_78731_m, z + Nametags.mc.func_175598_ae().field_78728_n);
        final int width = this.renderer.getStringWidth(displayTag) / 2;
        double scale = (0.0018 + this.scaling.getValue() * (distance * this.factor.getValue())) / 1000.0;
        if (distance <= 8.0 && this.smartScale.getValue()) {
            scale = 0.0245;
        }
        if (!this.scaleing.getValue()) {
            scale = this.scaling.getValue() / 100.0;
        }
        GlStateManager.func_179094_E();
        RenderHelper.func_74519_b();
        GlStateManager.func_179088_q();
        GlStateManager.func_179136_a(1.0f, -1500000.0f);
        GlStateManager.func_179140_f();
        GlStateManager.func_179109_b((float)x, (float)tempY + 1.4f, (float)z);
        GlStateManager.func_179114_b(-Nametags.mc.func_175598_ae().field_78735_i, 0.0f, 1.0f, 0.0f);
        GlStateManager.func_179114_b(Nametags.mc.func_175598_ae().field_78732_j, (Nametags.mc.field_71474_y.field_74320_O == 2) ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.func_179139_a(-scale, -scale, scale);
        GlStateManager.func_179097_i();
        GlStateManager.func_179147_l();
        GlStateManager.func_179147_l();
        if (this.rect.getValue()) {
            RenderUtil.drawRect((float)(-width - 2), (float)(-(this.renderer.getFontHeight() + 1)), width + 2.0f, 1.5f, 1426063360);
        }
        GlStateManager.func_179084_k();
        final ItemStack renderMainHand = player.func_184614_ca().func_77946_l();
        if (renderMainHand.func_77962_s() && (renderMainHand.func_77973_b() instanceof ItemTool || renderMainHand.func_77973_b() instanceof ItemArmor)) {
            renderMainHand.field_77994_a = 1;
        }
        if (this.heldStackName.getValue() && !renderMainHand.field_190928_g && renderMainHand.func_77973_b() != Items.field_190931_a) {
            final String stackName = renderMainHand.func_82833_r();
            final int stackNameWidth = this.renderer.getStringWidth(stackName) / 2;
            GL11.glPushMatrix();
            GL11.glScalef(0.75f, 0.75f, 0.0f);
            this.renderer.drawStringWithShadow(stackName, (float)(-stackNameWidth), -(this.getBiggestArmorTag(player) + 20.0f), -1);
            GL11.glScalef(1.5f, 1.5f, 1.0f);
            GL11.glPopMatrix();
        }
        if (this.armor.getValue()) {
            GlStateManager.func_179094_E();
            int xOffset = -8;
            for (final ItemStack stack : player.field_71071_by.field_70460_b) {
                if (stack != null) {
                    xOffset -= 8;
                }
            }
            xOffset -= 8;
            final ItemStack renderOffhand = player.func_184592_cb().func_77946_l();
            if (renderOffhand.func_77962_s() && (renderOffhand.func_77973_b() instanceof ItemTool || renderOffhand.func_77973_b() instanceof ItemArmor)) {
                renderOffhand.field_77994_a = 1;
            }
            this.renderItemStack(renderOffhand, xOffset, -26);
            xOffset += 16;
            for (final ItemStack stack2 : player.field_71071_by.field_70460_b) {
                if (stack2 != null) {
                    final ItemStack armourStack = stack2.func_77946_l();
                    if (armourStack.func_77962_s() && (armourStack.func_77973_b() instanceof ItemTool || armourStack.func_77973_b() instanceof ItemArmor)) {
                        armourStack.field_77994_a = 1;
                    }
                    this.renderItemStack(armourStack, xOffset, -26);
                    xOffset += 16;
                }
            }
            this.renderItemStack(renderMainHand, xOffset, -26);
            GlStateManager.func_179121_F();
        }
        this.renderer.drawStringWithShadow(displayTag, (float)(-width), (float)(-(this.renderer.getFontHeight() - 1)), this.getDisplayColour(player));
        camera.field_70165_t = originalPositionX;
        camera.field_70163_u = originalPositionY;
        camera.field_70161_v = originalPositionZ;
        GlStateManager.func_179126_j();
        GlStateManager.func_179084_k();
        GlStateManager.func_179113_r();
        GlStateManager.func_179136_a(1.0f, 1500000.0f);
        GlStateManager.func_179121_F();
    }
    
    private void renderItemStack(final ItemStack stack, final int x, final int y) {
        GlStateManager.func_179094_E();
        GlStateManager.func_179132_a(true);
        GlStateManager.func_179086_m(256);
        RenderHelper.func_74519_b();
        Nametags.mc.func_175599_af().field_77023_b = -150.0f;
        GlStateManager.func_179118_c();
        GlStateManager.func_179126_j();
        GlStateManager.func_179129_p();
        Nametags.mc.func_175599_af().func_180450_b(stack, x, y);
        Nametags.mc.func_175599_af().func_175030_a(Nametags.mc.field_71466_p, stack, x, y);
        Nametags.mc.func_175599_af().field_77023_b = 0.0f;
        RenderHelper.func_74518_a();
        GlStateManager.func_179089_o();
        GlStateManager.func_179141_d();
        GlStateManager.func_179152_a(0.5f, 0.5f, 0.5f);
        GlStateManager.func_179097_i();
        this.renderEnchantmentText(stack, x, y);
        GlStateManager.func_179126_j();
        GlStateManager.func_179152_a(2.0f, 2.0f, 2.0f);
        GlStateManager.func_179121_F();
    }
    
    private void renderEnchantmentText(final ItemStack stack, final int x, final int y) {
        int enchantmentY = y - 8;
        if (stack.func_77973_b() == Items.field_151153_ao && stack.func_77962_s()) {
            this.renderer.drawStringWithShadow("god", (float)(x * 2), (float)enchantmentY, -3977919);
            enchantmentY -= 8;
        }
        final NBTTagList enchants = stack.func_77986_q();
        for (int index = 0; index < enchants.func_74745_c(); ++index) {
            final short id = enchants.func_150305_b(index).func_74765_d("id");
            final short level = enchants.func_150305_b(index).func_74765_d("lvl");
            final Enchantment enc = Enchantment.func_185262_c((int)id);
            if (enc != null) {
                String encName = enc.func_190936_d() ? (TextFormatting.RED + enc.func_77316_c((int)level).substring(11).substring(0, 1).toLowerCase()) : enc.func_77316_c((int)level).substring(0, 1).toLowerCase();
                encName += level;
                this.renderer.drawStringWithShadow(encName, (float)(x * 2), (float)enchantmentY, -1);
                enchantmentY -= 8;
            }
        }
        if (DamageUtil.hasDurability(stack)) {
            final int percent = DamageUtil.getRoundedDamage(stack);
            String color;
            if (percent >= 60) {
                color = "§a";
            }
            else if (percent >= 25) {
                color = "§e";
            }
            else {
                color = "§c";
            }
            this.renderer.drawStringWithShadow(color + percent + "%", (float)(x * 2), (float)enchantmentY, -1);
        }
    }
    
    private float getBiggestArmorTag(final EntityPlayer player) {
        float enchantmentY = 0.0f;
        boolean arm = false;
        for (final ItemStack stack : player.field_71071_by.field_70460_b) {
            float encY = 0.0f;
            if (stack != null) {
                final NBTTagList enchants = stack.func_77986_q();
                for (int index = 0; index < enchants.func_74745_c(); ++index) {
                    final short id = enchants.func_150305_b(index).func_74765_d("id");
                    final Enchantment enc = Enchantment.func_185262_c((int)id);
                    if (enc != null) {
                        encY += 8.0f;
                        arm = true;
                    }
                }
            }
            if (encY > enchantmentY) {
                enchantmentY = encY;
            }
        }
        final ItemStack renderMainHand = player.func_184614_ca().func_77946_l();
        if (renderMainHand.func_77962_s()) {
            float encY2 = 0.0f;
            final NBTTagList enchants2 = renderMainHand.func_77986_q();
            for (int index2 = 0; index2 < enchants2.func_74745_c(); ++index2) {
                final short id2 = enchants2.func_150305_b(index2).func_74765_d("id");
                final Enchantment enc2 = Enchantment.func_185262_c((int)id2);
                if (enc2 != null) {
                    encY2 += 8.0f;
                    arm = true;
                }
            }
            if (encY2 > enchantmentY) {
                enchantmentY = encY2;
            }
        }
        final ItemStack renderOffHand = player.func_184592_cb().func_77946_l();
        if (renderOffHand.func_77962_s()) {
            float encY = 0.0f;
            final NBTTagList enchants = renderOffHand.func_77986_q();
            for (int index = 0; index < enchants.func_74745_c(); ++index) {
                final short id = enchants.func_150305_b(index).func_74765_d("id");
                final Enchantment enc = Enchantment.func_185262_c((int)id);
                if (enc != null) {
                    encY += 8.0f;
                    arm = true;
                }
            }
            if (encY > enchantmentY) {
                enchantmentY = encY;
            }
        }
        return (arm ? 0 : 20) + enchantmentY;
    }
    
    private String getDisplayTag(final EntityPlayer player) {
        String name = player.func_145748_c_().func_150254_d();
        if (name.contains(Nametags.mc.func_110432_I().func_111285_a())) {
            name = "You";
        }
        if (!this.health.getValue()) {
            return name;
        }
        final float health = EntityUtil.getHealth((Entity)player);
        String color;
        if (health > 18.0f) {
            color = "§a";
        }
        else if (health > 16.0f) {
            color = "§2";
        }
        else if (health > 12.0f) {
            color = "§e";
        }
        else if (health > 8.0f) {
            color = "§6";
        }
        else if (health > 5.0f) {
            color = "§c";
        }
        else {
            color = "§4";
        }
        String pingStr = "";
        if (this.ping.getValue()) {
            try {
                final int responseTime = Objects.requireNonNull(Nametags.mc.func_147114_u()).func_175102_a(player.func_110124_au()).func_178853_c();
                pingStr = pingStr + responseTime + "ms ";
            }
            catch (Exception ex) {}
        }
        String popStr = " ";
        if (this.totemPops.getValue()) {
            popStr += esohack.totemPopManager.getTotemPopString(player);
        }
        String idString = "";
        if (this.entityID.getValue()) {
            idString = idString + "ID: " + player.func_145782_y() + " ";
        }
        String gameModeStr = "";
        if (this.gamemode.getValue()) {
            if (player.func_184812_l_()) {
                gameModeStr += "[C] ";
            }
            else if (player.func_175149_v() || player.func_82150_aj()) {
                gameModeStr += "[I] ";
            }
            else {
                gameModeStr += "[S] ";
            }
        }
        if (Math.floor(health) == health) {
            name = name + color + " " + ((health > 0.0f) ? Integer.valueOf((int)Math.floor(health)) : "dead");
        }
        else {
            name = name + color + " " + ((health > 0.0f) ? Integer.valueOf((int)health) : "dead");
        }
        return pingStr + idString + gameModeStr + name + popStr;
    }
    
    private int getDisplayColour(final EntityPlayer player) {
        int colour = -5592406;
        if (this.whiter.getValue()) {
            colour = -1;
        }
        if (esohack.friendManager.isFriend(player)) {
            return -11157267;
        }
        if (player.func_82150_aj()) {
            colour = -1113785;
        }
        else if (player.func_70093_af() && this.sneak.getValue()) {
            colour = -6481515;
        }
        return colour;
    }
    
    private double interpolate(final double previous, final double current, final float delta) {
        return previous + (current - previous) * delta;
    }
    
    static {
        Nametags.INSTANCE = new Nametags();
    }
}
