package com.petcompass;

import com.petcompass.advancement.PetCompassTriggers;
import com.petcompass.network.PetCompassNetworking;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(PetCompass.MODID)
public class PetCompass {
    public static final String MODID = "petcompass";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MODID);
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    
    public static final RegistryObject<PetCompassItem> PET_COMPASS = ITEMS.register("pet_compass", 
        () -> new PetCompassItem(new Item.Properties().stacksTo(1)));

    public PetCompass() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        ITEMS.register(modEventBus);
        PetCompassTriggers.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        
        // Register networking
        PetCompassNetworking.register();
        
        // Register server events
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PET_COMPASS.get());
        }
    }
}
