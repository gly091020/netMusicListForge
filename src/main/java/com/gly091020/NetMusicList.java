package com.gly091020;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.block.EnderMusicPlayer;
import com.gly091020.block.EnderMusicPlayerEntity;
import com.gly091020.config.ConfigScreenGetter;
import com.gly091020.config.NetMusicListConfig;
import com.gly091020.datagen.*;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.item.NetMusicListManual;
import com.gly091020.item.NetMusicPlayerItem;
import com.gly091020.packet.PacketRegistry;
import com.gly091020.util.MP3Pack;
import com.gly091020.util.NetMusicListKeyMapping;
import com.gly091020.util.NetMusicListUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
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

import java.util.concurrent.CompletableFuture;

@Mod(NetMusicList.ModID)
public class NetMusicList {
    public static final String ModID = "net_music_list";

    public static final Logger LOGGER = LoggerFactory.getLogger(ModID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModID);
    public static final RegistryObject<NetMusicListItem> MUSIC_LIST_ITEM = ITEMS.register("music_list",
            NetMusicListItem::new);
    public static final RegistryObject<NetMusicPlayerItem> MUSIC_PLAYER_ITEM = ITEMS.register("music_player",
            NetMusicPlayerItem::new);
    public static final RegistryObject<Item> MANUAL_MODEL = ITEMS.register("manual_model",
            () -> new Item(new Item.Properties()));  // 俺寻思之力
    public static final RegistryObject<NetMusicListManual> MANUAL = ITEMS.register("manual",
            NetMusicListManual::new);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModID);
    public static final RegistryObject<EnderMusicPlayer> ENDER_MUSIC_PLAYER = BLOCKS.register("ender_music_player", EnderMusicPlayer::new);
    public static final RegistryObject<BlockEntityType<?>> ENDER_MUSIC_PLAYER_TYPE = BLOCK_ENTITY_TYPES.register("ender_music_player_entity", () -> BlockEntityType.Builder.of(EnderMusicPlayerEntity::new, ENDER_MUSIC_PLAYER.get()).build(null));
    public static final RegistryObject<Item> ENDER_PLAYER_ITEM = ITEMS.register("ender_music_player", () -> new BlockItem(ENDER_MUSIC_PLAYER.get(), new Item.Properties()));

    public static NetMusicListConfig CONFIG;

    @SuppressWarnings("all")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ModID, "send_data"),
            () -> "1.5",  // 协议版本
            "1.5"::equals,
            "1.5"::equals
    );

    @SuppressWarnings("removal")
    public NetMusicList(IEventBus modEventBus) {
        AutoConfig.register(NetMusicListConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(NetMusicListConfig.class).get();
        NetMusicListUtil.reloadConfig();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
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
            NetMusicListKeyMapping.init();
            modEventBus.addListener(NetMusicListKeyMapping::registerKeyBindings);
        }
        modEventBus.addListener(NetMusicList::gatherData);
    }

    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new BlockModelGenerator(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(output, existingFileHelper));
        generator.addProvider(event.includeServer(), new LootTableGenerator(output));
        generator.addProvider(event.includeServer(), new AdvancementGenerator(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new RecipeGenerator(output));
    }

    @SuppressWarnings("removal")
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
            event.getEntries().putAfter(
                    new ItemStack(MUSIC_PLAYER_ITEM.get()),
                    new ItemStack(MANUAL.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );

            event.getEntries().putAfter(
                    new ItemStack(MANUAL.get()),
                    new ItemStack(ENDER_PLAYER_ITEM.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }

    private void addPack(AddPackFindersEvent event) {
        event.addRepositorySource(new MP3Pack());
    }
}