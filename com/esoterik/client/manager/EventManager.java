// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.manager;

import com.esoterik.client.features.command.Command;
import net.minecraftforge.client.event.ClientChatEvent;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import com.esoterik.client.event.events.Render2DEvent;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import com.esoterik.client.event.events.Render3DEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import com.esoterik.client.event.events.TotemPopEvent;
import net.minecraft.world.World;
import net.minecraft.network.play.server.SPacketEntityStatus;
import com.esoterik.client.event.events.PacketEvent;
import com.esoterik.client.event.events.UpdateWalkingPlayerEvent;
import java.util.Iterator;
import net.minecraftforge.fml.common.eventhandler.Event;
import com.esoterik.client.event.events.DeathEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.esoterik.client.features.modules.client.Managers;
import com.esoterik.client.esohack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.common.MinecraftForge;
import java.util.concurrent.atomic.AtomicBoolean;
import com.esoterik.client.util.Timer;
import com.esoterik.client.features.Feature;

public class EventManager extends Feature
{
    private final Timer timer;
    private final Timer logoutTimer;
    private AtomicBoolean tickOngoing;
    
    public EventManager() {
        this.timer = new Timer();
        this.logoutTimer = new Timer();
        this.tickOngoing = new AtomicBoolean(false);
    }
    
    public void init() {
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    public void onUnload() {
        MinecraftForge.EVENT_BUS.unregister((Object)this);
    }
    
    @SubscribeEvent
    public void onUpdate(final LivingEvent.LivingUpdateEvent event) {
        if (!Feature.fullNullCheck() && event.getEntity().func_130014_f_().field_72995_K && event.getEntityLiving().equals((Object)EventManager.mc.field_71439_g)) {
            esohack.potionManager.update();
            esohack.totemPopManager.onUpdate();
            esohack.inventoryManager.update();
            esohack.holeManager.update();
            esohack.moduleManager.onUpdate();
            esohack.timerManager.update();
            if (this.timer.passedMs(Managers.getInstance().moduleListUpdates)) {
                esohack.moduleManager.sortModules(true);
                this.timer.reset();
            }
        }
    }
    
    @SubscribeEvent
    public void onClientConnect(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.logoutTimer.reset();
        esohack.moduleManager.onLogin();
    }
    
    @SubscribeEvent
    public void onClientDisconnect(final FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        esohack.moduleManager.onLogout();
        esohack.totemPopManager.onLogout();
        esohack.potionManager.onLogout();
    }
    
    public boolean ticksOngoing() {
        return this.tickOngoing.get();
    }
    
    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) {
        if (fullNullCheck()) {
            return;
        }
        esohack.moduleManager.onTick();
        for (final EntityPlayer player : EventManager.mc.field_71441_e.field_73010_i) {
            if (player != null) {
                if (player.func_110143_aJ() > 0.0f) {
                    continue;
                }
                MinecraftForge.EVENT_BUS.post((Event)new DeathEvent(player));
                esohack.totemPopManager.onDeath(player);
            }
        }
    }
    
    @SubscribeEvent
    public void onUpdateWalkingPlayer(final UpdateWalkingPlayerEvent event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0) {
            esohack.speedManager.updateValues();
            esohack.rotationManager.updateRotations();
            esohack.positionManager.updatePosition();
        }
        if (event.getStage() == 1) {
            esohack.rotationManager.restoreRotations();
            esohack.positionManager.restorePosition();
        }
    }
    
    @SubscribeEvent
    public void onPacketReceive(final PacketEvent.Receive event) {
        if (event.getStage() != 0) {
            return;
        }
        esohack.serverManager.onPacketReceived();
        if (event.getPacket() instanceof SPacketEntityStatus) {
            final SPacketEntityStatus packet = event.getPacket();
            if (packet.func_149160_c() == 35 && packet.func_149161_a((World)EventManager.mc.field_71441_e) instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer)packet.func_149161_a((World)EventManager.mc.field_71441_e);
                MinecraftForge.EVENT_BUS.post((Event)new TotemPopEvent(player));
                esohack.totemPopManager.onTotemPop(player);
                esohack.potionManager.onTotemPop(player);
            }
        }
        if (event.getPacket() instanceof SPacketPlayerListItem && !Feature.fullNullCheck() && this.logoutTimer.passedS(1.0)) {
            final SPacketPlayerListItem packet2 = event.getPacket();
            if (!SPacketPlayerListItem.Action.ADD_PLAYER.equals((Object)packet2.func_179768_b()) && !SPacketPlayerListItem.Action.REMOVE_PLAYER.equals((Object)packet2.func_179768_b())) {
                return;
            }
        }
        if (event.getPacket() instanceof SPacketTimeUpdate) {
            esohack.serverManager.update();
        }
    }
    
    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        if (event.isCanceled()) {
            return;
        }
        EventManager.mc.field_71424_I.func_76320_a("client");
        GlStateManager.func_179090_x();
        GlStateManager.func_179147_l();
        GlStateManager.func_179118_c();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        GlStateManager.func_179103_j(7425);
        GlStateManager.func_179097_i();
        GlStateManager.func_187441_d(1.0f);
        final Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
        esohack.moduleManager.onRender3D(render3dEvent);
        GlStateManager.func_187441_d(1.0f);
        GlStateManager.func_179103_j(7424);
        GlStateManager.func_179084_k();
        GlStateManager.func_179141_d();
        GlStateManager.func_179098_w();
        GlStateManager.func_179126_j();
        GlStateManager.func_179089_o();
        GlStateManager.func_179089_o();
        GlStateManager.func_179132_a(true);
        GlStateManager.func_179098_w();
        GlStateManager.func_179147_l();
        GlStateManager.func_179126_j();
        EventManager.mc.field_71424_I.func_76319_b();
    }
    
    @SubscribeEvent
    public void renderHUD(final RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            esohack.textManager.updateResolution();
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Text event) {
        if (event.getType().equals((Object)RenderGameOverlayEvent.ElementType.TEXT)) {
            final ScaledResolution resolution = new ScaledResolution(EventManager.mc);
            final Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
            esohack.moduleManager.onRender2D(render2DEvent);
            GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(final InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            esohack.moduleManager.onKeyPressed(Keyboard.getEventKey());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatSent(final ClientChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);
            try {
                EventManager.mc.field_71456_v.func_146158_b().func_146239_a(event.getMessage());
                if (event.getMessage().length() > 1) {
                    esohack.commandManager.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                }
                else {
                    Command.sendMessage("Please enter a command.");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage("§cAn error occurred while running this command. Check the log!");
            }
            event.setMessage("");
        }
    }
}
