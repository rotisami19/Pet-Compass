package com.petcompass.event;

import com.petcompass.PetCompass;
import com.petcompass.network.SyncScannedPetsPacket;
import com.petcompass.util.RegionScanner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles scanning for pets when a player logs in.
 * This triggers a background scan of region files to find pets in unloaded chunks.
 */
@EventBusSubscriber(modid = PetCompass.MODID)
public class PlayerLoginHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ServerLevel level = player.serverLevel();
        
        PetCompass.LOGGER.info("Player {} logged in, starting pet scan...", player.getName().getString());
        
        // Start async scan of region files
        RegionScanner.scanForPlayerPetsAsync(level, player.getUUID())
            .thenAccept(pets -> {
                if (!pets.isEmpty()) {
                    PetCompass.LOGGER.info("Found {} pets in region files for {}", pets.size(), player.getName().getString());
                    
                    // Send the scanned pets to the client
                    // This needs to be done on the main thread
                    level.getServer().execute(() -> {
                        if (player.isAlive() && player.connection != null) {
                            PacketDistributor.sendToPlayer(player, new SyncScannedPetsPacket(pets));
                        }
                    });
                }
            });
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Clear cache on logout
            RegionScanner.clearCache(player.getUUID());
        }
    }
}
