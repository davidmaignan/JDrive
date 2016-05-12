package drive.change.services;

import com.google.inject.Guice;
import com.google.inject.Inject;
import database.DatabaseModule;
import database.repository.FileRepository;
import drive.change.model.ChangeStruct;
import io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-05-05.
 */
public class FileUpdateService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private ChangeStruct structure;
    private FileRepository fileRepository;

    @Inject
    public FileUpdateService(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public void setStructure(ChangeStruct structure) {
        this.structure = structure;
    }


    @Override
    public boolean execute() {
        File file = Guice.createInjector(new DatabaseModule()).getInstance(File.class);

        file.setFileId(fileRepository.getFileId(this.structure.getFileNode()));

        return file.write(getNewPath());
    }

    private String getNewPath(){
        return String.format("%s/%s",
                fileRepository.getNodeAbsolutePath(structure.getNewParentNode()),
                structure.getNewName());
    }
}
