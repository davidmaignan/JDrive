package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import io.filesystem.modules.FileSystemModule;


/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-12.
 */
public class ChangeFactoryService {

    public static ChangeInterface get(CustomChange structure){
        ChangeInterface service;

        switch (structure.getType()){
            case DELETE:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(DeleteChange.class);
                break;
            case MOVE:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(MoveChange.class);
                break;
            case FILE_UPDATE:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(FileChange.class);
                break;
            case FOLDER_UPDATE:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(FolderChange.class);
                break;
            case DOCUMENT:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(DocumentChange.class);
                break;
            case TRASHED:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(TrashChange.class);
                break;
            case NEW_FILE:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(NewFileChange.class);
                break;
            case NEW_FOLDER:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(NewFolderChange.class);
                break;
            case UNTRASHED:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(UntrashChange.class);
                break;
            default:
                service = Guice.createInjector(
                        new ChangeModule(),
                        new FileSystemModule()).getInstance(NullChange.class);
                break;
        }

        service.setStructure(structure);

        return service;
    }
}
