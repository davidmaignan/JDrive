package io.filesystem.modules;

import com.google.inject.AbstractModule;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapper;
import io.filesystem.annotations.Real;

/**
 * Module to bind services for a change from drive api
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class FileSystemModule extends AbstractModule {
    @Override
    protected void configure() {
        //Real
        bind(FileSystemInterface.class).annotatedWith(Real.class).to(FileSystemWrapper.class);
    }
}