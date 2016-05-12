package drive.change.services;

import drive.change.ChangeStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionService implements DriveChangeInterface {
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
