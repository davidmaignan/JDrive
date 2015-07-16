package org.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reader to load the properties
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class Reader {

    private String propertyFile = "/config.properties";
    private Properties propertyList;

    public Reader() throws IOException, FileNotFoundException{
        // Load client secrets.
        InputStream inputStream = this.getClass().getResourceAsStream(propertyFile);
        propertyList = new Properties();

        if (inputStream == null) {
            throw  new FileNotFoundException("No property file found");
        }

        propertyList.load(inputStream);
    }

    public String getProperty(String name) throws Exception{
        if(propertyList.containsKey(name)) {
            return propertyList.getProperty(name);
        }

        throw new Exception("No property " + name + " in the configuration");
    }
}
