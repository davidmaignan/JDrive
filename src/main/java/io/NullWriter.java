package io;

import drive.change.ChangeStruct;
import drive.change.NeedNameInterface;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-01-12.
 */
public class NullWriter extends AbstractChangeService {
    private static Logger logger = LoggerFactory.getLogger(NullWriter.class);

    public NullWriter(ChangeStruct structure){
        super(structure);
        logger.debug(this.getClass().getSimpleName().toString());
    }

    @Override
    public boolean execute() {
        return true;
    }
}
