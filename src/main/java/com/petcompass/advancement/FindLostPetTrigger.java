package com.petcompass.advancement;

import com.google.gson.JsonObject;
import com.petcompass.PetCompass;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Custom trigger that fires when a player finds a lost pet using the Pet Compass.
 */
public class FindLostPetTrigger extends SimpleCriterionTrigger<FindLostPetTrigger.TriggerInstance> {

    static final ResourceLocation ID = new ResourceLocation(PetCompass.MODID, "find_lost_pet");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        int minDistance = json.has("min_distance") ? json.get("min_distance").getAsInt() : 0;
        return new TriggerInstance(player, minDistance);
    }

    public void trigger(ServerPlayer player, double distance) {
        this.trigger(player, instance -> instance.matches(distance));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final int minDistance;

        public TriggerInstance(ContextAwarePredicate player, int minDistance) {
            super(ID, player);
            this.minDistance = minDistance;
        }

        public boolean matches(double distance) {
            return distance >= minDistance;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("min_distance", minDistance);
            return json;
        }
    }
}
