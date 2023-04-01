package cz.gennario.newrotatingheads.developer.events;

import cz.gennario.newrotatingheads.system.RotatingHead;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class HeadPlayerDespawnEvent extends Event implements Cancellable {

    private final RotatingHead rotatingHead;
    private final Player player;
    private boolean isCancelled;

    public HeadPlayerDespawnEvent(RotatingHead rotatingHead, Player player, boolean isCancelled) {
        this.rotatingHead = rotatingHead;
        this.player = player;
        this.isCancelled = isCancelled;
    }


    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
