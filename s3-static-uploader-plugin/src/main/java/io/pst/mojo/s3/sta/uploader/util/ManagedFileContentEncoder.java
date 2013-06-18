package io.pst.mojo.s3.sta.uploader.util;

import java.io.File;

import io.pst.mojo.s3.sta.uploader.config.ManagedFile;

public interface ManagedFileContentEncoder {
    File encode(ManagedFile managedFile) throws Exception;
    boolean isContentEncodingSupported(String contentEncoding);
}
