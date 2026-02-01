package com.petcompass.network;

import com.petcompass.PetCompass;
import com.petcompass.PetCompassClientData;
import com.petcompass.util.RegionScanner.ScannedPetInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Packet to sync scanned pets (from region files) to the client.
 * This includes pets in unloaded chunks.
 */
public record SyncScannedPetsPacket(List<ScannedPetInfo> pets) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncScannedPetsPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PetCompass.MODID, "sync_scanned_pets"));

    public static final StreamCodec<FriendlyByteBuf, SyncScannedPetsPacket> STREAM_CODEC = 
        StreamCodec.of(SyncScannedPetsPacket::encode, SyncScannedPetsPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buf, SyncScannedPetsPacket pkt) {
        buf.writeInt(pkt.pets.size());
        for (ScannedPetInfo pet : pkt.pets) {
            buf.writeUUID(pet.petUUID());
            buf.writeUtf(pet.petName());
            buf.writeUtf(pet.petType());
            buf.writeDouble(pet.x());
            buf.writeDouble(pet.y());
            buf.writeDouble(pet.z());
            buf.writeUtf(pet.dimension());
        }
    }

    public static SyncScannedPetsPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ScannedPetInfo> pets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID uuid = buf.readUUID();
            String name = buf.readUtf();
            String type = buf.readUtf();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            String dimension = buf.readUtf();
            pets.add(new ScannedPetInfo(uuid, name, type, x, y, z, dimension));
        }
        return new SyncScannedPetsPacket(pets);
    }

    public static void handle(SyncScannedPetsPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Store scanned pets in client data
            PetCompassClientData.setScannedPets(pkt.pets);
            PetCompass.LOGGER.info("Received {} scanned pets from server", pkt.pets.size());
        });
    }
}
