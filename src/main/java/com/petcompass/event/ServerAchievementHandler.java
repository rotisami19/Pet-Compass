package com.petcompass.event;

import com.petcompass.CompassState;
import com.petcompass.PetCompass;
import com.petcompass.PetCompassConstants;
import com.petcompass.PetCompassItem;
import com.petcompass.advancement.PetCompassTriggers;
import com.petcompass.util.PetUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

/**
 * Server-side event handler for achievement triggers.
 * Checks if player has reached their tracked pet and awards the achievement.
 */
@EventBusSubscriber(modid = PetCompass.MODID)
public class ServerAchievementHandler {

    private static final int PROXIMITY_THRESHOLD = PetCompassConstants.PROXIMITY_THRESHOLD;
    private static final int CHECK_INTERVAL = PetCompassConstants.ACHIEVEMENT_CHECK_INTERVAL;
    private static final ResourceLocation FIND_LOST_PET_ADVANCEMENT = 
        ResourceLocation.fromNamespaceAndPath(PetCompass.MODID, "find_lost_pet");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Only check every second to reduce performance impact
        if (player.tickCount % CHECK_INTERVAL != 0) return;

        // Skip if player already has the achievement (performance optimization)
        var advancementHolder = player.server.getAdvancements().get(FIND_LOST_PET_ADVANCEMENT);
        if (advancementHolder != null && 
            player.getAdvancements().getOrStartProgress(advancementHolder).isDone()) {
            return;
        }

        // Check if player is holding the compass
        ItemStack compass = getHeldCompass(player);
        if (compass == null) return;

        // Check if compass has an active target
        if (PetCompassItem.getState(compass) != CompassState.FOUND) return;

        // Get the initial distance (when pet was selected)
        int initialDistance = PetCompassItem.getInitialDistance(compass);
        if (initialDistance < PetCompassConstants.MINIMUM_DISTANCE_THRESHOLD) return; // Pet wasn't far enough to count

        // Get the targeted pet UUID
        String petUuidString = PetCompassItem.getPetUUID(compass);
        if (petUuidString == null || petUuidString.isEmpty()) return;

        try {
            UUID petUUID = UUID.fromString(petUuidString);

            // Find the pet
            Entity pet = PetUtils.findPetByUUID(player.level(), player, petUUID,
                PetCompassConstants.ACHIEVEMENT_SEARCH_RADIUS);
            if (pet == null) return;

            // Check if player is close enough to the pet
            double currentDistance = player.distanceTo(pet);
            if (currentDistance <= PROXIMITY_THRESHOLD) {
                // Player found their lost pet! Award the achievement
                PetCompassTriggers.FIND_LOST_PET.get().trigger(player, initialDistance);
                
                // Reset initial distance to prevent re-triggering
                PetCompassItem.setInitialDistance(compass, 0);
            }
        } catch (IllegalArgumentException e) {
            // Invalid UUID, ignore
        }
    }

    private static ItemStack getHeldCompass(ServerPlayer player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.getItem() == PetCompass.PET_COMPASS.get()) {
            return mainHand;
        }
        
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offHand.getItem() == PetCompass.PET_COMPASS.get()) {
            return offHand;
        }
        
        return null;
    }
}
