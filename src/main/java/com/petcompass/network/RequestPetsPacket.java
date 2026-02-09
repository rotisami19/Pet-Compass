package com.petcompass.network;

import com.petcompass.util.PetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

/**
 * Packet sent from client to server to request the list of tamed pets.
 */
public class RequestPetsPacket {

    public RequestPetsPacket() {
    }

    public static void encode(RequestPetsPacket packet, FriendlyByteBuf buf) {
        // Nothing to encode
    }

    public static RequestPetsPacket decode(FriendlyByteBuf buf) {
        return new RequestPetsPacket();
    }

    public static void handle(RequestPetsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.get().getSender();
            if (serverPlayer != null) {
                // Search for pets across ALL dimensions (Overworld, Nether, End, etc.)
                List<PetUtils.TamedPetInfo> pets = PetUtils.getAllTamedPetsAcrossDimensions(
                    serverPlayer.getServer(),
                    serverPlayer
                );
                
                // Send the list back to the client
                PetCompassNetworking.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncPetsPacket(pets)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
