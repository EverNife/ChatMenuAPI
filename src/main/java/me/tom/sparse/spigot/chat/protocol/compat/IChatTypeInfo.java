package me.tom.sparse.spigot.chat.protocol.compat;

import com.comphenix.protocol.events.PacketContainer;

public interface IChatTypeInfo {

    public void onSetPosition(PacketContainer handle, byte value);

    public byte getID(PacketContainer handle);

    public void setChatType(PacketContainer handle);

}
