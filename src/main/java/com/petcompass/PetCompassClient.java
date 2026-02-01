package com.petcompass;

import com.petcompass.gui.PetCompassHUD;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-side setup for Pet Compass.
 */
@EventBusSubscriber(modid = PetCompass.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PetCompassClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register compass angle property for the needle animation
            ItemProperties.register(PetCompass.PET_COMPASS.get(), 
                ResourceLocation.withDefaultNamespace("angle"),
                (stack, level, entity, seed) -> {
                    if (entity == null || level == null) {
                        return 0.0f;
                    }
                    
                    if (!PetCompassClientData.isTracking) {
                        // Stay stable at 0 (pointing up) when not tracking
                        return 0.0f;
                    }
                    
                    // Calculate angle to target
                    double dx = PetCompassClientData.targetX - entity.getX();
                    double dz = PetCompassClientData.targetZ - entity.getZ();
                    double angle = Math.atan2(dz, dx);
                    
                    // Convert to 0-1 range and account for player rotation
                    float playerRot = entity.getYRot();
                    double targetAngle = Math.toDegrees(angle) - 90; // Adjust for Minecraft coordinate system
                    double relativeAngle = targetAngle - playerRot;
                    
                    // Normalize to 0-1
                    float normalizedAngle = (float) ((relativeAngle % 360 + 360) % 360) / 360.0f;
                    return normalizedAngle;
                }
            );
        });
    }
    
    @SubscribeEvent
    public static void registerHUD(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, 
            ResourceLocation.fromNamespaceAndPath(PetCompass.MODID, "pet_compass_hud"),
            new PetCompassHUD()
        );
    }
}
