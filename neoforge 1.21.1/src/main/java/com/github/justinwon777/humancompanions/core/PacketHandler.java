package com.github.justinwon777.humancompanions.core;

import com.github.justinwon777.humancompanions.networking.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Client → Server
        registrar.playToServer(
                SetAlertPacket.TYPE,
                SetAlertPacket.STREAM_CODEC,
                SetAlertPacket::handle);

        registrar.playToServer(
                SetHuntingPacket.TYPE,
                SetHuntingPacket.STREAM_CODEC,
                SetHuntingPacket::handle);

        registrar.playToServer(
                SetPatrolingPacket.TYPE,
                SetPatrolingPacket.STREAM_CODEC,
                SetPatrolingPacket::handle);

        registrar.playToServer(
                ClearTargetPacket.TYPE,
                ClearTargetPacket.STREAM_CODEC,
                ClearTargetPacket::handle);

        registrar.playToServer(
                SetStationeryPacket.TYPE,
                SetStationeryPacket.STREAM_CODEC,
                SetStationeryPacket::handle);

        registrar.playToServer(
                ReleasePacket.TYPE,
                ReleasePacket.STREAM_CODEC,
                ReleasePacket::handle);
    }
}
