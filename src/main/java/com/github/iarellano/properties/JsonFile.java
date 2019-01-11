package com.github.iarellano.properties;

import java.io.File;
import java.util.Arrays;

public class JsonFile {

    private String prefix;

    private File filePath;

    private Lookup[] lookups;

    private boolean failIfFileNotFound = false;

    private String charset = "UTF-8";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public File getFilePath() {
        return filePath;
    }

    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

    public Lookup[] getLookups() {
        return lookups;
    }

    public void setLookups(Lookup[] lookups) {
        this.lookups = lookups;
    }

    public boolean isFailIfFileNotFound() {
        return failIfFileNotFound;
    }

    public void setFailIfFileNotFound(boolean failIfFileNotFound) {
        this.failIfFileNotFound = failIfFileNotFound;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public String toString() {
        return "JsonFile{" +
                "prefix='" + prefix + '\'' +
                ", filePath=" + filePath +
                ", lookups=" + Arrays.toString(lookups) +
                ", failIfFileNotFound=" + failIfFileNotFound +
                '}';
    }
}
