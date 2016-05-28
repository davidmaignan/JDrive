package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;

public class NullChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public NullChangeUpdate(FileRepository fileRepository){
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
