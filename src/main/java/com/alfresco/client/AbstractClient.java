/*
 *   Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *   This file is part of Alfresco Java Client.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.alfresco.client;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.joda.time.DateTime;

import com.alfresco.client.logging.HttpLoggingInterceptorSLF4J;
import com.alfresco.client.samizerouta.retrofit2.adapter.download.DownloadCallAdapterFactory;
import com.alfresco.client.utils.Base64;
import com.alfresco.client.utils.DateTimeConverter;
import com.alfresco.client.utils.DateJava8Converter;
import com.alfresco.client.utils.ISO8601Utils;
import com.alfresco.client.utils.WsdlUtils;
import com.alfresco.swagger.api.client.ApiClient;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by jpascal on 22/01/2016.
 */
public abstract class AbstractClient<T>
{

    protected RestClient restClient;

    protected OkHttpClient okHttpClient;
    
    protected com.alfresco.swagger.api.client.ApiClient apiClientSwagger;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    protected AbstractClient(RestClient restClient, OkHttpClient okHttpClient)
    {
        this.restClient = restClient;
        this.okHttpClient = okHttpClient;
        this.apiClientSwagger = new ApiClient();
        
        this.apiClientSwagger.configureFromOkclient(okHttpClient);
        
//      Retrofit.Builder builder = new Retrofit.Builder();    
//        builder.baseUrl(this.restClient.endpoint);
//        builder.addConverterFactory(GsonConverterFactory.create());
//        builder.addConverterFactory(ScalarsConverterFactory.create());
//        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        
        Retrofit.Builder builder = alfrescoRetrofitBuilder(null);
        builder.baseUrl(this.restClient.endpoint).client(okHttpClient);
       
        this.apiClientSwagger.setAdapterBuilder(builder);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public OkHttpClient getOkHttpClient()
    {
        return okHttpClient;
    }

    public RestClient getRestClient()
    {
        return restClient;
    }

    public <T> T getAPI(final Class<T> service)
    {
        return restClient.retrofit.create(service);
    }
    
    
    
    public ApiClient getApiClientSwagger() {
		return apiClientSwagger;
	}

	public <T> T getAPISwagger(final Class<T> service)
    {
        return apiClientSwagger.createService(service);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BASIC AUTH
    // ///////////////////////////////////////////////////////////////////////////
    protected static String getBasicAuth(String username, String password)
    {
        // Prepare Basic AUTH
        if (username != null && password != null)
        {
            String credentials = username + ":" + password;
            return "Basic " + Base64.encodeBytes(credentials.getBytes());
        }
        throw new IllegalArgumentException("Invalid Credentials");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static abstract class Builder<T>
    {
        protected String endpoint, username, password, auth;

        protected String ticket, token;

        protected OkHttpClient okHttpClient;

        protected Retrofit retrofit;

        protected GsonBuilder gsonBuilder;

        protected HttpLoggingInterceptor.Level logginLevel = HttpLoggingInterceptor.Level.NONE;

        //MOD P4535992
        
        protected org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Builder.class);
        
        protected HostnameVerifier hostnameVerifier;

        protected SSLContext sslContext;
        
        //END MOD P4535992

        public Builder<T> connect(String endpoint, String username, String password)
        {
            if (endpoint != null && !endpoint.isEmpty())
            {
                this.endpoint = (endpoint.lastIndexOf("/") == (endpoint.length() - 1)) ? endpoint
                        : endpoint.concat("/");
            }
            this.username = username;
            this.password = password;
            return this;
        }
        
        //MOD P4535992
        
        public Builder<T> connect(String endpoint, String username, String password,HostnameVerifier hostnameVerifier, SSLContext sslContext)
        {
        	 if (endpoint != null && !endpoint.isEmpty())
             {
                 this.endpoint = (endpoint.lastIndexOf("/") == (endpoint.length() - 1)) ? endpoint
                         : endpoint.concat("/");
             }
             this.username = username;
             this.password = password;
             if(hostnameVerifier!=null){
            	 this.hostnameVerifier = hostnameVerifier;
             }
             if(sslContext!=null){
            	 this.sslContext = sslContext;
             }
 
             return this;
        }
        
        public Builder<T> connect(String endpoint, String username, String password,boolean acceptAllHosts, boolean acceptAllCertificates)
        {
            if (endpoint != null && !endpoint.isEmpty())
            {
                this.endpoint = (endpoint.lastIndexOf("/") == (endpoint.length() - 1)) ? endpoint
                        : endpoint.concat("/");
            }
            this.username = username;
            this.password = password;
            
            if(acceptAllHosts){
            	this.hostnameVerifier = WsdlUtils.setSystemTrustAllHost();
            	//HttpsURLConnection.setDefaultHostnameVerifier(this.hostnameVerifier);
            }
            if(acceptAllCertificates){
				try {				
	        		this.sslContext = WsdlUtils.setSystemTrustAllCertificateSSLContext("TLSv1.2");	        			        		
	        		//HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
				} catch (NoSuchAlgorithmException | KeyManagementException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					return null;
				}
        		
            }
            return this;
        }
        
        //END MOD P4535992

        /**
         * Usually for SAML or Alfresco Ticket authentication mechanism
         * 
         * @param endpoint
         * @param ticket
         * @return
         */
        // TODO Design it better.
        public Builder<T> connectWithTicket(String endpoint, String ticket)
        {
            if (endpoint != null && !endpoint.isEmpty())
            {
                this.endpoint = (endpoint.lastIndexOf("/") == (endpoint.length() - 1)) ? endpoint
                        : endpoint.concat("/");
            }
            this.ticket = ticket;
            return this;
        }
        
        //MOD P4535992
        
        public Builder<T> connectWithTicket(String endpoint, String ticket,HostnameVerifier hostnameVerifier, SSLContext sslContext)
        {
            if (endpoint != null && !endpoint.isEmpty())
            {
                this.endpoint = (endpoint.lastIndexOf("/") == (endpoint.length() - 1)) ? endpoint
                        : endpoint.concat("/");
            }
            this.ticket = ticket;
             if(hostnameVerifier!=null){
            	 this.hostnameVerifier = hostnameVerifier;
             }
             if(sslContext!=null){
            	 this.sslContext = sslContext;
             }
 
             return this;
        }
        
        public Builder<T> connectWithTicket(String endpoint, String ticket,boolean acceptAllHosts, boolean acceptAllCertificates)
        {
            if (endpoint != null && !endpoint.isEmpty())
            {
                this.endpoint = (endpoint.lastIndexOf("/") == (endpoint.length() - 1)) ? endpoint
                        : endpoint.concat("/");
            }
            this.ticket = ticket;
            
            if(acceptAllHosts){
            	this.hostnameVerifier = WsdlUtils.setSystemTrustAllHost();
            	//HttpsURLConnection.setDefaultHostnameVerifier(this.hostnameVerifier);
            }
            if(acceptAllCertificates){
				try {				
	        		this.sslContext = WsdlUtils.setSystemTrustAllCertificateSSLContext("TLSv1.2");	        			        		
	        		//HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
				} catch (NoSuchAlgorithmException | KeyManagementException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					return null;
				}
        		
            }
            return this;
        }
        
        //END MOD P4535992

        /**
         * Usually for Oauth authentication mechanism
         * 
         * @param endpoint
         * @param token
         * @return
         */
        // TODO Design it better.
        public Builder<T> connectWithToken(String endpoint, String token)
        {
            if (endpoint != null && !endpoint.isEmpty())
            {
                this.endpoint = (endpoint.lastIndexOf("/") == (endpoint.length() - 1)) ? endpoint
                        : endpoint.concat("/");
            }
            this.token = token;
            return this;
        }

        public Builder<T> httpLogging(HttpLoggingInterceptor.Level level)
        {
            this.logginLevel = level;
            return this;
        }

        public Builder<T> okHttpClient(OkHttpClient okHttpClient)
        {
            this.okHttpClient = okHttpClient;
            return this;
        }

        public Builder<T> retrofit(Retrofit retrofit)
        {
            this.retrofit = retrofit;
            return this;
        }

        public Builder<T> gsonBuilder(GsonBuilder gsonBuilder)
        {
            this.gsonBuilder = gsonBuilder;
            return this;
        }

        public T build()
        {
            // Check Parameters
            if (endpoint == null || endpoint.isEmpty()) { throw new IllegalArgumentException("Invalid url"); }

            // Prepare OKHTTP Layer
            if (okHttpClient == null)
            {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();

                ArrayList<Protocol> protocols = new ArrayList<>(1);
                protocols.add(Protocol.HTTP_1_1);
                builder.protocols(protocols);
                builder.connectTimeout(10, TimeUnit.SECONDS);

                Interceptor interceptor = null;
                if (ticket != null)
                {
                    interceptor = new TicketInterceptor(ticket);
                }
                else if (token != null)
                {
                    interceptor = new TokenInterceptor(token);
                }
                else if (username != null && password != null)
                {
                    interceptor = new BasicAuthInterceptor(username, password);
                }
                builder.addInterceptor(interceptor);

                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(logginLevel);
                builder.addInterceptor(logging);

                //MOD P4535992
                
                //ADD SLF4J LOGGING INTERCEPTOR
                
                HttpLoggingInterceptorSLF4J loggingSlf4j = new HttpLoggingInterceptorSLF4J(logger);             
                builder.addInterceptor(loggingSlf4j);
                
                //ADD HOSTNAME VERIFIER
                if(hostnameVerifier!=null){
                	builder.hostnameVerifier(hostnameVerifier);
                }
                
                //ADD EXPLICIT SSLCONTEXT
                if(sslContext!=null){
                	builder.socketFactory(sslContext.getSocketFactory());
                }
                //END MOD P4535992

                okHttpClient = builder.build();
            }

            // Prepare Retrofit
            if (retrofit == null)
            {
                Retrofit.Builder builder = alfrescoRetrofitBuilder(gsonBuilder); 
                builder.baseUrl(endpoint).client(okHttpClient);
                retrofit = builder.build();
            }

            return create(new RestClient(endpoint, retrofit, username), okHttpClient);
        }

        public abstract GsonBuilder getDefaultGsonBuilder();

        public abstract String getUSerAgent();

        public abstract T create(RestClient restClient, OkHttpClient okHttpClient);
        
    }
    
    public static Retrofit.Builder alfrescoRetrofitBuilder(GsonBuilder gsonBuilder){
        // Prepare Retrofit
    	if(gsonBuilder==null){
    		gsonBuilder = new GsonBuilder();
    	}
        try{
        	//https://stackoverflow.com/questions/6873020/gson-date-format
        	//g.setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        	//g.setDateFormat(DateFormat.FULL, DateFormat.FULL); 
        	//g.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            gsonBuilder.setDateFormat(ISO8601Utils.DATE_ISO_FORMAT);                   
            gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
            gsonBuilder.registerTypeAdapter(Date.class, new DateJava8Converter());             
            //gsonBuilder.registerTypeAdapter(com.alfresco.swagger.api.model.NodeEntry.class, new NodeEntryDeserializer()); 
            
            gsonBuilder.setLenient();
            
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        
        GsonConverterFactory gFactory = null;
        if (gsonBuilder != null)
        {
        	gFactory = GsonConverterFactory.create(gsonBuilder.create());
        }         
        Retrofit.Builder builder = new Retrofit.Builder();
		if(gFactory!=null){               		
			builder.addConverterFactory(gFactory);
		}else{
			builder.addConverterFactory(GsonConverterFactory.create());
		}                      
		builder.addConverterFactory(ScalarsConverterFactory.create());
        //.addCallAdapterFactory(RxJavaCallAdapterFactory.create());
		builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
		
		//https://github.com/samizerouta/retrofit2-download-adapter
		builder.addCallAdapterFactory(DownloadCallAdapterFactory.create());
		
		return builder;
    }

    protected final static class BasicAuthInterceptor implements Interceptor
    {

        String auth;

        public BasicAuthInterceptor(String username, String password)
        {
            String credentials = username + ":" + password;
            auth = "Basic " + Base64.encodeBytes(credentials.getBytes());
        }

        @Override
        public Response intercept(Chain chain) throws IOException
        {
            Request newRequest = chain.request().newBuilder().addHeader("Authorization", auth).build();
            return chain.proceed(newRequest);
        }
    }

    protected final static class TicketInterceptor implements Interceptor
    {
        String auth;

        public TicketInterceptor(String ticket)
        {
            auth = "Basic " + Base64.encodeBytes(ticket.getBytes());
        }

        @Override
        public Response intercept(Chain chain) throws IOException
        {
            Request newRequest = chain.request().newBuilder().addHeader("Authorization", auth).build();
            return chain.proceed(newRequest);
        }
    }

    protected final static class TokenInterceptor implements Interceptor
    {
        private static final String TOKEN_TYPE_BEARER = "Bearer";

        String auth;

        public TokenInterceptor(String accessToken)
        {
            if (accessToken != null && !accessToken.isEmpty())
            {
                auth = accessToken.startsWith(TOKEN_TYPE_BEARER) ? accessToken : TOKEN_TYPE_BEARER + " " + accessToken;
            }
        }

        @Override
        public Response intercept(Chain chain) throws IOException
        {
            Request newRequest = chain.request().newBuilder().addHeader("Authorization", auth).build();
            return chain.proceed(newRequest);
        }
    }
}
