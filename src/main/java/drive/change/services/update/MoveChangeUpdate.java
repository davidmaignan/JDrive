package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;

public class MoveChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public MoveChangeUpdate(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public boolean execute() {
        return fileRepository.updateParentRelation(structure.getFileNode(), structure.getNewParentNode())
                && fileRepository.update(structure.getFileNode(), structure.getChange().getFile());
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }
}
