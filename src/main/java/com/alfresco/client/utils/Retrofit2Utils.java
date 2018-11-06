package com.alfresco.client.utils;

import java.io.File;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Retrofit2Utils {
	/**
	 * @href https://futurestud.io/tutorials/retrofit-2-how-to-upload-multiple-files-to-server
	 * @param descriptionString
	 * @return
	 */
	@Nonnull
	private RequestBody createPartFromString(String descriptionString) {  
	    return RequestBody.create(
	            okhttp3.MultipartBody.FORM, descriptionString);
	}
	
	/**
	 * @href https://futurestud.io/tutorials/retrofit-2-how-to-upload-multiple-files-to-server
	 * @usage MultipartBody.Part requestFiledata = MultipartBody.Part.createFormData("filedata", filename, RequestBody.create(MediaType.parse("multipart/form-data"), tmp));
	 * @param partName
	 * @param fileUri
	 * @return
	 */
	@Nonnull
	private MultipartBody.Part prepareFilePart(String partName, File file) {  
	   return prepareFilePart(partName, file, "multipart/form-data");
	    
	} 

	/**
	 * @href https://futurestud.io/tutorials/retrofit-2-how-to-upload-multiple-files-to-server
	 * @usage MultipartBody.Part requestFiledata = MultipartBody.Part.createFormData("filedata", filename, RequestBody.create(MediaType.parse("multipart/form-data"), tmp));
	 * @param partName
	 * @param fileUri
	 * @return
	 */
	@Nonnull
	private MultipartBody.Part prepareFilePart(String partName, File file,String mimetype) {  		
		//https://stackoverflow.com/questions/34562950/post-multipart-form-data-using-retrofit-2-0-including-image
		//https://stackoverflow.com/questions/34562950/post-multipart-form-data-using-retrofit-2-0-including-image/38891018#38891018
		//https://community.alfresco.com/community/ecm/blog/2016/10/24/v1-rest-api-part-3-creating-nodes
		
		//MultipartBody.Part filePart = MultipartBody.Part.createFormData("filedata", filename, RequestBody.create(MediaType.parse(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM), tmp));
		//@Part MultipartBody.Part filedata
		//@Part("filedata\"; filename=\"pp.png\" ") RequestBody filedata
		//RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), media.getPath());
		//@Part RequestBody filedata
		//MultipartBody.Part requestFiledata = MultipartBody.Part.createFormData("filedata", filename, RequestBody.create(MediaType.parse("multipart/form-data"), tmp));
			
		// https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
	    // use the FileUtils to get the actual file by uri
	    //File file = FileUtils.getFile(this, fileUri);

	    // create RequestBody instance from file
	    RequestBody requestFile = RequestBody.create(MediaType.parse(mimetype), file);

	    // MultipartBody.Part is used to send also the actual file name
	    return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
	} 

}
