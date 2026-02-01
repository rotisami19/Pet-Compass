package com.petcompass.network;

import com.petcompass.PetCompass;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles registration of all network packets.
 */
public class PetCompassNetworking {

    public static final ResourceLocation REQUEST_PETS_ID = ResourceLocation.fromNamespaceAndPath(PetCompass.MODID, "request_pets");
    public static final ResourceLocation SYNC_PETS_ID = ResourceLocation.fromNamespaceAndPath(PetCompass.MODID, "sync_pets");
    public static final ResourceLocation SELECT_PET_ID = ResourceLocation.fromNamespaceAndPath(PetCompass.MODID, "select_pet");
    public static final ResourceLocation UPDATE_COMPASS_ID = ResourceLocation.fromNamespaceAndPath(PetCompass.MODID, "update_compass");

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PetCompassNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PetCompass.MODID).versioned("1.0.0");

        // Client -> Server: Request list of pets
        registrar.playToServer(
            RequestPetsPacket.TYPE,
            RequestPetsPacket.STREAM_CODEC,
            RequestPetsPacket::handle
        );

        // Server -> Client: Sync list of pets
        registrar.playToClient(
            SyncPetsPacket.TYPE,
            SyncPetsPacket.STREAM_CODEC,
            SyncPetsPacket::handle
        );

        // Client -> Server: Select a pet to track
        registrar.playToServer(
            SelectPetPacket.TYPE,
            SelectPetPacket.STREAM_CODEC,
            SelectPetPacket::handle
        );

        // Server -> Client: Update compass data
        registrar.playToClient(
            UpdateCompassPacket.TYPE,
            UpdateCompassPacket.STREAM_CODEC,
            UpdateCompassPacket::handle
        );

        // Server -> Client: Sync scanned pets from region files
        registrar.playToClient(
            SyncScannedPetsPacket.TYPE,
            SyncScannedPetsPacket.STREAM_CODEC,
            SyncScannedPetsPacket::handle
        );
    }
}
