package drive.change.services.apply;

import drive.change.model.CustomChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a file or folder locally when receiving a deletion change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DocumentUpdateService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private CustomChange structure;

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }

    public final boolean execute() {
        return true;
    }
}
