package drive.change.services.apply;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Delete a file or folder locally when receiving a deletion change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FolderService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public FolderService(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }

    public final boolean execute(){
        Path path = FileSystems.getDefault().getPath(getNewPath());

        if(! Files.exists(path)){
            Folder folder =  new Folder();
            return folder.write(path.toString());
        }

        return true;
    }

    private String getNewPath(){
        return String.format("%s/%s",
                fileRepository.getNodeAbsolutePath(structure.getNewParentNode()),
                structure.getNewName());
    }
}
