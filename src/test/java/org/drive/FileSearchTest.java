package org.drive;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.model.tree.TreeModule;


import java.util.ArrayList;

public class FileSearchTest {

    private FileSearch fileSearch;

    @Before
    public void setUp(){
        ArrayList<AbstractModule> moduleList = new ArrayList<>();
        moduleList.add(new TreeModule());

        //@todo Mock reader object injected
        Injector injector = Guice.createInjector(moduleList);
        fileSearch = injector.getInstance(FileSearch.class);
    }

//    @Test(timeout = 1000)
//    public void testPath(){
//
//        System.out.println(fileSearch.getAbsolutePath("file22.txt"));
//    }

}