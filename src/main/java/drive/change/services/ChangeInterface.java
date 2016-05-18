package drive.change.services;

import drive.change.model.CustomChange;
import drive.change.services.apply.ChangeServiceInterface;
import drive.change.services.update.ChangeUpdateInterface;

/**
 *
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-12.
 */
public interface ChangeInterface {
    boolean execute();
    void setStructure(CustomChange structure);
    ChangeServiceInterface getService();
    ChangeUpdateInterface getUpdate();
}
