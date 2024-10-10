package br.com.finalcraft.evernifecore.chatmenuapi.protocol.compat;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;

public class ChatTypeNormal implements IChatTypeInfo{

    @Override
    public void onSetPosition(PacketContainer handle, byte value){
        if (EnumWrappers.getChatTypeClass() != null) {
            for (EnumWrappers.ChatType chatType : EnumWrappers.ChatType.values()) {
                if (chatType.getId() == value){
                    handle.getChatTypes().writeSafely(0, chatType);
                    return;
                }
            }
        }
    }

    @Override
    public byte getID(PacketContainer handle){
        return handle.getChatTypes().read(0).getId();
    }

    @Override
    public void setChatType(PacketContainer handle) {
        handle.getChatTypes().write(0, EnumWrappers.ChatType.CHAT);
    }

}
