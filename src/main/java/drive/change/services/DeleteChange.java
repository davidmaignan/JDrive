package drive.change.services;

import com.google.inject.Inject;
import drive.change.ChangeInterface;
import drive.change.model.ChangeStruct;
import drive.change.annotations.Delete;
import drive.change.services.update.ChangeUpdateInterface;


/**
 * Service to apply all the steps for a deletion of a file/folder
 */
public class DeleteChange implements ChangeInterface {
    private ChangeStruct structure;
    private ChangeServiceInterface service;
    private ChangeUpdateInterface update;

    @Inject
    public DeleteChange(@Delete ChangeServiceInterface service, @Delete ChangeUpdateInterface update){
        this.service = service;
        this.update = update;
    }

    public void setStructure(ChangeStruct structure){
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
