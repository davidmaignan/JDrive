package drive.change.services.apply;

import drive.change.model.CustomChange;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-05.
 */
public interface ChangeServiceInterface {
    public boolean execute();
    public void setStructure(CustomChange structure);
}
