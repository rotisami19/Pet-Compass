package com.petcompass.network;

import com.petcompass.PetCompass;
import com.petcompass.PetCompassItem;
import com.petcompass.util.PetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Packet sent from client to server when a pet is selected in the GUI.
 * Now includes all pet data so we can track pets in unloaded chunks.
 */
public record SelectPetPacket(
    UUID petUUID,
    String petName,
    int x,
    int y,
    int z,
    String dimension
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SelectPetPacket> TYPE = 
        new CustomPacketPayload.Type<>(PetCompassNetworking.SELECT_PET_ID);

    public static final StreamCodec<FriendlyByteBuf, SelectPetPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public SelectPetPacket decode(FriendlyByteBuf buf) {
                return new SelectPetPacket(
                    buf.readUUID(),
                    buf.readUtf(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readUtf()
                );
            }

            @Override
            public void encode(FriendlyByteBuf buf, SelectPetPacket packet) {
                buf.writeUUID(packet.petUUID);
                buf.writeUtf(packet.petName);
                buf.writeInt(packet.x);
                buf.writeInt(packet.y);
                buf.writeInt(packet.z);
                buf.writeUtf(packet.dimension);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectPetPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Find the compass in player's hand
                ItemStack stack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                if (!(stack.getItem() instanceof PetCompassItem)) {
                    stack = serverPlayer.getItemInHand(InteractionHand.OFF_HAND);
                }
                
                if (stack.getItem() instanceof PetCompassItem) {
                    // Try to find the pet if it's loaded (for more accurate position)
                    Entity pet = PetUtils.findPetByUUID(serverPlayer.level(), serverPlayer, packet.petUUID, 200);
                    
                    int targetX, targetY, targetZ;
                    String targetDimension;
                    String petName;
                    
                    if (pet != null) {
                        // Pet is loaded - use live position
                        targetX = (int) pet.getX();
                        targetY = (int) pet.getY();
                        targetZ = (int) pet.getZ();
                        targetDimension = pet.level().dimension().location().toString();
                        petName = pet.hasCustomName() ? pet.getCustomName().getString() : pet.getType().getDescription().getString();
                    } else {
                        // Pet is NOT loaded - use the scanned data from the packet
                        targetX = packet.x;
                        targetY = packet.y;
                        targetZ = packet.z;
                        targetDimension = packet.dimension;
                        petName = packet.petName;
                    }
                    
                    // Set the compass target
                    PetCompassItem.setTarget(stack, packet.petUUID, petName, 
                        targetX, targetY, targetZ, targetDimension);
                    
                    // Calculate distance (approximate for cross-dimension)
                    int distance;
                    String playerDimension = serverPlayer.level().dimension().location().toString();
                    if (playerDimension.equals(targetDimension)) {
                        double dx = serverPlayer.getX() - targetX;
                        double dy = serverPlayer.getY() - targetY;
                        double dz = serverPlayer.getZ() - targetZ;
                        distance = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    } else {
                        // Cross-dimension: show a large placeholder distance
                        distance = 99999;
                    }
                    
                    PetCompassItem.setDistance(stack, distance);
                    PetCompassItem.setInitialDistance(stack, distance);
                    
                    // Send update to client
                    PacketDistributor.sendToPlayer(serverPlayer, new UpdateCompassPacket(
                        packet.petUUID.toString(),
                        petName,
                        targetX,
                        targetY,
                        targetZ,
                        distance,
                        true,
                        targetDimension
                    ));
                }
            }
        });
    }
}
