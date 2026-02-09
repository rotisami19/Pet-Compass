package com.petcompass.network;

import com.petcompass.PetCompassClientData;
import com.petcompass.gui.PetCompassScreen;
import com.petcompass.util.PetUtils;
import com.petcompass.util.RegionScanner.ScannedPetInfo;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Client-side packet handler. This class is only loaded on the client side
 * to avoid referencing client-only classes (Minecraft, Screen, etc.) on the server.
 */
public class ClientPacketHandler {

    public static void handleSyncPets(List<PetUtils.TamedPetInfo> pets) {
        List<PetUtils.TamedPetInfo> mergedPets = mergePetsWithScanned(pets);
        Minecraft.getInstance().setScreen(new PetCompassScreen(mergedPets));
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
