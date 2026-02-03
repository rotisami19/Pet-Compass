package com.petcompass.network;

import com.petcompass.PetCompass;
import com.petcompass.PetCompassClientData;
import com.petcompass.util.RegionScanner.ScannedPetInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet to sync scanned pets (from region files) to the client.
 * This includes pets in unloaded chunks.
 */
public class SyncScannedPetsPacket {

    private final List<ScannedPetInfo> pets;

    public SyncScannedPetsPacket(List<ScannedPetInfo> pets) {
        this.pets = pets;
    }

    public static void encode(SyncScannedPetsPacket pkt, FriendlyByteBuf buf) {
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

    public static void handle(SyncScannedPetsPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Store scanned pets in client data
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PetCompassClientData.setScannedPets(pkt.pets);
                PetCompass.LOGGER.info("Received {} scanned pets from server", pkt.pets.size());
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
