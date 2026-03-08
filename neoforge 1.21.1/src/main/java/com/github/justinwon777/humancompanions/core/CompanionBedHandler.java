package com.github.justinwon777.humancompanions.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-only handler for "pending" companion bed selection.
 * When a player shift+right-clicks a companion bed, we store that bed here;
 * the next shift+right-click on a companion (by the same player) binds that companion to the bed.
 * Cleared when the player binds a companion or logs out.
 */
public final class CompanionBedHandler {

    /** (player UUID -> pending bed). Only used on server; not persisted. */
    private static final Map<UUID, PendingBedSelection> PENDING_BEDS = new ConcurrentHashMap<>();

    private CompanionBedHandler() {}

    /**
     * Set the pending bed for this player (call when they shift+right-click a companion bed).
     * Server-only.
     */
    public static void setPendingBed(Player player, BlockPos pos, String dimension) {
        if (player.level().isClientSide()) return;
        PENDING_BEDS.put(player.getUUID(), new PendingBedSelection(pos, dimension));
    }

    /**
     * Get and remove the pending bed for this player, if any.
     * Server-only. Returns empty if none or on client.
     */
    public static Optional<PendingBedSelection> getAndClearPendingBed(Player player) {
        if (player.level().isClientSide()) return Optional.empty();
        return Optional.ofNullable(PENDING_BEDS.remove(player.getUUID()));
    }

    /**
     * Clear pending bed for this player (e.g. on logout).
     * Safe to call from server logout event; use {@link #clearForPlayer(UUID)} there to avoid touching player state.
     */
    public static void clearForPlayer(Player player) {
        PENDING_BEDS.remove(player.getUUID());
    }

    /** Clear by player UUID (e.g. from PlayerLoggedOutEvent on server). */
    public static void clearForPlayer(UUID playerId) {
        PENDING_BEDS.remove(playerId);
    }

    public record PendingBedSelection(BlockPos pos, String dimension) {}
}
