package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

class Logger {

    private static boolean saveLog = true;

    public enum type{
        DEBUG,
        ERROR,
        WARNING,
        INFO,
        MESSAGE
    }

    public static void debug(String message) {
        addEntry(type.DEBUG, message);
    }

    public static void error(String message) {
        addEntry(type.ERROR, message);
    }

    public static void warning(String message){
        addEntry(type.WARNING, message);
    }

    public static void info(String message){
        addEntry(type.INFO, message);
    }

    public static void message(String sender, String message){
        addEntry(type.MESSAGE, sender, message);
    }

    private static void addEntry(type type, String... args){
        String logEntry = "";
        logEntry += "[ " +  new SimpleDateFormat("h:mm a").format(new Date()) + " ] ";
        if(type != Logger.type.MESSAGE) logEntry += type.name() + ": ";
        if(type == type.MESSAGE) {
            logEntry += args[0] + ": ";
            logEntry += args[1];
        } else {
            logEntry += args[0];
        }

        if(saveLog) {
            try{
                Files.write(Paths.get(new Date().getYear() + "_" + new Date().getMonth() + "_" + new Date().getDate() + ".log"), (logEntry + '\n').getBytes());
            }catch (Exception e){
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(new Date().getYear() + "_" + new Date().getMonth() + "_" + new Date().getDate() + ".log");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                try {
                    bufferedWriter.write(logEntry + '\n');
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void setSaveLog(boolean saveLog) {
        Logger.saveLog = saveLog;
    }
}
