package com.petcompass.network;

import com.petcompass.PetCompass;
import com.petcompass.PetCompassConstants;
import com.petcompass.util.PetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Packet sent from client to server to request the list of tamed pets.
 */
public record RequestPetsPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestPetsPacket> TYPE = 
        new CustomPacketPayload.Type<>(PetCompassNetworking.REQUEST_PETS_ID);

    public static final StreamCodec<FriendlyByteBuf, RequestPetsPacket> STREAM_CODEC = 
        StreamCodec.unit(new RequestPetsPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestPetsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Search for pets in a configurable block radius
                List<PetUtils.TamedPetInfo> pets = PetUtils.getTamedPets(
                    serverPlayer.level(),
                    serverPlayer,
                    PetCompassConstants.DEFAULT_SEARCH_RADIUS
                );

                // Send the list back to the client
                PacketDistributor.sendToPlayer(serverPlayer, new SyncPetsPacket(pets));
            }
        });
    }
}
