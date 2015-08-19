package org.io;

import com.google.api.services.drive.model.Change;

import java.io.IOException;

/**
 * Change interface implements execute for command pattern
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public interface ChangeInterface {
    public void execute() throws IOException;
    public void setChange(Change change);
}
