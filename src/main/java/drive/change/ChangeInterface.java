package drive.change;

import drive.change.model.ChangeStruct;

/**
 *
 *
 * Created by david on 2016-05-12.
 */
public interface ChangeInterface {
    boolean execute();
    void setStructure(ChangeStruct structure);
}
