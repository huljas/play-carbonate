package com.carbonfive.db.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author huljas
 */
public class Resource {
    private File file;

    public Resource(File file) {
        this.file = file;
    }

    public String getFilename() {
        return file.getName();
    }

    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new MigrationException("Resource doesn't exist", e);
        }
    }
}
