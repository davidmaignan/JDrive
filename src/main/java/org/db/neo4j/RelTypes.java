package org.db.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-21.
 */
public enum RelTypes implements RelationshipType {
    PARENT, CHILD
}
