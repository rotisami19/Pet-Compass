package com.petcompass;

import com.petcompass.advancement.PetCompassTriggers;
import com.petcompass.network.PetCompassNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(PetCompass.MODID)
public class PetCompass {
    public static final String MODID = "petcompass";
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    
    public static final DeferredHolder<Item, PetCompassItem> PET_COMPASS = ITEMS.register("pet_compass", 
        () -> new PetCompassItem(new Item.Properties().stacksTo(1)));

    public PetCompass(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        PetCompassComponents.register(modEventBus);
        PetCompassTriggers.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        
        // Register networking
        PetCompassNetworking.register(modEventBus);
    }

    
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PET_COMPASS.get());
        }
    }
}
