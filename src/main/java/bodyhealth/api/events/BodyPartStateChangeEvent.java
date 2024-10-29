package bodyhealth.api.events;

import bodyhealth.core.BodyPart;
import bodyhealth.core.BodyPartState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/***
 * Called whenever any BodyPart of any player changes its BodyPartState.
 * This can be used to add custom Effects via the API instead of using the command effect.
 * NOTE THAT BOTH STATES MAY BE NULL WHEN A PLAYER JOINS/LEAVES OR THE SYSTEM IS RELOADED
 */
public class BodyPartStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    Player player;
    BodyPart bodyPart;
    BodyPartState oldState;
    BodyPartState newState;

    public BodyPartStateChangeEvent(@NotNull Player player, @NotNull BodyPart bodyPart, @Nullable BodyPartState oldState, @Nullable BodyPartState newState) {
        this.player = player;
        this.bodyPart = bodyPart;
        this.oldState = oldState;
        this.newState = newState;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull BodyPart getBodyPart() {
        return bodyPart;
    }

    public @Nullable BodyPartState getOldState() {
        return oldState;
    }

    public @Nullable BodyPartState getNewState() {
        return newState;
    }

}