package io;

import com.google.inject.Guice;
import database.DatabaseModule;
import database.repository.FileRepository;
import drive.change.ChangeStruct;
import drive.change.NeedNameInterface;
import org.neo4j.graphdb.Node;

/**
 * Created by david on 2016-05-05.
 */
public class FileUpdateService extends AbstractChangeService {

    public FileUpdateService(ChangeStruct structure) {
        super(structure);
        logger.debug(this.getClass().getSimpleName().toString());
    }

    @Override
    public boolean execute() {
        File file = Guice.createInjector(new DatabaseModule()).getInstance(File.class);

        FileRepository fileRepository = Guice.createInjector(new DatabaseModule()).getInstance(FileRepository.class);

        Node fileNode = fileRepository.getNodeById(structure.getChange().getFileId());

        file.setNode(fileNode);

        return file.write(structure.getNewPath());
    }
}
