package com.github.justinwon777.humancompanions.entity;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.core.BlockInit;
import com.github.justinwon777.humancompanions.core.CompanionBedHandler;
import com.github.justinwon777.humancompanions.core.CompanionLastPositionData;
import com.github.justinwon777.humancompanions.core.CompanionRespawnData;
import com.github.justinwon777.humancompanions.core.CompanionRespawnRequest;
import com.github.justinwon777.humancompanions.core.Config;
import com.github.justinwon777.humancompanions.block.CompanionBedBlock;
import com.github.justinwon777.humancompanions.block.CompanionBedBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = HumanCompanions.MOD_ID)
public class CompanionEvents {

    @SubscribeEvent
    public static void giveExperience(final LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof AbstractHumanCompanionEntity companion
                && event.getEntity().level() instanceof ServerLevel serverLevel) {
            companion.giveExperiencePoints(event.getEntity().getExperienceReward(serverLevel, companion));
        }
    }

    /** When the owner dies, save all their companions' positions so recall works after respawn (chunks may unload). */
    @SubscribeEvent
    public static void onOwnerDeathSaveCompanionPositions(final LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        var server = serverLevel.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            for (AbstractHumanCompanionEntity companion : level.getEntitiesOfClass(AbstractHumanCompanionEntity.class, AABB.INFINITE)) {
                if (companion.isTame() && companion.getOwner() != null && companion.getOwner().getUUID().equals(player.getUUID())) {
                    CompanionLastPositionData.get(server).setPosition(companion.getUUID(), level, companion.blockPosition());
                }
            }
        }
    }

    /** On companion death, if they have a bound companion bed, enqueue a respawn request. */
    @SubscribeEvent
    public static void onCompanionDeathEnqueueRespawn(final LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof AbstractHumanCompanionEntity companion) || !companion.hasCompanionBed()) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        String entityTypeId = serverLevel.registryAccess().registryOrThrow(Registries.ENTITY_TYPE)
                .getKey(companion.getType()).toString();
        String dimension = companion.getCompanionBedDimension();
        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", entityTypeId);
        companion.saveWithoutId(entityData);
        String companionName = companion.getCustomName() != null ? companion.getCustomName().getString() : "";

        CompanionRespawnRequest request = new CompanionRespawnRequest(
                entityTypeId,
                dimension,
                entityData,
                companion.getCompanionBedPos(),
                companionName
        );
        CompanionRespawnData.get(serverLevel.getServer()).addRequest(request);
    }

    /** When a companion bed is broken, clear the bound companion's bed and remove any respawn requests for that bed. */
    @SubscribeEvent
    public static void onCompanionBedBroken(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getState().getBlock() != BlockInit.COMPANION_BED.get()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        var blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (!(blockEntity instanceof CompanionBedBlockEntity bedEntity)) return;

        java.util.UUID boundUuid = bedEntity.getBoundCompanionUUID();
        if (boundUuid != null) {
            var server = serverLevel.getServer();
            for (ServerLevel level : server.getAllLevels()) {
                var entity = level.getEntity(boundUuid);
                if (entity instanceof AbstractHumanCompanionEntity companion) {
                    companion.clearCompanionBed();
                    break;
                }
            }
        }
        CompanionRespawnData.get(serverLevel.getServer()).removeRequestsFor(serverLevel, event.getPos());
        CompanionBedBlock.clearBreakWarningFor(event.getPos());
    }

    /** When the owner changes dimension (e.g. enters portal), save their companions' positions in the level they're leaving. */
    @SubscribeEvent
    public static void onOwnerChangeDimensionSaveCompanionPositions(final EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level() instanceof ServerLevel fromLevel) {
            var server = fromLevel.getServer();
            for (AbstractHumanCompanionEntity companion : fromLevel.getEntitiesOfClass(AbstractHumanCompanionEntity.class, AABB.INFINITE)) {
                if (companion.isTame() && companion.getOwner() != null && companion.getOwner().getUUID().equals(player.getUUID())) {
                    CompanionLastPositionData.get(server).setPosition(companion.getUUID(), fromLevel, companion.blockPosition());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        CompanionBedHandler.clearForPlayer(player.getUUID());
    }

    @SubscribeEvent
    public static void friendlyFire(final LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof AbstractHumanCompanionEntity companion && companion.isTame()) {
            if (!Config.FRIENDLY_FIRE_PLAYER.get()) {
                if (event.getEntity() instanceof Player player) {
                    if (companion.getOwner() == player) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
            if (!Config.FRIENDLY_FIRE_COMPANIONS.get()) {
                if (event.getEntity() instanceof TamableAnimal entity) {
                    if (entity.isTame()) {
                        LivingEntity owner1 = entity.getOwner();
                        LivingEntity owner2 = companion.getOwner();
                        if (owner1 == owner2) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }
}
