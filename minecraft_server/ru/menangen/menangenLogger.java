package ru.menangen;

/**
 * Created by menangen on 19.08.2015.
 */

import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;

public class menangenLogger {

    private static Logger ServerLogger = LogManager.getLogger();


    public static void infoToChat(String textToLog) {
        MinecraftServer.getServer().getConfigurationManager()
                .sendChatMsg(new ChatComponentTranslation("chat.type.emote", new Object[] { "Info" , "§e" + textToLog }));
    }

    public static void fullInfoToChat(String capital, String textToLog) {
        MinecraftServer.getServer().getConfigurationManager()
                .sendChatMsg(new ChatComponentTranslation("chat.type.emote", new Object[] { capital , "§e" + textToLog }));
    }

    public static void newToChat(String textToLog) {
        MinecraftServer.getServer().getConfigurationManager()
                .sendChatMsg(new ChatComponentTranslation("chat.type.announcement", new Object[] { "Server" , "§2" + textToLog }));
    }

    public static void warningToChat(String textToLog) {
        MinecraftServer.getServer().getConfigurationManager()
                .sendChatMsg(new ChatComponentTranslation("chat.type.announcement", new Object[] { "Server" , "§c" + textToLog }));
    }

    public static void infoToConsole(String textToLog) {
        ServerLogger.info(textToLog);
    }

    public static void stackClassToConsole(int deep) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        ServerLogger.info(stackTraceElements[deep].getClass());
    }

    public static void stackMethodToConsole(int deep) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        ServerLogger.info(stackTraceElements[deep].getMethodName());
    }
}