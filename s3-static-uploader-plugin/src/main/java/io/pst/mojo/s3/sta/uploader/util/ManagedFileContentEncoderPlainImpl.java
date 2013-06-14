package io.pst.mojo.s3.sta.uploader.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.pst.mojo.s3.sta.uploader.config.ManagedFile;

public class ManagedFileContentEncoderPlainImpl implements ManagedFileContentEncoder {

    private static final String CONTENT_ENCODING_PLAIN = "plain";
    
    private final List<String> supportedContentEncodings; 
    
    public ManagedFileContentEncoderPlainImpl() {
        this.supportedContentEncodings = new ArrayList<String>();
        this.supportedContentEncodings.add(CONTENT_ENCODING_PLAIN);
    }
    
    @Override
    public File encode(ManagedFile managedFile) throws Exception {
        return new File(managedFile.getFilename());
    }

    @Override
    public boolean isContentEncodingSupported(String contentEncoding) {
        return supportedContentEncodings.contains(contentEncoding.toLowerCase());
    }
}
