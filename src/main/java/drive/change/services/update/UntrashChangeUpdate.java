package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;

public class UntrashChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public UntrashChangeUpdate(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public boolean execute() {
        if(structure.getOldParentNode() != structure.getNewParentNode()) {
            return fileRepository.updateParentRelation(structure.getFileNode(), structure.getNewParentNode()) &&
                    fileRepository.update(structure.getFileNode(), structure.getChange().getFile());
        }

        return fileRepository.update(structure.getFileNode(), structure.getChange().getFile());
    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }
}
