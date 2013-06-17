package io.pst.mojo.s3.sta.uploader.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFilesListBuilderTest {

    protected static final String ROOT_FOLDER = "src" + File.separator + "test" + File.separator + "resources"; 
    
    protected List<Include> buildIncludeList() {
        // Includes
        Include staticInclude = new StaticInclude();
        Include volatileInclude = new VolatileInclude();
        Include volatileNakedInclude = new VolatileNakedInclude();
        
        // Includes list
        List<Include> includes = new ArrayList<Include>(3);
        includes.add(staticInclude);
        includes.add(volatileInclude);
        includes.add(volatileNakedInclude);
        
        return includes;
    }
    
    protected List<String> buildExcludeList() {
        List<String> excludes = new ArrayList<String>(1);
        excludes.add("WEB-INF/**");
        return excludes;
    }
    
    protected List<Metadata> buildMetadataList() {
        List<Metadata> metadatas = new ArrayList<Metadata>(4);
        metadatas.add(new StaticMetadata());
        metadatas.add(new StaticLongLivedMetadata());
        metadatas.add(new VolatileMetadata());
        metadatas.add(new VolatileNakedMetadata());
        return metadatas;
    }

    protected static class StaticBind extends Bind {
        StaticBind() {
            super();
            this.pattern = "%regex[([^\\s]+(\\.(?i)(jpg|png|gif|bmp|tif|pdf|swf|eps))$)]";
        }
    }
    
    protected static class StaticLongLivedBind extends Bind {
        StaticLongLivedBind() {
            super();
            this.pattern = "**/next*.png";
            this.metadataId = "static-longlived"; 
        }
    }
    
    protected static class VolatileBind extends Bind {
        VolatileBind() {
            super();
            this.pattern = "%regex[([^\\s]+(\\.(?i)(html|css|js))$)]";
            this.metadataId = "volatile"; 
        }
    }

    protected static class VolatileNakedBind extends Bind {
        VolatileNakedBind() {
            super();
            this.pattern = "%regex[^[^.]+$]";
            this.metadataId = "volatile-naked"; 
        }
    }
    
    protected static class StaticMetadata extends Metadata {
        StaticMetadata() {
            super();
            this.id = "static"; 
            this.cacheControl = "public, max-age=31536000";
            this.secondsToExpire = 31536000;
            this.contentEncoding = "gzip";
            this.cannedAcl = "PublicRead";
            this.def = true;
        }
    }
    
    protected static class StaticLongLivedMetadata extends Metadata {
        StaticLongLivedMetadata() {
            super();
            this.id = "static-longlived";
            this.cacheControl = "public, max-age=315360000";
            this.secondsToExpire = 315360000;
            this.contentEncoding = "gzip";
            this.cannedAcl = "PublicRead";
        }
    }
    
    protected static class VolatileMetadata extends Metadata {
        VolatileMetadata() {
            super();
            this.id = "volatile";
            this.cacheControl = "private, max-age=86400";
            this.secondsToExpire = 86400;
            this.contentEncoding = "gzip";
            this.cannedAcl = "PublicRead";
        }
    }
    
    protected static class VolatileNakedMetadata extends VolatileMetadata {
        VolatileNakedMetadata() {
            super();
            this.id = "volatile-naked";
            this.contentType = "text/html";
        }
    }
    
    protected static class StaticInclude extends Include {
        StaticInclude() {
            super();
            this.bind = new StaticBind();
            List<Bind> constraints = new ArrayList<Bind>(1);
            constraints.add(new StaticLongLivedBind());
            this.constraints = constraints;
        }
    }
    
    protected static class VolatileInclude extends Include {
        VolatileInclude() {
            super();
            this.bind = new VolatileBind();
        }
    }
    
    protected static class VolatileNakedInclude extends Include {
        VolatileNakedInclude() {
            this.bind = new VolatileNakedBind();
        }
    }
    
    protected String relativePathForFile(String folder, String fileName) {
        StringBuilder builder = new StringBuilder(ROOT_FOLDER + File.separator); 
        if (folder != null && !"".equals(folder)) {
            builder.append(folder).append(File.separator);
        }
        builder.append(fileName);
        return builder.toString();
    }
}
