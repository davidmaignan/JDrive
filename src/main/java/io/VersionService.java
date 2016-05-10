package io;

import drive.change.ChangeStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-01-12.
 */
public class VersionService extends AbstractChangeService {
    private static Logger logger = LoggerFactory.getLogger(VersionService.class);

    public VersionService(ChangeStruct structure){
        super(structure);
        logger.debug(this.getClass().getSimpleName().toString());
    }

    @Override
    public boolean execute() {

        //Check mimeType and update content

        return true;
    }
}
