package com.gly091020;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.Mod;

@Mod(value = NetMusicList.ModID)
@OnlyIn(Dist.CLIENT)
public class NetMusicListKeyMapping {
    public static final KeyMapping TOGGLE_MUSIC_SPEED_UP = new KeyMapping(
            "key.net_music_list.toggle_music_speed_up",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT,
            "modmenu.nameTranslation.net_music_list"
    );
    public static final KeyMapping TOGGLE_MUSIC_TRANSFORM = new KeyMapping(
            "key.net_music_list.toggle_music_transform",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LALT,
            "modmenu.nameTranslation.net_music_list"
    );

    public static void registerKeyBindings(final RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MUSIC_SPEED_UP);
        event.register(TOGGLE_MUSIC_TRANSFORM);
    }
}
