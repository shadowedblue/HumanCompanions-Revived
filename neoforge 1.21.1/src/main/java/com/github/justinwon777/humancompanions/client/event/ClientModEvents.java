package com.github.justinwon777.humancompanions.client.event;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.client.CompanionScreen;
import com.github.justinwon777.humancompanions.client.renderer.CompanionRenderer;
import com.github.justinwon777.humancompanions.core.EntityInit;
import com.github.justinwon777.humancompanions.core.MenuInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = HumanCompanions.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents () {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.Knight.get(), CompanionRenderer::new);
        event.registerEntityRenderer(EntityInit.Archer.get(), CompanionRenderer::new);
        event.registerEntityRenderer(EntityInit.Arbalist.get(), CompanionRenderer::new);
        event.registerEntityRenderer(EntityInit.Axeguard.get(), CompanionRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuInit.COMPANION_MENU.get(), CompanionScreen::new);
    }
}
