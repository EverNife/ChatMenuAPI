package br.com.finalcraft.evernifecore.chatmenuapi.menu;

import br.com.finalcraft.evernifecore.chatmenuapi.menu.element.Element;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface IElementContainer {
    /**
     * Add the specified element to this container
     *
     * @param element the element to add
     * @param <T>     the type of element being added
     * @return the element that was added
     */
    <T extends Element> T add(T element);

    /**
     * Remove the specified element from this container
     *
     * @param element the element to remove
     * @return true if the element was removed
     */
    boolean remove(Element element);

    /**
     * @return an unmodifiable list of all the elements in this container
     */
    List<Element> getElements();

    /**
     * @return a list of all the elements in this container
     * and all the elements in any sub-containers
     */
    default List<Element> getElementsRecursively(){
        List<Element> elements = getElements();
        List<Element> allElements = new ArrayList<>(elements);
        for (Element element : elements) {
            if (element instanceof IElementContainer) {
                allElements.addAll(((IElementContainer) element).getElementsRecursively());
            }
        }
        return allElements;
    }

    /**
     * @param element the element to interact with
     * @return the command used to interact with the provided element
     */
    String getCommand(Element element);

    /**
     * Display this container to the specified player
     *
     * @param player the player to open this container for
     */
    void openFor(Player player);

    /**
     * Displays this container again to all of the players currently viewing it
     */
    void refresh();

    /**
     * Cancel ALL the expected chat for this container
     *
     * For example, if an input element is expecting a chat,
     * this method will cancel that expectation
     */
    void cancelInnerElementsExpectedChat();

    ChatMenu getChatMenu();
}
