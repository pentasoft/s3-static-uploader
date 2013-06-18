package io.pst.mojo.s3.sta.uploader.config;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ConstrainedFilesListBuilderTest extends AbstractFilesListBuilderTest {

    private ConstrainedFilesListBuilder builder;
    
    @Before
    public void setUp() {
        builder = new ConstrainedFilesListBuilder(new File(ROOT_FOLDER), buildExcludeList(), buildMetadataList());
    }    

    @Test
    public void testsExcludedFiles() throws IOException {
        List<ManagedFile> expectedList = buildExpectedManagedFilesList();
        
        List<ManagedFile> builtList = builder.build(new StaticInclude());

        // Both lists must be equal, but order may differ
        assertTrue(builtList.containsAll(expectedList));
        assertTrue(expectedList.containsAll(builtList));
    }

    private List<ManagedFile> buildExpectedManagedFilesList() {
        Metadata staticLongLivedMetadata = new StaticLongLivedMetadata();

        List<ManagedFile> managedFiles = new ArrayList<ManagedFile>();
        
        ManagedFile managedFile_next1png = new ManagedFile(relativePathForFile("Resources", "next1.png"), staticLongLivedMetadata);
        managedFiles.add(managedFile_next1png);        

        ManagedFile managedFile_next2png = new ManagedFile(relativePathForFile("Resources", "next2.png"), staticLongLivedMetadata);
        managedFiles.add(managedFile_next2png);
        
        return managedFiles;
    }
}
