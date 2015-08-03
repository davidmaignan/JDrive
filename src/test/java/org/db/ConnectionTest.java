package org.db;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.model.tree.TreeBuilder;
import org.model.tree.TreeModule;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Test the connection to orientDB
 */
public class ConnectionTest {

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        ArrayList<AbstractModule> moduleList = new ArrayList<>();
        moduleList.add(new TreeModule());

        //@todo Mock reader object injected
        Injector injector = Guice.createInjector(moduleList);
        connection = injector.getInstance(Connection.class);
    }

    @Test
    @Ignore
    public void testGetConnection(){
        ODatabaseDocumentTx dbConnection = connection.getConnection();
        assertTrue(dbConnection instanceof ODatabaseDocumentTx);

        dbConnection.close();
    }
}