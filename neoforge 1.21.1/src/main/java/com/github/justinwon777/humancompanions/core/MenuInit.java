package com.github.justinwon777.humancompanions.core;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.container.CompanionContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuInit {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, HumanCompanions.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CompanionContainer>> COMPANION_MENU =
            MENUS.register("companion_menu", () ->
                    IMenuTypeExtension.create(CompanionContainer::fromClient));
}
