package drive.change.services.apply;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.File;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-05.
 */
public class NewFileService implements ChangeServiceInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private CustomChange structure;
    private FileRepository fileRepository;
    private File file;

    @Inject
    public NewFileService(FileRepository fileRepository, File file){
        this.fileRepository = fileRepository;
        this.file = file;
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }


    @Override
    public boolean execute() {
        Node newFileNode = fileRepository.createIfNotExists(this.structure.getChange().getFile());

        //@todo refactor to split producer creation and producer writing content
        if(newFileNode != null){
            file.setFileId(this.structure.getChange().getFileId());
            structure.setFileNode(newFileNode);
            return file.write(fileRepository.getNodeAbsolutePath(newFileNode));
        }

        return false;
    }
}
