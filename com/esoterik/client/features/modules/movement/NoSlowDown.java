// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.movement;

import net.minecraft.client.gui.GuiChat;
import com.esoterik.client.event.events.KeyEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.settings.KeyBinding;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class NoSlowDown extends Module
{
    public Setting<Boolean> guiMove;
    public Setting<Boolean> noSlow;
    public Setting<Boolean> soulSand;
    private static NoSlowDown INSTANCE;
    private static KeyBinding[] keys;
    
    public NoSlowDown() {
        super("NoSlowDown", "Prevents you from getting slowed down.", Category.MOVEMENT, true, false, false);
        this.guiMove = (Setting<Boolean>)this.register(new Setting("GuiMove", (T)true));
        this.noSlow = (Setting<Boolean>)this.register(new Setting("NoSlow", (T)true));
        this.soulSand = (Setting<Boolean>)this.register(new Setting("SoulSand", (T)true));
        this.setInstance();
    }
    
    private void setInstance() {
        NoSlowDown.INSTANCE = this;
    }
    
    public static NoSlowDown getInstance() {
        if (NoSlowDown.INSTANCE == null) {
            NoSlowDown.INSTANCE = new NoSlowDown();
        }
        return NoSlowDown.INSTANCE;
    }
    
    @Override
    public void onUpdate() {
        if (this.guiMove.getValue()) {
            if (NoSlowDown.mc.currentScreen instanceof GuiOptions || NoSlowDown.mc.currentScreen instanceof GuiVideoSettings || NoSlowDown.mc.currentScreen instanceof GuiScreenOptionsSounds || NoSlowDown.mc.currentScreen instanceof GuiContainer || NoSlowDown.mc.currentScreen instanceof GuiIngameMenu) {
                for (final KeyBinding bind : NoSlowDown.keys) {
                    KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));
                }
            }
            else if (NoSlowDown.mc.currentScreen == null) {
                for (final KeyBinding bind : NoSlowDown.keys) {
                    if (!Keyboard.isKeyDown(bind.getKeyCode())) {
                        KeyBinding.setKeyBindState(bind.getKeyCode(), false);
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onInput(final InputUpdateEvent event) {
        if (this.noSlow.getValue() && NoSlowDown.mc.player.isHandActive() && !NoSlowDown.mc.player.isRiding()) {
            final MovementInput movementInput = event.getMovementInput();
            movementInput.moveStrafe *= 5.0f;
            final MovementInput movementInput2 = event.getMovementInput();
            movementInput2.moveForward *= 5.0f;
        }
    }
    
    @SubscribeEvent
    public void onKeyEvent(final KeyEvent event) {
        if (this.guiMove.getValue() && event.getStage() == 0 && !(NoSlowDown.mc.currentScreen instanceof GuiChat)) {
            event.info = event.pressed;
        }
    }
    
    static {
        NoSlowDown.INSTANCE = new NoSlowDown();
        NoSlowDown.keys = new KeyBinding[] { NoSlowDown.mc.gameSettings.keyBindForward, NoSlowDown.mc.gameSettings.keyBindBack, NoSlowDown.mc.gameSettings.keyBindLeft, NoSlowDown.mc.gameSettings.keyBindRight, NoSlowDown.mc.gameSettings.keyBindJump, NoSlowDown.mc.gameSettings.keyBindSprint };
    }
}
