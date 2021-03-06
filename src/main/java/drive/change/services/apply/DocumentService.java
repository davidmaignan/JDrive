package drive.change.services.apply;

import drive.change.model.CustomChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-01-12.
 */
public class DocumentService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private CustomChange structure;

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }

    @Override
    public boolean execute() {
        return true;
    }
}
