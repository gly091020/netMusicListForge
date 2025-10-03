package com.gly091020.client;

import com.gly091020.NetMusicList;
import com.gly091020.hud.MusicInfoHud;
import com.gly091020.hud.MusicListLayer;
import com.gly091020.sounds.PlayerNetMusicSound;
import com.gly091020.util.NetMusicListKeyMapping;
import com.gly091020.util.NetMusicListUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = NetMusicList.ModID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event){
        var graphics = event.getGuiGraphics();
        MusicInfoHud.render(graphics);
        MusicListLayer.render(graphics);
        if(NetMusicListUtil.globalStopMusic){
            var w = event.getWindow().getGuiScaledWidth();
            var h = event.getWindow().getGuiScaledHeight();
            event.getGuiGraphics().drawCenteredString(Minecraft.getInstance().font,
                    Component.translatable("text.net_music_list.fast_stoping"),
                    w / 2, (int) (h * 0.1f), 0xFFFFFFFF
            );
        }
    }

    @SubscribeEvent
    public static void onKeyClick(InputEvent.Key event){
        if(MusicListLayer.isRender && event.getKey() == InputConstants.KEY_ESCAPE){
            MusicListLayer.isRender = false;
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event){
        if(!MusicListLayer.isRender){return;}
        var d = event.getScrollDelta();
        if(Math.abs(d) >= 1){
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        }
        MusicListLayer.index -= (int)(d * (NetMusicListKeyMapping.TOGGLE_MUSIC_SPEED_UP.isDown() ? 5 : 1));
        if(MusicListLayer.index < 0){ MusicListLayer.index = 0;}
        if(MusicListLayer.index >= MusicListLayer.count){MusicListLayer.index = MusicListLayer.count - 1;}
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase != TickEvent.Phase.END){return;}
        soundFix();
        fastStop();
    }

    private static void fastStop(){
        if(NetMusicListKeyMapping.FAST_STOP.consumeClick()){
            NetMusicListUtil.globalStopMusic = !NetMusicListUtil.globalStopMusic;
        }
    }

    private static void soundFix(){
        var server = Minecraft.getInstance().getSingleplayerServer();
        if(!Minecraft.getInstance().isLocalServer() || (server != null && server.isPublished())){return;}
        var sounds = NetMusicListUtil.getTickableSounds();
        var screen = Minecraft.getInstance().screen;
        // 猜猜我用了几个Mixin？
        // SB MOJANG
        if(screen != null && !NetMusicListUtil.isPaused() && screen.isPauseScreen()){
            for(TickableSoundInstance instance: sounds){
                if(instance instanceof PlayerNetMusicSound playerNetMusicSound){
                    playerNetMusicSound.onlyTickUpdate();
                }
            }
        }
    }
}
