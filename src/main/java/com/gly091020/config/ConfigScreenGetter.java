package com.gly091020.config;

import com.gly091020.NetMusicListUtil;
import com.gly091020.client.MoveHudScreen;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.gly091020.NetMusicList.CONFIG;

@OnlyIn(Dist.CLIENT)
public class ConfigScreenGetter {
    public static Screen getConfigScreen(Screen parent){
        var builder = ConfigBuilder.create();
        builder.setParentScreen(parent);
        builder.setTitle(Component.translatable("config.net_music_list.title"));
        var category = builder.getOrCreateCategory(Component.empty());
        var entryBuilder = builder.entryBuilder();

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.net_music_list.select_hud_artist"),
                CONFIG.selectHudShowArtist)
                .setDefaultValue(true).setSaveConsumer(b -> CONFIG.selectHudShowArtist = b).build());
        category.addEntry(entryBuilder.startIntSlider(Component.translatable("config.net_music_list.select_hud_length"),
                        CONFIG.selectHudCount, 10, 30)
                        .setDefaultValue(20)
                        .setSaveConsumer(i -> CONFIG.selectHudCount = i)
                .build());
        category.addEntry(entryBuilder.startIntSlider(Component.translatable("config.net_music_list.select_hud_size"), (int)(CONFIG.selectHudSize * 10), 3, 10)
                        .setDefaultValue(7)
                        .setSaveConsumer(integer -> CONFIG.selectHudSize = integer / 10f)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.net_music_list.music_hud"),
                        CONFIG.musicHUD).setSaveConsumer(b -> CONFIG.musicHUD = b).setDefaultValue(true)
                .build());

        var buttonWidget = new ButtonEntry(Component.translatable("config.net_music_list.set_hud_pos"),
                (button) -> MoveHudScreen.open());
        buttonWidget.isEnable(CONFIG.musicHUD);
        category.addEntry(buttonWidget);
        builder.setSavingRunnable(NetMusicListUtil::reloadConfig);

        return builder.build();
    }
}
