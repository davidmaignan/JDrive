package io;

import io.filesystem.modules.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Created by david on 2016-05-19.
 */
public class DocumentTest {
    private static Logger logger = LoggerFactory.getLogger(FolderTest.class.getSimpleName());
    Document document;
    FileSystemWrapperTest fs;

    @Before
    public void setUp(){
        fs = new FileSystemWrapperTest();

        document = new Document(fs);
    }

    @Test
    public void write() throws Exception {

    }

    @Test
    public void write1() throws Exception {

    }

    @Test
    public void setFileId() throws Exception {

    }

}