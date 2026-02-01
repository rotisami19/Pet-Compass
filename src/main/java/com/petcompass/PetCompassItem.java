package com.petcompass;

import java.util.UUID;

import com.petcompass.gui.PetCompassScreen;
import com.petcompass.network.PetCompassNetworking;
import com.petcompass.network.RequestPetsPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PetCompassItem extends Item {

    public PetCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (player.isShiftKeyDown()) {
            // Shift + right click : reset compass
            clearTarget(stack);
            if (level.isClientSide) {
                // Also stop client-side tracking immediately (HUD/needle/outline)
                PetCompassClientData.reset();
            }
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("chat.petcompass.reset"), true);
            }
        } else {
            // Right click: open GUI
            if (level.isClientSide) {
                // Request pet list from server and open GUI
                PacketDistributor.sendToServer(new RequestPetsPacket());
            }
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompassState state = getState(stack);
        if (state == CompassState.FOUND) {
            String petName = getPetName(stack);
            int distance = getDistance(stack);
            String dimension = getDimension(stack);
            String dimensionName = getDimensionDisplayName(dimension);
            tooltipComponents.add(Component.translatable("tooltip.petcompass.tracking", petName));
            tooltipComponents.add(Component.translatable("tooltip.petcompass.distance", distance));
            tooltipComponents.add(Component.translatable("tooltip.petcompass.dimension", dimensionName));
        } else if (state == CompassState.SEARCHING) {
            tooltipComponents.add(Component.translatable("tooltip.petcompass.searching"));
        } else if (state == CompassState.NOT_FOUND) {
            tooltipComponents.add(Component.translatable("tooltip.petcompass.not_found"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.petcompass.inactive"));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getState(stack) == CompassState.FOUND;
    }

    // ========== State Management ==========
    
    public static CompassState getState(ItemStack stack) {
        if (stack.has(PetCompassComponents.COMPASS_STATE)) {
            return CompassState.fromId(stack.get(PetCompassComponents.COMPASS_STATE));
        }
        return CompassState.INACTIVE;
    }
    
    public static void setState(ItemStack stack, CompassState state) {
        stack.set(PetCompassComponents.COMPASS_STATE, state.getId());
    }
    
    public static void setTarget(ItemStack stack, UUID petUUID, String petName, int x, int y, int z, String dimension) {
        stack.set(PetCompassComponents.COMPASS_STATE, CompassState.FOUND.getId());
        stack.set(PetCompassComponents.PET_UUID, petUUID.toString());
        stack.set(PetCompassComponents.PET_NAME, petName);
        stack.set(PetCompassComponents.TARGET_X, x);
        stack.set(PetCompassComponents.TARGET_Y, y);
        stack.set(PetCompassComponents.TARGET_Z, z);
        stack.set(PetCompassComponents.DIMENSION, dimension);
    }
    
    public static void clearTarget(ItemStack stack) {
        stack.remove(PetCompassComponents.COMPASS_STATE);
        stack.remove(PetCompassComponents.PET_UUID);
        stack.remove(PetCompassComponents.PET_NAME);
        stack.remove(PetCompassComponents.TARGET_X);
        stack.remove(PetCompassComponents.TARGET_Y);
        stack.remove(PetCompassComponents.TARGET_Z);
        stack.remove(PetCompassComponents.DIMENSION);
    }

    public static String getDimension(ItemStack stack) {
        return stack.getOrDefault(PetCompassComponents.DIMENSION, "minecraft:overworld");
    }

    public static String getDimensionDisplayName(String dimensionId) {
        return switch (dimensionId) {
            case "minecraft:overworld" -> "Overworld";
            case "minecraft:the_nether" -> "Nether";
            case "minecraft:the_end" -> "End";
            default -> {
                // Extract the dimension name from modded dimensions like "mod:dimension_name"
                int colonIndex = dimensionId.indexOf(':');
                if (colonIndex >= 0 && colonIndex < dimensionId.length() - 1) {
                    yield dimensionId.substring(colonIndex + 1).replace('_', ' ');
                }
                yield dimensionId;
            }
        };
    }
    
    public static String getPetUUID(ItemStack stack) {
        return stack.getOrDefault(PetCompassComponents.PET_UUID, "");
    }
    
    public static String getPetName(ItemStack stack) {
        return stack.getOrDefault(PetCompassComponents.PET_NAME, "Unknown");
    }
    
    public static int getTargetX(ItemStack stack) {
        return stack.getOrDefault(PetCompassComponents.TARGET_X, 0);
    }
    
    public static int getTargetY(ItemStack stack) {
        return stack.getOrDefault(PetCompassComponents.TARGET_Y, 0);
    }
    
    public static int getTargetZ(ItemStack stack) {
        return stack.getOrDefault(PetCompassComponents.TARGET_Z, 0);
    }

    public static int getDistance(ItemStack stack) {
        return stack.getOrDefault(PetCompassComponents.DISTANCE, 0);
    }

    public static void setDistance(ItemStack stack, int distance) {
        stack.set(PetCompassComponents.DISTANCE, distance);
    }

    public static int getInitialDistance(ItemStack stack) {
        return stack.getOrDefault(PetCompassComponents.INITIAL_DISTANCE, 0);
    }

    public static void setInitialDistance(ItemStack stack, int distance) {
        stack.set(PetCompassComponents.INITIAL_DISTANCE, distance);
    }
}
