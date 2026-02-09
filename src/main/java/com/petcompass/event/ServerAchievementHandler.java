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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Server-side event handler for achievement triggers.
 * Checks if player has reached their tracked pet and awards the achievement.
 */
@Mod.EventBusSubscriber(modid = PetCompass.MODID)
public class ServerAchievementHandler {

    private static final ResourceLocation FIND_LOST_PET_ADVANCEMENT =
        new ResourceLocation(PetCompass.MODID, "find_lost_pet");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Run only at end of tick and on server side
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) return;
        
        if (!(event.player instanceof ServerPlayer player)) return;

        // Only check every second to reduce performance impact
        if (player.tickCount % PetCompassConstants.ACHIEVEMENT_CHECK_INTERVAL != 0) return;

        // Skip if player already has the achievement (performance optimization)
        var advancementHolder = player.server.getAdvancements().getAdvancement(FIND_LOST_PET_ADVANCEMENT);
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
        PetCompass.LOGGER.debug("[Achievement] Checking... initialDistance={}, threshold={}", initialDistance, PetCompassConstants.MINIMUM_DISTANCE_THRESHOLD);
        if (initialDistance < PetCompassConstants.MINIMUM_DISTANCE_THRESHOLD) return; // Pet wasn't far enough to count
        
        // IMPORTANT: Check if the pet is in the same dimension as the player
        String petDimension = PetCompassItem.getDimension(compass);
        String playerDimension = player.level().dimension().location().toString();
        PetCompass.LOGGER.debug("[Achievement] petDimension={}, playerDimension={}", petDimension, playerDimension);
        if (!playerDimension.equals(petDimension)) {
            return;
        }

        // Get the targeted pet UUID
        String petUuidString = PetCompassItem.getPetUUID(compass);
        if (petUuidString == null || petUuidString.isEmpty()) return;

        try {
            UUID petUUID = UUID.fromString(petUuidString);

            // Find the pet
            Entity pet = PetUtils.findPetByUUID(player.level(), player, petUUID, PetCompassConstants.ACHIEVEMENT_SEARCH_RADIUS);
            PetCompass.LOGGER.debug("[Achievement] Pet found? {}", pet != null);
            if (pet == null) return;

            // Check if player is close enough to the pet
            double currentDistance = player.distanceTo(pet);
            PetCompass.LOGGER.debug("[Achievement] currentDistance={}, proximityThreshold={}", currentDistance, PetCompassConstants.PROXIMITY_THRESHOLD);
            if (currentDistance <= PetCompassConstants.PROXIMITY_THRESHOLD) {
                // Player found their lost pet! Award the achievement
                PetCompass.LOGGER.info("[Achievement] TRIGGERING find_lost_pet! initialDistance={}", initialDistance);
                PetCompassTriggers.FIND_LOST_PET.trigger(player, initialDistance);
                
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
