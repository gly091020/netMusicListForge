package com.gly091020.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;

public class NetMusicListKeyMapping {
    // 不要使用@OnlyIn(Dist.CLIENT)！会导致正式服务器环境加载失败
    public static KeyMapping TOGGLE_MUSIC_SPEED_UP;
    public static KeyMapping TOGGLE_MUSIC_TRANSFORM;
    public static KeyMapping FAST_STOP;

    public static void init(){
        TOGGLE_MUSIC_TRANSFORM = new KeyMapping(
                "key.net_music_list.toggle_music_transform",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LALT,
                "modmenu.nameTranslation.net_music_list"
        );
        TOGGLE_MUSIC_SPEED_UP = new KeyMapping(
                "key.net_music_list.toggle_music_speed_up",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LSHIFT,
                "modmenu.nameTranslation.net_music_list"
        );
        FAST_STOP = new KeyMapping(
                "key.net_music_list.toggle_music_fast_stop",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                "modmenu.nameTranslation.net_music_list"
        );
    }

    public static void registerKeyBindings(final RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MUSIC_SPEED_UP);
        event.register(TOGGLE_MUSIC_TRANSFORM);
        event.register(FAST_STOP);
    }
}
