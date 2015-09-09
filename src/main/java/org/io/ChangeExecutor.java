package org.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Change executor: execute recursively a list of change service
 * <p>
 * David Maignan <davidmaignan@gmail.com>
 */
public class ChangeExecutor {

    private List<ChangeInterface> changeList;
    private List<ChangeInterface> changeListFailed;

    public ChangeExecutor() {
        changeList = new ArrayList<>();
        changeListFailed = new ArrayList<>();
    }

    public void addChange(ChangeInterface service) {
        changeList.add(service);
    }

    public void clean() {
        List<ChangeInterface> listNull = new ArrayList<>();
        listNull.add(null);

        changeList.removeAll(listNull);
    }

    public int size() {
        return changeList.size();
    }

    public void debug() {
        for (ChangeInterface service : changeList) {
            System.out.println("service: " + service);
        }
    }

    public List<ChangeInterface> getChangeListFailed() {
        return changeListFailed;
    }

    /**
     * Execute list of ChangeInterface services
     *
     * @throws IOException
     */
    public void execute() throws IOException {
        for (ChangeInterface service : changeList) {
            if( ! service.execute()) {
                changeListFailed.add(service);
            }
        }
    }
}
