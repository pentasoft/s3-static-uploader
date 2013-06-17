package io.pst.mojo.s3.sta.uploader.config;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ParametersValidatorTest extends AbstractFilesListBuilderTest {

    private ParametersValidator parametersValidator;
    
    @Test(expected=IllegalStateException.class)
    public void testsNoDefaultMetadataValidation() {
        parametersValidator = new ParametersValidator(buildIncludeList(), buildMetadataListNoDefault());
        
        parametersValidator.validate();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testsContentEncodingValidation() {
        parametersValidator = new ParametersValidator(buildIncludeList(), buildMetadataListErroneousContentEncoding());
        
        parametersValidator.validate();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testsSecondsToExpireValidation() {
        parametersValidator = new ParametersValidator(buildIncludeList(), buildMetadataListErroneousSecondsToExpire());
        
        parametersValidator.validate();
    }

    @Test(expected=IllegalStateException.class)
    public void testsCannedAclValidation() {
        parametersValidator = new ParametersValidator(buildIncludeList(), buildMetadataListErroneousCannedAcl());
        
        parametersValidator.validate();
    }
    
    private List<Metadata> buildMetadataListNoDefault() {
        List<Metadata> metadatas = new ArrayList<Metadata>(3);
        metadatas.add(new StaticLongLivedMetadata());
        metadatas.add(new VolatileMetadata());
        metadatas.add(new VolatileNakedMetadata());
        return metadatas;
    }
    
    private List<Metadata> buildMetadataListErroneousContentEncoding() {
        List<Metadata> metadatas = new ArrayList<Metadata>(1);
        metadatas.add(new StaticMetadataErroneousContentEncoding());
        return metadatas;
    }
    
    private List<Metadata> buildMetadataListErroneousSecondsToExpire() {
        List<Metadata> metadatas = new ArrayList<Metadata>(1);
        metadatas.add(new StaticMetadataErroneousSecondsToExpire());
        return metadatas;
    }

    private List<Metadata> buildMetadataListErroneousCannedAcl() {
        List<Metadata> metadatas = new ArrayList<Metadata>(1);
        metadatas.add(new StaticMetadataErroneousCannedAcl());
        return metadatas;
    }
    
    private static class StaticMetadataErroneousContentEncoding extends StaticMetadata {
        StaticMetadataErroneousContentEncoding() {
            super();
            this.contentEncoding = "deflate";
        }
    } 
    
    private static class StaticMetadataErroneousSecondsToExpire extends StaticMetadata {
        StaticMetadataErroneousSecondsToExpire() {
            super();
            this.secondsToExpire = -10;
        }
    }
    
    private static class StaticMetadataErroneousCannedAcl extends StaticMetadata {
        StaticMetadataErroneousCannedAcl() {
            super();
            this.cannedAcl = "EveryoneRead";
        }
    }
}
