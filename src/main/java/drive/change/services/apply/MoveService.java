package drive.change.services.apply;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Move a file or directory in file system when receiving a change event from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class MoveService implements ChangeServiceInterface {
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
                structure.getOldName());
    }

    private String getNewPath(){
        return String.format("%s/%s",
                fileRepository.getNodeAbsolutePath(structure.getNewParentNode()),
                structure.getNewName());
    }
}
