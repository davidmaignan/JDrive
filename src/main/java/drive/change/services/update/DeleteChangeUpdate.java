package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.ChangeStruct;


public class DeleteChangeUpdate implements ChangeUpdateInterface{
    private ChangeStruct structure;
    private FileRepository fileRepository;
    private ChangeRepository changeRepository;

    @Inject
    public DeleteChangeUpdate(FileRepository fileRepository, ChangeRepository changeRepository){
        this.fileRepository = fileRepository;
        this.changeRepository = changeRepository;
    }

    @Override
    public boolean execute() {
        return true;
//        return changeRepository.markAsDeleted(structure.getChangeNode()) &&
//                fileRepository.markAsDeleted(structure.getFileNode());
    }

    @Override
    public void setStructure(ChangeStruct structure) {
        this.structure = structure;
    }
}
