package com.github.justinwon777.humancompanions.block;

import com.github.justinwon777.humancompanions.core.BlockInit;
import com.github.justinwon777.humancompanions.core.CompanionBedHandler;
import com.github.justinwon777.humancompanions.core.CompanionLastPositionData;
import com.github.justinwon777.humancompanions.core.CompanionRespawnData;
import com.github.justinwon777.humancompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CompanionBedBlock extends BaseEntityBlock {

    /** Carpet-like shape: full width/depth, 1/16 block tall. */
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    /** Tracks last bed we warned this player about (so we warn once per bed while breaking). Cleared when that bed is broken. */
    private static final Map<UUID, BlockPos> LAST_BREAK_WARN_BED = new HashMap<>();

    public CompanionBedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return (com.mojang.serialization.MapCodec<? extends BaseEntityBlock>) (com.mojang.serialization.MapCodec<?>) Block.CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompanionBedBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, BlockInit.COMPANION_BED_BLOCK_ENTITY.get(), CompanionBedBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        float progress = super.getDestroyProgress(state, player, level, pos);
        if (level instanceof ServerLevel serverLevel) {
            boolean hasPending = !CompanionRespawnData.get(serverLevel.getServer()).getRequestsFor(serverLevel, pos).isEmpty();
            if (hasPending) {
                if (player instanceof ServerPlayer serverPlayer && !pos.equals(LAST_BREAK_WARN_BED.get(serverPlayer.getUUID()))) {
                    LAST_BREAK_WARN_BED.put(serverPlayer.getUUID(), pos);
                    serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_bed_break_warning"));
                }
                return progress * 0.2F;
            }
        }
        return progress;
    }

    /** Call when a companion bed is broken so we stop tracking the break warning for that pos. */
    public static void clearBreakWarningFor(BlockPos pos) {
        LAST_BREAK_WARN_BED.entrySet().removeIf(e -> e.getValue().equals(pos));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (hand != InteractionHand.MAIN_HAND) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;

        if (player.isShiftKeyDown()) {
            CompanionBedHandler.setPendingBed(player, pos, level.dimension().location().toString());
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_bed_set_pending"));
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.is(Items.GOLDEN_APPLE)) {
            if (level instanceof ServerLevel serverLevel && CompanionRespawnData.processOneRespawn(serverLevel, pos)) {
                stack.shrink(1);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_respawned"));
                }
                return ItemInteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.no_companion_awaiting_respawn"));
            }
            return ItemInteractionResult.FAIL;
        }

        if (stack.is(Items.STICK)) {
            if (level instanceof ServerLevel serverLevel) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof CompanionBedBlockEntity bedEntity && bedEntity.getBoundCompanionUUID() != null) {
                    UUID companionUUID = bedEntity.getBoundCompanionUUID();
                    AbstractHumanCompanionEntity companion = findCompanionInAnyLevel(serverLevel.getServer(), companionUUID);
                    // If not in any loaded dimension, try loading their last-known chunk (recall from unloaded)
                    if (companion == null) {
                        companion = tryFindCompanionByLoadingChunk(serverLevel.getServer(), companionUUID);
                    }
                    if (companion != null) {
                        teleportCompanionToBed(companion, serverLevel, pos);
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_recalled", bedEntity.getBoundCompanionName()));
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_recall_failed"));
                    }
                    return ItemInteractionResult.FAIL;
                }
            }
        }

        if (stack.isEmpty()) {
            sendBedOwnerMessage(level, pos, player);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    private static void sendBedOwnerMessage(Level level, BlockPos pos, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CompanionBedBlockEntity bedEntity)) return;
        String name = bedEntity.getBoundCompanionName();
        if (!name.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_bed_belongs_to", name));
            if (level instanceof ServerLevel serverLevel) {
                if (!CompanionRespawnData.get(serverLevel.getServer()).getRequestsFor(serverLevel, pos).isEmpty()) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_bed_awaiting_respawn_hint"));
                } else if (bedEntity.getBoundCompanionUUID() != null && isCompanionAwayButRecallable(serverLevel, bedEntity.getBoundCompanionUUID())) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_bed_recall_hint"));
                }
            }
        } else {
            serverPlayer.sendSystemMessage(Component.translatable("message.humancompanions.companion_bed_no_bound"));
        }
    }

    /** True if the companion is in another dimension (recallable with stick). False if in bed level or in unloaded chunks. */
    private static boolean isCompanionAwayButRecallable(ServerLevel bedLevel, UUID companionUUID) {
        if (findCompanionInLevel(bedLevel, companionUUID) != null) return false;
        return findCompanionInAnyLevel(bedLevel.getServer(), companionUUID) != null;
    }

    /** Uses level's entity lookup by UUID so we find the companion if they are loaded in this dimension. */
    @Nullable
    private static AbstractHumanCompanionEntity findCompanionInLevel(ServerLevel level, UUID uuid) {
        Entity e = level.getEntity(uuid);
        return e instanceof AbstractHumanCompanionEntity c ? c : null;
    }

    @Nullable
    private static AbstractHumanCompanionEntity findCompanionInAnyLevel(net.minecraft.server.MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            AbstractHumanCompanionEntity found = findCompanionInLevel(level, uuid);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Try to find the companion by loading chunks around their last-known position (they may be in unloaded chunks).
     * We load a 3x3 chunk area so we find them even if they moved to a neighboring chunk before unload.
     * Returns the companion if found after loading, otherwise null.
     */
    @Nullable
    private static AbstractHumanCompanionEntity tryFindCompanionByLoadingChunk(net.minecraft.server.MinecraftServer server, UUID uuid) {
        CompanionLastPositionData.Entry last = CompanionLastPositionData.get(server).getPosition(uuid);
        if (last == null) return null;
        ServerLevel targetLevel = null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().toString().equals(last.dimension())) {
                targetLevel = level;
                break;
            }
        }
        if (targetLevel == null) return null;
        ChunkPos center = new ChunkPos(last.pos());
        // Load 3x3 chunks around last position so we find companion even if they moved before chunks unloaded
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                targetLevel.getChunkSource().getChunk(center.x + dx, center.z + dz, true);
            }
        }
        return findCompanionInLevel(targetLevel, uuid);
    }

    private static void teleportCompanionToBed(AbstractHumanCompanionEntity companion, ServerLevel bedLevel, BlockPos bedPos) {
        teleportCompanionTo(companion, bedLevel, bedPos.getX() + 0.5, bedPos.getY() + 0.8, bedPos.getZ() + 0.5);
    }

    /** Teleport a companion to a position in the given level (handles dimension change). Used by bed recall and Companion Recall item. */
    public static void teleportCompanionTo(AbstractHumanCompanionEntity companion, ServerLevel targetLevel, double x, double y, double z) {
        if (companion.level() == targetLevel) {
            companion.teleportTo(x, y, z);
        } else {
            Vec3 targetPos = new Vec3(x, y, z);
            DimensionTransition transition = new DimensionTransition(
                    targetLevel, targetPos, Vec3.ZERO,
                    companion.getYRot(), companion.getXRot(),
                    false, DimensionTransition.DO_NOTHING);
            Entity newEntity = companion.changeDimension(transition);
            if (newEntity != null) {
                newEntity.teleportTo(x, y, z);
            }
        }
        // Save position immediately so recall works even if player leaves right away
        var server = targetLevel.getServer();
        if (server != null) {
            CompanionLastPositionData.get(server).setPosition(companion.getUUID(), targetLevel, BlockPos.containing(x, y, z));
        }
    }

    /** Find companion by UUID in any level, or by loading last-known chunk. Public for Companion Recall item. */
    @Nullable
    public static AbstractHumanCompanionEntity findCompanionForRecall(net.minecraft.server.MinecraftServer server, UUID uuid) {
        AbstractHumanCompanionEntity c = findCompanionInAnyLevel(server, uuid);
        if (c == null) c = tryFindCompanionByLoadingChunk(server, uuid);
        return c;
    }
}
