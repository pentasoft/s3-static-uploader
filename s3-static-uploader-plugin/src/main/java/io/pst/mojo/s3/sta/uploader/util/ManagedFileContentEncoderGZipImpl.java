package io.pst.mojo.s3.sta.uploader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import io.pst.mojo.s3.sta.uploader.config.ManagedFile;

public class ManagedFileContentEncoderGZipImpl implements ManagedFileContentEncoder {

    private static final String CONTENT_ENCODING_GZIP = "gzip";
    private static final int BUFFER_SIZE = 4096;

    private final File tmpDirectory;
    private final List<String> supportedContentEncodings;
    
    public ManagedFileContentEncoderGZipImpl(File tmpDirectory) {
        this.tmpDirectory = tmpDirectory;
        this.supportedContentEncodings = new ArrayList<String>();
        this.supportedContentEncodings.add(CONTENT_ENCODING_GZIP);
        if (!tmpDirectory.exists() && !tmpDirectory.mkdirs()) {
            throw new IllegalStateException("cannot create directory " + tmpDirectory);
        }
    }
    
    @Override
    public File encode(ManagedFile managedFile) throws Exception {
        File file = new File(managedFile.getFilename());
        File encodedFile = null;
        FileInputStream fis = null;
        GZIPOutputStream gzipos = null;
        try {
            byte buffer[] = new byte[BUFFER_SIZE];
            encodedFile = File.createTempFile(file.getName() + "-", ".tmp", tmpDirectory);
            fis = new FileInputStream(file);
            gzipos = new GZIPOutputStream(new FileOutputStream(encodedFile));
            int read = 0;
            do {
                read = fis.read(buffer, 0, buffer.length);
                if (read>0)
                    gzipos.write(buffer, 0, read);
            } while (read>=0);
        } finally {
            if (fis!= null) {
                fis.close();
            }
            if (gzipos!= null) {
                gzipos.close();
            }
        }
        return encodedFile;
    }

    @Override
    public boolean isContentEncodingSupported(String contentEncoding) {
        return supportedContentEncodings.contains(contentEncoding.toLowerCase());
    }
}
