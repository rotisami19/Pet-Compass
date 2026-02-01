package com.petcompass;

import com.petcompass.util.RegionScanner.ScannedPetInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side data storage for compass HUD display.
 */
public class PetCompassClientData {
    public static String petUUID = "";
    public static String petName = "";
    public static int targetX = 0;
    public static int targetY = 0;
    public static int targetZ = 0;
    public static int distance = 0;
    public static boolean isTracking = false;
    public static String dimension = "minecraft:overworld";
    
    // Scanned pets from region files (includes pets in unloaded chunks)
    private static List<ScannedPetInfo> scannedPets = new ArrayList<>();
    
    public static void reset() {
        petUUID = "";
        petName = "";
        targetX = 0;
        targetY = 0;
        targetZ = 0;
        distance = 0;
        isTracking = false;
        dimension = "minecraft:overworld";
    }
    
    public static void setScannedPets(List<ScannedPetInfo> pets) {
        scannedPets = new ArrayList<>(pets);
    }
    
    public static List<ScannedPetInfo> getScannedPets() {
        return scannedPets;
    }
    
    public static void clearScannedPets() {
        scannedPets.clear();
    }
}

