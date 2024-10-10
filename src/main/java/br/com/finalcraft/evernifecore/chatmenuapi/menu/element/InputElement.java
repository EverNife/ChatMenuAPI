package br.com.finalcraft.evernifecore.chatmenuapi.menu.element;

import br.com.finalcraft.evernifecore.chatmenuapi.listeners.CMListener;
import br.com.finalcraft.evernifecore.chatmenuapi.listeners.expectedchat.ExpectedChat;
import br.com.finalcraft.evernifecore.chatmenuapi.menu.ChatMenuAPI;
import br.com.finalcraft.evernifecore.chatmenuapi.menu.IElementContainer;
import br.com.finalcraft.evernifecore.chatmenuapi.util.State;
import br.com.finalcraft.evernifecore.chatmenuapi.util.Text;
import br.com.finalcraft.evernifecore.fancytext.FancyText;
import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class InputElement extends Element {
    @NotNull
    private final State<String> value;
    private final FancyText tooLong;

    protected int width;
    private boolean editing;
    private transient ExpectedChat expectedChat;

    private static final FancyText DEFAULT_TOO_LONG = new FancyText("§4Too long")
            .setHoverText("The text is too long to fit!" +
                    "\n" +
                    "\nCurrent Value: §e%value%"
            );

    @FCLocale(lang = LocaleType.EN_US,
            text = "§4Too long",
            hover = "The text is too long to fit!" +
                    "\n" +
                    "\nCurrent Value: §e%value%"
    )
    @FCLocale(lang = LocaleType.PT_BR,
            text = "§4Muito longo",
            hover = "O texto é muito longo para caber!" +
                    "\n" +
                    "\nValor Atual: §e%value%"
    )
    private static LocaleMessage TOO_LONG;

    /**
     * Constructs a new {@code InputElement}
     *
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param width the max width of the text
     * @param value the starting text
     */
    public InputElement(int x, int y, int width, @NotNull String value) {
        super(x, y);
        this.width = width;
        this.value = new State<>(value);
        this.tooLong = null;
    }

    /**
     * Constructs a new {@code InputElement}
     *
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param width the max width of the text
     * @param value the starting text
     * @param tooLong the text to display if the text is too long
     */
    public InputElement(int x, int y, int width, @NotNull String value, FancyText tooLong) {
        super(x, y);
        this.width = width;
        this.value = new State<>(value);
        this.tooLong = tooLong;
    }

    /**
     * @return the current value
     */
    @Nullable
    public String getValue() {
        return value.getCurrent();
    }

    /**
     * Sets the text of this element, if the text is longer than the max width it will display "Too long"
     *
     * @param value the new value
     */
    public void setValue(@NotNull String value) {
//		if(ChatMenuAPI.getWidth(text) > width)
//			throw new IllegalArgumentException("The provided text is too wide to fit!");
        this.value.setCurrent(value);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return 1;
    }

    @NotNull
    public List<Text> render(@NotNull IElementContainer context) {
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, context.getCommand(this));

        String current = value.getOptionalCurrent().orElse("");
        boolean contentIsTooLong = ChatMenuAPI.getWidth(current) > width;

        Text text;

        if (contentIsTooLong){
            FancyText tooLongFancyText = null;

            if (contentIsTooLong){
                if (this.tooLong != null) {
                    tooLongFancyText = this.tooLong.clone().replace("%value%", current);
                } else if (TOO_LONG != null) {
                    tooLongFancyText = TOO_LONG.getDefaultFancyText().clone().replace("%value%", current);
                }else {
                    tooLongFancyText = DEFAULT_TOO_LONG.clone().replace("%value%", current);
                }
            }

            text = new Text(tooLongFancyText);
        }else {
            text = new Text(current);
        }
        text.expandToWidth(width);

        text.getComponents().forEach(it -> {
            if (editing){
                it.setColor(ChatColor.GRAY);
            }
            it.setUnderlined(true);
            it.setClickEvent(click);
        });

        return Collections.singletonList(text);
    }

    public boolean onClick(@NotNull IElementContainer container, @NotNull Player player) {
        super.onClick(container, player);

        for (Element element : container.getElements()) {
            if (element instanceof InputElement && element != this) {
                //Disable editing on all other elements on the same container
                ((InputElement) element).editing = false;
            }
        }

        editing = !editing;

        if (expectedChat != null) {
            expectedChat.cancel();
            expectedChat = null;
        }

        expectedChat = ChatMenuAPI.getChatListener().expectPlayerChat(player, (message) -> {

            editing = false;
            setValue(message);
            container.refresh();

            return CMListener.IChatAction.ActionResult.SUCCESS_AND_CANCEL_CHAT_EVENT;
        });

        return true;
    }

    public void edit(@NotNull IElementContainer container, @NotNull String[] args) {

    }

    @NotNull
    public List<State<?>> getStates() {
        return Collections.singletonList(value);
    }
}
