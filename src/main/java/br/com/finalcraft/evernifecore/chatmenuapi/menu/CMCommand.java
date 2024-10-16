package br.com.finalcraft.evernifecore.chatmenuapi.menu;

import br.com.finalcraft.evernifecore.util.FCInputReader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMCommand implements CommandExecutor {
    public CMCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player == false){
            return true;
        }

        if (args.length < 2){ //Usage: /cm <menuID> <element> [...args]
            return true;
        }

        String menuID = args[0];

        if (menuID.equalsIgnoreCase("close")) {
            ChatMenuAPI.setCurrentMenu((Player) sender, null);
            return true;
        }

        ChatMenu menu = ChatMenuAPI.getMenu(menuID);
        if (menu == null){
            return true;
        }

        Integer element = FCInputReader.parseInt(args[1]);

        if (element == null){
            return true;
        }

        String[] elementArgs = new String[args.length - 2];
        System.arraycopy(args, 2, elementArgs, 0, elementArgs.length);

        if (element < 0 || element >= menu.elements.size()){
            //Menu not Found or Element not found inside this Menu
            return true;
        }

        menu.edit((Player) sender, element, elementArgs);
        return true;
    }

}
