package br.com.finalcraft.evernifecore.chatmenuapi.listeners;

import br.com.finalcraft.evernifecore.chatmenuapi.listeners.expectedchat.ExpectedChat;
import br.com.finalcraft.evernifecore.chatmenuapi.menu.CMCommand;
import br.com.finalcraft.evernifecore.scheduler.FCScheduler;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CMListener implements Listener {

    private Multimap<UUID, ExpectedChat> CHAT_LISTENERS = Multimaps.synchronizedMultimap(HashMultimap.create());

    private final CMCommand command;

    public CMListener(Plugin plugin) {
        command = new CMCommand();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public Multimap<UUID, ExpectedChat> getChatListeners() {
        return CHAT_LISTENERS;
    }

    /**
     * Expect a player's to chat a message.
     *
     * @param player The player to expect a chat from.
     * @param chatAction The action to perform when the player chats.
     */
    public ExpectedChat expectPlayerChat(Player player, IChatAction chatAction) {
        return expectPlayerChat(player, chatAction, 0, null, null);
    }

    /**
     * Expect a player's to chat a message.
     *
     * @param player The player to expect a chat from.
     * @param chatAction The action to perform when the player chats.
     * @param expiration The time in milliseconds the wait for the chat.
     */
    public ExpectedChat expectPlayerChat(Player player, IChatAction chatAction, long expiration) {
        return expectPlayerChat(player, chatAction, expiration, null, null);
    }

    /**
     * Expect a player's to chat a message.
     *
     * @param player The player to expect a chat from.
     * @param chatAction The action to perform when the player chats.
     * @param expiration The time in milliseconds the wait for the chat.
     * @param onExpireAction The action to perform when the chat expires.
     */
    public ExpectedChat expectPlayerChat(Player player, IChatAction chatAction, long expiration, Runnable onExpireAction) {
        return expectPlayerChat(player, chatAction, expiration, onExpireAction, null);
    }

    /**
     * Expect a player's to chat a message.
     *
     * @param player The player to expect a chat from.
     * @param chatAction The action to perform when the player chats.
     * @param expiration The time in milliseconds the wait for the chat.
     * @param onExpireAction The action to perform when the chat expires.
     * @param onPlayerQuitAction The action to perform when the player quits.
     */
    public ExpectedChat expectPlayerChat(Player player, CMListener.IChatAction chatAction, long expiration, Runnable onExpireAction, Runnable onPlayerQuitAction) {
        if (player == null || !player.isOnline()) {
            throw new IllegalArgumentException("Cannot wait for chat for a null/offline player.");
        }
        if (chatAction == null) {
            throw new IllegalArgumentException("Cannot call null function.");
        }

        ExpectedChat expectedChat = new ExpectedChat(player, chatAction, expiration, onExpireAction, onPlayerQuitAction);

        if (onExpireAction != null){
            ScheduledFuture<?> future = FCScheduler.getScheduler().schedule(() -> {
                if (expectedChat.wasConsumed() || expectedChat.wasCancelled()) {
                    return;
                }

                expectedChat.getOnExpireAction().run();
            }, expiration, TimeUnit.MILLISECONDS);
            expectedChat.setFuture(future);
        }

        CHAT_LISTENERS.put(player.getUniqueId(), expectedChat);

        return expectedChat;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Collection<ExpectedChat> listener = new ArrayList<>(CHAT_LISTENERS.get(player.getUniqueId()));

        expectations: for (ExpectedChat expectedChat : listener) {

            if (expectedChat.wasCancelled() || expectedChat.wasConsumed() || expectedChat.hasExpired()) {
                CHAT_LISTENERS.remove(player.getUniqueId(), expectedChat);
                continue;
            }

            IChatAction.ActionResult actionResult = expectedChat.getChatAction().onChat(e.getMessage());

            switch (actionResult){
                case SUCCESS_AND_CONSUME:
                    CHAT_LISTENERS.remove(player.getUniqueId(), expectedChat);
                    expectedChat.setWasConsumed(true);
                    e.setCancelled(true);
                    if (expectedChat.getFuture() != null){
                        expectedChat.getFuture().cancel(false);
                    }
                    break expectations;

                case SUCCESS:
                    CHAT_LISTENERS.remove(player.getUniqueId(), expectedChat);
                    expectedChat.setWasConsumed(true);
                    if (expectedChat.getFuture() != null){
                        expectedChat.getFuture().cancel(false);
                    }
                    continue expectations;

                case IGNORE_CURRENT_MESSAGE:
                    //The chat event was processed, but maybe we should wait for the next message
                    continue expectations;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        Collection<ExpectedChat> expectedChats = CHAT_LISTENERS.removeAll(player.getUniqueId());
        for (ExpectedChat expectedChat : expectedChats) {
            expectedChat.setWasCancelled(true);

            if (expectedChat.isCancelExpirationActionOnPlayerQuit() && expectedChat.getFuture() != null){
                expectedChat.getFuture().cancel(false);
            }

            if (expectedChat.getOnPlayerQuitAction() != null){
                expectedChat.getOnPlayerQuitAction().run();
            }

        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage().substring(1);
        if (cmd.length() == 0){
            return;
        }

        String[] unprocessedArgs = cmd.split(" ");
        String label = unprocessedArgs[0];

        if (label.equalsIgnoreCase("cmapi")) {
            String[] args = new String[unprocessedArgs.length - 1];
            System.arraycopy(unprocessedArgs, 1, args, 0, args.length);

            e.setCancelled(true);
            command.onCommand(e.getPlayer(), null, label, args);
        }
    }

    public static interface IChatAction {

        /**
         * @param message The message the player chatted.
         *
         * @return true if the chat message was consumed
         *              and should not propagate further.
         */
        public ActionResult onChat(String message);

        public static enum ActionResult {
            SUCCESS,
            IGNORE_CURRENT_MESSAGE,
            SUCCESS_AND_CONSUME
        }
    }


}
