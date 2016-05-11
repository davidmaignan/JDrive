package drive.change.services;

import drive.change.ChangeStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a file or folder locally when receiving a deletion change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DocumentUpdateService implements DriveChangeInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private ChangeStruct structure;

    @Override
    public void setStructure(ChangeStruct structure) {
        this.structure = structure;
    }

    public final boolean execute() {
        return true;
    }
}
