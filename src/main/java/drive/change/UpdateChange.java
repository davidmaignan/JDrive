package drive.change;

import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.ChangeStruct;

/**
 * Created by david on 2016-05-11.
 */
public class UpdateChange {
    private FileRepository fileRepository;
    private ChangeRepository changeRepository;

    public UpdateChange(FileRepository fileRepository, ChangeRepository changeRepository){
        this.fileRepository = fileRepository;
        this.changeRepository = changeRepository;
    }

    public boolean execute(ChangeStruct structure){

        boolean result = changeRepository.markAsProcessed(structure.getChangeNode());

        switch (structure.getType()){
            case DELETE:
                result = changeRepository.markAsDeleted(structure.getChangeNode());
                result = fileRepository.markAsDeleted(structure.getFileNode());
                break;
            case MOVE:
                result = fileRepository.updateParentRelation(structure.getFileNode(), structure.getNewParentNode());
                result = fileRepository.update(structure.getFileNode(), structure.getChange().getFile());
                break;
            case TRASHED:
                result = changeRepository.markAsTrashed(structure.getChangeNode());
                result = fileRepository.markAsTrashed(structure.getFileNode());
                break;
            default:
                result = changeRepository.markAsProcessed(structure.getChangeNode());
                result = fileRepository.update(structure.getFileNode(), structure.getChange().getFile());
                break;
        }


        return result;
    }
}
