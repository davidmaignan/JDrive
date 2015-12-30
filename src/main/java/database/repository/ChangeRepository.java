package database.repository;

import com.google.api.services.drive.model.Change;
import database.Fields;
import database.RelTypes;
import org.configuration.Configuration;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2015-12-30.
 */
public class ChangeRepository extends DatabaseService {

    private static Logger logger = LoggerFactory.getLogger(ChangeRepository.class);

    public ChangeRepository(GraphDatabaseService graphDb, Configuration configuration) {
        super(graphDb, configuration);
    }

    public Node getChangeById(long value) {
        Node node = null;

        String query = "match (change:Change) where change.identifier=%s return change" ;

        try (Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, value));

            tx.success();

            if(result.hasNext()) {
                return (Node) result.next().get("change");
            }

        } catch (Exception exception) {
            logger.error("Fail to get change: " + value);
            logger.error(exception.getMessage());
        }

        return node;
    }

    public boolean addChange(Change change) {
        try (Transaction tx = graphDB.beginTx()) {

            Node changeNode = this.getChangeById(change.getId());

            if(changeNode != null) {
                return false;
            }

            changeNode = graphDB.createNode();
            changeNode.addLabel(DynamicLabel.label("Change"));

            changeNode.setProperty(Fields.ID, change.getId());
            changeNode.setProperty(Fields.FILE_ID, change.getFileId());
            changeNode.setProperty(Fields.MODIFIED_DATE, change.getModificationDate().getValue());
            changeNode.setProperty(Fields.SELF_LINK, change.getSelfLink());
            changeNode.setProperty(Fields.DELETED, change.getDeleted());
            changeNode.setProperty(Fields.PROCESSED, false);

            Node fileNode     = this.getNodeById(change.getFileId());
            Node relationNode = this.getLastNodeOfTheQueueChange(change.getFileId());

            relationNode = (relationNode == null) ? fileNode : relationNode;

            changeNode.createRelationshipTo(relationNode, RelTypes.CHANGE);

            tx.success();

            return true;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }
}
