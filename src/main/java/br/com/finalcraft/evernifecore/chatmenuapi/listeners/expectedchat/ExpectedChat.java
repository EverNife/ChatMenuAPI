package br.com.finalcraft.evernifecore.chatmenuapi.listeners.expectedchat;

import br.com.finalcraft.evernifecore.chatmenuapi.listeners.CMListener;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.bukkit.entity.Player;

@Data
public class ExpectedChat {
    private Player player;
    private CMListener.IChatAction chatAction;
    private long expiration = 0;

    private @Getter(AccessLevel.NONE) boolean wasConsumed = false;
    private @Getter(AccessLevel.NONE) boolean wasCancelled = false;

    public ExpectedChat(Player player, CMListener.IChatAction chatAction, long expiration) {
        this.player = player;
        this.chatAction = chatAction;
        this.expiration = expiration;
    }

    public void cancel() {
        wasCancelled = true;
    }

    public boolean wasCancelled() {
        return wasCancelled;
    }

    public boolean wasConsumed() {
        return wasConsumed;
    }

    public boolean hasExpired() {
        return expiration > 0 && System.currentTimeMillis() > expiration;
    }
}