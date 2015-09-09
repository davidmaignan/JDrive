package org.db;

import com.google.common.eventbus.AsyncEventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.db.neo4j.DatabaseService;

import java.util.concurrent.Executor;
import java.util.concurrent.*;

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
