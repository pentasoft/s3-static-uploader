package io.pst.mojo.s3.sta.uploader.config;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class IncludedFilesListBuilderTest extends AbstractFilesListBuilderTest {

    private IncludedFilesListBuilder builder;
    
    @Before
    public void setUp() {
        builder = new IncludedFilesListBuilder(new File(ROOT_FOLDER), 
                                                buildIncludeList(), buildExcludeList(), 
                                                buildMetadataList());
    }
    
    @Test
    public void testsIncludedFiles() throws IOException {
        List<ManagedFile> expectedList = buildExpectedManagedFilesList();
        
        List<ManagedFile> builtList = builder.build();

        // Both lists must be equal, but order may differ
        assertTrue(builtList.containsAll(expectedList));
        assertTrue(expectedList.containsAll(builtList));
    }
    
    private List<ManagedFile> buildExpectedManagedFilesList() {
        Metadata staticMetadata = new StaticMetadata();
        Metadata staticLongLivedMetadata = new StaticLongLivedMetadata();
        Metadata volatileMetadata = new VolatileMetadata();
        Metadata volatileNakedMetadata = new VolatileNakedMetadata();

        List<ManagedFile> managedFiles = new ArrayList<ManagedFile>();
        
        ManagedFile managedFile_1jpg = new ManagedFile(relativePathForFile("Images", "1.jpg"), staticMetadata);
        managedFiles.add(managedFile_1jpg);        
        
        ManagedFile managedFile_2jpg = new ManagedFile(relativePathForFile("Images", "2.jpg"), staticMetadata);
        managedFiles.add(managedFile_2jpg);

        ManagedFile managedFile_1html = new ManagedFile(relativePathForFile("Pages", "1.html"), volatileMetadata);
        managedFiles.add(managedFile_1html);        

        ManagedFile managedFile_2html = new ManagedFile(relativePathForFile("Pages", "2.html"), volatileMetadata);
        managedFiles.add(managedFile_2html);        

        ManagedFile managedFile_1png = new ManagedFile(relativePathForFile("Resources", "1.png"), staticMetadata);
        managedFiles.add(managedFile_1png);        

        ManagedFile managedFile_2png = new ManagedFile(relativePathForFile("Resources", "2.png"), staticMetadata);
        managedFiles.add(managedFile_2png);        

        ManagedFile managedFile_next1png = new ManagedFile(relativePathForFile("Resources", "next1.png"), staticLongLivedMetadata);
        managedFiles.add(managedFile_next1png);        

        ManagedFile managedFile_next2png = new ManagedFile(relativePathForFile("Resources", "next2.png"), staticLongLivedMetadata);
        managedFiles.add(managedFile_next2png);        

        ManagedFile managedFile_11png = new ManagedFile(relativePathForFile("Thumbnails", "11.jpg"), staticMetadata);
        managedFiles.add(managedFile_11png);        

        ManagedFile managedFile_12png = new ManagedFile(relativePathForFile("Thumbnails", "12.jpg"), staticMetadata);
        managedFiles.add(managedFile_12png);
        
        ManagedFile managedFile_index = new ManagedFile(relativePathForFile("", "index"), volatileNakedMetadata);
        managedFiles.add(managedFile_index);
        
        return managedFiles;
    }
}
