// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.gui.components.items.buttons;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import com.esoterik.client.features.setting.Bind;
import com.esoterik.client.features.gui.esohackGui;
import com.esoterik.client.util.RenderUtil;
import com.esoterik.client.features.modules.client.ClickGui;
import com.esoterik.client.esohack;
import com.esoterik.client.features.setting.Setting;

public class BindButton extends Button
{
    private Setting setting;
    public boolean isListening;
    
    public BindButton(final Setting setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }
    
    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        RenderUtil.drawRect(this.x, this.y, this.x + this.width + 7.4f, this.y + this.height - 0.5f, this.getState() ? (this.isHovering(mouseX, mouseY) ? esohack.colorManager.getColorWithAlpha(((ClickGui)esohack.moduleManager.getModuleByName("ClickGui")).alpha.getValue()) : esohack.colorManager.getColorWithAlpha(((ClickGui)esohack.moduleManager.getModuleByName("ClickGui")).hoverAlpha.getValue())) : (this.isHovering(mouseX, mouseY) ? -2007673515 : 290805077));
        if (this.isListening) {
            esohack.textManager.drawStringWithShadow("Listening...", this.x + 2.3f, this.y - 1.7f - esohackGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        }
        else {
            esohack.textManager.drawStringWithShadow(this.setting.getName() + " " + "§7" + this.setting.getValue().toString(), this.x + 2.3f, this.y - 1.7f - esohackGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        }
    }
    
    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }
    
    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            BindButton.mc.getSoundHandler().playSound((ISound)PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }
    
    @Override
    public void onKeyTyped(final char typedChar, final int keyCode) {
        if (this.isListening) {
            Bind bind = new Bind(keyCode);
            if (bind.toString().equalsIgnoreCase("Escape")) {
                return;
            }
            if (bind.toString().equalsIgnoreCase("Delete")) {
                bind = new Bind(-1);
            }
            this.setting.setValue(bind);
            super.onMouseClick();
        }
    }
    
    @Override
    public int getHeight() {
        return 14;
    }
    
    @Override
    public void toggle() {
        this.isListening = !this.isListening;
    }
    
    @Override
    public boolean getState() {
        return !this.isListening;
    }
}
