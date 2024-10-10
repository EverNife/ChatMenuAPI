package br.com.finalcraft.evernifecore.chatmenuapi.protocol.compat;

import com.comphenix.protocol.events.PacketContainer;

//Legacy ProtocoLib (1.7.10) does not have ChatType Info, as there is only a single chat
public class ChatTypeLegacy implements IChatTypeInfo{

    @Override
    public void onSetPosition(PacketContainer handle, byte value) {
        //Do Nothing
    }

    @Override
    public byte getID(PacketContainer handle) {
        return 0; //Always return 0
    }

    @Override
    public void setChatType(PacketContainer handle) {
        //Do Nothing
    }
}
