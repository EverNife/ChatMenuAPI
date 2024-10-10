package me.tom.sparse.spigot.chat.console;

import br.com.finalcraft.evernifecore.consolefilter.base.BaseLog4jFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;

public class ECChatMenuConsoleFilter extends BaseLog4jFilter {

    private static boolean applied = false;

    @Override
    public Result filter(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();

        if (message.contains("issued server command: /cmapi")){
            return Result.DENY;
        }

        return Result.NEUTRAL;
    }

    public static void applyFilterIfNotAppliedYet() {
        if (applied == false){
            applied = true;
            ((Logger) LogManager.getRootLogger()).addFilter(new ECChatMenuConsoleFilter());
        }
    }

}
