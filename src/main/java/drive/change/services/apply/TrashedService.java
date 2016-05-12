package drive.change.services.apply;

import drive.change.model.ChangeStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a file or folder locally when receiving a delete change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TrashedService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private ChangeStruct structure;

    @Override
    public void setStructure(ChangeStruct structure) {
        this.structure = structure;
    }

    public final boolean execute(){
        return true;

    }
}
