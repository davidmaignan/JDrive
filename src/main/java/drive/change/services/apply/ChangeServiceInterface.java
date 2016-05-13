package drive.change.services.apply;

import drive.change.model.CustomChange;

/**
 * Created by david on 2016-05-05.
 */
public interface ChangeServiceInterface {
    public boolean execute();
    public void setStructure(CustomChange structure);
}
