package com.gly091020.block;

import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnderMusicPlayer extends BlockMusicPlayer {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderMusicPlayerEntity(pos, state);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        var entity = worldIn.getBlockEntity(pos);
        if(!(entity instanceof EnderMusicPlayerEntity enderMusicPlayer))return InteractionResult.PASS;
        if(playerIn.isShiftKeyDown() && !enderMusicPlayer.isPlay()){
            if(worldIn.isClientSide){
                playerIn.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
                return InteractionResult.SUCCESS;
            }
            var uuid = playerIn.getUUID();
            if(enderMusicPlayer.hasPlayer(uuid)){
                enderMusicPlayer.removePlayer(uuid);
                playerIn.sendSystemMessage(Component.translatable("block.ender_music_player.remove_player", playerIn.getDisplayName()));
            }else{
                playerIn.sendSystemMessage(Component.translatable("block.ender_music_player.add_player", playerIn.getDisplayName()));
                enderMusicPlayer.addPlayer(uuid);
            }
            return InteractionResult.SUCCESS;
        }

        IItemHandler handler = enderMusicPlayer.getPlayerInv();
        if (!handler.getStackInSlot(0).isEmpty()) {
            ItemStack extract = handler.extractItem(0, 1, false);
            popResource(worldIn, pos, extract);
            return InteractionResult.SUCCESS;
        } else {
            ItemStack stack = playerIn.getMainHandItem();
            ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stack);
            if (info == null) {
                return InteractionResult.PASS;
            } else if (info.vip) {
                if (worldIn.isClientSide) {
                    playerIn.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip").withStyle(ChatFormatting.RED));
                }

                return InteractionResult.FAIL;
            } else {
                handler.insertItem(0, stack.copy(), false);
                if (!playerIn.isCreative()) {
                    stack.shrink(1);
                }

                enderMusicPlayer.setPlayToClient(info);
                enderMusicPlayer.markDirty();
                return InteractionResult.SUCCESS;
            }
        }
    }
}
