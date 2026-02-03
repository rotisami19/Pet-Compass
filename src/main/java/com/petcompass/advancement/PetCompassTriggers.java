package com.petcompass.advancement;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Registry for custom advancement triggers.
 */
public class PetCompassTriggers {

    public static final FindLostPetTrigger FIND_LOST_PET = new FindLostPetTrigger();

    public static void register(IEventBus modEventBus) {
        CriteriaTriggers.register(FIND_LOST_PET);
    }
}
