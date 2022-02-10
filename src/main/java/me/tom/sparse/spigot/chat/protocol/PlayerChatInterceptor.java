package me.tom.sparse.spigot.chat.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerChatInterceptor implements Listener {

    private Map<UUID, PlayerMData> playerMessageDataMap = new ConcurrentHashMap<>();

    public PlayerChatInterceptor(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.CHAT) {

            @Override
            public void onPacketSending(PacketEvent event) {
                PlayerMData playerMData = getMessageData(event.getPlayer());

                boolean paused = playerMData.isPaused();
                if (!paused){
                    return; //Early Return to prevent wasted processing
                }

                if (paused){
                    WrapperPlayServerChat chat = new WrapperPlayServerChat(event.getPacket());

                    BaseComponent[] spigot = chat.getHandle().getSpecificModifier(BaseComponent[].class).read(0);
                    WrappedChatComponent msg;
                    if (spigot != null) {
                        msg = WrappedChatComponent.fromJson(ComponentSerializer.toString(spigot));
                    } else {
                        msg = chat.getMessage();
                    }

                    boolean allowThisMessage = playerMData.hasAnyAllowedMessages() && playerMData.isAllowed(msg);

                    if (!allowThisMessage){ //If not allowed, add to queue to be sent on resume() and cancel the event
                        while (playerMData.messageQueue.size() > 20) {
                            playerMData.messageQueue.remove();
                        }

                        playerMData.messageQueue.add(msg);

                        event.setCancelled(true);
                    }
                }
            }
        });
    }

    /**
     * Sends a message to the player associated with this, regardless of chat being paused.
     *
     * @param message the message to send
     */
    public void sendMessage(Player player, BaseComponent... message) {
        PlayerMData messageData = getMessageData(player);
        if (messageData.isPaused()) {
            messageData.allowedMessages.add(WrappedChatComponent.fromJson(ComponentSerializer.toString(message)));
        }
        player.spigot().sendMessage(message);
    }

    public void pause(Player player) {
        PlayerMData messageData = getMessageData(player);
        if (messageData.isPaused()) return;
        messageData.paused = true;
    }

    public void resume(Player player) {
        PlayerMData playerMData = getMessageData(player);
        if (playerMData.isPaused()) return;

        playerMData.paused = false;

        int i = 0;
        // copy so that we don't catch new messages
        Queue<WrappedChatComponent> queuedMessages = new ConcurrentLinkedQueue<>(playerMData.messageQueue);
        while (i < 20 - queuedMessages.size()) {
            i++;
            player.sendMessage(" ");
        }

        for (WrappedChatComponent components : queuedMessages) {
            WrapperPlayServerChat chat = new WrapperPlayServerChat();
            chat.setMessage(components);
            chat.setChatType();
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, chat.getHandle());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        playerMData.clearMessageQueues();
    }

    public PlayerMData getMessageData(Player player){
        return playerMessageDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerMData());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerMessageDataMap.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        PlayerMData messageData = getMessageData(e.getPlayer());
        if (messageData.isPaused()) e.setCancelled(true);
    }

    public void disable() {
        playerMessageDataMap.clear();
    }

    protected class PlayerMData {
        private boolean paused = false;
        private Queue<WrappedChatComponent> messageQueue = new ConcurrentLinkedQueue<>();
        private Queue<WrappedChatComponent> allowedMessages = new ConcurrentLinkedQueue<>();

        protected void clearMessageQueues(){
            messageQueue.clear();
            allowedMessages.clear();
        }

        protected boolean isPaused(){
            return paused;
        }

        protected boolean hasAnyAllowedMessages(){
            return allowedMessages.size() > 0;
        }

        protected boolean isAllowed(WrappedChatComponent message) {
            return allowedMessages.remove(message);
        }
    }
}
