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

import javax.print.URIException;

import retrofit2.Retrofit;

public class RestClient
{
	private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RestClient.class);
	
    public final String username;

    public final String endpoint;

    public final Retrofit retrofit;
    
    //ALFRESCO SERVICE
      
    /**
     * Search API - Provides access to the search features of Alfresco Content Services.
     */
    public final String endpointSearchApi;
    /**
     * Core API - Provides access to the core features of Alfresco Content Services.
     */
    public final String endpointCoreApi;
    /**
     * Authentication API - Provides access to the authentication features of Alfresco Content Services.
     */
    public final String endpointAuthenticationApi;
    /**
     * Discovery API - Provides access to information about Alfresco Content Services.
     */
    public final String endpointDiscoveryApi;
    /**
     * Workflow API - Provides access to the workflow features of Alfresco Content Services.
     */
    public final String endpointWorkflowApi;
       
    //public final String endpointCoreApiGetContent;

    public RestClient(String endpoint, Retrofit retrofit, String username)
    {
    	//PREPARE URL ALFRESCO
    	String baseUrl = endpoint;	
    	
    	baseUrl = (baseUrl.lastIndexOf("/") == (baseUrl.length() - 1)) ? baseUrl
                : baseUrl.concat("/");
    	
//    	if (!baseUrl.endsWith("alfresco")){
//        	if (!baseUrl.endsWith("/")){
//        		baseUrl = baseUrl + "/";
//        	}
//        	if (!baseUrl.endsWith("alfresco/")){
//        		baseUrl = baseUrl + "alfresco/";
//        	}   		
//    	}else{
//    		baseUrl = baseUrl + "/";
//    	}
        
    	if (!baseUrl.endsWith("alfresco/")){
			logger.error("The url of alfresco <"+baseUrl+"> is wrong");
		}
        
        this.endpoint = baseUrl;
        this.retrofit = retrofit;
        this.username = username;
        
        //SUPPORT FOR SWAGGER ALFRESCO 5.2
        // [ base url: /alfresco/api/-default-/public/alfresco/versions/1 , api version: 1 ] 
        this.endpointCoreApi = this.endpoint + "api/-default-/public/alfresco/versions/"+Version.SDK+"/";
        // [ base url: /alfresco/api/-default-/public/search/versions/1 , api version: 1 ] 
        this.endpointSearchApi =  this.endpoint + "api/-default-/public/search/versions/"+Version.SDK+"/";
        // [ base url: /alfresco/api/-default-/public/authentication/versions/1 , api version: 1 ] 
        this.endpointAuthenticationApi = this.endpoint + "api/-default-/public/authentication/versions/"+Version.SDK+"/";
        // [ base url: /alfresco/api , api version: 1 ] 
        this.endpointDiscoveryApi = this.endpoint + "api/"+Version.SDK+"/";
        // [ base url: /alfresco/api/-default-/public/workflow/versions/1 , api version: 1 ] 
        this.endpointWorkflowApi = this.endpoint + "api/-default-/public/workflow/versions/"+Version.SDK+"/";
        
        //this.endpointCoreApiGetContent =  this.endpointCoreApi+"/nodes/{nodeId}/content";
    }
    

    
}
