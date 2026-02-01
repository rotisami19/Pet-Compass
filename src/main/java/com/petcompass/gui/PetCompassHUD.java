package com.petcompass.gui;

import com.petcompass.PetCompass;
import com.petcompass.PetCompassClientData;
import com.petcompass.PetCompassItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * HUD overlay that shows pet tracking information when holding the compass.
 */
public class PetCompassHUD implements LayeredDraw.Layer {

    // Distance threshold to show "nearby" indicator
    private static final int NEARBY_DISTANCE = 32;
    private static final int VERY_CLOSE_DISTANCE = 10;
    
    // For blinking animation
    private long lastBlinkTime = 0;
    private boolean blinkState = true;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player == null) return;
        
        // Check if player is holding the compass
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        
        boolean holdingCompass = mainHand.getItem() instanceof PetCompassItem || 
                                  offHand.getItem() instanceof PetCompassItem;
        
        if (!holdingCompass || !PetCompassClientData.isTracking) {
            return;
        }
        
        // Calculate live distance
        double dx = PetCompassClientData.targetX - player.getX();
        double dy = PetCompassClientData.targetY - player.getY();
        double dz = PetCompassClientData.targetZ - player.getZ();
        int distance = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // Update blink animation
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBlinkTime > 500) {
            blinkState = !blinkState;
            lastBlinkTime = currentTime;
        }
        
        // Render HUD overlay in top-left corner
        int x = 10;
        int y = 10;
        int bgColor = 0x80000000; // Semi-transparent black
        int textColor = 0xFFFFFFFF;
        int accentColor = 0xFF55FF55; // Green
        int nearbyColor = 0xFFFFFF55; // Yellow
        int veryCloseColor = 0xFF55FFFF; // Cyan
        
        // Determine if pet is nearby
        boolean isNearby = distance <= NEARBY_DISTANCE;
        boolean isVeryClose = distance <= VERY_CLOSE_DISTANCE;
        
        // Background panel
        int panelWidth = 150;
        int panelHeight = isNearby ? 75 : 72;
        
        // Special background color when very close
        if (isVeryClose && blinkState) {
            bgColor = 0x8000AA00; // Semi-transparent green
        } else if (isNearby) {
            bgColor = 0x80333300; // Semi-transparent dark yellow
        }
        
        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, bgColor);
        
        // Title with nearby indicator
        if (isVeryClose) {
            // Blinking "PET FOUND!" when very close
            if (blinkState) {
                guiGraphics.drawString(mc.font, "§b§l✦ PET FOUND! ✦", x + 5, y + 5, veryCloseColor);
            } else {
                guiGraphics.drawString(mc.font, Component.translatable("hud.petcompass.tracking"), x + 5, y + 5, accentColor);
            }
        } else if (isNearby) {
            guiGraphics.drawString(mc.font, "§e§l◆ NEARBY!", x + 5, y + 5, nearbyColor);
        } else {
            guiGraphics.drawString(mc.font, Component.translatable("hud.petcompass.tracking"), x + 5, y + 5, accentColor);
        }
        
        // Pet name
        guiGraphics.drawString(mc.font, PetCompassClientData.petName, x + 5, y + 18, textColor);
        
        // Distance with color based on proximity
        String distanceText = distance + " blocks";
        int distanceColor = textColor;
        if (isVeryClose) {
            distanceColor = veryCloseColor;
        } else if (isNearby) {
            distanceColor = nearbyColor;
        }
        guiGraphics.drawString(mc.font, Component.translatable("hud.petcompass.distance", distanceText), x + 5, y + 30, distanceColor);
        
        // Dimension
        String dimension = PetCompassClientData.dimension;
        String dimensionName = PetCompassItem.getDimensionDisplayName(dimension);
        guiGraphics.drawString(mc.font, Component.translatable("hud.petcompass.dimension", dimensionName), x + 5, y + 42, 0xAAAAFF);
        
        // Coordinates
        String coords = String.format("X:%d Y:%d Z:%d", 
            PetCompassClientData.targetX, 
            PetCompassClientData.targetY, 
            PetCompassClientData.targetZ);
        guiGraphics.drawString(mc.font, coords, x + 5, y + 54, 0xAAAAAA);
        
        // Draw paw icon when nearby
        if (isNearby) {
            drawPawIcon(guiGraphics, x + panelWidth - 20, y + 5, isVeryClose, blinkState);
        }
    }
    
    /**
     * Draw a simple paw icon using Unicode characters
     */
    private void drawPawIcon(GuiGraphics guiGraphics, int x, int y, boolean isVeryClose, boolean blink) {
        Minecraft mc = Minecraft.getInstance();
        int color;
        
        if (isVeryClose) {
            color = blink ? 0xFF55FFFF : 0xFF00AAAA; // Blinking cyan
        } else {
            color = 0xFFFFFF55; // Yellow
        }
        
        // Draw paw using Unicode (🐾)
        guiGraphics.drawString(mc.font, "🐾", x, y, color);
    }
}
