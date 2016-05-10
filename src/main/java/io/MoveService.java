package io;

import drive.change.ChangeStruct;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Move a file or directory in file system when receiving a change event from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class MoveService extends AbstractChangeService{
    public MoveService(ChangeStruct structure){
        super(structure);
        logger.debug(this.getClass().getSimpleName().toString());
    }

    @Override
    public boolean execute() {
        try {
            Path oldPath = FileSystems.getDefault().getPath(structure.getOldPath());
            Path newPath = FileSystems.getDefault().getPath(structure.getNewPath());

            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return true;

        }catch (IOException exception){
            logger.error(exception.getMessage());
        } catch (Exception exception){
            logger.error(exception.getMessage());
        }

        return false;
    }
}
