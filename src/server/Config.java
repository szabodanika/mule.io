package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Config {

    static ArrayList<ConfigValue> values = new ArrayList<>();

    protected void init(Server server){
        if(!load()) generateDefault(server);
    }

    private void generateDefault(Server server){

        System.out.println("Initiating configuration file");

        addValue("hostname", "localhost","");
        addValue("port", "2929","");
        addValue("messagesPerMinute", "10","Maximum allowed number of messages sent to the server by each client in a minute.\n#If the client sends more, it will get a flooding notice and will be rejected until the next minute.");
        addValue("messagesPerMinute_blocklength", "1","");
        addValue("connectionsAttemptsPerMinute", "100", "If more connections are initiated than this value, the server will completely ignore any incoming connections");
        addValue("connectionsAttemptsPerMinute_blocklength", "5", "");
        addValue("reconnectionAttemptsPerMinute", "6","Each client can initiate connecting this many times every minute");
        addValue("reconnectionsAttemptsPerMinute_blocklength", "1","");
        addValue("autoBanEnabled", "1","Enables the firewall to block inetAddress after a certain amount of connection initiations");
        addValue("autoMuteEnabled", "1","Enables the firewall to mute inetAddress after a certain amount of messages");
        addValue("autoConnectionShutEnabled", "1","Enables the firewall to refuse all connection attempts after a certain amount of connection initiations");

        save();
        server.stop();
    }

    private boolean load(){
        Charset charset = Charset.forName("US-ASCII");
        String line = null;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("mule.conf"), charset)) {
            while ((line = reader.readLine()) != null) {
                if(!(line.length()==0 || line.startsWith("#"))){
                    values.add(new ConfigValue(line.substring(0, line.indexOf(" = ")),line.substring(line.indexOf(" = ") + 3),""));
                }
            }
            reader.close();
            System.out.println("Successfully loaded mule.conf");
            return true;
        } catch (IOException x) {
            System.out.println("Could not find mule.conf");
            return false;
        }
    }

    private void save(){
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("mule.conf"), charset)) {
            for(ConfigValue configValue : values){
                if(configValue.description.length()>0) writer.write("#" + configValue.description + "\n", 0, configValue.description.length()+2);
                writer.write(configValue.name + " = ", 0, configValue.name.length() + 3);
                writer.write(configValue.value + "\n", 0, configValue.value.length()+1);
                writer.write("\n", 0, 1);
            }
            writer.flush();
            writer.close();
            System.out.println("Successfully saved mule.conf");
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    private void addValue(String name, String value, String description){
        values.add(new ConfigValue(name, value, description));
    }

    public static int getIntValue(String name){
        int i = 0;
        try {
            for (; i < values.size(); i++) {
                if (values.get(i).name.equals(name)) return Integer.parseInt(values.get(i).value);
            }
            System.out.println("Value not found: " + name);
            return 0;
        }catch (Exception e){
            System.out.println("Invalid integer value: " + name + ": " + values.get(i).value);
            return 0;
        }
    }

    public static String getValue(String name){
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).name.equals(name)) return values.get(i).value;
        }
        System.out.println("Value not found: " + name);
        return null;
    }

}

class ConfigValue{
    String name, value, description;

    public ConfigValue(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }
}
