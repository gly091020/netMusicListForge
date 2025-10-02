package com.gly091020.item;

import com.gly091020.client.NetMusicListManualRenderer;
import com.gly091020.util.PatchouliOpener;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NetMusicListManual extends Item {
    public NetMusicListManual() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return NetMusicListManualRenderer.getInstance();
            }
        });
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if(!player.level().isClientSide && ModList.get().isLoaded("patchouli")){
            PatchouliOpener.open((ServerPlayer) player);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }
}
