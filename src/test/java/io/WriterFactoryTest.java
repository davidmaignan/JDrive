package io;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import model.types.MimeType;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by david on 2016-05-20.
 */
@RunWith(DataProviderRunner.class)
public class WriterFactoryTest {
    @Test
    public void getWriterFolder() throws Exception {
        assertTrue(WriterFactory.getWriter(MimeType.FOLDER) instanceof Folder);
    }

    @Test
    public void getWriterFile() throws Exception {
        assertTrue(WriterFactory.getWriter("PDF") instanceof File);
    }

    @DataProvider
    public static Object[][] dataProvider(){
        return new Object[][]{
                {MimeType.AUDIO, true},
                {MimeType.DOCUMENT, true},
                {MimeType.DRAWING, true},
                {MimeType.FILE, true},
                {MimeType.FOLDER, false},
                {MimeType.FORM, true},
                {MimeType.FUSIONTABLE, true},
                {MimeType.PHOTO, true},
                {MimeType.PRESENTATION, true},
                {MimeType.SCRIPTS, true},
                {MimeType.SITES, true},
                {MimeType.SPREADSHIT, true},
                {MimeType.UNKNOW, true},
                {MimeType.VIDEO, true},

        };
    }

    @Test
    @UseDataProvider("dataProvider")
    public void testMimeTypes(String mimeType, boolean expected){
        assertEquals(expected, WriterFactory.getWriter(mimeType) instanceof Document);
    }
}