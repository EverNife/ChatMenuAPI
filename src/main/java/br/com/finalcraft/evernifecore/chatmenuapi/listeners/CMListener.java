package br.com.finalcraft.evernifecore.chatmenuapi.listeners;

import br.com.finalcraft.evernifecore.chatmenuapi.listeners.expectedchat.ExpectedChat;
import br.com.finalcraft.evernifecore.chatmenuapi.menu.CMCommand;
import com.google.common.collect.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CMListener implements Listener {

    private Multimap<Player, ExpectedChat> CHAT_LISTENERS = (Multimap<Player, ExpectedChat>)
            Collections.synchronizedMap(
                    (Map<? extends Player, ? extends ExpectedChat>) (Object) HashMultimap.create()
            );

    private final CMCommand command;

    public CMListener(Plugin plugin) {
        HashMultimap<Object, Object> objectObjectHashMultimap = HashMultimap.create();

        command = new CMCommand();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void cancelAllExpectationsFor(Player player) {
        Collection<ExpectedChat> expectedChats = CHAT_LISTENERS.removeAll(player);
        expectedChats.forEach(ExpectedChat::cancel);
    }

    /**
     * Expect a player's to chat a message.
     */
    public ExpectedChat expectPlayerChat(Player player, IChatAction chatAction) {
        return expectPlayerChat(player, chatAction, 0);
    }

    /**
     * Expect a player's to chat a message.
     */
    public ExpectedChat expectPlayerChat(Player player, IChatAction chatAction, long expiration) {
        if (player == null || !player.isOnline())
            throw new IllegalArgumentException("Cannot wait for chat for a null/offline player.");
        if (chatAction == null)
            throw new IllegalArgumentException("Cannot call null function.");

        ExpectedChat expectedChat = new ExpectedChat(player, chatAction, expiration);

        CHAT_LISTENERS.put(player, expectedChat);

        return expectedChat;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Collection<ExpectedChat> listener = CHAT_LISTENERS.get(player);

        for (ExpectedChat expectedChat : listener) {

            if (expectedChat.wasCancelled() || expectedChat.wasConsumed() || expectedChat.hasExpired()) {
                CHAT_LISTENERS.remove(player, expectedChat);
                continue;
            }

            IChatAction.ActionResult actionResult = expectedChat.getChatAction().onChat(e.getMessage());

            switch (actionResult){
                case SUCCESS:
                    CHAT_LISTENERS.remove(player, expectedChat);
                    expectedChat.setWasConsumed(true);
                    continue;

                case SUCCESS_AND_CANCEL_CHAT_EVENT:
                    CHAT_LISTENERS.remove(player, expectedChat);
                    expectedChat.setWasConsumed(true);
                    e.setCancelled(true);
                    break;

                case IGNORE_CURRENT_MESSAGE:
                    //The chat event was processed but not consumed
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        cancelAllExpectationsFor(e.getPlayer());
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
            SUCCESS_AND_CANCEL_CHAT_EVENT
        }
    }


}
