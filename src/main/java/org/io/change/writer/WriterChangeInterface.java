package org.io.change.writer;

import com.google.api.services.drive.model.Change;

/**
 * Writer change Interface
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public interface WriterChangeInterface {
    public boolean write(Change change);
}
