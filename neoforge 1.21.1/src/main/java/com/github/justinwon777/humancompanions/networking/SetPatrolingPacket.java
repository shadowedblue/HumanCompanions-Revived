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

public record SetPatrolingPacket(int entityId) implements CustomPacketPayload {

    public static final Type<SetPatrolingPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HumanCompanions.MOD_ID, "set_patroling"));

    public static final StreamCodec<FriendlyByteBuf, SetPatrolingPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, SetPatrolingPacket::entityId, SetPatrolingPacket::new);

    @Override
    public Type<SetPatrolingPacket> type() {
        return TYPE;
    }

    public static void handle(SetPatrolingPacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level() instanceof ServerLevel) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof AbstractHumanCompanionEntity companion) {
                    if (companion.isFollowing()) {
                        companion.setPatrolling(true);
                        companion.setFollowing(false);
                        companion.setGuarding(false);
                        companion.setPatrolPos(companion.blockPosition());
                    } else if (companion.isPatrolling()) {
                        companion.setPatrolling(false);
                        companion.setFollowing(false);
                        companion.setGuarding(true);
                        companion.setPatrolPos(companion.blockPosition());
                    } else {
                        companion.setPatrolling(false);
                        companion.setFollowing(true);
                        companion.setGuarding(false);
                    }
                }
            }
        });
    }
}
