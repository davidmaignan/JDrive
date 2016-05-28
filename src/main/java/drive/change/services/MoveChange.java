package drive.change.services;

import com.google.inject.Inject;
import drive.change.annotations.Move;
import drive.change.model.CustomChange;
import drive.change.services.apply.ChangeServiceInterface;
import drive.change.services.update.ChangeUpdateInterface;


/**
 * Service to apply all the steps for a move change
 */
public class MoveChange implements ChangeInterface {
    private CustomChange structure;
    private ChangeServiceInterface service;
    private ChangeUpdateInterface update;

    @Inject
    public MoveChange(@Move ChangeServiceInterface service, @Move ChangeUpdateInterface update){
        this.service = service;
        this.update = update;
    }

    public void setStructure(CustomChange structure){
        this.structure = structure;
    }

    @Override
    public ChangeServiceInterface getService() {
        return service;
    }

    @Override
    public ChangeUpdateInterface getUpdate() {
        return update;
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
