package drive.change.services;

import drive.change.model.CustomChange;

/**
 *
 *
 * Created by david on 2016-05-12.
 */
public interface ChangeInterface {
    boolean execute();
    void setStructure(CustomChange structure);
}