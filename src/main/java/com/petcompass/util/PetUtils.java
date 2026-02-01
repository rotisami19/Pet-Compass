package com.petcompass.util;

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
