package org.db;

import com.google.inject.Guice;
import org.db.neo4j.DatabaseModule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseConfigurationTest {

    private DatabaseConfiguration dbConfig;

    @Before
    public void setUp() throws Exception {
        dbConfig = Guice.createInjector(new DatabaseModule()).getInstance(DatabaseConfiguration.class);
    }

    @Test
    public void testGetDBPath(){
        assertEquals("db_test", dbConfig.getDBPath());
    }

    @Test
    public void testGetDBName(){
        assertEquals("db_test", dbConfig.getDBPath());
    }

    @Test
    @Ignore
    public void testGetDBUsername(){
        //@todo to implement
    }

    @Test
    @Ignore
    public void testGetDBPassword(){
        //@todo to implement
    }
}