package com.petcompass.network;

import com.petcompass.util.PetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from server to client with the list of tamed pets.
 */
public class SyncPetsPacket {

    private final List<PetUtils.TamedPetInfo> pets;

    public SyncPetsPacket(List<PetUtils.TamedPetInfo> pets) {
        this.pets = pets;
    }

    public static void encode(SyncPetsPacket packet, FriendlyByteBuf buf) {
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

    public static SyncPetsPacket decode(FriendlyByteBuf buf) {
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

    public static void handle(SyncPetsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handleSyncPets(packet.pets);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
