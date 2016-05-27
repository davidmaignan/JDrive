package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.CustomChange;

public class FolderChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public FolderChangeUpdate(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Override
    public boolean execute() {
        return fileRepository.update(structure.getFileNode(), structure.getChange().getFile());

    }

    @Override
    public void setStructure(CustomChange structure) {
        this.structure = structure;
    }
}
