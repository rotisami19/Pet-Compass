package com.petcompass.network;

import com.petcompass.PetCompassClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet sent from server to client to update compass display data.
 */
public record UpdateCompassPacket(
    String petUUID,
    String petName,
    int targetX,
    int targetY,
    int targetZ,
    int distance,
    boolean found,
    String dimension
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateCompassPacket> TYPE = 
        new CustomPacketPayload.Type<>(PetCompassNetworking.UPDATE_COMPASS_ID);

    public static final StreamCodec<FriendlyByteBuf, UpdateCompassPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public UpdateCompassPacket decode(FriendlyByteBuf buf) {
                return new UpdateCompassPacket(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readUtf()
                );
            }

            @Override
            public void encode(FriendlyByteBuf buf, UpdateCompassPacket packet) {
                buf.writeUtf(packet.petUUID);
                buf.writeUtf(packet.petName);
                buf.writeInt(packet.targetX);
                buf.writeInt(packet.targetY);
                buf.writeInt(packet.targetZ);
                buf.writeInt(packet.distance);
                buf.writeBoolean(packet.found);
                buf.writeUtf(packet.dimension);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateCompassPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Update client-side data for HUD rendering
            PetCompassClientData.petUUID = packet.petUUID;
            PetCompassClientData.petName = packet.petName;
            PetCompassClientData.targetX = packet.targetX;
            PetCompassClientData.targetY = packet.targetY;
            PetCompassClientData.targetZ = packet.targetZ;
            PetCompassClientData.distance = packet.distance;
            PetCompassClientData.isTracking = packet.found;
            PetCompassClientData.dimension = packet.dimension;
        });
    }
}
