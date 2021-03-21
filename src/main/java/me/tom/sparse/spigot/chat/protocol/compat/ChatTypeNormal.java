package me.tom.sparse.spigot.chat.protocol.compat;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;

import java.util.Arrays;

public class ChatTypeNormal implements IChatTypeInfo{

    @Override
    public void onSetPosition(PacketContainer handle, byte value){
        if (EnumWrappers.getChatTypeClass() != null) {
            Arrays.stream(EnumWrappers.ChatType.values()).filter(t -> t.getId() == value).findAny()
                    .ifPresent(t -> handle.getChatTypes().writeSafely(0, t));
        }
    }

    @Override
    public byte getID(PacketContainer handle){
        return getChatType(handle).getId();
    }

    public EnumWrappers.ChatType getChatType(PacketContainer handle) {
        return handle.getChatTypes().read(0);
    }

    @Override
    public void setChatType(PacketContainer handle) {
        handle.getChatTypes().write(0, EnumWrappers.ChatType.CHAT);
    }

}
