package drive.change.services;

import com.google.inject.Inject;
import drive.change.annotations.Document;
import drive.change.model.CustomChange;
import drive.change.services.apply.ChangeServiceInterface;
import drive.change.services.update.ChangeUpdateInterface;


/**
 * Service to apply all the steps for a document change
 */
public class DocumentChange implements ChangeInterface {
    private CustomChange structure;
    private ChangeServiceInterface service;
    private ChangeUpdateInterface update;

    @Inject
    public DocumentChange(@Document ChangeServiceInterface service, @Document ChangeUpdateInterface update){
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
