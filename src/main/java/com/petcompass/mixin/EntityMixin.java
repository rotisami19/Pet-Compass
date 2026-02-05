package com.petcompass.mixin;

import com.petcompass.CompassState;
import com.petcompass.PetCompass;
import com.petcompass.PetCompassItem;
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
 * Mixin to make the tracked pet glow when the local player holds the Pet Compass.
 * Client-only: only the player holding the compass sees the outline; other players do not.
 * Intercepts isCurrentlyGlowing during rendering.
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
        
        // Check main hand and off hand for Pet Compass
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        ItemStack compassStack = null;
        if (!mainHand.isEmpty() && mainHand.getItem() == PetCompass.PET_COMPASS.get()) {
            compassStack = mainHand;
        } else if (!offHand.isEmpty() && offHand.getItem() == PetCompass.PET_COMPASS.get()) {
            compassStack = offHand;
        }
        if (compassStack == null) return;
        
        if (PetCompassItem.getState(compassStack) != CompassState.FOUND) return;
        
        String petUuidString = PetCompassItem.getPetUUID(compassStack);
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

