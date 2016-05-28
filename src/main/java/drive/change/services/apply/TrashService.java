package drive.change.services.apply;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Trashed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a file or folder locally when receiving a delete change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TrashService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private CustomChange structure;
    private FileRepository fileRepository;
    private Trashed trash;

    @Inject
    public TrashService(FileRepository fileRepository, Trashed trash){
        this.fileRepository = fileRepository;
        this.trash = trash;
    }

    @Override
    public boolean execute() {
        return trash.write(getPath());
    }

    private String getPath(){
        return String.format("%s/%s",
                fileRepository.getNodeAbsolutePath(structure.getOldParentNode()),
                structure.getOldName()).replaceFirst("^/", "");
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }
}
