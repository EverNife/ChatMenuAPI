package br.com.finalcraft.evernifecore.chatmenuapi.menu.element;

import br.com.finalcraft.evernifecore.chatmenuapi.listeners.expectedchat.ExpectedChat;

import javax.annotation.Nullable;

public interface ICanExpectChat {

    public @Nullable ExpectedChat getExpectedChat();

    public void cancelExpectedChat();

}
