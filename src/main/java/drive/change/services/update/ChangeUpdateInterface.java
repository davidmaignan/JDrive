package drive.change.services.update;

import drive.change.model.CustomChange;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-12.
 */
public interface ChangeUpdateInterface {
    public boolean execute();
    public void setStructure(CustomChange structure);
}
