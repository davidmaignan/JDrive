package org.main;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.config.Reader;
import org.drive.ChangeService;
import org.drive.FileService;
import org.model.tree.TreeBuilder;
import org.model.tree.TreeModule;
import org.model.tree.TreeWriter;

import java.io.IOException;
import java.util.*;

/**
 * JDriveMain class
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class JDriveMain {

    /**
     * Main
     *
     * @param args
     * @throws IOException
     * @throws Throwable
     */
    public static void main(String[] args) throws IOException, Throwable {
        ArrayList<AbstractModule> moduleList = new ArrayList<>();
        moduleList.add(new TreeModule());

        Injector injector       = Guice.createInjector(moduleList);
        TreeBuilder treeBuilder = injector.getInstance(TreeBuilder.class);
        FileService fileService = injector.getInstance(FileService.class);

        ChangeService changeService = injector.getInstance(ChangeService.class);

        List<Change> changeList = changeService.getAll(null);

        System.out.println(changeList);

//        List<File> result = fileService.getAll();
//
//        treeBuilder.build(result);
//        TreeBuilder.printTree(treeBuilder.getRoot());
//
//        Boolean writeSuccess = new TreeWriter().writeTree(treeBuilder.getRoot());

//        MonitorService monitorService = injector.getInstance(MonitorService.class);
//        monitorService.start();
    }

    private static Reader setConfiguration() throws IOException{
        Reader configReader = new Reader();

        return configReader;
    }
}