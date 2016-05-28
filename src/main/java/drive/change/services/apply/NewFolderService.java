package drive.change.services.apply;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Folder;
import org.neo4j.graphdb.Node;

/**
 * Create a file or folder from a change request
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class NewFolderService implements ChangeServiceInterface {
    private CustomChange structure;
    private FileRepository fileRepository;
    private Folder folder;

    @Inject
    public NewFolderService(FileRepository fileRepository, Folder folder){
        this.fileRepository = fileRepository;
        this.folder = folder;
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }

    @Override
    public boolean execute(){
        Node newFileNode = fileRepository.createIfNotExists(this.structure.getChange().getFile());

        if(newFileNode != null) {
            structure.setFileNode(newFileNode);
            return folder.write(fileRepository.getNodeAbsolutePath(newFileNode));
        }

        return false;
    }
}
