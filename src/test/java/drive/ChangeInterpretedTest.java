package drive;

import com.google.api.services.drive.model.Change;
import database.Fields;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import org.api.change.ChangeService;
import org.configuration.Configuration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.core.NodeProxy;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.neo4j.graphdb.Node;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-04.
 */
public class ChangeInterpretedTest {
    private ChangeRepository spyChangeRepository;
    private FileRepository spyFileRepository;
    private ChangeService spyChangeService;

    private ChangeInterpreted service;

    private static Logger logger;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger("DatabaseServiceTest");
    }

    @Before
    public void setUp() throws Exception {
        spyChangeRepository = mock(ChangeRepository.class);
        spyFileRepository = mock(FileRepository.class);
        spyChangeService = mock(ChangeService.class);


        service = new ChangeInterpreted(spyFileRepository, spyChangeRepository, spyChangeService);
    }

    @Test(timeout = 10000)
    public void testChangeNotExists() throws Exception {
        Node spyNode = mock(Node.class);

        when(spyChangeRepository.getId(spyNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(null);
        when(spyChangeRepository.delete(spyNode)).thenReturn(true);

        assertTrue(service.execute(spyNode));
    }

    @Test(timeout = 10000)
    public void testFileNodeNotExists() throws Exception {
        Node spyNode = mock(Node.class);
        Change change = new Change();

        when(spyChangeRepository.getId(spyNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(spyNode)).thenReturn(null);
        when(spyChangeRepository.update(change)).thenReturn(true);

        assertTrue(service.execute(spyNode));
    }

    @Test(timeout = 10000)
    public void testSameVersionBetweenChangeAndFile() throws Exception {
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Change change = new Change();

        Long changeVersion = new Long(100l);
        Long fileVersion = new Long(100l);


        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(changeNode)).thenReturn(fileNode);

        when(spyChangeRepository.getVersion(changeNode)).thenReturn(changeVersion);
        when(spyFileRepository.getVersion(fileNode)).thenReturn(fileVersion);

        when(spyChangeRepository.update(change)).thenReturn(true);

        assertTrue(service.execute(changeNode));
    }

    @Test(timeout = 10000)
    public void testDeleteChange() throws Exception {
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Change change = new Change();
        change.setDeleted(true);

        Long changeVersion = new Long(100l);
        Long fileVersion = new Long(101l);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(changeNode)).thenReturn(fileNode);

        when(spyChangeRepository.getVersion(changeNode)).thenReturn(changeVersion);
        when(spyFileRepository.getVersion(fileNode)).thenReturn(fileVersion);

        when(spyChangeRepository.getTrashed(change)).thenReturn(true);

//        assertTrue(service.execute(changeNode));
    }


}