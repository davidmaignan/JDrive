package drive.change.services.update;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;

public class FileChangeUpdate implements ChangeUpdateInterface{
    private CustomChange structure;
    private FileRepository fileRepository;

    @Inject
    public FileChangeUpdate(FileRepository fileRepository){
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
