/*
 * Copyright 2016 Sami Zerouta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alfresco.client.samizerouta.retrofit2.adapter.download;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.alfresco.client.endpoint.Service;

import com.alfresco.client.utils.TempFileProvider;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;

final class Util {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Util.class);
	
    private Util() {
    }

    static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    static void closeQuietly(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    static boolean isAnnotationPresent(Annotation[] annotations, Class<? extends Annotation> cls) {
        for (Annotation annotation : annotations) {
            if (cls.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }
    
    // =============================================================
    // https://github.com/codexpedia/android_retrofit_download_zip
    // ==============================================================
    
//    private void downloadZipFileRx() {
//    	// https://github.com/GameJs/gamejs/archive/master.zip
//    	// AtomicGameEngine/AtomicGameEngine/archive/master.zip
//    	//RetrofitInterface downloadService = createService(RetrofitInterface.class, "https://github.com/");
//    	//downloadService.downloadFileByUrlRx("AtomicGameEngine/AtomicGameEngine/archive/master.zip")
//    	downloadFileByUrlRx("AtomicGameEngine/AtomicGameEngine/archive/master.zip")
//    	.flatMap(processResponse())
//    	.flatMap(unpackZip())
//    	.subscribeOn(Schedulers.io())
//    	.observeOn(AndroidSchedulers.mainThread())
//    	.subscribe(handleResult());
//
//    }

    private Func1<Response<ResponseBody>, Observable<File>> processResponse() {
    	return new Func1<Response<ResponseBody>, Observable<File>>() {
    		@Override
    		public Observable<File> call(Response<ResponseBody> responseBodyResponse) {
    			return saveToDiskRx(responseBodyResponse);
    		}
    	};
    }

    private Observable<File> saveToDiskRx(final Response<ResponseBody> response) {
    	return Observable.create(new Observable.OnSubscribe<File>() {
    		@Override
    		public void call(Subscriber<? super File> subscriber) {
    			try {
    				String header = response.headers().get("Content-Disposition");
    				String filename = header.replace("attachment; filename=", "");

    				//new File("/data/data/" + getPackageName() + "/games").mkdir();
    				File destinationFile = new File(TempFileProvider.getTempDir(),filename);

    				BufferedSink bufferedSink = Okio.buffer(Okio.sink(destinationFile));
    				bufferedSink.writeAll(response.body().source());
    				bufferedSink.close();

    				subscriber.onNext(destinationFile);
    				subscriber.onCompleted();
    			} catch (IOException e) {
    				e.printStackTrace();
    				subscriber.onError(e);
    			}
    		}
    	});
    }

    private Func1<File, Observable<File>> unpackZip() {
    	return new Func1<File, Observable<File>>() {
    		@Override
    		public Observable<File> call(File file) {
    			InputStream is;
    			ZipInputStream zis;
    			String parentFolder;
    			String filename;
    			try {
    				parentFolder = file.getParentFile().getPath();

    				is = new FileInputStream(file.getAbsolutePath());
    				zis = new ZipInputStream(new BufferedInputStream(is));
    				ZipEntry ze;
    				byte[] buffer = new byte[1024];
    				int count;

    				while ((ze = zis.getNextEntry()) != null) {
    					filename = ze.getName();

    					if (ze.isDirectory()) {
    						File fmd = new File(parentFolder + "/" + filename);
    						fmd.mkdirs();
    						continue;
    					}

    					FileOutputStream fout = new FileOutputStream(parentFolder + "/" + filename);

    					while ((count = zis.read(buffer)) != -1) {
    						fout.write(buffer, 0, count);
    					}

    					fout.close();
    					zis.closeEntry();
    				}

    				zis.close();

    			} catch (IOException e) {
    				e.printStackTrace();
    			}


    			File extractedFile = new File(file.getAbsolutePath().replace(".zip",""));
    			if (!file.delete()) logger.info("unpackZip" + "Failed to deleted the zip file.");
    			return Observable.just(extractedFile);
    		}
    	};
    }

    private Observer<File> handleResult() {
    	return new Observer<File>() {
    		@Override
    		public void onCompleted() {
    			logger.info("onCompleted");
    		}

    		@Override
    		public void onError(Throwable e) {
    			e.printStackTrace();
    			logger.info("Error " + e.getMessage());
    		}

    		@Override
    		public void onNext(File file) {
    			logger.info("File downloaded and extracted to " + file.getAbsolutePath());
    		}
    	};
    }

    private void downloadZipFile() {
    	Service downloadService = createService(Service.class, "https://github.com/");
    	Call<ResponseBody> call = downloadService.downloadFileByUrl("gameplay3d/GamePlay/archive/master.zip");

    	call.enqueue(new Callback<ResponseBody>() {
    		@Override
    		public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
    			if (response.isSuccessful()) {
    				logger.info("Got the body for the file");

//    				new AsyncTask<Void, Long, Void>() {
//    					@Override
//    					protected Void doInBackground(Void... voids) {
//    						saveToDisk(response.body(), "gameplay3d.zip");
//    						return null;
//    					}
//    				}.execute();

    			} else {
    				logger.info("Connection failed " + response.errorBody());
    			}
    		}

    		@Override
    		public void onFailure(Call<ResponseBody> call, Throwable t) {
    			t.printStackTrace();
    			logger.error(t.getMessage());
    		}
    	});

    }

    private void saveToDisk(ResponseBody body, File filename) {
    	try {
    		//new File("/data/data/" + getPackageName() + "/games").mkdir();
    		File destinationFile = filename;//new File("/data/data/" + getPackageName() + "/games/" + filename);

    		InputStream is = null;
    		OutputStream os = null;

    		try {
    			long filesize = body.contentLength();
    			logger.info("File Size=" + filesize);
    			is = body.byteStream();
    			os = new FileOutputStream(destinationFile);

    			byte data[] = new byte[4096];
    			int count;
    			int progress = 0;
    			while ((count = is.read(data)) != -1) {
    				os.write(data, 0, count);
    				progress +=count;
    				logger.info("Progress: " + progress + "/" + filesize + " >>>> " + (float) progress/filesize);
    			}

    			os.flush();

    			logger.info("File saved successfully!");
    			return;
    		} catch (IOException e) {
    			e.printStackTrace();
    			logger.info("Failed to save the file!");
    			return;
    		} finally {
    			if (is != null) is.close();
    			if (os != null) os.close();
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    		logger.error("Failed to save the file!");
    		return;
    	}
    }
    
    public <T> T createService(Class<T> serviceClass, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient.Builder().build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build();
        return retrofit.create(serviceClass);
    }
}
