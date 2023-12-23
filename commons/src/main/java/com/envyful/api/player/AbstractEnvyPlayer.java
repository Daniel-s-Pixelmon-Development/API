package com.envyful.api.player;

import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.player.attribute.Attribute;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.player.save.SaveManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 *
 * This interface is designed to provide basic useful
 * methods for all the different player implementations independent
 * of the platform details (i.e. auto-translates all text sent
 * to the player, and makes it less complicated to do
 * different functions such as sending titles etc.).
 * <br>
 * It also stores {@link PlayerAttribute} from the plugin implementation
 * that will include specific data from the
 * plugin / mod. The attributes stored by the plugin's / manager's
 * class as to allow each mod / plugin to have multiple
 * attributes for storing different sets of data.
 *
 * @param <T> The specific platform implementation of the player object.
 */
public abstract class AbstractEnvyPlayer<T> implements EnvyPlayer<T> {

    protected final Map<Class<?>, AttributeInstance> attributes = Maps.newHashMap();

    protected final SaveManager<T> saveManager;

    protected T parent;

    protected AbstractEnvyPlayer(SaveManager<T> saveManager) {
        this.saveManager = saveManager;
    }

    @Override
    public T getParent() {
        return this.parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Attribute<B>, B> CompletableFuture<A> getAttribute(Class<A> attributeClass) {
        if (!this.attributes.containsKey(attributeClass)) {
            return null;
        }

        AttributeInstance<A, B> instance = (AttributeInstance<A, B>) this.attributes.get(attributeClass);
        return instance.getAttribute();
    }

    @Override
    public <A extends Attribute<B>, B> A getAttributeNow(Class<A> attributeClass) {
        var future = this.getAttribute(attributeClass);

        if (future == null) {
            return null;
        }

        return future.join();
    }

    @Override
    public void invalidateAttribute(Attribute<?> attribute) {
        this.attributes.remove(attribute.getClass());
    }

    @Override
    public <A extends Attribute<B>, B> CompletableFuture<A> loadAttribute(
            Class<? extends A> attributeClass, B id) {
        AttributeInstance<A, B> instance = new AttributeInstance<>(this.saveManager.loadAttribute(attributeClass, id));
        this.attributes.put(attributeClass, instance);
        return instance.getAttribute();
    }

    @Override
    public <A extends Attribute<B>, B> void setAttribute(A attribute) {
        this.attributes.put(attribute.getClass(), new AttributeInstance<>(attribute));
    }

    @Override
    public List<Attribute<?>> getAttributes() {
        List<Attribute<?>> attributes = Lists.newArrayList();

        for (AttributeInstance<?, ?> attribute : this.attributes.values()) {
            if (attribute.getAttributeNow() != null) {
                attributes.add(attribute.getAttributeNow());
            }
        }

        return attributes;
    }

    public static class AttributeInstance<A extends Attribute<B>, B> {

        private A attribute;
        private CompletableFuture<A> loadingAttribute;

        public AttributeInstance(A attribute) {
            this.attribute = attribute;
            this.loadingAttribute = null;
        }

        public AttributeInstance(CompletableFuture<A> loadingAttribute) {
            this.attribute = null;
            this.loadingAttribute = loadingAttribute.whenComplete((a, throwable) -> {
                if (throwable != null) {
                    UtilLogger.logger().ifPresent(logger -> logger.error("Failed to load attribute", throwable));
                } else {
                    this.attribute = a;
                    this.loadingAttribute = null;
                }
            });
        }

        public CompletableFuture<A> getAttribute() {
            return this.loadingAttribute == null ? CompletableFuture.completedFuture(this.attribute) : this.loadingAttribute;
        }

        @Nullable
        public A getAttributeNow() {
            return this.attribute;
        }

        public void invalidate() {
            this.attribute = null;

            if (this.loadingAttribute != null) {
                this.loadingAttribute.cancel(true);
            }

            this.loadingAttribute = null;
        }
    }
}
