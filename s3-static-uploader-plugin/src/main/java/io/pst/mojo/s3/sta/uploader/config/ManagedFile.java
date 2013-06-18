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

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        return result;        
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ManagedFile)) {
            return false;
        }
        ManagedFile managedFile = (ManagedFile) o;
        return (fileName == null ? managedFile.fileName == null : fileName.equals(managedFile.fileName))
            && (metadata == null ? managedFile.metadata == null : metadata.equals(managedFile.metadata));
    }    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("managedFile [")
                .append(" fileName=").append(fileName)
                .append(" metadata=").append(metadata)
                .append(" ]");
        return builder.toString();
    }
}
