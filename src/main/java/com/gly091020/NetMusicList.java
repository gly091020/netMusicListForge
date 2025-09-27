package com.gly091020;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.config.ConfigScreenGetter;
import com.gly091020.config.NetMusicListConfig;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.item.NetMusicPlayerItem;
import com.gly091020.packet.PacketRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NetMusicList.ModID)
public class NetMusicList {
    public static final String ModID = "net_music_list";

    public static final Logger LOGGER = LoggerFactory.getLogger(ModID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModID);

    public static final RegistryObject<NetMusicListItem> MUSIC_LIST_ITEM = ITEMS.register("music_list",
            NetMusicListItem::new);
    public static final RegistryObject<NetMusicPlayerItem> MUSIC_PLAYER_ITEM = ITEMS.register("music_player",
            NetMusicPlayerItem::new);

    public static NetMusicListConfig CONFIG;

    @SuppressWarnings("all")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ModID, "send_data"),
            () -> "1.3",  // 协议版本
            "1.3"::equals,
            "1.3"::equals
    );

    public NetMusicList(IEventBus modEventBus) {
        AutoConfig.register(NetMusicListConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(NetMusicListConfig.class).get();
        NetMusicListUtil.reloadConfig();

        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addItemsToCreativeTab);
        if(FMLEnvironment.dist == Dist.CLIENT){
            PacketRegistry.registryClient();
        }else{
            PacketRegistry.registryServer();
        }
        modEventBus.addListener(this::addPack);

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, screen) ->
                                ConfigScreenGetter.getConfigScreen(screen)
                )
        );

        if(FMLEnvironment.dist.isClient()){
            modEventBus.addListener(NetMusicListKeyMapping::registerKeyBindings);
        }
    }

    public NetMusicList() {
        this(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == InitItems.NET_MUSIC_TAB.get()) {
            event.getEntries().putAfter(
                    new ItemStack(InitItems.MUSIC_CD.get()),
                    new ItemStack(MUSIC_LIST_ITEM.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(MUSIC_LIST_ITEM.get()),
                    new ItemStack(MUSIC_PLAYER_ITEM.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }

    private void addPack(AddPackFindersEvent event) {
        event.addRepositorySource(new MP3Pack());
    }
}