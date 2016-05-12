package drive.change.services.apply;

import drive.change.model.ChangeStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-01-12.
 */
public class DocumentService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private ChangeStruct structure;

    @Override
    public void setStructure(ChangeStruct structure) {
        this.structure = structure;
    }

    @Override
    public boolean execute() {
        return true;
    }
}
