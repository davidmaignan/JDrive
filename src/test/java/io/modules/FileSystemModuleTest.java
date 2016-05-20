package io.modules;

import com.google.inject.AbstractModule;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapper;
import io.filesystem.annotations.Real;
import io.filesystem.modules.FileSystemWrapperTest;

/**
 * Module to bind services for a change from drive api
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class FileSystemModuleTest extends AbstractModule {
    @Override
    protected void configure() {
        //Real
        bind(FileSystemInterface.class).annotatedWith(Real.class).to(FileSystemWrapperTest.class);
    }
}