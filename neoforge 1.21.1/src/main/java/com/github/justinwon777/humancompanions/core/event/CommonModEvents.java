package com.github.justinwon777.humancompanions.core.event;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.core.EntityInit;
import com.github.justinwon777.humancompanions.entity.Arbalist;
import com.github.justinwon777.humancompanions.entity.Archer;
import com.github.justinwon777.humancompanions.entity.Axeguard;
import com.github.justinwon777.humancompanions.entity.Knight;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = HumanCompanions.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityInit.Knight.get(), Knight.createAttributes().build());
        event.put(EntityInit.Archer.get(), Archer.createAttributes().build());
        event.put(EntityInit.Arbalist.get(), Arbalist.createAttributes().build());
        event.put(EntityInit.Axeguard.get(), Axeguard.createAttributes().build());
    }
}
