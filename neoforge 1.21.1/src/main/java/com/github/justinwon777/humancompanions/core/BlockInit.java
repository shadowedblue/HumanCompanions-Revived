package com.github.justinwon777.humancompanions.core;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.block.CompanionBedBlock;
import com.github.justinwon777.humancompanions.block.CompanionBedBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockInit {

    private BlockInit() {}

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, HumanCompanions.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HumanCompanions.MOD_ID);

    public static final DeferredHolder<Block, CompanionBedBlock> COMPANION_BED = BLOCKS.register("companion_bed",
            () -> new CompanionBedBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(1.5F, 1200.0F)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompanionBedBlockEntity>> COMPANION_BED_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("companion_bed", () ->
                    BlockEntityType.Builder.of(CompanionBedBlockEntity::new, COMPANION_BED.get()).build(null));
}
