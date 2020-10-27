package Main;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static Main.Main.logAdd;

public class Config {
    private final Map<String, String> logopass;
    private final String logPath;
    public boolean LOG;
    public BufferedWriter log;

    public Config(String logopassPath, String logPath) {
        this.logPath = logPath;
        this.logopass = createLogopass(logopassPath);
    }

    private Map<String, String> createLogopass (String logopassPath) {
        try {
            Map<String, String> result = new HashMap<>(15);
            BufferedReader logopassReader = new BufferedReader(new FileReader(logopassPath));
            String inline;
            while ((inline = logopassReader.readLine()) != null) {
                String[] pair = inline.split(" ");
                result.put(pair[0], pair[1]);
            }
            result.remove("");
            logopassReader.close();
            return result;
        }
        catch (Exception e) {
            logAdd("Problem with logopass file reading!");
            logAdd(e.toString());
        }
        return null;
    }

    public Map<String, String> getLogopass() {
        return logopass;
    }

    public void openLog () {
        try {
            new File(logPath).mkdirs();
            log = new BufferedWriter(new FileWriter(logPath + "/log", true));
            LOG = true;
            logAdd("Program was started at " + new Date());
        } catch (Exception e) {
            logAdd("Problem with opening log file!");
            logAdd(e.toString());
        }
    }

    public void closeLog () {
        try {
            LOG = false;
            log.close();
        } catch (IOException e) {
            logAdd("Problem with closing log file!");
            logAdd(e.toString());
        }
    }

}
