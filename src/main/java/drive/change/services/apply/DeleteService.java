package drive.change.services.apply;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Delete;

/**
 * Delete a file or folder locally when receiving a delete change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DeleteService implements ChangeServiceInterface {
    private CustomChange structure;
    private FileRepository fileRepository;
    private Delete delete;

    @Inject
    public DeleteService(FileRepository fileRepository, Delete delete){
        this.fileRepository = fileRepository;
        this.delete = delete;
    }

    @Override
    public boolean execute() {
        return delete.write(getPath());
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }

    //@todo remove replaceFirst
    private String getPath(){
        return String.format("%s/%s",
                fileRepository.getNodeAbsolutePath(structure.getOldParentNode()),
                structure.getOldName()).replaceFirst("^/", "");
    }
}
