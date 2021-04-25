// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.mixin.mixins;

import net.minecraft.client.gui.ChatLine;
import java.util.List;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.esoterik.client.features.modules.misc.ChatModifier;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.gui.Gui;

@Mixin({ GuiNewChat.class })
public class MixinGuiNewChat extends Gui
{
    @Redirect(method = { "drawChat" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void drawRectHook(final int left, final int top, final int right, final int bottom, final int color) {
        Gui.func_73734_a(left, top, right, bottom, (ChatModifier.getInstance().isOn() && ChatModifier.getInstance().clean.getValue()) ? 0 : color);
    }
    
    @Redirect(method = { "setChatLine" }, at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0))
    public int drawnChatLinesSize(final List<ChatLine> list) {
        return (ChatModifier.getInstance().isOn() && ChatModifier.getInstance().infinite.getValue()) ? -2147483647 : list.size();
    }
    
    @Redirect(method = { "setChatLine" }, at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2))
    public int chatLinesSize(final List<ChatLine> list) {
        return (ChatModifier.getInstance().isOn() && ChatModifier.getInstance().infinite.getValue()) ? -2147483647 : list.size();
    }
}
