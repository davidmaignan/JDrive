package org.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-18.
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

    public void execute() throws IOException {
        for (ChangeInterface service : changeList) {
            service.execute();
        }
    }
}
