package com.petcompass.network;

import com.petcompass.PetCompassClientData;
import com.petcompass.gui.PetCompassScreen;
import com.petcompass.util.PetUtils;
import com.petcompass.util.RegionScanner.ScannedPetInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from server to client with the list of tamed pets.
 */
public class SyncPetsPacket {

    private final List<PetUtils.TamedPetInfo> pets;

    public SyncPetsPacket(List<PetUtils.TamedPetInfo> pets) {
        this.pets = pets;
    }

    public static void encode(SyncPetsPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.pets.size());
        for (PetUtils.TamedPetInfo pet : packet.pets) {
            buf.writeUUID(pet.uuid());
            buf.writeUtf(pet.name());
            buf.writeUtf(pet.entityType());
            buf.writeInt(pet.x());
            buf.writeInt(pet.y());
            buf.writeInt(pet.z());
            buf.writeUtf(pet.dimension());
        }
    }

    public static SyncPetsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<PetUtils.TamedPetInfo> pets = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUUID();
            String name = buf.readUtf();
            String entityType = buf.readUtf();
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            String dimension = buf.readUtf();
            pets.add(new PetUtils.TamedPetInfo(uuid, name, entityType, x, y, z, dimension));
        }
        return new SyncPetsPacket(pets);
    }

    public static void handle(SyncPetsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Open the GUI on the client with the received pet list
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                List<PetUtils.TamedPetInfo> mergedPets = mergePetsWithScanned(packet.pets);
                Minecraft.getInstance().setScreen(new PetCompassScreen(mergedPets));
            });
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Merges the loaded pets (from server) with scanned pets (from region files).
     * Loaded pets take priority (more up-to-date positions).
     */
    private static List<PetUtils.TamedPetInfo> mergePetsWithScanned(List<PetUtils.TamedPetInfo> loadedPets) {
        List<PetUtils.TamedPetInfo> result = new ArrayList<>(loadedPets);
        Set<UUID> loadedUUIDs = new HashSet<>();
        
        for (PetUtils.TamedPetInfo pet : loadedPets) {
            loadedUUIDs.add(pet.uuid());
        }
        
        // Add scanned pets that aren't already in the loaded list
        for (ScannedPetInfo scanned : PetCompassClientData.getScannedPets()) {
            if (!loadedUUIDs.contains(scanned.petUUID())) {
                // Convert ScannedPetInfo to TamedPetInfo
                result.add(new PetUtils.TamedPetInfo(
                    scanned.petUUID(),
                    scanned.petName(),
                    scanned.petType(),
                    (int) scanned.x(),
                    (int) scanned.y(),
                    (int) scanned.z(),
                    scanned.dimension()
                ));
            }
        }
        
        return result;
    }
}
