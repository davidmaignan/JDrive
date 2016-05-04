package io;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-01-12.
 */
public class NullWriter implements WriterInterface {
    private static Logger logger = LoggerFactory.getLogger(NullWriter.class);

    @Override
    public boolean write(String path) {
        logger.error("Writer for " + path + " is not implemented");

        return true;
    }

    @Override
    public void setNode(Node node) {

    }

    @Override
    public boolean delete(String path) {

        return false;
    }
}
