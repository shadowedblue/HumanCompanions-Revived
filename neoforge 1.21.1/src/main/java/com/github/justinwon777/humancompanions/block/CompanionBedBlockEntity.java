package com.github.justinwon777.humancompanions.block;

import com.github.justinwon777.humancompanions.core.BlockInit;
import com.github.justinwon777.humancompanions.core.CompanionRespawnData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class CompanionBedBlockEntity extends BlockEntity {

    private static final String BOUND_NAME_KEY = "BoundCompanionName";
    private static final String BOUND_UUID_KEY = "BoundCompanionUUID";

    private String boundCompanionName = "";
    @Nullable
    private UUID boundCompanionUUID;

    public CompanionBedBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.COMPANION_BED_BLOCK_ENTITY.get(), pos, state);
    }

    public String getBoundCompanionName() {
        return boundCompanionName;
    }

    @Nullable
    public UUID getBoundCompanionUUID() {
        return boundCompanionUUID;
    }

    public void setBoundCompanion(String name, @Nullable UUID uuid) {
        this.boundCompanionName = name == null ? "" : name;
        this.boundCompanionUUID = uuid;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(BOUND_NAME_KEY, boundCompanionName);
        if (boundCompanionUUID != null) {
            tag.putUUID(BOUND_UUID_KEY, boundCompanionUUID);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        boundCompanionName = tag.getString(BOUND_NAME_KEY);
        boundCompanionUUID = tag.hasUUID(BOUND_UUID_KEY) ? tag.getUUID(BOUND_UUID_KEY) : null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CompanionBedBlockEntity blockEntity) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        long time = level.getDayTime() % 24000L;
        if (time != 1) return;

        if (CompanionRespawnData.processOneRespawn(serverLevel, pos)) {
            // Optional: notify owner in chat (Phase 5 can add this)
        }
    }
}
