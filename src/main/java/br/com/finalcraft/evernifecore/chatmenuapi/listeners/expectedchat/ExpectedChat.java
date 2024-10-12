package br.com.finalcraft.evernifecore.chatmenuapi.listeners.expectedchat;

import br.com.finalcraft.evernifecore.chatmenuapi.listeners.CMListener;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

@Data
@Accessors(chain = true)
public class ExpectedChat {
    private final Player player;
    private final CMListener.IChatAction chatAction;
    private final long expiration;
    private final @Nullable Runnable onExpireAction;
    private final @Nullable Runnable onPlayerQuitAction;
    private final AtomicReference<ScheduledFuture<?>> future; //Holds the future of the expiration task

    private transient long creationTime = System.currentTimeMillis();
    private transient boolean cancelExpirationActionOnPlayerQuit = true;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean wasConsumed = false;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean wasCancelled = false;

    public ExpectedChat(Player player, CMListener.IChatAction chatAction, long expiration, Runnable onExpireAction, Runnable onPlayerQuitAction, AtomicReference<ScheduledFuture<?>> future) {
        this.player = player;
        this.chatAction = chatAction;
        this.expiration = expiration;
        this.onExpireAction = onExpireAction;
        this.onPlayerQuitAction = onPlayerQuitAction;
        this.future = future;
    }

    public boolean wasConsumed() {
        return wasConsumed;
    }

    public boolean wasCancelled() {
        return wasCancelled;
    }

    public ExpectedChat setConsumed(boolean consumed) {
        this.wasConsumed = consumed;
        return this;
    }

    public ExpectedChat setCancelled(boolean cancelled) {
        this.wasCancelled = cancelled;
        return this;
    }

    public boolean hasExpired() {
        return expiration > 0 && System.currentTimeMillis() > (creationTime + expiration);
    }

    public boolean isWaitingForResponse() {
        return !wasCancelled && !wasConsumed && !hasExpired();
    }
}