package com.freelancer.om.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


/**
 * @author Oliver
 * All configuration loaded from properties file
 */
public class ConfigManagement {
	
    private static class SingletonHolder {
        private static final ConfigManagement INSTANCE = new ConfigManagement();
    }
    private Properties props;
    
    private void readFile() {
        InputStream fileinputstream = ConfigManagement.class.getClassLoader().getResourceAsStream("/config/config.properties");
        try {
            props = new Properties();
            props.load(new InputStreamReader(fileinputstream, "utf-8"));
        } catch (Exception e) {
        	System.out.println("No config file found!");
        }
    }
    
	private ConfigManagement () {
		readFile();
    }

    public static final ConfigManagement getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Properties getProps() {
		return props;
	}
    
    public void refreshConfig() {
    	props.clear();
    	readFile();
    }
    
}
