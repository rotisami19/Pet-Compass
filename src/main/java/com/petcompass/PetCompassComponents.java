package com.petcompass;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PetCompassComponents {
    
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = 
        DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, PetCompass.MODID);
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COMPASS_STATE = 
        register("compass_state", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
            .build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PET_UUID = 
        register("pet_uuid", () -> DataComponentType.<String>builder()
            .persistent(Codec.STRING)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
            .build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PET_NAME = 
        register("pet_name", () -> DataComponentType.<String>builder()
            .persistent(Codec.STRING)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
            .build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TARGET_X = 
        register("target_x", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
            .build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TARGET_Y = 
        register("target_y", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
            .build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TARGET_Z = 
        register("target_z", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DISTANCE = 
        register("distance", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> DIMENSION = 
        register("dimension", () -> DataComponentType.<String>builder()
            .persistent(Codec.STRING)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
            .build());

    // Distance initiale au moment de la sélection (pour l'achievement)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> INITIAL_DISTANCE = 
        register("initial_distance", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
            .build());
    
    @SuppressWarnings("unchecked")
    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, Supplier<DataComponentType<T>> supplier) {
        return (DeferredHolder<DataComponentType<?>, DataComponentType<T>>) (Object) DATA_COMPONENTS.register(name, supplier);
    }
    
    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
