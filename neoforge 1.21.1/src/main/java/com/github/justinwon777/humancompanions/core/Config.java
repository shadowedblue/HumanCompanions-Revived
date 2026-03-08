package com.github.justinwon777.humancompanions.core;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

import java.util.List;

public class Config {

    public static ModConfigSpec.IntValue AVERAGE_HOUSE_SEPARATION;
    public static ModConfigSpec.BooleanValue FRIENDLY_FIRE_COMPANIONS;
    public static ModConfigSpec.BooleanValue FRIENDLY_FIRE_PLAYER;
    public static ModConfigSpec.BooleanValue FALL_DAMAGE;
    public static ModConfigSpec.BooleanValue SPAWN_ARMOR;
    public static ModConfigSpec.BooleanValue SPAWN_WEAPON;
    public static ModConfigSpec.IntValue BASE_HEALTH;
    public static ModConfigSpec.BooleanValue LOW_HEALTH_FOOD;
    public static ModConfigSpec.BooleanValue CREEPER_WARNING;

    /** List of "biome_id|structure1,structure2,..." for structure biome overrides. */
    public static ModConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_BIOME_OVERRIDES;

    public static void register(ModContainer container) {
        registerCommonConfig(container);
        registerStructureBiomeOverridesConfig(container);
    }

    private static void registerCommonConfig(ModContainer container) {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        COMMON_BUILDER.comment("Settings for world gen (Doesn't work in 1.18.2 and beyond. Use datapacks instead.)").push("World" +
                " Gen");
        AVERAGE_HOUSE_SEPARATION = COMMON_BUILDER
                .comment("Average chunk separation between companion houses")
                .defineInRange("averageHouseSeparation", 20, 11, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();
        COMMON_BUILDER.push("Companion config");
        FRIENDLY_FIRE_COMPANIONS = COMMON_BUILDER
                .comment("Toggles friendly fire between companions")
                .define("friendlyFireCompanions", false);
        FRIENDLY_FIRE_PLAYER = COMMON_BUILDER
                .comment("Toggles friendly fire between player and companion")
                .define("friendlyFirePlayer", false);
        FALL_DAMAGE = COMMON_BUILDER
                .comment("Toggles fall damage for companions")
                .define("fallDamage", true);
        SPAWN_ARMOR = COMMON_BUILDER
                .comment("Toggles whether companions spawn with armor")
                .define("spawnArmor", true);
        SPAWN_WEAPON = COMMON_BUILDER
                .comment("Toggles whether companions spawn with a weapon")
                .define("spawnWeapon", true);
        BASE_HEALTH = COMMON_BUILDER
                .comment("Sets the base health of each companion. Companions spawn with up to +-4 from the base health")
                .defineInRange("baseHealth", 20, 5, Integer.MAX_VALUE);
        LOW_HEALTH_FOOD = COMMON_BUILDER
                .comment("Toggles whether companions ask for food if their health goes below half.")
                .define("lowHealthFood", true);
        CREEPER_WARNING = COMMON_BUILDER
                .comment("Toggles whether companions alert you if a creeper is nearby.")
                .define("creeperWarning", true);
        container.registerConfig(ModConfig.Type.COMMON, COMMON_BUILDER.build());
    }

    private static void registerStructureBiomeOverridesConfig(ModContainer container) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Additional biomes where companion structures can spawn (e.g. from mods like Biomes O' Plenty).",
                        "Each entry: \"biome_id|structure1,structure2,...\". Each structure uses a pool of building variants (e.g. oak_house can place cabin, medieval_*, etc.).",
                        "Structure IDs: acacia_house, birch_house, dark_oak_house, oak_house, oak_birch_house, sandstone_house, spruce_house, terracotta_house.",
                        "Full pool reference (which pieces each structure can place): see humancompanions-structure-biomes-example.toml in config/.")
                .push("structure_biome_overrides");
        STRUCTURE_BIOME_OVERRIDES = builder
                .comment("Example: \"biomesoplenty:savannah_lush|acacia_house,oak_house\"")
                .defineList("entries", List.of(), s -> s instanceof String);
        builder.pop();
        container.registerConfig(ModConfig.Type.COMMON, builder.build(), "humancompanions-structure-biomes.toml");
    }
}
