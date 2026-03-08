package com.github.justinwon777.humancompanions.item;

import com.github.justinwon777.humancompanions.block.CompanionBedBlock;
import com.github.justinwon777.humancompanions.core.DataComponentInit;
import com.github.justinwon777.humancompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CompanionRecallItem extends Item {

    public CompanionRecallItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        var bound = stack.get(DataComponentInit.BOUND_COMPANION.get());
        if (bound == null) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_recall_not_bound"));
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) return InteractionResultHolder.fail(stack);

        AbstractHumanCompanionEntity companion = CompanionBedBlock.findCompanionForRecall(serverLevel.getServer(), bound.companionId());
        if (companion == null) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_recall_failed"));
            }
            return InteractionResultHolder.fail(stack);
        }

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        CompanionBedBlock.teleportCompanionTo(companion, serverLevel, x, y, z);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_recalled_to_you", bound.companionName()));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        var bound = stack.get(DataComponentInit.BOUND_COMPANION.get());
        if (bound != null) {
            tooltipComponents.add(Component.translatable("item.humancompanions.companion_recall.bound", bound.companionName()));
        } else {
            tooltipComponents.add(Component.translatable("item.humancompanions.companion_recall.hint"));
        }
    }
}
