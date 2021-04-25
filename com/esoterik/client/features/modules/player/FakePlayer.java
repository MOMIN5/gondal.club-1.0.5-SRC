// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.player;

import net.minecraft.potion.PotionEffect;
import com.esoterik.client.esohack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.Iterator;
import java.util.ArrayList;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import java.util.List;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class FakePlayer extends Module
{
    private Setting<Boolean> copyInv;
    public Setting<Boolean> multi;
    private Setting<Integer> players;
    private static final String[] fitInfo;
    public static final String[][] phobosInfo;
    public List<Integer> fakePlayerIdList;
    private final List<EntityOtherPlayerMP> fakeEntities;
    private static FakePlayer INSTANCE;
    
    public FakePlayer() {
        super("FakePlayer", "Spawns in a fake player", Category.PLAYER, true, false, false);
        this.copyInv = (Setting<Boolean>)this.register(new Setting("CopyInv", (T)true));
        this.multi = (Setting<Boolean>)this.register(new Setting("Multi", (T)false));
        this.players = (Setting<Integer>)this.register(new Setting("Players", (T)1, (T)1, (T)9, v -> this.multi.getValue(), "Amount of other players."));
        this.fakePlayerIdList = new ArrayList<Integer>();
        this.fakeEntities = new ArrayList<EntityOtherPlayerMP>();
        this.setInstance();
    }
    
    private void setInstance() {
        FakePlayer.INSTANCE = this;
    }
    
    public static FakePlayer getInstance() {
        if (FakePlayer.INSTANCE == null) {
            FakePlayer.INSTANCE = new FakePlayer();
        }
        return FakePlayer.INSTANCE;
    }
    
    @Override
    public void onLoad() {
        this.disable();
    }
    
    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            this.disable();
            return;
        }
        this.fakePlayerIdList = new ArrayList<Integer>();
        if (this.multi.getValue()) {
            int amount = 0;
            int entityId = -101;
            for (final String[] data : FakePlayer.phobosInfo) {
                this.addFakePlayer(data[0], data[1], entityId, Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                if (++amount >= this.players.getValue()) {
                    return;
                }
                entityId -= amount;
            }
        }
        else {
            this.addFakePlayer(FakePlayer.fitInfo[0], FakePlayer.fitInfo[1], -100, 0, 0);
        }
    }
    
    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        for (final int id : this.fakePlayerIdList) {
            FakePlayer.mc.field_71441_e.func_73028_b(id);
        }
    }
    
    @Override
    public void onLogout() {
        if (this.isOn()) {
            this.disable();
        }
    }
    
    private void addFakePlayer(final String uuid, final String name, final int entityId, final int offsetX, final int offsetZ) {
        final GameProfile profile = new GameProfile(UUID.fromString(uuid), name);
        final EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, profile);
        fakePlayer.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer.field_70165_t += offsetX;
        fakePlayer.field_70161_v += offsetZ;
        if (this.copyInv.getValue()) {
            for (final PotionEffect potionEffect : esohack.potionManager.getOwnPotions()) {
                fakePlayer.func_70690_d(potionEffect);
            }
            fakePlayer.field_71071_by.func_70455_b(FakePlayer.mc.field_71439_g.field_71071_by);
        }
        fakePlayer.func_70606_j(FakePlayer.mc.field_71439_g.func_110143_aJ() + FakePlayer.mc.field_71439_g.func_110139_bj());
        this.fakeEntities.add(fakePlayer);
        FakePlayer.mc.field_71441_e.func_73027_a(entityId, (Entity)fakePlayer);
        this.fakePlayerIdList.add(entityId);
    }
    
    static {
        fitInfo = new String[] { "fdee323e-7f0c-4c15-8d1c-0f277442342a", "Fit" };
        phobosInfo = new String[][] { { "8af022c8-b926-41a0-8b79-2b544ff00fcf", "3arthqu4ke", "3", "0" }, { "0aa3b04f-786a-49c8-bea9-025ee0dd1e85", "zb0b", "-3", "0" }, { "19bf3f1f-fe06-4c86-bea5-3dad5df89714", "3vt", "0", "-3" }, { "e47d6571-99c2-415b-955e-c4bc7b55941b", "Phobos_eu", "0", "3" }, { "b01f9bc1-cb7c-429a-b178-93d771f00926", "bakpotatisen", "6", "0" }, { "b232930c-c28a-4e10-8c90-f152235a65c5", "948", "-6", "0" }, { "ace08461-3db3-4579-98d3-390a67d5645b", "Browswer", "0", "-6" }, { "5bead5b0-3bab-460d-af1d-7929950f40c2", "fsck", "0", "6" }, { "78ee2bd6-64c4-45f0-96e5-0b6747ba7382", "Fit", "0", "9" }, { "78ee2bd6-64c4-45f0-96e5-0b6747ba7382", "deathcurz0", "0", "-9" } };
        FakePlayer.INSTANCE = new FakePlayer();
    }
}
