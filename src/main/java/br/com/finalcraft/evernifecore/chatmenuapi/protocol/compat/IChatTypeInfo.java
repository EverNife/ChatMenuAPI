package br.com.finalcraft.evernifecore.chatmenuapi.protocol.compat;

import com.comphenix.protocol.events.PacketContainer;

public interface IChatTypeInfo {

    public void onSetPosition(PacketContainer handle, byte value);

    public byte getID(PacketContainer handle);

    public void setChatType(PacketContainer handle);

}
