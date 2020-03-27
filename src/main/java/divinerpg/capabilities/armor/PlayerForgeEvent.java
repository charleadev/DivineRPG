package divinerpg.capabilities.armor;

import divinerpg.api.armor.binded.IPlayerForgeEvent;
import divinerpg.api.armor.registry.IForgeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class PlayerForgeEvent<T extends Event> implements IPlayerForgeEvent<T> {
    private final IForgeEvent<T> base;
    private final EntityPlayer player;
    private boolean isListen = false;

    public PlayerForgeEvent(IForgeEvent<T> base, EntityPlayer player) {
        this.base = base;
        this.player = player;

        Objects.requireNonNull(base);
        Objects.requireNonNull(player);
    }

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isListening() {
        return isListen;
    }

    /**
     * Subscribe on current event
     */
    @Override
    public void subscribe() {
        MinecraftForge.EVENT_BUS.register(this);
        isListen = true;
    }

    /**
     * Unregister from event subscribing
     */
    @Override
    public void unsubscribe() {
        MinecraftForge.EVENT_BUS.unregister(this);
        isListen = false;
    }

    @Override
    public void handle(T event) {
        base.handle(event);
    }

    @Override
    public Class<T> getParameterClass() {
        return base.getParameterClass();
    }

    /**
     * Listening method. Can handle only if that event is assotiated with current player
     *
     * @param event - forge event
     */
    @SubscribeEvent
    public void handleEvent(T event) {
        if (getParameterClass().equals(event.getClass()) && canHandle(event)) {
            handle(event);
        }
    }
}
