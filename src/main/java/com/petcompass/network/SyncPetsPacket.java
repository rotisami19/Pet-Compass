package com.petcompass.network;

import com.petcompass.gui.PetCompassScreen;
import com.petcompass.util.PetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Packet sent from server to client with the list of tamed pets.
 */
public record SyncPetsPacket(List<PetUtils.TamedPetInfo> pets) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncPetsPacket> TYPE = 
        new CustomPacketPayload.Type<>(PetCompassNetworking.SYNC_PETS_ID);

    public static final StreamCodec<FriendlyByteBuf, SyncPetsPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public SyncPetsPacket decode(FriendlyByteBuf buf) {
                int size = buf.readVarInt();
                List<PetUtils.TamedPetInfo> pets = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    UUID uuid = buf.readUUID();
                    String name = buf.readUtf();
                    String entityType = buf.readUtf();
                    int x = buf.readInt();
                    int y = buf.readInt();
                    int z = buf.readInt();
                    String dimension = buf.readUtf();
                    pets.add(new PetUtils.TamedPetInfo(uuid, name, entityType, x, y, z, dimension));
                }
                return new SyncPetsPacket(pets);
            }

            @Override
            public void encode(FriendlyByteBuf buf, SyncPetsPacket packet) {
                buf.writeVarInt(packet.pets.size());
                for (PetUtils.TamedPetInfo pet : packet.pets) {
                    buf.writeUUID(pet.uuid());
                    buf.writeUtf(pet.name());
                    buf.writeUtf(pet.entityType());
                    buf.writeInt(pet.x());
                    buf.writeInt(pet.y());
                    buf.writeInt(pet.z());
                    buf.writeUtf(pet.dimension());
                }
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPetsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Open the GUI on the client with the received pet list
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setScreen(new PetCompassScreen(packet.pets));
            });
        });
    }
}
