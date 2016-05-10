package drive.change;

import io.*;

import static drive.change.ChangeTypes.*;

/**
 * Created by david on 2016-05-05.
 */
public class ChangeFactory {
    public static NeedNameInterface getWriter(ChangeStruct structure){
        NeedNameInterface service;
        switch (structure.getType()){
            case DELETE:
                service = new DeleteService(structure);
                break;
            case MOVE:
                service = new MoveService(structure);
                break;
            case FILE_UPDATE:
                service = new FileUpdateService(structure);
                break;
            case FOLDER_UPDATE:
                service = new FolderUpdateService(structure);
                break;
            case GOOGLE_TYPE_UPDATE:
                service = new DocumentUpdateService(structure);
                break;
            case TRASHED:
                service = new TrashedService(structure);
                break;
//            case VERSION:
//                service = new VersionService(structure);
//                break;
            default:
                service = new NullWriter(structure);
                break;
        }

        return service;
    }
}
