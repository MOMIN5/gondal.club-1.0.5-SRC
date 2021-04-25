// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client;

import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.Display;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.esoterik.client.manager.SafetyManager;
import com.esoterik.client.manager.NotificationManager;
import com.esoterik.client.manager.HoleManager;
import com.esoterik.client.manager.TotemPopManager;
import com.esoterik.client.manager.ReloadManager;
import com.esoterik.client.manager.PacketManager;
import com.esoterik.client.manager.TimerManager;
import com.esoterik.client.manager.InventoryManager;
import com.esoterik.client.manager.PotionManager;
import com.esoterik.client.manager.ServerManager;
import com.esoterik.client.manager.ColorManager;
import com.esoterik.client.manager.TextManager;
import com.esoterik.client.manager.FriendManager;
import com.esoterik.client.manager.FileManager;
import com.esoterik.client.manager.ConfigManager;
import com.esoterik.client.manager.EventManager;
import com.esoterik.client.manager.CommandManager;
import com.esoterik.client.manager.RotationManager;
import com.esoterik.client.manager.PositionManager;
import com.esoterik.client.manager.SpeedManager;
import com.esoterik.client.manager.ModuleManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "esohack", name = "esohack", version = "1.0.5")
public class esohack
{
    public static final String MODID = "esohack";
    public static final String MODNAME = "esohack";
    public static final String MODVER = "1.0.5";
    public static final Logger LOGGER;
    private static String name;
    public static ModuleManager moduleManager;
    public static SpeedManager speedManager;
    public static PositionManager positionManager;
    public static RotationManager rotationManager;
    public static CommandManager commandManager;
    public static EventManager eventManager;
    public static ConfigManager configManager;
    public static FileManager fileManager;
    public static FriendManager friendManager;
    public static TextManager textManager;
    public static ColorManager colorManager;
    public static ServerManager serverManager;
    public static PotionManager potionManager;
    public static InventoryManager inventoryManager;
    public static TimerManager timerManager;
    public static PacketManager packetManager;
    public static ReloadManager reloadManager;
    public static TotemPopManager totemPopManager;
    public static HoleManager holeManager;
    public static NotificationManager notificationManager;
    public static SafetyManager safetyManager;
    private static boolean unloaded;
    @Mod.Instance
    public static esohack INSTANCE;
    
    public static String getName() {
        return esohack.name;
    }
    
    public static void setName(final String newName) {
        esohack.name = newName;
    }
    
    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        Display.setTitle("esohack - v.1.0.5");
        load();
    }
    
    public static void load() {
        esohack.LOGGER.info("\n\nLoading esohack 1.0.5");
        esohack.unloaded = false;
        if (esohack.reloadManager != null) {
            esohack.reloadManager.unload();
            esohack.reloadManager = null;
        }
        esohack.totemPopManager = new TotemPopManager();
        esohack.timerManager = new TimerManager();
        esohack.packetManager = new PacketManager();
        esohack.serverManager = new ServerManager();
        esohack.colorManager = new ColorManager();
        esohack.textManager = new TextManager();
        esohack.moduleManager = new ModuleManager();
        esohack.speedManager = new SpeedManager();
        esohack.rotationManager = new RotationManager();
        esohack.positionManager = new PositionManager();
        esohack.commandManager = new CommandManager();
        esohack.eventManager = new EventManager();
        esohack.configManager = new ConfigManager();
        esohack.fileManager = new FileManager();
        esohack.friendManager = new FriendManager();
        esohack.potionManager = new PotionManager();
        esohack.inventoryManager = new InventoryManager();
        esohack.holeManager = new HoleManager();
        esohack.notificationManager = new NotificationManager();
        esohack.safetyManager = new SafetyManager();
        esohack.LOGGER.info("Initialized Managers");
        esohack.moduleManager.init();
        esohack.LOGGER.info("Modules loaded.");
        esohack.configManager.init();
        esohack.eventManager.init();
        esohack.LOGGER.info("EventManager loaded.");
        esohack.textManager.init(true);
        esohack.moduleManager.onLoad();
        esohack.totemPopManager.init();
        esohack.timerManager.init();
        esohack.LOGGER.info("esohack initialized!\n");
    }
    
    public static void unload(final boolean unload) {
        esohack.LOGGER.info("\n\nUnloading esohack 1.0.5");
        if (unload) {
            (esohack.reloadManager = new ReloadManager()).init((esohack.commandManager != null) ? esohack.commandManager.getPrefix() : ".");
        }
        onUnload();
        esohack.eventManager = null;
        esohack.holeManager = null;
        esohack.timerManager = null;
        esohack.moduleManager = null;
        esohack.totemPopManager = null;
        esohack.serverManager = null;
        esohack.colorManager = null;
        esohack.textManager = null;
        esohack.speedManager = null;
        esohack.rotationManager = null;
        esohack.positionManager = null;
        esohack.commandManager = null;
        esohack.configManager = null;
        esohack.fileManager = null;
        esohack.friendManager = null;
        esohack.potionManager = null;
        esohack.inventoryManager = null;
        esohack.notificationManager = null;
        esohack.safetyManager = null;
        esohack.LOGGER.info("esohack unloaded!\n");
    }
    
    public static void reload() {
        unload(false);
        load();
    }
    
    public static void onUnload() {
        if (!esohack.unloaded) {
            esohack.eventManager.onUnload();
            esohack.moduleManager.onUnload();
            esohack.configManager.saveConfig(esohack.configManager.config.replaceFirst("esohack/", ""));
            esohack.moduleManager.onUnloadPost();
            esohack.timerManager.unload();
            esohack.unloaded = true;
        }
    }
    
    static {
        LOGGER = LogManager.getLogger("esohack");
        esohack.name = "esohack";
        esohack.unloaded = false;
    }
}
