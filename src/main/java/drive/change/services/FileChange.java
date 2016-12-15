package drive.change.services;

import com.google.inject.Inject;
import drive.change.annotations.File;
import drive.change.model.CustomChange;
import drive.change.services.apply.ChangeServiceInterface;
import drive.change.services.update.ChangeUpdateInterface;


/**
 * Service to apply all the steps for a producer change
 */
public class FileChange implements ChangeInterface {
    private CustomChange structure;
    private ChangeServiceInterface service;
    private ChangeUpdateInterface update;

    @Inject
    public FileChange(@File ChangeServiceInterface service, @File ChangeUpdateInterface update){
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

        if(result)
            return update.execute();

        return false;
    }
}
