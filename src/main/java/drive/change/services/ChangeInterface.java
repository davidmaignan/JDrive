package drive.change.services;

import drive.change.model.CustomChange;

/**
 *
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-12.
 */
public interface ChangeInterface {
    boolean execute();
    void setStructure(CustomChange structure);
}
