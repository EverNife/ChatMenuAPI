package br.com.finalcraft.evernifecore.chatmenuapi.menu.element;

import br.com.finalcraft.evernifecore.chatmenuapi.menu.ChatMenu;
import br.com.finalcraft.evernifecore.chatmenuapi.menu.IElementContainer;
import br.com.finalcraft.evernifecore.chatmenuapi.util.Text;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GroupElement extends Element implements IElementContainer {
    @NotNull
    protected final IElementContainer parent;
    @NotNull
    protected List<Element> elements;

    /**
     * Constructs an element at the given x and y coordinates.
     *
     * @param parent The parent element of this group. Usually a {@code ChatMenu} or another {@code GroupElement}
     * @param x      the x coordinate to put this element at
     * @param y      the y coordinate to put this element at
     */
    public GroupElement(@NotNull IElementContainer parent, int x, int y) {
        super(x, y);
        this.parent = parent;
        this.elements = new ArrayList<>();
    }

    /**
     * @param element the element to add
     * @param <T>     the type of element to add
     * @return the element that was added
     */
    @Override
    public <T extends Element> T add(@NotNull T element) {
        Objects.requireNonNull(element);
        elements.add(element);
        elements.sort(Comparator.comparingInt(Element::getX));
        return element;
    }

    /**
     * Removes the specified element from this group.
     *
     * @param element the element to remove
     * @return true if the element was removed
     */
    @Override
    public boolean remove(@NotNull Element element) {
        return elements.remove(element);
    }

    /**
     * @return an unmodifiable list of all the elements in this group.
     */
    @NotNull
    @Override
    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }

    /**
     * @param element the element to interact with
     * @return the command used to interact with this element
     */
    @NotNull
    @Override
    public String getCommand(@NotNull Element element) {
        int index = elements.indexOf(element);
        if (index == -1)
            throw new IllegalArgumentException("Unable to interact with the provided element");
        return parent.getCommand(this) + index + " ";
    }

    @Override
    public int getWidth() {
        Element furthest = elements.stream().max(Comparator.comparingInt(Element::getRight)).orElse(null);
        if (furthest == null) return 0;
        return furthest.getRight();
    }

    @Override
    public int getHeight() {
        Element furthest = elements.stream().max(Comparator.comparingInt(Element::getBottom)).orElse(null);
        if (furthest == null) return 0;
        return furthest.getBottom();
    }

    @NotNull
    @Override
    public List<Text> render(@NotNull IElementContainer context) {
        if (context != parent)
            throw new IllegalStateException("Attempted to render GroupElement with non-parent context");
        int height = getHeight();
        List<Text> lines = new ArrayList<>(height);
        for (int i = 0; i < height; i++)
            lines.add(new Text());

        for (Element element : elements) {
            if (!element.isVisible())
                continue;

            List<Text> elementTexts = element.render(this);
            for (int j = 0; j < elementTexts.size(); j++) {
                int lineY = element.getY() + j;

                if (lineY < 0 || lineY >= height)
                    continue;

                Text text = lines.get(lineY);
                text.expandToWidth(element.getX());

                Text toAdd = elementTexts.get(j);
                toAdd.expandToWidth(element.getWidth());
                text.append(toAdd);
            }
        }

        return lines;
    }

    @Override
    public void edit(@NotNull IElementContainer container, @NotNull String[] args) {
        int index = Integer.parseInt(args[0]);
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        elements.get(index).edit(container, newArgs);
    }

    @Override
    public boolean isVisible() {
        return super.isVisible();
    }

    @Override
    public void openFor(@NotNull Player player) {
        parent.openFor(player);
    }

    @Override
    public void refresh() {
        parent.refresh();
    }

    @Override
    public void cancelInnerElementsExpectedChat() {
        for (Element element : getElements()) {
            if (element instanceof ICanExpectChat) {
                ((ICanExpectChat) element).cancelExpectedChat();
            }else if (element instanceof GroupElement){
                ((GroupElement) element).cancelInnerElementsExpectedChat();
            }
        }
    }

    @Override
    public ChatMenu getChatMenu() {
        return parent == null ? null : parent.getChatMenu();
    }
}
