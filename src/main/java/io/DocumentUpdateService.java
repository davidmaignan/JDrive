package io;

import drive.change.ChangeStruct;
import org.slf4j.Logger;

/**
 * Delete a file or folder locally when receiving a deletion change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DocumentUpdateService extends AbstractChangeService {
    public DocumentUpdateService(ChangeStruct structure){
        super(structure);
        logger.debug(this.getClass().getSimpleName().toString());
    }

    public final boolean execute() {

        return true;
    }
}
