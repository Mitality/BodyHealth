package bodyhealth.api.events;

import bodyhealth.core.BodyPart;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/***
 * Called whenever the health value (PERCENT!) of any BodyPart of any player is changed.
 * NOTE THAT HEALTH VALUES ARE GIVEN IN PERCENT TO SUPPORT CHANGES OF PLAYERS MAX HEALTH
 */
public class BodyPartHealthChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    boolean cancelled = false;

    Player player;
    BodyPart bodyPart;
    double oldHealth;
    double newHealth;

    public BodyPartHealthChangeEvent(Player player, BodyPart bodyPart, double oldHealth, double newHealth) {
        this.player = player;
        this.bodyPart = bodyPart;
        this.oldHealth = oldHealth;
        this.newHealth = newHealth;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    public @NotNull static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull BodyPart getBodyPart() {
        return bodyPart;
    }

    public double getOldHealth() {
        return oldHealth;
    }

    public double getNewHealth() {
        return newHealth;
    }

    public void setNewHealth(double health) {
        newHealth = health;
    }

}
