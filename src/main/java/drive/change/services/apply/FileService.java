package drive.change.services.apply;

import com.google.inject.Guice;
import com.google.inject.Inject;
import database.DatabaseModule;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-05.
 */
public class FileService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public FileService(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public void setStructure(CustomChange structure) {
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
