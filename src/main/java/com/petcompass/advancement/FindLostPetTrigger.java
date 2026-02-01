package com.petcompass.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

/**
 * Custom trigger that fires when a player finds a lost pet using the Pet Compass.
 * The pet must be at least minDistance blocks away to count as "lost".
 */
public class FindLostPetTrigger extends SimpleCriterionTrigger<FindLostPetTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    /**
     * Call this when a player successfully navigates to a tracked pet.
     * @param player The player who found the pet
     * @param distance The distance they traveled to find it
     */
    public void trigger(ServerPlayer player, double distance) {
        this.trigger(player, instance -> instance.matches(distance));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, int minDistance) 
            implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.INT.optionalFieldOf("min_distance", 100).forGetter(TriggerInstance::minDistance)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(double distance) {
            return distance >= minDistance;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }

        public static Criterion<TriggerInstance> found(int minDistance) {
            return PetCompassTriggers.FIND_LOST_PET.get().createCriterion(
                new TriggerInstance(Optional.empty(), minDistance)
            );
        }
    }
}
