package drive.change.services;

import com.google.inject.Inject;
import drive.change.annotations.NewFolder;
import drive.change.model.CustomChange;
import drive.change.services.apply.ChangeServiceInterface;
import drive.change.services.update.ChangeUpdateInterface;

/**
 * Created by david on 2016-05-26.
 */
public class NewFolderChange implements ChangeInterface {
    private CustomChange structure;
    private ChangeServiceInterface service;
    private ChangeUpdateInterface update;

    @Inject
    public NewFolderChange(@NewFolder ChangeServiceInterface service, @NewFolder ChangeUpdateInterface update){
        this.service = service;
        this.update = update;
    }

    @Override
    public ChangeServiceInterface getService() {
        return service;
    }

    @Override
    public ChangeUpdateInterface getUpdate() {
        return update;
    }

    public void setStructure(CustomChange structure){
        this.structure = structure;
    }

    @Override
    public boolean execute() {
        service.setStructure(structure);
        update.setStructure(structure);

        boolean result = service.execute();

        if(result) {
            return update.execute();
        }

        return false;
    }
}
