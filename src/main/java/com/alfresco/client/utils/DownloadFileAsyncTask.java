package com.alfresco.client.utils;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @href https://gldraphael.com/blog/downloading-a-file-using-retrofit/
 */
public class DownloadFileAsyncTask { //extends AsyncTask<InputStream, Void, Boolean> {
	
	public interface IDownloadFileAsyncTaskService {
	    @GET
	    @Streaming
	    Call<ResponseBody> getFile(@Url String url);
	}

	
	

//    final String appDirectoryName = "AppName";
//    final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
//            Environment.DIRECTORY_PICTURES), appDirectoryName);
//    final String filename = "image.jpg";
//
//    @Override
//    protected Boolean doInBackground(InputStream... params) {
//        InputStream inputStream = params[0];
//        File file = new File(imageRoot, filename); 
//        OutputStream output = null;
//        try {
//            output = new FileOutputStream(file);
//
//            byte[] buffer = new byte[1024]; // or other buffer size
//            int read;
//
//            Log.d(TAG, "Attempting to write to: " + imageRoot + "/" + filename);
//            while ((read = inputStream.read(buffer)) != -1) {
//                output.write(buffer, 0, read);
//                Log.v(TAG, "Writing to buffer to output stream.");
//            }
//            Log.d(TAG, "Flushing output stream.");
//            output.flush();
//            Log.d(TAG, "Output flushed.");
//        } catch (IOException e) {
//            Log.e(TAG, "IO Exception: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        } finally {
//            try {
//                if (output != null) {
//                    output.close();
//                    Log.d(TAG, "Output stream closed sucessfully.");
//                }
//                else{
//                    Log.d(TAG, "Output stream is null");
//                }
//            } catch (IOException e){
//                Log.e(TAG, "Couldn't close output stream: " + e.getMessage());
//                e.printStackTrace();
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    protected void onPostExecute(Boolean result) {
//        super.onPostExecute(result);
//
//        Log.d(TAG, "Download success: " + result);
//        // TODO: show a snackbar or a toast
//    }
}
