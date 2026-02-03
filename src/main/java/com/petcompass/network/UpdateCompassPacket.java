package com.petcompass.network;

import com.petcompass.PetCompassClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to update compass display data.
 */
public class UpdateCompassPacket {

    private final String petUUID;
    private final String petName;
    private final int targetX;
    private final int targetY;
    private final int targetZ;
    private final int distance;
    private final boolean found;
    private final String dimension;

    public UpdateCompassPacket(String petUUID, String petName, int targetX, int targetY, int targetZ, int distance, boolean found, String dimension) {
        this.petUUID = petUUID;
        this.petName = petName;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.distance = distance;
        this.found = found;
        this.dimension = dimension;
    }

    public static void encode(UpdateCompassPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.petUUID);
        buf.writeUtf(packet.petName);
        buf.writeInt(packet.targetX);
        buf.writeInt(packet.targetY);
        buf.writeInt(packet.targetZ);
        buf.writeInt(packet.distance);
        buf.writeBoolean(packet.found);
        buf.writeUtf(packet.dimension);
    }

    public static UpdateCompassPacket decode(FriendlyByteBuf buf) {
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

    public static void handle(UpdateCompassPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client-side data for HUD rendering
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PetCompassClientData.petUUID = packet.petUUID;
                PetCompassClientData.petName = packet.petName;
                PetCompassClientData.targetX = packet.targetX;
                PetCompassClientData.targetY = packet.targetY;
                PetCompassClientData.targetZ = packet.targetZ;
                PetCompassClientData.distance = packet.distance;
                PetCompassClientData.isTracking = packet.found;
                PetCompassClientData.dimension = packet.dimension;
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
