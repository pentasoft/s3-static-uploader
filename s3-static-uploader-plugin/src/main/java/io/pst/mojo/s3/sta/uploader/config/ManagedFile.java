package io.pst.mojo.s3.sta.uploader.config;

public class ManagedFile {

    private final String fileName;
    private final Metadata metadata;
    
    public ManagedFile(String fileName, Metadata metadata) {
        this.fileName = fileName;
        this.metadata = metadata;
    }
    
    public String getFilename() {
        return fileName;
    }
    
    public Metadata getMetadata() {
        return metadata;
    }
}
