package org.db;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.configuration.Configuration;

/**
 * JDrive
 * David Maignan <davidmaignan@gmail.com>
 */
public class Database {
    private Connection connection;
    ODatabaseDocumentTx db;

    public Database(Connection connection){
        this.connection = connection;
    }

    public void createTreeNode(){
        db = new ODatabaseDocumentTx(this.connection.getDBPath()).create();

        OClass treeNode = db.getMetadata().getSchema().createClass("TreeNode");

        treeNode.createProperty(Fields.ID, OType.STRING);
        treeNode.createProperty(Fields.TITLE, OType.STRING);
        treeNode.createProperty(Fields.MIME_TYPE, OType.STRING);
        treeNode.createProperty(Fields.CREATED_DATE, OType.LONG).setNotNull(false);
        treeNode.createProperty(Fields.MODIFIED_DATE, OType.LONG).setNotNull(false);
        treeNode.createProperty(Fields.PATH, OType.STRING);
        treeNode.createProperty(Fields.PARENTS, OType.EMBEDDEDSET);
        treeNode.createProperty(Fields.IS_TRASHED, OType.BOOLEAN).setMin("0");
//
        treeNode.createIndex("IDIdx", OClass.INDEX_TYPE.UNIQUE, Fields.ID);

        db.close();
    }
}
