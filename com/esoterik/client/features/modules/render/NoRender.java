// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.render;

import net.minecraft.init.SoundEvents;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraft.entity.passive.EntityBat;
import net.minecraftforge.client.event.RenderLivingEvent;
import java.util.Iterator;
import java.util.HashMap;
import net.minecraft.world.BossInfo;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.BossInfoClient;
import java.util.UUID;
import java.util.Map;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import net.minecraft.world.GameType;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.item.EntityItem;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class NoRender extends Module
{
    public Setting<Boolean> fire;
    public Setting<Boolean> portal;
    public Setting<Boolean> pumpkin;
    public Setting<Boolean> totemPops;
    public Setting<Boolean> items;
    public Setting<Boolean> nausea;
    public Setting<Boolean> hurtcam;
    public Setting<Fog> fog;
    public Setting<Boolean> noWeather;
    public Setting<Boss> boss;
    public Setting<Float> scale;
    public Setting<Boolean> bats;
    public Setting<NoArmor> noArmor;
    public Setting<Skylight> skylight;
    public Setting<Boolean> barriers;
    private static NoRender INSTANCE;
    
    public NoRender() {
        super("NoRender", "Allows you to stop rendering stuff", Category.RENDER, true, false, false);
        this.fire = (Setting<Boolean>)this.register(new Setting("Fire", (T)false, "Removes the portal overlay."));
        this.portal = (Setting<Boolean>)this.register(new Setting("Portal", (T)false, "Removes the portal overlay."));
        this.pumpkin = (Setting<Boolean>)this.register(new Setting("Pumpkin", (T)false, "Removes the pumpkin overlay."));
        this.totemPops = (Setting<Boolean>)this.register(new Setting("TotemPop", (T)false, "Removes the Totem overlay."));
        this.items = (Setting<Boolean>)this.register(new Setting("Items", (T)false, "Removes items on the ground."));
        this.nausea = (Setting<Boolean>)this.register(new Setting("Nausea", (T)false, "Removes Portal Nausea."));
        this.hurtcam = (Setting<Boolean>)this.register(new Setting("HurtCam", (T)false, "Removes shaking after taking damage."));
        this.fog = (Setting<Fog>)this.register(new Setting("Fog", (T)Fog.NONE, "Removes Fog."));
        this.noWeather = (Setting<Boolean>)this.register(new Setting("Weather", (T)false, "AntiWeather"));
        this.boss = (Setting<Boss>)this.register(new Setting("BossBars", (T)Boss.NONE, "Modifies the bossbars."));
        this.scale = (Setting<Float>)this.register(new Setting("Scale", (T)0.0f, (T)0.5f, (T)1.0f, v -> this.boss.getValue() == Boss.MINIMIZE || this.boss.getValue() != Boss.STACK, "Scale of the bars."));
        this.bats = (Setting<Boolean>)this.register(new Setting("Bats", (T)false, "Removes bats."));
        this.noArmor = (Setting<NoArmor>)this.register(new Setting("NoArmor", (T)NoArmor.NONE, "Doesnt Render Armor on players."));
        this.skylight = (Setting<Skylight>)this.register(new Setting("Skylight", (T)Skylight.NONE));
        this.barriers = (Setting<Boolean>)this.register(new Setting("Barriers", (T)false, "Barriers"));
        this.setInstance();
    }
    
    private void setInstance() {
        NoRender.INSTANCE = this;
    }
    
    @Override
    public void onUpdate() {
        if (this.items.getValue()) {
            NoRender.mc.field_71441_e.field_72996_f.stream().filter(EntityItem.class::isInstance).map(EntityItem.class::cast).forEach(Entity::func_70106_y);
        }
        if (this.noWeather.getValue() && NoRender.mc.field_71441_e.func_72896_J()) {
            NoRender.mc.field_71441_e.func_72894_k(0.0f);
        }
    }
    
    public void doVoidFogParticles(final int posX, final int posY, final int posZ) {
        final int i = 32;
        final Random random = new Random();
        final ItemStack itemstack = NoRender.mc.field_71439_g.func_184614_ca();
        final boolean flag = !this.barriers.getValue() || (NoRender.mc.field_71442_b.func_178889_l() == GameType.CREATIVE && !itemstack.func_190926_b() && itemstack.func_77973_b() == Item.func_150898_a(Blocks.field_180401_cv));
        final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for (int j = 0; j < 667; ++j) {
            this.showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
            this.showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
        }
    }
    
    public void showBarrierParticles(final int x, final int y, final int z, final int offset, final Random random, final boolean holdingBarrier, final BlockPos.MutableBlockPos pos) {
        final int i = x + NoRender.mc.field_71441_e.field_73012_v.nextInt(offset) - NoRender.mc.field_71441_e.field_73012_v.nextInt(offset);
        final int j = y + NoRender.mc.field_71441_e.field_73012_v.nextInt(offset) - NoRender.mc.field_71441_e.field_73012_v.nextInt(offset);
        final int k = z + NoRender.mc.field_71441_e.field_73012_v.nextInt(offset) - NoRender.mc.field_71441_e.field_73012_v.nextInt(offset);
        pos.func_181079_c(i, j, k);
        final IBlockState iblockstate = NoRender.mc.field_71441_e.func_180495_p((BlockPos)pos);
        iblockstate.func_177230_c().func_180655_c(iblockstate, (World)NoRender.mc.field_71441_e, (BlockPos)pos, random);
        if (!holdingBarrier && iblockstate.func_177230_c() == Blocks.field_180401_cv) {
            NoRender.mc.field_71441_e.func_175688_a(EnumParticleTypes.BARRIER, (double)(i + 0.5f), (double)(j + 0.5f), (double)(k + 0.5f), 0.0, 0.0, 0.0, new int[0]);
        }
    }
    
    @SubscribeEvent
    public void onRenderPre(final RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && this.boss.getValue() != Boss.NONE) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onRenderPost(final RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && this.boss.getValue() != Boss.NONE) {
            if (this.boss.getValue() == Boss.MINIMIZE) {
                final Map<UUID, BossInfoClient> map = (Map<UUID, BossInfoClient>)NoRender.mc.field_71456_v.func_184046_j().field_184060_g;
                if (map == null) {
                    return;
                }
                final ScaledResolution scaledresolution = new ScaledResolution(NoRender.mc);
                final int i = scaledresolution.func_78326_a();
                int j = 12;
                for (final Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                    final BossInfoClient info = entry.getValue();
                    final String text = info.func_186744_e().func_150254_d();
                    final int k = (int)(i / this.scale.getValue() / 2.0f - 91.0f);
                    GL11.glScaled((double)this.scale.getValue(), (double)this.scale.getValue(), 1.0);
                    if (!event.isCanceled()) {
                        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
                        NoRender.mc.func_110434_K().func_110577_a(GuiBossOverlay.field_184058_a);
                        NoRender.mc.field_71456_v.func_184046_j().func_184052_a(k, j, (BossInfo)info);
                        NoRender.mc.field_71466_p.func_175063_a(text, i / this.scale.getValue() / 2.0f - NoRender.mc.field_71466_p.func_78256_a(text) / 2, (float)(j - 9), 16777215);
                    }
                    GL11.glScaled(1.0 / this.scale.getValue(), 1.0 / this.scale.getValue(), 1.0);
                    j += 10 + NoRender.mc.field_71466_p.field_78288_b;
                }
            }
            else if (this.boss.getValue() == Boss.STACK) {
                final Map<UUID, BossInfoClient> map = (Map<UUID, BossInfoClient>)NoRender.mc.field_71456_v.func_184046_j().field_184060_g;
                final HashMap<String, Pair<BossInfoClient, Integer>> to = new HashMap<String, Pair<BossInfoClient, Integer>>();
                for (final Map.Entry<UUID, BossInfoClient> entry2 : map.entrySet()) {
                    final String s = entry2.getValue().func_186744_e().func_150254_d();
                    if (to.containsKey(s)) {
                        Pair<BossInfoClient, Integer> p = to.get(s);
                        p = new Pair<BossInfoClient, Integer>(p.getKey(), p.getValue() + 1);
                        to.put(s, p);
                    }
                    else {
                        final Pair<BossInfoClient, Integer> p = new Pair<BossInfoClient, Integer>(entry2.getValue(), 1);
                        to.put(s, p);
                    }
                }
                final ScaledResolution scaledresolution2 = new ScaledResolution(NoRender.mc);
                final int l = scaledresolution2.func_78326_a();
                int m = 12;
                for (final Map.Entry<String, Pair<BossInfoClient, Integer>> entry3 : to.entrySet()) {
                    String text = entry3.getKey();
                    final BossInfoClient info2 = entry3.getValue().getKey();
                    final int a = entry3.getValue().getValue();
                    text = text + " x" + a;
                    final int k2 = (int)(l / this.scale.getValue() / 2.0f - 91.0f);
                    GL11.glScaled((double)this.scale.getValue(), (double)this.scale.getValue(), 1.0);
                    if (!event.isCanceled()) {
                        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
                        NoRender.mc.func_110434_K().func_110577_a(GuiBossOverlay.field_184058_a);
                        NoRender.mc.field_71456_v.func_184046_j().func_184052_a(k2, m, (BossInfo)info2);
                        NoRender.mc.field_71466_p.func_175063_a(text, l / this.scale.getValue() / 2.0f - NoRender.mc.field_71466_p.func_78256_a(text) / 2, (float)(m - 9), 16777215);
                    }
                    GL11.glScaled(1.0 / this.scale.getValue(), 1.0 / this.scale.getValue(), 1.0);
                    m += 10 + NoRender.mc.field_71466_p.field_78288_b;
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderLiving(final RenderLivingEvent.Pre<?> event) {
        if (this.bats.getValue() && event.getEntity() instanceof EntityBat) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onPlaySound(final PlaySoundAtEntityEvent event) {
        if ((this.bats.getValue() && event.getSound().equals(SoundEvents.field_187740_w)) || event.getSound().equals(SoundEvents.field_187742_x) || event.getSound().equals(SoundEvents.field_187743_y) || event.getSound().equals(SoundEvents.field_189108_z) || event.getSound().equals(SoundEvents.field_187744_z)) {
            event.setVolume(0.0f);
            event.setPitch(0.0f);
            event.setCanceled(true);
        }
    }
    
    public static NoRender getInstance() {
        if (NoRender.INSTANCE == null) {
            NoRender.INSTANCE = new NoRender();
        }
        return NoRender.INSTANCE;
    }
    
    static {
        NoRender.INSTANCE = new NoRender();
    }
    
    public enum Skylight
    {
        NONE, 
        WORLD, 
        ENTITY, 
        ALL;
    }
    
    public enum Fog
    {
        NONE, 
        AIR, 
        NOFOG;
    }
    
    public enum Boss
    {
        NONE, 
        REMOVE, 
        STACK, 
        MINIMIZE;
    }
    
    public enum NoArmor
    {
        NONE, 
        ALL, 
        HELMET;
    }
    
    public static class Pair<T, S>
    {
        private T key;
        private S value;
        
        public Pair(final T key, final S value) {
            this.key = key;
            this.value = value;
        }
        
        public T getKey() {
            return this.key;
        }
        
        public S getValue() {
            return this.value;
        }
        
        public void setKey(final T key) {
            this.key = key;
        }
        
        public void setValue(final S value) {
            this.value = value;
        }
    }
}
