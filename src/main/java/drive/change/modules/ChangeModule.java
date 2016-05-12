package drive.change.modules;

import com.google.inject.AbstractModule;
import drive.change.annotation.Trash;
import drive.change.services.ChangeServiceInterface;
import drive.change.annotation.Delete;
import drive.change.services.DeleteService;
import drive.change.services.update.ChangeUpdateInterface;
import drive.change.services.update.DeleteChangeUpdate;

/**
 * Module to bind services for a change from drive api
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class ChangeModule extends AbstractModule {
    @Override
    protected void configure() {
        //Delete
        bind(ChangeServiceInterface.class).annotatedWith(Delete.class).to(DeleteService.class);
        bind(ChangeUpdateInterface.class).annotatedWith(Delete.class).to(DeleteChangeUpdate.class);

    }
}
