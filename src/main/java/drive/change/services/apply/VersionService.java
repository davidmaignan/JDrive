package drive.change.services.apply;

import drive.change.model.ChangeStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionService implements ChangeServiceInterface {
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
