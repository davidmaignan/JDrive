package drive.change.modules;

import com.google.inject.AbstractModule;
import drive.change.annotations.*;
import drive.change.services.apply.*;
import drive.change.services.update.*;

/**
 * Module to bind services for a change from drive api
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class ChangeModule extends AbstractModule {
    @Override
    protected void configure() {
        //Delete
        bind(ChangeServiceInterface.class).annotatedWith(Delete.class).to(DeleteService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(Delete.class).to(DeleteChangeUpdate.class);

        //Trash
        bind(ChangeServiceInterface.class).annotatedWith(Trash.class).to(TrashService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(Trash.class).to(TrashChangeUpdate.class);

        //Move
        bind(ChangeServiceInterface.class).annotatedWith(Move.class).to(MoveService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(Move.class).to(MoveChangeUpdate.class);

        //Null
        bind(ChangeServiceInterface.class).annotatedWith(Null.class).to(NullService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(Null.class).to(NullChangeUpdate.class);

        //File update
        bind(ChangeServiceInterface.class).annotatedWith(File.class).to(FileService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(File.class).to(FileChangeUpdate.class);

        //Folder update
        bind(ChangeServiceInterface.class).annotatedWith(Folder.class).to(FolderService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(Folder.class).to(FolderChangeUpdate.class);

        //Google mime types
        bind(ChangeServiceInterface.class).annotatedWith(Document.class).to(DocumentService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(Document.class).to(DocumentChangeUpdate.class);

        //NewFile File
        bind(ChangeServiceInterface.class).annotatedWith(NewFile.class).to(NewFileService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(NewFile.class).to(NewFileChangeUpdate.class);

        //NewFile Folder
        bind(ChangeServiceInterface.class).annotatedWith(NewFolder.class).to(NewFolderService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(NewFolder.class).to(NewFolderChangeUpdate.class);

        bind(ChangeServiceInterface.class).annotatedWith(UnTrash.class).to(UntrashService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(UnTrash.class).to(UntrashChangeUpdate.class);
    }
}