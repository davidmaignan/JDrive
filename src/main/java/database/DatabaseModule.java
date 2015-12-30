package database;

import com.google.inject.AbstractModule;
import database.repository.DatabaseService;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class DatabaseModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DatabaseServiceInterface.class).to(DatabaseService.class).asEagerSingleton();
    }


}
