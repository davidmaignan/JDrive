package org.writer;

import com.google.inject.AbstractModule;

/**
 * Writer Module
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WriterInterface.class).to(FileWriter.class);
    }
}