package org.configuration;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * Configuration to load the properties
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class Configuration {
    private String propertyFile = "config.properties";
    private Properties propertyList;

    public Configuration() throws IOException{
        // Load client secrets.

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propertyFile);
        propertyList = new Properties();

        if (inputStream == null) {
            throw  new FileNotFoundException("No property file found");
        }

        propertyList.load(inputStream);
    }

    public String getRootFolder(){
        return this.getProperty("rootFolder");
    }

    public String getRootDirSystem(){
        return this.getProperty("rootDirSystem");
    }

    /**
     * Get property by name
     *
     * @param name
     * @return
     */
    public String getProperty(String name){
        if(propertyList.containsKey(name)) {
            return propertyList.getProperty(name);
        }

        return null;
    }

    /**
     * Set String property
     *
     * @param name
     * @param value
     */
    public void putProperty(String name, String value){
        propertyList.put(name, value);
    }

    /**
     * Set Long property
     *
     * @param name
     * @param value
     */
    public void putProperty(String name, Long value){
        propertyList.put(name, value);
    }

    /**
     * Write String property to the configuration
     * @param name
     * @param value
     * @throws FileNotFoundException
     */
    public void writeProperty(String name, String value) throws FileNotFoundException{
        this.putProperty(name, value);
        this.write();
    }

    /**
     * Write Long property to the configuration
     * @param name
     * @param value
     * @throws FileNotFoundException
     */
    public void writeProperty(String name, Long value) throws FileNotFoundException {
        this.putProperty(name, String.valueOf(value));
        this.write();
    }

    private void write() throws FileNotFoundException{
        URL configURL = this.getClass().getClassLoader().getResource(propertyFile);

        PrintWriter writer = new PrintWriter(configURL.getFile());

        propertyList.list(writer);
        writer.close();
    }
}
