package com.gly091020.client;

import com.gly091020.NetMusicList;
import com.gly091020.hud.MusicInfoHud;
import com.gly091020.hud.MusicListLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = NetMusicList.ModID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event){
        var graphics = event.getGuiGraphics();
        MusicInfoHud.render(graphics);
        MusicListLayer.render(graphics);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event){
        if(!MusicListLayer.isRender){return;}
        var d = event.getScrollDelta();
        if(Math.abs(d) >= 1){
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        }
        MusicListLayer.index -= (int)(d * (Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.isShiftKeyDown() ? 5 : 1));
        if(MusicListLayer.index < 0){ MusicListLayer.index = 0;}
        if(MusicListLayer.index >= MusicListLayer.count){MusicListLayer.index = MusicListLayer.count - 1;}
        event.setCanceled(true);
    }
}
