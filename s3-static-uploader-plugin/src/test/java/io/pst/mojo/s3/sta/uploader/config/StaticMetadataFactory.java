package io.pst.mojo.s3.sta.uploader.config;

public class StaticMetadataFactory extends AbstractFilesListBuilderTest {

    public static Metadata createStatic() {
        return new StaticMetadata();
    }
    
    public static Metadata createVolatileNaked() {
        return new VolatileNakedMetadata();
    }
}
