package drive.change.services.apply;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Move a file or directory in file system when receiving a change event from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class MoveService implements ChangeServiceInterface {
    private static Logger logger = LoggerFactory.getLogger(MoveService.class.getSimpleName());
    private CustomChange structure;
    private FileRepository fileRepository;
    private Move move;

    @Inject
    public MoveService(FileRepository fileRepository, Move move){
        this.fileRepository = fileRepository;
        this.move = move;
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }

    @Override
    public boolean execute() {
        return move.write(getOldPath(), getNewPath());
    }

    private String getOldPath(){
        return String.format("%s/%s",
                fileRepository.getNodeAbsolutePath(structure.getOldParentNode()),
                structure.getOldName()).replaceFirst("^/", "");
    }

    private String getNewPath(){
        return String.format("%s/%s",
                fileRepository.getNodeAbsolutePath(structure.getNewParentNode()),
                structure.getNewName()).replaceFirst("^/", "");
    }
}
