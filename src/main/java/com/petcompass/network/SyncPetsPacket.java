package com.petcompass.network;

import com.petcompass.PetCompassClientData;
import com.petcompass.gui.PetCompassScreen;
import com.petcompass.util.PetUtils;
import com.petcompass.util.RegionScanner.ScannedPetInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Packet sent from server to client with the list of tamed pets.
 */
public record SyncPetsPacket(List<PetUtils.TamedPetInfo> pets) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncPetsPacket> TYPE = 
        new CustomPacketPayload.Type<>(PetCompassNetworking.SYNC_PETS_ID);

    public static final StreamCodec<FriendlyByteBuf, SyncPetsPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public SyncPetsPacket decode(FriendlyByteBuf buf) {
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

            @Override
            public void encode(FriendlyByteBuf buf, SyncPetsPacket packet) {
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
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPetsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Open the GUI on the client with the received pet list + scanned pets
            Minecraft.getInstance().execute(() -> {
                List<PetUtils.TamedPetInfo> mergedPets = mergePetsWithScanned(packet.pets);
                Minecraft.getInstance().setScreen(new PetCompassScreen(mergedPets));
            });
        });
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

