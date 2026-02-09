package com.petcompass.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for finding and working with tamed pets.
 */
public class PetUtils {

    /**
     * Get all tamed entities owned by a player within the loaded chunks.
     * @param level The world
     * @param player The player owner
     * @param searchRadius The radius to search in blocks
     * @return List of tamed entities belonging to this player
     */
    public static List<TamedPetInfo> getTamedPets(Level level, Player player, int searchRadius) {
        List<TamedPetInfo> pets = new ArrayList<>();
        UUID playerUUID = player.getUUID();
        
        AABB searchBox = new AABB(
            player.getX() - searchRadius, player.getY() - 256, player.getZ() - searchRadius,
            player.getX() + searchRadius, player.getY() + 256, player.getZ() + searchRadius
        );
        
        List<Entity> entities = level.getEntities(player, searchBox, entity -> {
            // Check if it's a tameable animal that belongs to this player
            if (entity instanceof TamableAnimal tamable) {
                return tamable.isTame() && playerUUID.equals(tamable.getOwnerUUID());
            }
            // Check for horses and similar
            if (entity instanceof AbstractHorse horse) {
                return horse.isTamed() && playerUUID.equals(horse.getOwnerUUID());
            }
            // Check for other ownable entities
            if (entity instanceof OwnableEntity ownable) {
                return playerUUID.equals(ownable.getOwnerUUID());
            }
            return false;
        });
        
        for (Entity entity : entities) {
            String name = entity.hasCustomName() ? entity.getCustomName().getString() : entity.getType().getDescription().getString();
            pets.add(new TamedPetInfo(
                entity.getUUID(),
                name,
                entity.getType().getDescriptionId(),
                (int) entity.getX(),
                (int) entity.getY(),
                (int) entity.getZ(),
                entity.level().dimension().location().toString()
            ));
        }
        
        return pets;
    }
    
    /**
     * Get all tamed entities owned by a player across ALL dimensions.
     * This searches Overworld, Nether, End, and any modded dimensions.
     * @param server The Minecraft server
     * @param player The player owner
     * @return List of tamed entities belonging to this player across all dimensions
     */
    public static List<TamedPetInfo> getAllTamedPetsAcrossDimensions(MinecraftServer server, ServerPlayer player) {
        List<TamedPetInfo> allPets = new ArrayList<>();
        UUID playerUUID = player.getUUID();
        
        // Iterate through all loaded dimensions
        for (ServerLevel level : server.getAllLevels()) {
            // Get all entities in this dimension that belong to the player
            for (Entity entity : level.getAllEntities()) {
                if (isOwnedByPlayer(entity, playerUUID)) {
                    String name = entity.hasCustomName() ? entity.getCustomName().getString() : entity.getType().getDescription().getString();
                    allPets.add(new TamedPetInfo(
                        entity.getUUID(),
                        name,
                        entity.getType().getDescriptionId(),
                        (int) entity.getX(),
                        (int) entity.getY(),
                        (int) entity.getZ(),
                        level.dimension().location().toString()
                    ));
                }
            }
        }
        
        return allPets;
    }
    
    /**
     * Check if an entity is owned by the specified player.
     */
    private static boolean isOwnedByPlayer(Entity entity, UUID playerUUID) {
        // Check if it's a tameable animal that belongs to this player
        if (entity instanceof TamableAnimal tamable) {
            return tamable.isTame() && playerUUID.equals(tamable.getOwnerUUID());
        }
        // Check for horses and similar
        if (entity instanceof AbstractHorse horse) {
            return horse.isTamed() && playerUUID.equals(horse.getOwnerUUID());
        }
        // Check for other ownable entities
        if (entity instanceof OwnableEntity ownable) {
            return playerUUID.equals(ownable.getOwnerUUID());
        }
        return false;
    }
    
    /**
     * Find a specific pet by UUID.
     */
    public static Entity findPetByUUID(Level level, Player player, UUID petUUID, int searchRadius) {
        AABB searchBox = new AABB(
            player.getX() - searchRadius, player.getY() - 256, player.getZ() - searchRadius,
            player.getX() + searchRadius, player.getY() + 256, player.getZ() + searchRadius
        );
        
        List<Entity> entities = level.getEntities(player, searchBox, entity -> entity.getUUID().equals(petUUID));
        return entities.isEmpty() ? null : entities.get(0);
    }
    
    /**
     * Data class to hold information about a tamed pet.
     */
    public static record TamedPetInfo(
        UUID uuid,
        String name,
        String entityType,
        int x,
        int y,
        int z,
        String dimension
    ) {}
}
