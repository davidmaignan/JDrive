package drive.change.services.update;

import drive.change.model.CustomChange;

/**
 * Created by david on 2016-05-12.
 */
public interface ChangeUpdateInterface {
    public boolean execute();
    public void setStructure(CustomChange structure);
}
