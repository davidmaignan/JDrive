package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.CustomChange;


public class DeleteChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;
    private ChangeRepository changeRepository;

    @Inject
    public DeleteChangeUpdate(FileRepository fileRepository, ChangeRepository changeRepository){
        this.fileRepository = fileRepository;
        this.changeRepository = changeRepository;
    }

    @Override
    public boolean execute() {
        return changeRepository.markAsProcessed(structure.getChangeNode())
                && changeRepository.markAsDeleted(structure.getChangeNode())
                && fileRepository.markAsDeleted(structure.getFileNode());
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }
}
