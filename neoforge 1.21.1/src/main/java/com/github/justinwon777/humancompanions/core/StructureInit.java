package com.github.justinwon777.humancompanions.core;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.world.CompanionHouseStructure;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class StructureInit {
    public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTRY_STRUCTURE =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, HumanCompanions.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<CompanionHouseStructure>> COMPANION_HOUSE =
            DEFERRED_REGISTRY_STRUCTURE.register("companion_house", () -> typeConvert(CompanionHouseStructure.CODEC));

    private static <S extends Structure> StructureType<S> typeConvert(MapCodec<S> codec) {
        return () -> codec;
    }
}
