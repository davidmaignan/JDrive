package drive.change.services.update;

import drive.change.ChangeStruct;

/**
 * Created by david on 2016-05-12.
 */
public interface ChangeUpdateInterface {
    public boolean execute();
    public void setStructure(ChangeStruct structure);
}
