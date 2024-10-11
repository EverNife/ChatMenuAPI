package br.com.finalcraft.evernifecore.chatmenuapi.listeners.expectedchat;

import br.com.finalcraft.evernifecore.chatmenuapi.listeners.CMListener;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.ScheduledFuture;

@Data
public class ExpectedChat {
    private Player player;
    private CMListener.IChatAction chatAction;
    private long expiration;
    private Runnable onExpireAction;
    private Runnable onPlayerQuitAction;

    private transient long creationTime = System.currentTimeMillis();
    private transient boolean cancelExpirationActionOnPlayerQuit = true;
    private transient ScheduledFuture<?> future; //Holds the future of the expiration task

    private @Getter(AccessLevel.NONE) boolean wasConsumed = false;
    private @Getter(AccessLevel.NONE) boolean wasCancelled = false;

    public ExpectedChat(Player player, CMListener.IChatAction chatAction, long expiration, Runnable onExpireAction, Runnable onPlayerQuitAction) {
        this.player = player;
        this.chatAction = chatAction;
        this.expiration = expiration;
        this.onExpireAction = onExpireAction;
        this.onPlayerQuitAction = onPlayerQuitAction;
    }

    public boolean wasCancelled() {
        return wasCancelled;
    }

    public boolean wasConsumed() {
        return wasConsumed;
    }

    public boolean hasExpired() {
        return expiration > 0 && System.currentTimeMillis() > (creationTime + expiration);
    }
}