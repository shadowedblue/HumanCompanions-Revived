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

public record SetAlertPacket(int entityId) implements CustomPacketPayload {

    public static final Type<SetAlertPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HumanCompanions.MOD_ID, "set_alert"));

    public static final StreamCodec<FriendlyByteBuf, SetAlertPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, SetAlertPacket::entityId, SetAlertPacket::new);

    @Override
    public Type<SetAlertPacket> type() {
        return TYPE;
    }

    public static void handle(SetAlertPacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level() instanceof ServerLevel) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof AbstractHumanCompanionEntity companion) {
                    companion.setAlert(!companion.isAlert());
                    if (companion.isAlert()) {
                        companion.addAlertGoals();
                    } else {
                        companion.removeAlertGoals();
                    }
                }
            }
        });
    }
}
