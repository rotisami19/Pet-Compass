package com.petcompass;

import java.util.UUID;
import java.util.List;

import com.petcompass.network.PetCompassNetworking;
import com.petcompass.network.RequestPetsPacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class PetCompassItem extends Item {

    // NBT Keys
    public static final String KEY_STATE = "CompassState";
    public static final String KEY_PET_UUID = "PetUUID";
    public static final String KEY_PET_NAME = "PetName";
    public static final String KEY_TARGET_X = "TargetX";
    public static final String KEY_TARGET_Y = "TargetY";
    public static final String KEY_TARGET_Z = "TargetZ";
    public static final String KEY_DIMENSION = "TargetDimension";
    public static final String KEY_DISTANCE = "Distance";
    public static final String KEY_INITIAL_DISTANCE = "InitialDistance";

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
            } else {
                player.displayClientMessage(Component.translatable("chat.petcompass.reset"), true);
            }
        } else {
            // Right click: open GUI
            if (level.isClientSide) {
                // Request pet list from server and open GUI
                PetCompassNetworking.CHANNEL.send(PacketDistributor.SERVER.noArg(), new RequestPetsPacket());
            }
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
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
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getState(stack) == CompassState.FOUND;
    }

    // ========== State Management (NBT) ==========
    
    public static CompassState getState(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(KEY_STATE)) {
            return CompassState.fromId(tag.getInt(KEY_STATE));
        }
        return CompassState.INACTIVE;
    }
    
    public static void setState(ItemStack stack, CompassState state) {
        stack.getOrCreateTag().putInt(KEY_STATE, state.getId());
    }
    
    public static void setTarget(ItemStack stack, UUID petUUID, String petName, int x, int y, int z, String dimension) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(KEY_STATE, CompassState.FOUND.getId());
        tag.putString(KEY_PET_UUID, petUUID.toString());
        tag.putString(KEY_PET_NAME, petName);
        tag.putInt(KEY_TARGET_X, x);
        tag.putInt(KEY_TARGET_Y, y);
        tag.putInt(KEY_TARGET_Z, z);
        tag.putString(KEY_DIMENSION, dimension);
    }
    
    public static void clearTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(KEY_STATE);
            tag.remove(KEY_PET_UUID);
            tag.remove(KEY_PET_NAME);
            tag.remove(KEY_TARGET_X);
            tag.remove(KEY_TARGET_Y);
            tag.remove(KEY_TARGET_Z);
            tag.remove(KEY_DIMENSION);
            tag.remove(KEY_DISTANCE);
            tag.remove(KEY_INITIAL_DISTANCE);
        }
    }

    public static String getDimension(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(KEY_DIMENSION)) ? tag.getString(KEY_DIMENSION) : "minecraft:overworld";
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
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(KEY_PET_UUID)) ? tag.getString(KEY_PET_UUID) : "";
    }
    
    public static String getPetName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(KEY_PET_NAME)) ? tag.getString(KEY_PET_NAME) : "Unknown";
    }
    
    public static int getTargetX(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(KEY_TARGET_X)) ? tag.getInt(KEY_TARGET_X) : 0;
    }
    
    public static int getTargetY(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(KEY_TARGET_Y)) ? tag.getInt(KEY_TARGET_Y) : 0;
    }
    
    public static int getTargetZ(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(KEY_TARGET_Z)) ? tag.getInt(KEY_TARGET_Z) : 0;
    }

    public static int getDistance(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(KEY_DISTANCE)) ? tag.getInt(KEY_DISTANCE) : 0;
    }

    public static void setDistance(ItemStack stack, int distance) {
        stack.getOrCreateTag().putInt(KEY_DISTANCE, distance);
    }

    public static int getInitialDistance(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(KEY_INITIAL_DISTANCE)) ? tag.getInt(KEY_INITIAL_DISTANCE) : 0;
    }

    public static void setInitialDistance(ItemStack stack, int distance) {
        stack.getOrCreateTag().putInt(KEY_INITIAL_DISTANCE, distance);
    }
}
