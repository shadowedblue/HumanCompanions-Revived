package com.github.justinwon777.humancompanions.world;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.core.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.FileSystems;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads structure biome overrides from config and generates a datapack that adds
 * those biomes to the structure tags (e.g. for mod-added biomes like Biomes O' Plenty).
 */
@EventBusSubscriber(modid = HumanCompanions.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class StructureBiomeOverrides {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PACK_MCMETA = """
            {"pack":{"pack_format":34,"description":"HumanCompanions structure biome overrides"}}
            """;

    /** Example config file (fully commented, unused). Written to config/ so users can copy into humancompanions-structure-biomes.toml. */
    private static final String EXAMPLE_CONFIG = """
            # HumanCompanions - Structure biome overrides (EXAMPLE - this file is not loaded)
            # Copy entries you want into: humancompanions-structure-biomes.toml
            #
            # Format: each entry is "biome_id|structure1,structure2,..."
            # Biome ID = mod:biome_name (e.g. from Biomes O' Plenty). This applies to world generation (server / common config).
            #
            # --- Structure IDs and their pools (random building variants placed when that structure is chosen) ---
            # oak_house         -> oak_pool:         medieval_mixed1/2/3, medieval_mixed_double, medieval_oak_double, cabin, cabin2, medieval_triple, oak_house
            # oak_birch_house   -> oak_birch_pool:   medieval_mixed1/2/3, medieval_mixed_double, medieval_oak_double, medieval_birch_double, birch_house, oak_house
            # birch_house       -> birch_pool:       medieval_mixed1/2/3, medieval_mixed_double, medieval_birch_double, birch_house
            # spruce_house      -> spruce_pool:      medieval_mixed1/2/3, medieval_mixed_double, medieval_spruce_darkoak, cabin, cabin2, medieval_triple, medieval_spruce_double, spruce_house
            # dark_oak_house     -> dark_oak_pool:    medieval_spruce_darkoak, medieval_spruce_double
            # acacia_house      -> acacia_pool:     medieval_mixed1/2/3, medieval_acacia_double, acacia_house
            # sandstone_house   -> sandstone_pool:   fortified_desert, sandstone_house, desert, desert_double
            # terracotta_house  -> terracotta_pool: terracotta1, terracotta2, terracotta_double
            #
            # [structure_biome_overrides]
            # entries = [
            #     "biomesoplenty:grassland|oak_house,oak_birch_house,birch_house",
            #     "biomesoplenty:clover_patch|oak_house,birch_house",
            #     "biomesoplenty:field|oak_house,oak_birch_house",
            #     "biomesoplenty:forested_field|oak_house,spruce_house,birch_house,dark_oak_house"
            # ]
            """;

    /**
     * Parse config entries "biome_id|structure1,structure2,..." into structure -> additional biomes.
     */
    public static Map<String, List<String>> getStructureToAdditionalBiomes() {
        List<? extends String> entries = Config.STRUCTURE_BIOME_OVERRIDES.get();
        Map<String, List<String>> structureToBiomes = new HashMap<>();
        for (Object entry : entries) {
            String line = entry.toString().trim();
            if (line.isEmpty()) continue;
            int pipe = line.indexOf('|');
            if (pipe <= 0 || pipe == line.length() - 1) continue;
            String biomeId = line.substring(0, pipe).trim();
            String structureList = line.substring(pipe + 1).trim();
            if (biomeId.isEmpty() || structureList.isEmpty()) continue;
            for (String structureId : structureList.split(",")) {
                String s = structureId.trim();
                if (s.isEmpty()) continue;
                structureToBiomes.computeIfAbsent(s, k -> new ArrayList<>()).add(biomeId);
            }
        }
        return structureToBiomes;
    }

    /**
     * Generate datapack folder with tag JSONs and return the folder path, or empty if nothing to add.
     */
    public static Path generateDatapackFolder() throws IOException {
        Map<String, List<String>> structureToBiomes = getStructureToAdditionalBiomes();
        if (structureToBiomes.isEmpty()) return null;

        Path configDir = FMLPaths.CONFIGDIR.get();
        Path packRoot = configDir.resolve("humancompanions").resolve("structure_biome_datapack");
        Path dataDir = packRoot.resolve("data").resolve(HumanCompanions.MOD_ID).resolve("tags").resolve("worldgen").resolve("biome").resolve("has_structure");

        Files.createDirectories(dataDir);

        Files.writeString(packRoot.resolve("pack.mcmeta"), PACK_MCMETA.trim(), StandardCharsets.UTF_8);

        for (Map.Entry<String, List<String>> e : structureToBiomes.entrySet()) {
            String structureId = e.getKey();
            List<String> biomes = e.getValue();
            if (biomes.isEmpty()) continue;
            JsonObject tag = new JsonObject();
            tag.addProperty("replace", false);
            JsonArray values = new JsonArray();
            biomes.forEach(values::add);
            tag.add("values", values);
            Path tagFile = dataDir.resolve(structureId + ".json");
            Files.writeString(tagFile, GSON.toJson(tag), StandardCharsets.UTF_8);
        }

        return packRoot;
    }

    /** Writes the example config file to config/ if it doesn't exist (for format reference only, not loaded). */
    public static void ensureExampleConfigExists() {
        Path examplePath = FMLPaths.CONFIGDIR.get().resolve("humancompanions-structure-biomes-example.toml");
        if (Files.exists(examplePath)) return;
        try {
            Files.createDirectories(examplePath.getParent());
            Files.writeString(examplePath, EXAMPLE_CONFIG.trim(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            HumanCompanions.LOGGER.debug("Could not write structure biome example config", e);
        }
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        ensureExampleConfigExists();
        try {
            Path packRoot = generateDatapackFolder();
            if (packRoot == null) return;
            event.addRepositorySource(new FolderRepositorySource(
                    packRoot,
                    PackType.SERVER_DATA,
                    PackSource.FEATURE,
                    new DirectoryValidator(FileSystems.getDefault().getPathMatcher("glob:**"))
            ));
        } catch (IOException e) {
            HumanCompanions.LOGGER.warn("Failed to generate structure biome overrides datapack", e);
        }
    }
}
