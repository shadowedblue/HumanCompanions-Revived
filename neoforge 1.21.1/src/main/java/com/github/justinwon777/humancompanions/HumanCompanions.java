package com.github.justinwon777.humancompanions;

import com.github.justinwon777.humancompanions.core.*;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.github.justinwon777.humancompanions.core.ItemInit.*;

@Mod(HumanCompanions.MOD_ID)
public class HumanCompanions {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "humancompanions";

    public HumanCompanions(IEventBus modBus, ModContainer container) {
        EntityInit.ENTITIES.register(modBus);
        BlockInit.BLOCKS.register(modBus);
        BlockInit.BLOCK_ENTITY_TYPES.register(modBus);
        DataComponentInit.DATA_COMPONENTS.register(modBus);
        ItemInit.ITEMS.register(modBus);
        MenuInit.MENUS.register(modBus);
        modBus.addListener(this::buildContents);
        StructureInit.DEFERRED_REGISTRY_STRUCTURE.register(modBus);
        modBus.addListener(PacketHandler::onRegisterPayloads);
        Config.register(container);
    }

    public void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(Arbalist_Spawn_Egg);
            event.accept(Knight_Spawn_Egg);
            event.accept(Archer_Spawn_Egg);
            event.accept(Axeguard_Spawn_Egg);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ItemInit.COMPANION_BED);
            event.accept(ItemInit.COMPANION_RECALL);
        }
    }
}
