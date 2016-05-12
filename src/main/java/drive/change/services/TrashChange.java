package drive.change.services;

import com.google.inject.Inject;
import drive.change.annotations.Move;
import drive.change.annotations.Trash;
import drive.change.model.ChangeStruct;
import drive.change.services.apply.ChangeServiceInterface;
import drive.change.services.update.ChangeUpdateInterface;


/**
 * Service to apply all the steps for a trash change
 */
public class TrashChange implements ChangeInterface {
    private ChangeStruct structure;
    private ChangeServiceInterface service;
    private ChangeUpdateInterface update;

    @Inject
    public TrashChange(@Trash ChangeServiceInterface service, @Trash ChangeUpdateInterface update){
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
