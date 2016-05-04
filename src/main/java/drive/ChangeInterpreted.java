package drive;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.Fields;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import io.DeleteService;
import org.api.change.ChangeService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-05-04.
 */
public class ChangeInterpreted {

    private FileRepository fileRepository;
    private ChangeRepository changeRepository;
    private ChangeService changeService;
    private Node changeNode;
    private Node fileNode;
    private Change change;
    private String fileName;
    private String path;
    private String newPath;


    private static Logger logger = LoggerFactory.getLogger(ChangeInterpreted.class);

    @Inject
    public ChangeInterpreted(FileRepository fileRepository, ChangeRepository changeRepository, ChangeService changeService){
        this.fileRepository = fileRepository;
        this.changeRepository = changeRepository;
        this.changeService = changeService;
    }

    public void setChange(Node changeNode){
        this.changeNode = changeNode;
    }

    public boolean execute(Node changeNode){
        this.setChange(changeNode);

        String changeId = changeRepository.getId(changeNode);

        change = changeService.get(changeId);

        if(change == null){
            return changeRepository.delete(changeNode);
        }

        fileNode = fileRepository.getFileNodeFromChange(changeNode);

        if(fileNode == null){
            return changeRepository.update(change);
        }

        Long changeVersion = changeRepository.getVersion(changeNode);
        Long fileVersion = fileRepository.getVersion(fileNode);

        if(changeVersion.equals(fileVersion)){
            return changeRepository.update(change);
        }


        //Check if deleted
        boolean deleted = changeRepository.getTrashed(change);

        if(deleted) {
            DeleteService service = new DeleteService(path);

            boolean result = service.execute();

            if(result){
                return changeRepository.update(change);
            }
        }

        //Check if moved




        //Check if new content


        return false;
    }
}
