package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-12.
 */
public class ChangeFactoryService {

    public static ChangeInterface get(CustomChange structure){
        ChangeInterface service;

        switch (structure.getType()){
            case DELETE:
                service = Guice.createInjector(new ChangeModule()).getInstance(DeleteChange.class);
                break;
            case MOVE:
                service = Guice.createInjector(new ChangeModule()).getInstance(MoveChange.class);
                break;
            case FILE_UPDATE:
                service = Guice.createInjector(new ChangeModule()).getInstance(FileChange.class);
                break;
            case FOLDER_UPDATE:
                service = Guice.createInjector(new ChangeModule()).getInstance(FolderChange.class);
                break;
            case DOCUMENT:
                service = Guice.createInjector(new ChangeModule()).getInstance(DocumentChange.class);
                break;
            case TRASHED:
                service = Guice.createInjector(new ChangeModule()).getInstance(TrashChange.class);
                break;
            default:
                service = Guice.createInjector(new ChangeModule()).getInstance(NullChange.class);
                break;
        }

        service.setStructure(structure);

        return service;
    }
}
