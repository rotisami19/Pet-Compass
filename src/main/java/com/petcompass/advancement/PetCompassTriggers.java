package com.petcompass.advancement;

import com.petcompass.PetCompass;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for custom advancement triggers.
 */
public class PetCompassTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = 
        DeferredRegister.create(Registries.TRIGGER_TYPE, PetCompass.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, FindLostPetTrigger> FIND_LOST_PET = 
        TRIGGERS.register("find_lost_pet", FindLostPetTrigger::new);

    public static void register(IEventBus modEventBus) {
        TRIGGERS.register(modEventBus);
    }
}
