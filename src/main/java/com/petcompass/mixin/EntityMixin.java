package com.petcompass.mixin;

import com.petcompass.PetCompass;
import com.petcompass.PetCompassClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Mixin to make the tracked pet glow when player holds the Pet Compass.
 * This intercepts the isCurrentlyGlowing check during rendering.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void petcompass$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        
        // Only run on client side
        if (!self.level().isClientSide()) return;
        
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        
        // Check if player is holding the Pet Compass in MAIN hand only
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.isEmpty() || mainHand.getItem() != PetCompass.PET_COMPASS.get()) {
            return; // Not holding compass in main hand - no glow
        }
        
        // Use client-side data which is updated immediately on reset
        if (!PetCompassClientData.isTracking) return;
        
        // Get targeted pet UUID from client data
        String petUuidString = PetCompassClientData.petUUID;
        if (petUuidString == null || petUuidString.isEmpty()) return;
        
        try {
            UUID targetUUID = UUID.fromString(petUuidString);
            
            // If this entity is the targeted pet, make it glow!
            if (self.getUUID().equals(targetUUID)) {
                cir.setReturnValue(true);
            }
        } catch (IllegalArgumentException e) {
            // Invalid UUID, ignore
        }
    }
}

