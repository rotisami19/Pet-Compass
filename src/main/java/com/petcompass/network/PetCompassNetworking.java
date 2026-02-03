package com.petcompass.network;

import com.petcompass.PetCompass;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Handles registration of all network packets for Forge 1.20.1.
 */
public class PetCompassNetworking {

    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(PetCompass.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // Client -> Server: Request list of pets
        CHANNEL.registerMessage(packetId++,
            RequestPetsPacket.class,
            RequestPetsPacket::encode,
            RequestPetsPacket::decode,
            RequestPetsPacket::handle
        );

        // Server -> Client: Sync list of pets
        CHANNEL.registerMessage(packetId++,
            SyncPetsPacket.class,
            SyncPetsPacket::encode,
            SyncPetsPacket::decode,
            SyncPetsPacket::handle
        );

        // Client -> Server: Select a pet to track
        CHANNEL.registerMessage(packetId++,
            SelectPetPacket.class,
            SelectPetPacket::encode,
            SelectPetPacket::decode,
            SelectPetPacket::handle
        );

        // Server -> Client: Update compass data
        CHANNEL.registerMessage(packetId++,
            UpdateCompassPacket.class,
            UpdateCompassPacket::encode,
            UpdateCompassPacket::decode,
            UpdateCompassPacket::handle
        );

        // Server -> Client: Sync scanned pets from region files
        CHANNEL.registerMessage(packetId++,
            SyncScannedPetsPacket.class,
            SyncScannedPetsPacket::encode,
            SyncScannedPetsPacket::decode,
            SyncScannedPetsPacket::handle
        );
    }
}
