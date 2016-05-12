package drive.change.services.apply;

import drive.change.model.ChangeStruct;

/**
 * Created by david on 2016-05-05.
 */
public interface ChangeServiceInterface {
    public boolean execute();
    public void setStructure(ChangeStruct structure);
}
