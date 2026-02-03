package com.petcompass;

import com.petcompass.gui.PetCompassHUD;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side setup for Pet Compass.
 */
@Mod.EventBusSubscriber(modid = PetCompass.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PetCompassClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register compass angle property for the needle animation
            ItemProperties.register(PetCompass.PET_COMPASS.get(), 
                new ResourceLocation("angle"),
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
    public static void registerHUD(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "pet_compass_hud", new PetCompassHUD());
    }
}
