package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;

public class TrashChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public TrashChangeUpdate(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public boolean execute() {
        return fileRepository.markAsTrashed(structure.getFileNode());
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }
}
