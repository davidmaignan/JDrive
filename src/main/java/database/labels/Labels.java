package database.labels;

import org.neo4j.graphdb.RelationshipType;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-21.
 */
public enum Labels implements RelationshipType {
    File, Change
}
