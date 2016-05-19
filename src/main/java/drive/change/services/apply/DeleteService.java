package drive.change.services.apply;

import drive.change.model.CustomChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a file or folder locally when receiving a delete change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DeleteService implements ChangeServiceInterface {
    private CustomChange structure;

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }

}
