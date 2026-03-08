package com.github.justinwon777.humancompanions.networking;

import com.github.justinwon777.humancompanions.HumanCompanions;
import com.github.justinwon777.humancompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetHuntingPacket(int entityId) implements CustomPacketPayload {

    public static final Type<SetHuntingPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HumanCompanions.MOD_ID, "set_hunting"));

    public static final StreamCodec<FriendlyByteBuf, SetHuntingPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, SetHuntingPacket::entityId, SetHuntingPacket::new);

    @Override
    public Type<SetHuntingPacket> type() {
        return TYPE;
    }

    public static void handle(SetHuntingPacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level() instanceof ServerLevel) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof AbstractHumanCompanionEntity companion) {
                    companion.setHunting(!companion.isHunting());
                    if (companion.isHunting()) {
                        companion.addHuntingGoals();
                    } else {
                        companion.removeHuntingGoals();
                    }
                }
            }
        });
    }
}
