// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.features.modules.render;

import com.esoterik.client.util.RenderUtil;
import com.esoterik.client.esohack;
import com.esoterik.client.features.modules.client.Colors;
import java.awt.Color;
import com.esoterik.client.event.events.Render3DEvent;
import net.minecraft.init.Blocks;
import com.esoterik.client.util.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import com.esoterik.client.features.setting.Setting;
import com.esoterik.client.features.modules.Module;

public class BreakESP extends Module
{
    public Setting<Boolean> box;
    public Setting<Boolean> outline;
    private final Setting<Integer> boxAlpha;
    private final Setting<Float> lineWidth;
    private BlockPos lastPos;
    public BlockPos currentPos;
    public IBlockState currentBlockState;
    private final Timer timer;
    
    public BreakESP() {
        super("BreakESP", "Highlights blocks you mine", Category.RENDER, true, false, false);
        this.box = (Setting<Boolean>)this.register(new Setting("Box", (T)false));
        this.outline = (Setting<Boolean>)this.register(new Setting("Outline", (T)true));
        this.boxAlpha = (Setting<Integer>)this.register(new Setting("BoxAlpha", (T)85, (T)0, (T)255));
        this.lineWidth = (Setting<Float>)this.register(new Setting("LineWidth", (T)1.0f, (T)0.1f, (T)5.0f));
        this.lastPos = null;
        this.timer = new Timer();
    }
    
    @Override
    public void onTick() {
        if (this.currentPos != null && (!BreakESP.mc.field_71441_e.func_180495_p(this.currentPos).equals(this.currentBlockState) || BreakESP.mc.field_71441_e.func_180495_p(this.currentPos).func_177230_c() == Blocks.field_150350_a)) {
            this.currentPos = null;
            this.currentBlockState = null;
        }
    }
    
    @Override
    public void onRender3D(final Render3DEvent event) {
        if (this.currentPos != null) {
            final Color color = new Color(255, 255, 255, 255);
            final Color readyColor = Colors.INSTANCE.isEnabled() ? Colors.INSTANCE.getCurrentColor() : new Color(125, 105, 255, 255);
            RenderUtil.drawBoxESP(this.currentPos, this.timer.passedMs((int)(2000.0f * esohack.serverManager.getTpsFactor())) ? readyColor : color, false, color, this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), false);
        }
    }
}
