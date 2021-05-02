// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.misc;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.Vec3i;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Date;
import java.text.SimpleDateFormat;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.esoterik.client.esohack;
import net.minecraft.network.play.client.CPacketChatMessage;
import com.esoterik.client.event.events.PacketEvent;
import com.esoterik.client.util.Timer;
import com.esoterik.client.util.TextUtil;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class ChatModifier extends Module
{
    public Setting<TextUtil.Color> timeStamps;
    public Setting<TextUtil.Color> bracket;
    public Setting<Boolean> suffix;
    public Setting<Boolean> versionSuffix;
    public Setting<Boolean> clean;
    public Setting<Boolean> infinite;
    private final Timer timer;
    private static ChatModifier INSTANCE;
    
    public ChatModifier() {
        super("CustomChat", "Customises aspects of the chat", Category.MISC, true, false, false);
        this.timeStamps = (Setting<TextUtil.Color>)this.register(new Setting("Time", (T)TextUtil.Color.NONE));
        this.bracket = (Setting<TextUtil.Color>)this.register(new Setting("BracketColor", (T)TextUtil.Color.WHITE, v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
        this.suffix = (Setting<Boolean>)this.register(new Setting("ChatSuffix", (T)true, "Appends esohack suffix to all messages."));
        this.versionSuffix = (Setting<Boolean>)this.register(new Setting("IncludeVersion", (T)true, v -> this.suffix.getValue()));
        this.clean = (Setting<Boolean>)this.register(new Setting("CleanChat", (T)false, "Cleans your chat."));
        this.infinite = (Setting<Boolean>)this.register(new Setting("InfiniteLength", (T)false, "Makes your chat infinitely scrollable."));
        this.timer = new Timer();
        this.setInstance();
    }
    
    private void setInstance() {
        ChatModifier.INSTANCE = this;
    }
    
    public static ChatModifier getInstance() {
        if (ChatModifier.INSTANCE == null) {
            ChatModifier.INSTANCE = new ChatModifier();
        }
        return ChatModifier.INSTANCE;
    }
    
    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
            final CPacketChatMessage packet = event.getPacket();
            String s = packet.getMessage();
            if (s.startsWith("/") || s.startsWith("!")) {
                return;
            }
            if (this.suffix.getValue()) {
                s = s + " \u23d0 " + esohack.getName();
                if (this.versionSuffix.getValue()) {
                    s += " v1.0.5";
                }
            }
            if (s.length() >= 256) {
                s = s.substring(0, 256);
            }
            packet.message = s;
        }
    }
    
    @SubscribeEvent
    public void onPacketReceive(final PacketEvent.Receive event) {
        if (event.getStage() == 0 && this.timeStamps.getValue() != TextUtil.Color.NONE && event.getPacket() instanceof SPacketChat) {
            if (!event.getPacket().isSystem()) {
                return;
            }
            final String originalMessage = event.getPacket().chatComponent.getUnformattedText();
            final String message = this.getTimeString() + originalMessage;
            event.getPacket().chatComponent = (ITextComponent)new TextComponentString(message);
        }
    }
    
    public String getTimeString() {
        final String date = new SimpleDateFormat("k:mm").format(new Date());
        return ((this.bracket.getValue() == TextUtil.Color.NONE) ? "" : TextUtil.coloredString("<", this.bracket.getValue())) + TextUtil.coloredString(date, this.timeStamps.getValue()) + ((this.bracket.getValue() == TextUtil.Color.NONE) ? "" : TextUtil.coloredString(">", this.bracket.getValue())) + " " + "§r";
    }
    
    private boolean shouldSendMessage(final EntityPlayer player) {
        return player.dimension == 1 && player.getPosition().equals((Object)new Vec3i(0, 240, 0));
    }
    
    static {
        ChatModifier.INSTANCE = new ChatModifier();
    }
}
