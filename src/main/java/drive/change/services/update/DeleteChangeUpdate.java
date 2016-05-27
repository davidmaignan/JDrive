package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.CustomChange;


public class DeleteChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public DeleteChangeUpdate(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public boolean execute() {
        return fileRepository.markAsDeleted(structure.getFileNode());
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }
}
