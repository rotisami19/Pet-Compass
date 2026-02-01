package com.petcompass.util;

import com.petcompass.PetCompass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Scans region files to find tamed pets in unloaded chunks.
 * This allows finding pets even when they are far away and not loaded in memory.
 */
public class RegionScanner {

    // Cache of scanned pets per player UUID
    private static final Map<UUID, List<ScannedPetInfo>> PLAYER_PET_CACHE = new ConcurrentHashMap<>();
    
    // Track if a scan is in progress for a player
    private static final Set<UUID> SCANNING_PLAYERS = ConcurrentHashMap.newKeySet();

    /**
     * Info about a pet found in region files.
     */
    public record ScannedPetInfo(
        UUID petUUID,
        String petName,
        String petType,
        double x, double y, double z,
        String dimension
    ) {}

    /**
     * Starts an async scan of all region files for the given player's pets.
     */
    public static CompletableFuture<List<ScannedPetInfo>> scanForPlayerPetsAsync(ServerLevel level, UUID playerUUID) {
        if (SCANNING_PLAYERS.contains(playerUUID)) {
            // Already scanning, return cached or empty
            return CompletableFuture.completedFuture(PLAYER_PET_CACHE.getOrDefault(playerUUID, new ArrayList<>()));
        }
        
        SCANNING_PLAYERS.add(playerUUID);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ScannedPetInfo> pets = scanAllDimensions(level, playerUUID);
                PLAYER_PET_CACHE.put(playerUUID, pets);
                PetCompass.LOGGER.info("Found {} pets for player {} in region files", pets.size(), playerUUID);
                return pets;
            } catch (Exception e) {
                PetCompass.LOGGER.error("Error scanning region files for pets", e);
                return new ArrayList<>();
            } finally {
                SCANNING_PLAYERS.remove(playerUUID);
            }
        });
    }

    /**
     * Gets cached pets for a player (from previous scan).
     */
    public static List<ScannedPetInfo> getCachedPets(UUID playerUUID) {
        return PLAYER_PET_CACHE.getOrDefault(playerUUID, new ArrayList<>());
    }

    /**
     * Clears the cache for a player (call on logout).
     */
    public static void clearCache(UUID playerUUID) {
        PLAYER_PET_CACHE.remove(playerUUID);
    }

    /**
     * Scans all dimensions for pets.
     */
    private static List<ScannedPetInfo> scanAllDimensions(ServerLevel level, UUID playerUUID) {
        List<ScannedPetInfo> allPets = new ArrayList<>();
        
        // Get the server and iterate all levels
        var server = level.getServer();
        for (ServerLevel dimension : server.getAllLevels()) {
            String dimName = dimension.dimension().location().toString();
            List<ScannedPetInfo> dimPets = scanDimension(dimension, playerUUID, dimName);
            allPets.addAll(dimPets);
        }
        
        return allPets;
    }

    /**
     * Scans a single dimension's entity region files.
     */
    private static List<ScannedPetInfo> scanDimension(ServerLevel level, UUID playerUUID, String dimension) {
        List<ScannedPetInfo> pets = new ArrayList<>();
        
        Path worldFolder = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        Path dimensionFolder = getDimensionFolder(worldFolder, level);
        Path entitiesFolder = dimensionFolder.resolve("entities");
        
        if (!Files.exists(entitiesFolder)) {
            // Older format or no entities saved yet
            return pets;
        }
        
        try (Stream<Path> regionFiles = Files.list(entitiesFolder)) {
            regionFiles
                .filter(p -> p.toString().endsWith(".mca"))
                .forEach(regionPath -> {
                    try {
                        List<ScannedPetInfo> regionPets = scanRegionFile(regionPath, playerUUID, dimension);
                        pets.addAll(regionPets);
                    } catch (Exception e) {
                        // Log but continue with other files
                        PetCompass.LOGGER.debug("Error reading region file {}: {}", regionPath, e.getMessage());
                    }
                });
        } catch (IOException e) {
            PetCompass.LOGGER.error("Error listing entity region files", e);
        }
        
        return pets;
    }

    /**
     * Gets the correct folder for a dimension.
     */
    private static Path getDimensionFolder(Path worldFolder, ServerLevel level) {
        var dimKey = level.dimension();
        
        if (dimKey.equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return worldFolder;
        } else if (dimKey.equals(net.minecraft.world.level.Level.NETHER)) {
            return worldFolder.resolve("DIM-1");
        } else if (dimKey.equals(net.minecraft.world.level.Level.END)) {
            return worldFolder.resolve("DIM1");
        } else {
            // Custom dimension
            return worldFolder.resolve("dimensions")
                .resolve(dimKey.location().getNamespace())
                .resolve(dimKey.location().getPath());
        }
    }

    /**
     * Scans a single region file for tamed pets using raw file I/O.
     */
    private static List<ScannedPetInfo> scanRegionFile(Path regionPath, UUID playerUUID, String dimension) throws IOException {
        List<ScannedPetInfo> pets = new ArrayList<>();
        
        try (RandomAccessFile raf = new RandomAccessFile(regionPath.toFile(), "r")) {
            // Region files have a 8KB header (4KB locations + 4KB timestamps)
            if (raf.length() < 8192) {
                return pets;
            }
            
            // Read chunk locations from header
            byte[] header = new byte[4096];
            raf.readFully(header);
            
            for (int i = 0; i < 1024; i++) {
                int offset = ((header[i * 4] & 0xFF) << 16) | 
                            ((header[i * 4 + 1] & 0xFF) << 8) | 
                            (header[i * 4 + 2] & 0xFF);
                int sectorCount = header[i * 4 + 3] & 0xFF;
                
                if (offset == 0 || sectorCount == 0) {
                    continue;
                }
                
                try {
                    // Seek to chunk data
                    raf.seek(offset * 4096L);
                    
                    int length = raf.readInt();
                    byte compressionType = raf.readByte();
                    
                    if (length <= 0 || length > sectorCount * 4096) {
                        continue;
                    }
                    
                    byte[] data = new byte[length - 1];
                    raf.readFully(data);
                    
                    // Decompress based on compression type
                    InputStream is = new ByteArrayInputStream(data);
                    if (compressionType == 1) {
                        is = new GZIPInputStream(is);
                    } else if (compressionType == 2) {
                        is = new InflaterInputStream(is);
                    }
                    
                    CompoundTag chunkNBT = NbtIo.read(new DataInputStream(is));
                    if (chunkNBT == null) continue;
                    
                    // Entity data is in "Entities" list
                    if (chunkNBT.contains("Entities", 9)) { // 9 = ListTag
                        ListTag entities = chunkNBT.getList("Entities", 10); // 10 = CompoundTag
                        
                        for (int j = 0; j < entities.size(); j++) {
                            CompoundTag entityNBT = entities.getCompound(j);
                            ScannedPetInfo pet = parsePetFromNBT(entityNBT, playerUUID, dimension);
                            if (pet != null) {
                                pets.add(pet);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip problematic chunks
                }
            }
        }
        
        return pets;
    }

    /**
     * Parses a pet from NBT data if it belongs to the player.
     */
    private static ScannedPetInfo parsePetFromNBT(CompoundTag nbt, UUID playerUUID, String dimension) {
        // Get entity type first to filter out non-pet entities
        String entityType = nbt.getString("id");
        if (!isTameableEntity(entityType)) {
            return null;
        }
        
        // Check if this is a tameable entity with an owner
        if (!nbt.contains("Owner") && !nbt.contains("OwnerUUID")) {
            return null;
        }
        
        UUID ownerUUID = null;
        
        // Try different owner formats
        if (nbt.contains("Owner")) {
            // Older format or some mods use "Owner" as UUID array
            if (nbt.contains("Owner", 11)) { // IntArray
                int[] arr = nbt.getIntArray("Owner");
                if (arr.length == 4) {
                    ownerUUID = new UUID(
                        (long) arr[0] << 32 | (arr[1] & 0xFFFFFFFFL),
                        (long) arr[2] << 32 | (arr[3] & 0xFFFFFFFFL)
                    );
                }
            }
        }
        
        if (ownerUUID == null && nbt.contains("OwnerUUID")) {
            // String format
            try {
                ownerUUID = UUID.fromString(nbt.getString("OwnerUUID"));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        // Also check for UUID format used in 1.16+
        if (ownerUUID == null && nbt.hasUUID("Owner")) {
            ownerUUID = nbt.getUUID("Owner");
        }
        
        if (ownerUUID == null || !ownerUUID.equals(playerUUID)) {
            return null;
        }
        
        // Get entity UUID
        UUID petUUID;
        if (nbt.hasUUID("UUID")) {
            petUUID = nbt.getUUID("UUID");
        } else {
            return null; // No UUID, can't track
        }
        
        // Get position
        ListTag pos = nbt.getList("Pos", 6); // 6 = DoubleTag
        if (pos.size() < 3) return null;
        
        double x = pos.getDouble(0);
        double y = pos.getDouble(1);
        double z = pos.getDouble(2);
        
        // Format entity type for display (entityType already defined at start of method)
        String petType = formatEntityType(entityType);
        
        // Get custom name if present
        String petName = petType;
        if (nbt.contains("CustomName")) {
            String customNameJson = nbt.getString("CustomName");
            petName = parseCustomName(customNameJson);
        }
        
        return new ScannedPetInfo(petUUID, petName, petType, x, y, z, dimension);
    }

    /**
     * Formats an entity type ID to a readable name.
     */
    private static String formatEntityType(String entityId) {
        // "minecraft:wolf" -> "Wolf"
        if (entityId.contains(":")) {
            entityId = entityId.substring(entityId.indexOf(':') + 1);
        }
        // "wolf" -> "Wolf"
        return entityId.substring(0, 1).toUpperCase() + entityId.substring(1).replace('_', ' ');
    }

    /**
     * Parses a custom name from JSON format.
     */
    private static String parseCustomName(String json) {
        // Simple parsing for {"text":"Name"} format
        if (json.contains("\"text\":\"")) {
            int start = json.indexOf("\"text\":\"") + 8;
            int end = json.indexOf("\"", start);
            if (end > start) {
                return json.substring(start, end);
            }
        }
        // Fallback: return as-is without quotes
        return json.replace("\"", "").replace("{", "").replace("}", "");
    }

    /**
     * Checks if an entity type is a tameable/rideable pet.
     * This filters out projectiles, items, and other non-pet entities that may have an "Owner" tag.
     */
    private static boolean isTameableEntity(String entityType) {
        if (entityType == null || entityType.isEmpty()) {
            return false;
        }
        
        // Normalize entity type
        String type = entityType.toLowerCase();
        if (type.contains(":")) {
            type = type.substring(type.indexOf(':') + 1);
        }
        
        // List of known tameable/rideable entities
        Set<String> tameableEntities = Set.of(
            // Vanilla pets
            "wolf", "cat", "parrot", "horse", "donkey", "mule", 
            "llama", "trader_llama", "camel", "allay",
            // Rideable animals
            "pig", "strider",
            // Common modded pets (Alex's Mobs, etc.)
            "beehemoth", "grizzly_bear", "elephant", "hammerhead_shark",
            "crocodile", "gorilla", "crow", "raccoon", "orca",
            // Ice and Fire
            "fire_dragon", "ice_dragon", "lightning_dragon", "hippogryph", "amphithere",
            // Other common mods
            "tamed_dragon", "pet", "mount", "companion"
        );
        
        // Check if it's in the list or contains common pet keywords
        if (tameableEntities.contains(type)) {
            return true;
        }
        
        // Check for common patterns in modded entities
        return type.contains("tamed") || 
               type.contains("pet") || 
               type.contains("mount") ||
               type.contains("dragon") ||
               type.contains("wolf") ||
               type.contains("cat") ||
               type.contains("horse") ||
               type.contains("dog");
    }
}


