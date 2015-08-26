package org.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Change executor: execute recursively a list of change service
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class ChangeExecutor {

    private List<ChangeInterface> changeList;

    public ChangeExecutor() {
        changeList = new ArrayList<>();
    }

    public void addChange(ChangeInterface service) {
        changeList.add(service);
    }

    public void clean(){
        List<ChangeInterface> listNull = new ArrayList<>();
        listNull.add(null);

        changeList.removeAll(listNull);
    }

    public int size(){
        return changeList.size();
    }

    public void debug(){
        for (ChangeInterface service : changeList) {
            System.out.println("service: " + service);
        }
    }

    /**
     * Execute list of ChangeInterface services
     *
     * @throws IOException
     */
    public void execute() throws IOException {
        while(changeList.size() > 0) {
            for (ChangeInterface service : changeList) {
                if (service.execute()) {
                    changeList.remove(service);
                }
            }
        }
    }
}
