package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.CustomChange;

public class DocumentChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;
    private ChangeRepository changeRepository;

    @Inject
    public DocumentChangeUpdate(FileRepository fileRepository, ChangeRepository changeRepository){
        this.fileRepository = fileRepository;
        this.changeRepository = changeRepository;
    }

    @Override
    public boolean execute() {
        return changeRepository.markAsProcessed(structure.getChangeNode())
                && fileRepository.update(structure.getFileNode(), structure.getChange().getFile());

    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }
}
