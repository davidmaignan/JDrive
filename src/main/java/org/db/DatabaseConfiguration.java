package org.db;

import com.google.inject.Inject;
import org.configuration.Configuration;

/**
 * DatabaseConfiguration
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DatabaseConfiguration {
    private Configuration configuration;

    @Inject
    public DatabaseConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Get database path
     *
     * @return String
     */
    public String getDBPath(){return String.format("%s", this.getDBName());}

    /**
     * Get database name
     *
     * @return String
     */
    private String getDBName(){
        return configuration.getProperty("dbName");
    }

    /**
     * Get database username
     *
     * @return String
     */
    private String getUsername(){
        return configuration.getProperty("dbUsername");
    }

    /**
     * Get database password
     *
     * @return String
     */
    private String getPassword(){
        return configuration.getProperty("dbPassword");
    }
}