// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.mixin;

import java.util.Map;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.launch.MixinBootstrap;
import com.esoterik.client.esohack;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class esohackMixinLoader implements IFMLLoadingPlugin
{
    private static boolean isObfuscatedEnvironment;
    
    public esohackMixinLoader() {
        esohack.LOGGER.info("esohack mixins initialized");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.esohack.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        esohack.LOGGER.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
    }
    
    public String[] getASMTransformerClass() {
        return new String[0];
    }
    
    public String getModContainerClass() {
        return null;
    }
    
    public String getSetupClass() {
        return null;
    }
    
    public void injectData(final Map<String, Object> data) {
        esohackMixinLoader.isObfuscatedEnvironment = data.get("runtimeDeobfuscationEnabled");
    }
    
    public String getAccessTransformerClass() {
        return null;
    }
    
    static {
        esohackMixinLoader.isObfuscatedEnvironment = false;
    }
}
