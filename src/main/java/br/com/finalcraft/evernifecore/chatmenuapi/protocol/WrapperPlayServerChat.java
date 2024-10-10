/**
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets Copyright (C) dmulloy2 <http://dmulloy2.net> Copyright (C)
 * Kristian S. Strangeland
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package br.com.finalcraft.evernifecore.chatmenuapi.protocol;

import br.com.finalcraft.evernifecore.chatmenuapi.protocol.compat.ChatTypeLegacy;
import br.com.finalcraft.evernifecore.chatmenuapi.protocol.compat.ChatTypeNormal;
import br.com.finalcraft.evernifecore.chatmenuapi.protocol.compat.IChatTypeInfo;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class WrapperPlayServerChat extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.CHAT;
    public static IChatTypeInfo CHAT_TYPE_INFO;
    static {
        try {
            CHAT_TYPE_INFO = new ChatTypeNormal(); //On legacy, this will throw an error
        }catch (Throwable ignored){
            CHAT_TYPE_INFO = new ChatTypeLegacy();
        }
    }

    public WrapperPlayServerChat() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerChat(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Retrieve the chat message.
     * <p>
     * Limited to 32767 bytes
     *
     * @return The current message
     */
    public WrappedChatComponent getMessage() {
        return handle.getChatComponents().read(0);
    }

    /**
     * Set the message.
     *
     * @param value - new value.
     */
    public void setMessage(WrappedChatComponent value) {
        handle.getChatComponents().write(0, value);
    }

    public void setChatType() {
        CHAT_TYPE_INFO.setChatType(handle);
    }

    /**
     * Retrieve Position.
     * <p>
     * Notes: 0 - Chat (chat box) ,1 - System Message (chat box), 2 - Above action bar
     *
     * @return The current Position
     * @deprecated Magic values replaced by enum
     */
    @Deprecated
    public byte getPosition() {
        Byte position = handle.getBytes().readSafely(0);
        if (position != null) {
            return position;
        } else {
            return CHAT_TYPE_INFO.getID(handle);
        }
    }

    /**
     * Set Position.
     *
     * @param value - new value.
     * @deprecated Magic values replaced by enum
     */
    @Deprecated
    public void setPosition(byte value) {
        handle.getBytes().writeSafely(0, value);

        CHAT_TYPE_INFO.onSetPosition(handle, value);
    }
}
