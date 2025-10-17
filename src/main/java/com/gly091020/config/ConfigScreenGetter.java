package com.gly091020.config;

import com.gly091020.NetMusicList;
import com.gly091020.client.FuckBlitNineSlicedScreen;
import com.gly091020.util.NetMusicListUtil;
import com.gly091020.client.MoveHudScreen;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

import static com.gly091020.NetMusicList.CONFIG;

@OnlyIn(Dist.CLIENT)
public class ConfigScreenGetter {
    public static Screen getConfigScreen(Screen parent){
        var builder = ConfigBuilder.create();
        builder.setParentScreen(parent);
        builder.setTitle(Component.translatable("config.net_music_list.title"));
        var category = builder.getOrCreateCategory(Component.empty());
        var entryBuilder = builder.entryBuilder();

        if(NetMusicListUtil.isGLY() || !FMLEnvironment.production){
            category.addEntry(new ImageEntry(ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "textures/gui/server.png")));
        }
        if(NetMusicListUtil.isWangRenZe9788() || NetMusicListUtil.isN44()){
            // gly特有的自黑
            category.addEntry(new ImageEntry(ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "textures/gui/gly_is_suck.png")));
        }

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.net_music_list.select_hud_artist"),
                CONFIG.selectHudShowArtist)
                .setDefaultValue(true).setSaveConsumer(b -> CONFIG.selectHudShowArtist = b).build());
        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.net_music_list.not_pause_sound"),
                        CONFIG.notPauseSoundOnGamePause)
                        .setTooltip(Component.translatable("config.net_music_list.not_pause_sound.tip"))
                .setDefaultValue(false).setSaveConsumer(b -> CONFIG.notPauseSoundOnGamePause = b).build());
        category.addEntry(entryBuilder.startIntSlider(Component.translatable("config.net_music_list.max_import_list"), CONFIG.maxImportList, 100, 1000)
                .setDefaultValue(300)
                .setSaveConsumer(i -> CONFIG.maxImportList = i)
                .build());
        category.addEntry(entryBuilder.startIntSlider(Component.translatable("config.net_music_list.select_hud_length"),
                        CONFIG.selectHudCount, 10, 50)
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

        if(!FMLEnvironment.production){
            category.addEntry(new ButtonEntry(Component.literal("打开FuckBlitNineSlicedScreen"), button ->
                    Minecraft.getInstance().setScreen(new FuckBlitNineSlicedScreen())));
        }

        builder.setSavingRunnable(NetMusicListUtil::reloadConfig);

        return builder.build();
    }
}
