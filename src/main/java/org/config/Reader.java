package org.config;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.properties");
        propertyList = new Properties();

        if (inputStream == null) {
            throw  new FileNotFoundException("No property file found");
        }

        propertyList.load(inputStream);
    }

    /**
     * Get property by name
     *
     * @param name
     * @return
     * @throws Exception
     */
    public String getProperty(String name) throws Exception{
        if(propertyList.containsKey(name)) {
            return propertyList.getProperty(name);
        }

        throw new Exception("No property " + name + " in the configuration");
    }

    /**
     * Set property
     *
     * @param name
     * @param value
     */
    public void putProperty(String name, String value){
        propertyList.put(name, value);
    }

    /**
     * Write property to the configuration
     * @param name
     * @param value
     * @throws FileNotFoundException
     */
    public void writeProperty(String name, String value) throws FileNotFoundException {
        this.putProperty(name, value);

        URL configURL = this.getClass().getClassLoader().getResource("config.properties");

        PrintWriter writer = new PrintWriter(configURL.getFile());

        propertyList.list(writer);
        writer.close();
    }
}
