package drive.change;

import com.google.inject.Guice;
import database.DatabaseModule;
import drive.change.model.ChangeStruct;
import drive.change.services.*;

public class ChangeFactory {
    public static ChangeServiceInterface getWriter(ChangeStruct structure){
        ChangeServiceInterface service;
        switch (structure.getType()){
            case DELETE:
                service = Guice.createInjector(new DatabaseModule()).getInstance(DeleteService.class);
                break;
            case MOVE:
                service = Guice.createInjector(new DatabaseModule()).getInstance(MoveService.class);
                break;
            case FILE_UPDATE:
                service = Guice.createInjector(new DatabaseModule()).getInstance(FileUpdateService.class);
                break;
            case FOLDER_UPDATE:
                service = Guice.createInjector(new DatabaseModule()).getInstance(FolderUpdateService.class);
                break;
            case GOOGLE_TYPE_UPDATE:
                service = Guice.createInjector(new DatabaseModule()).getInstance(DocumentUpdateService.class);
                break;
            case TRASHED:
                service = Guice.createInjector(new DatabaseModule()).getInstance(TrashedService.class);
                break;
            default:
                service = Guice.createInjector(new DatabaseModule()).getInstance(NullWriter.class);
                break;
        }

        service.setStructure(structure);

        return service;
    }
}
