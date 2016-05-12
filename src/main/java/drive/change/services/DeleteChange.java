package drive.change.services;

import com.google.inject.Inject;
import drive.change.ChangeInterface;
import drive.change.ChangeStruct;
import drive.change.annotation.Delete;
import drive.change.services.ChangeServiceInterface;
import drive.change.services.update.ChangeUpdateInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-05-12.
 */
public class DeleteChange implements ChangeInterface {
    private static Logger logger = LoggerFactory.getLogger(DeleteChange.class);
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
