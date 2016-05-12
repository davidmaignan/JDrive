package drive.change;

import com.google.inject.Guice;
import drive.change.model.ChangeStruct;
import drive.change.modules.ChangeModule;
import drive.change.services.DeleteChange;

/**
 * Created by david on 2016-05-12.
 */
public class ChangeFactoryService {

    public static ChangeInterface get(ChangeStruct structure){
        ChangeInterface service = null;

        switch (structure.getType()){
            default:
                service = Guice.createInjector(new ChangeModule()).getInstance(DeleteChange.class);
                break;
        }

        service.setStructure(structure);

        return service;
    }

}
