package database;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class Connection {

    private GraphDatabaseService graphDB;

    //create an object of SingleObject
    private static Connection instance = new Connection();

    //make the constructor private so that this class cannot be
    //instantiated
    private Connection(){
        graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File("db"));
    }

    //Get the only object available
    public static Connection getInstance(){
        return instance;
    }

    public GraphDatabaseService getGraphDB(){
        return graphDB;
    }
}