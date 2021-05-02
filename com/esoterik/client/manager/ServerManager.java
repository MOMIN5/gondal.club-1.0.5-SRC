// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.manager;

import java.util.Objects;
import net.minecraft.client.network.NetHandlerPlayClient;
import java.util.Arrays;
import com.esoterik.client.features.modules.client.Managers;
import com.esoterik.client.util.Timer;
import java.text.DecimalFormat;
import com.esoterik.client.features.Feature;

public class ServerManager extends Feature
{
    private float TPS;
    private long lastUpdate;
    private final float[] tpsCounts;
    private final DecimalFormat format;
    private String serverBrand;
    private final Timer timer;
    
    public ServerManager() {
        this.TPS = 20.0f;
        this.lastUpdate = -1L;
        this.tpsCounts = new float[10];
        this.format = new DecimalFormat("##.00#");
        this.serverBrand = "";
        this.timer = new Timer();
    }
    
    public void onPacketReceived() {
        this.timer.reset();
    }
    
    public boolean isServerNotResponding() {
        return this.timer.passedMs(Managers.getInstance().respondTime.getValue());
    }
    
    public long serverRespondingTime() {
        return this.timer.getPassedTimeMs();
    }
    
    public void update() {
        final long currentTime = System.currentTimeMillis();
        if (this.lastUpdate == -1L) {
            this.lastUpdate = currentTime;
            return;
        }
        final long timeDiff = currentTime - this.lastUpdate;
        float tickTime = timeDiff / 20.0f;
        if (tickTime == 0.0f) {
            tickTime = 50.0f;
        }
        float tps = 1000.0f / tickTime;
        if (tps > 20.0f) {
            tps = 20.0f;
        }
        System.arraycopy(this.tpsCounts, 0, this.tpsCounts, 1, this.tpsCounts.length - 1);
        this.tpsCounts[0] = tps;
        double total = 0.0;
        for (final float f : this.tpsCounts) {
            total += f;
        }
        total /= this.tpsCounts.length;
        if (total > 20.0) {
            total = 20.0;
        }
        this.TPS = Float.parseFloat(this.format.format(total));
        this.lastUpdate = currentTime;
    }
    
    @Override
    public void reset() {
        Arrays.fill(this.tpsCounts, 20.0f);
        this.TPS = 20.0f;
    }
    
    public float getTpsFactor() {
        return 20.0f / this.TPS;
    }
    
    public float getTPS() {
        return this.TPS;
    }
    
    public String getServerBrand() {
        return this.serverBrand;
    }
    
    public void setServerBrand(final String brand) {
        this.serverBrand = brand;
    }
    
    public int getPing() {
        if (fullNullCheck()) {
            return 0;
        }
        try {
            return Objects.requireNonNull(ServerManager.mc.getConnection()).getPlayerInfo(ServerManager.mc.getConnection().getGameProfile().getId()).getResponseTime();
        }
        catch (Exception e) {
            return 0;
        }
    }
}
