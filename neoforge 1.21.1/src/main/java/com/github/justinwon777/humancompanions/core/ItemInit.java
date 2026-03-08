package com.github.justinwon777.humancompanions.core;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.item.CompanionRecallItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemInit {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HumanCompanions.MOD_ID);

    public static final DeferredItem<DeferredSpawnEggItem> Arbalist_Spawn_Egg = ITEMS.register("arbalist_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityInit.Arbalist, 0xE8AF5A, 0xFF0000,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<DeferredSpawnEggItem> Archer_Spawn_Egg = ITEMS.register("archer_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityInit.Archer, 0xE8AF5A, 0x0000FF,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<DeferredSpawnEggItem> Axeguard_Spawn_Egg = ITEMS.register("axeguard_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityInit.Axeguard, 0xE8AF5A, 0x00FF00,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<DeferredSpawnEggItem> Knight_Spawn_Egg = ITEMS.register("knight_spawn_egg",
            () -> new DeferredSpawnEggItem(EntityInit.Knight, 0xE8AF5A, 0xFFFF00,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<BlockItem> COMPANION_BED = ITEMS.register("companion_bed",
            () -> new BlockItem(BlockInit.COMPANION_BED.get(), new Item.Properties()));

    public static final DeferredItem<CompanionRecallItem> COMPANION_RECALL = ITEMS.register("companion_recall",
            () -> new CompanionRecallItem(new Item.Properties().stacksTo(1)));
}
