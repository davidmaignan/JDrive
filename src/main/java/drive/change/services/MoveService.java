package drive.change.services;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.ChangeStruct;
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
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private ChangeStruct structure;
    private FileRepository fileRepository;

    @Inject
    public MoveService(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public void setStructure(ChangeStruct structure) {
        this.structure = structure;
    }

    @Override
    public boolean execute() {
        try {
            Path oldPath = FileSystems.getDefault().getPath(getOldPath());
            Path newPath = FileSystems.getDefault().getPath(getNewPath());

            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return true;

        }catch (IOException exception){
            logger.error(exception.getMessage());
        } catch (Exception exception){
            logger.error(exception.getMessage());
        }

        return false;
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
