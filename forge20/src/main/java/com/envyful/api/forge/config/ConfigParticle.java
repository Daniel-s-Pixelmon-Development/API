package com.envyful.api.forge.config;

import com.envyful.api.math.Vector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConfigParticle {

    protected String particleType;
    protected transient ParticleOptions cachedType;
    protected int amount;
    protected Vector distance;
    protected float maxSpeed;

    public ConfigParticle() {
    }

    public ConfigParticle(Builder builder) {
        this.particleType = builder.particleType;
        this.amount = builder.amount;
        this.distance = builder.distance;
        this.maxSpeed = builder.maxSpeed;
    }

    public void spawnParticles(ServerLevel world, BlockPos pos) {
        world.sendParticles(this.getCachedType(),
                pos.getX(), pos.getY(), pos.getZ(),
                this.amount,
                this.distance.getX(), this.distance.getY(), this.distance.getZ(), this.maxSpeed);
    }

    private ParticleOptions getCachedType() {
        if (this.cachedType == null) {
            this.cachedType = (ParticleOptions) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(this.particleType));
        }

        return this.cachedType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected String particleType;
        protected int amount;
        protected Vector distance;
        protected float maxSpeed;

        protected Builder() {
        }

        public Builder particleType(ParticleOptions particleType) {
            this.particleType = ForgeRegistries.PARTICLE_TYPES.getKey(particleType.getType()).toString();
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder distance(Vector distance) {
            this.distance = distance;
            return this;
        }

        public Builder maxSpeed(float maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }

        public ConfigParticle build() {
            return new ConfigParticle(this);
        }
    }
}
