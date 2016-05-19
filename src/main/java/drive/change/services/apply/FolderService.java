package drive.change.services.apply;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Folder;

/**
 * Create a file or folder from a change request
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FolderService implements ChangeServiceInterface {
    private CustomChange structure;
    private FileRepository fileRepository;
    private Folder folder;

    @Inject
    public FolderService(FileRepository fileRepository, Folder folder){
        this.fileRepository = fileRepository;
        this.folder = folder;
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }

    @Override
    public boolean execute(){
        return folder.write(getNewPath());
    }

    private String getNewPath(){
        return String.format("%s/%s",
                fileRepository.getNodeAbsolutePath(structure.getNewParentNode()),
                structure.getNewName());
    }
}
