package drive.change.services;

import drive.change.ChangeStruct;

/**
 * Created by david on 2016-05-05.
 */
public interface DriveChangeInterface {
    public boolean execute();
    public void setStructure(ChangeStruct structure);
}