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
 */
public record SelectPetPacket(UUID petUUID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SelectPetPacket> TYPE = 
        new CustomPacketPayload.Type<>(PetCompassNetworking.SELECT_PET_ID);

    public static final StreamCodec<FriendlyByteBuf, SelectPetPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public SelectPetPacket decode(FriendlyByteBuf buf) {
                return new SelectPetPacket(buf.readUUID());
            }

            @Override
            public void encode(FriendlyByteBuf buf, SelectPetPacket packet) {
                buf.writeUUID(packet.petUUID);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectPetPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Find the pet and update the compass
                Entity pet = PetUtils.findPetByUUID(serverPlayer.level(), serverPlayer, packet.petUUID, 5000);
                
                if (pet != null) {
                    // Find the compass in player's hand
                    ItemStack stack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                    if (!(stack.getItem() instanceof PetCompassItem)) {
                        stack = serverPlayer.getItemInHand(InteractionHand.OFF_HAND);
                    }
                    
                    if (stack.getItem() instanceof PetCompassItem) {
                        String petName = pet.hasCustomName() ? pet.getCustomName().getString() : pet.getType().getDescription().getString();
                        String dimension = pet.level().dimension().location().toString();
                        PetCompassItem.setTarget(stack, pet.getUUID(), petName, 
                            (int) pet.getX(), (int) pet.getY(), (int) pet.getZ(), dimension);
                        
                        // Calculate and store initial distance (for achievement)
                        int distance = (int) serverPlayer.distanceTo(pet);
                        PetCompassItem.setDistance(stack, distance);
                        PetCompassItem.setInitialDistance(stack, distance);
                        
                        // Send update to client
                        PacketDistributor.sendToPlayer(serverPlayer, new UpdateCompassPacket(
                            pet.getUUID().toString(),
                            petName,
                            (int) pet.getX(),
                            (int) pet.getY(),
                            (int) pet.getZ(),
                            distance,
                            true,
                            dimension
                        ));
                    }
                } else {
                    // Pet not found in range
                    PacketDistributor.sendToPlayer(serverPlayer, new UpdateCompassPacket(
                        "",
                        "",
                        0, 0, 0,
                        -1,
                        false,
                        ""
                    ));
                }
            }
        });
    }
}
