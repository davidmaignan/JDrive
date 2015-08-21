package org.db;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.api.DriveService;
import org.configuration.Configuration;

/**
 * Connection to orientDB database
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class Connection {
    private Configuration configuration;

    @Inject
    public Connection(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Get an orientDB connection
     *
     * @return ODatabaseDocumentTx
     */
    public OrientGraph getConnection() {
        return new OrientGraph(String.format("plocal:./%s", this.getDBName()));
    }

    /**
     * Get database path
     *
     * @return String
     */
    public String getDBPath(){return String.format("plocal:./%s", this.getDBName());}

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
