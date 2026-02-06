package com.petcompass.gui;

import com.petcompass.PetCompass;
import com.petcompass.PetCompassClientData;
import com.petcompass.PetCompassConstants;
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
        if (currentTime - lastBlinkTime > PetCompassConstants.BLINK_INTERVAL_MS) {
            blinkState = !blinkState;
            lastBlinkTime = currentTime;
        }

        // Render HUD overlay in top-left corner
        int x = PetCompassConstants.HUD_X;
        int y = PetCompassConstants.HUD_Y;
        int bgColor = PetCompassConstants.COLOR_BG_BLACK;
        int textColor = PetCompassConstants.COLOR_WHITE_ALPHA;
        int accentColor = PetCompassConstants.COLOR_GREEN;
        int nearbyColor = PetCompassConstants.COLOR_YELLOW;
        int veryCloseColor = PetCompassConstants.COLOR_CYAN;
        
        // Determine if pet is nearby
        boolean isNearby = distance <= NEARBY_DISTANCE;
        boolean isVeryClose = distance <= VERY_CLOSE_DISTANCE;
        
        // Background panel
        int panelWidth = PetCompassConstants.HUD_PANEL_WIDTH;
        int panelHeight = isNearby ? PetCompassConstants.HUD_PANEL_HEIGHT_NEARBY :
            PetCompassConstants.HUD_PANEL_HEIGHT;

        // Special background color when very close
        if (isVeryClose && blinkState) {
            bgColor = PetCompassConstants.COLOR_BG_GREEN;
        } else if (isNearby) {
            bgColor = PetCompassConstants.COLOR_BG_YELLOW;
        }
        
        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, bgColor);

        // Title with nearby indicator
        int xPad = x + PetCompassConstants.HUD_PADDING;
        int yLine1 = y + PetCompassConstants.HUD_PADDING;
        int yLine2 = yLine1 + PetCompassConstants.HUD_LINE_HEIGHT + 6;
        int yLine3 = yLine2 + PetCompassConstants.HUD_LINE_HEIGHT;
        int yLine4 = yLine3 + PetCompassConstants.HUD_LINE_HEIGHT;
        int yLine5 = yLine4 + PetCompassConstants.HUD_LINE_HEIGHT;

        if (isVeryClose) {
            // Blinking "PET FOUND!" when very close
            if (blinkState) {
                guiGraphics.drawString(mc.font, "§b§l✦ PET FOUND! ✦", xPad, yLine1, veryCloseColor);
            } else {
                guiGraphics.drawString(mc.font, Component.translatable("hud.petcompass.tracking"), xPad, yLine1, accentColor);
            }
        } else if (isNearby) {
            guiGraphics.drawString(mc.font, "§e§l◆ NEARBY!", xPad, yLine1, nearbyColor);
        } else {
            guiGraphics.drawString(mc.font, Component.translatable("hud.petcompass.tracking"), xPad, yLine1, accentColor);
        }

        // Pet name
        guiGraphics.drawString(mc.font, PetCompassClientData.petName, xPad, yLine2, textColor);

        // Distance with color based on proximity
        String distanceText = distance + " blocks";
        int distanceColor = textColor;
        if (isVeryClose) {
            distanceColor = veryCloseColor;
        } else if (isNearby) {
            distanceColor = nearbyColor;
        }
        guiGraphics.drawString(mc.font, Component.translatable("hud.petcompass.distance", distanceText), xPad, yLine3, distanceColor);

        // Dimension
        String dimension = PetCompassClientData.dimension;
        String dimensionName = PetCompassItem.getDimensionDisplayName(dimension);
        guiGraphics.drawString(mc.font, Component.translatable("hud.petcompass.dimension", dimensionName),
            xPad, yLine4, PetCompassConstants.COLOR_LIGHT_PURPLE);

        // Coordinates
        String coords = String.format("X:%d Y:%d Z:%d",
            PetCompassClientData.targetX,
            PetCompassClientData.targetY,
            PetCompassClientData.targetZ);
        guiGraphics.drawString(mc.font, coords, xPad, yLine5, PetCompassConstants.COLOR_GRAY);

        // Draw paw icon when nearby
        if (isNearby) {
            drawPawIcon(guiGraphics, x + panelWidth - PetCompassConstants.HUD_PAW_OFFSET,
                y + PetCompassConstants.HUD_PADDING, isVeryClose, blinkState);
        }
    }
    
    /**
     * Draw a simple paw icon using Unicode characters
     */
    private void drawPawIcon(GuiGraphics guiGraphics, int x, int y, boolean isVeryClose, boolean blink) {
        Minecraft mc = Minecraft.getInstance();
        int color;

        if (isVeryClose) {
            color = blink ? PetCompassConstants.COLOR_CYAN : PetCompassConstants.COLOR_DARK_CYAN;
        } else {
            color = PetCompassConstants.COLOR_YELLOW;
        }

        // Draw paw using Unicode (🐾)
        guiGraphics.drawString(mc.font, "🐾", x, y, color);
    }
}
