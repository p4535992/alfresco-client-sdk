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

import java.util.ArrayList;
import java.util.HashMap;

import com.alfresco.client.endpoint.Service;
import com.alfresco.client.utils.ISO8601Utils;
import com.alfresco.swagger.api.ActivitiesApi;
import com.alfresco.swagger.api.AuditApi;
import com.alfresco.swagger.api.AuthenticationApi;
import com.alfresco.swagger.api.CommentsApi;
import com.alfresco.swagger.api.DeploymentsApi;
import com.alfresco.swagger.api.DiscoveryApi;
import com.alfresco.swagger.api.DownloadsApi;
import com.alfresco.swagger.api.FavoritesApi;
import com.alfresco.swagger.api.GroupsApi;
import com.alfresco.swagger.api.NetworksApi;
import com.alfresco.swagger.api.NodesApi;
import com.alfresco.swagger.api.PeopleApi;
import com.alfresco.swagger.api.PreferencesApi;
import com.alfresco.swagger.api.ProcessDefinitionsApi;
import com.alfresco.swagger.api.ProcessesApi;
import com.alfresco.swagger.api.QueriesApi;
import com.alfresco.swagger.api.RatingsApi;
import com.alfresco.swagger.api.RenditionsApi;
import com.alfresco.swagger.api.SearchApi;
import com.alfresco.swagger.api.SharedLinksApi;
import com.alfresco.swagger.api.SitesApi;
import com.alfresco.swagger.api.TagsApi;
import com.alfresco.swagger.api.TasksApi;
import com.alfresco.swagger.api.TrashcanApi;
import com.alfresco.swagger.api.VersionsApi;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class AlfrescoClientSwagger extends AbstractClient<AlfrescoClientSwagger>
{
    protected static final Object LOCK = new Object();

    protected static AlfrescoClientSwagger mInstance;
    
    // Core Api

    protected ActivitiesApi activitiesAPI;
    
    protected AuditApi auditAPI;

    protected CommentsApi commentsAPI;
    
    protected DownloadsApi downloadsAPI;
    
    protected FavoritesApi favoritesAPI;
    
    protected GroupsApi groupsAPI;
    
    protected NetworksApi networksAPI;
    
    protected NodesApi nodesAPI;

    protected PeopleApi peopleAPI;
    
    protected PreferencesApi preferencesAPI;
    
    protected QueriesApi queriesAPI;
    
    protected RatingsApi ratingsAPI;
    
    protected RenditionsApi renditionsAPI;
    
    protected SharedLinksApi sharedLinksAPI;
    
    protected SitesApi sitesAPI;
    
    protected TagsApi tagsAPI;
    
    protected TrashcanApi trashcanAPI;
    
    protected VersionsApi versionAPI;
    
    // Search Api
    
    protected SearchApi searchAPI;
    
    // Authentication API
    
    protected AuthenticationApi authenticationAPI;

    // Discovery Api

    protected DiscoveryApi discoveryAPI;
    
    // Workflow API
    
    protected DeploymentsApi deploymentsAPI;
    
    protected ProcessDefinitionsApi processDefinitionsAPI;
    
    protected ProcessesApi processesAPI;
    
    protected TasksApi tasksAPI;
    
    // ENTERPRISE ACTIVITI
    
    //protected DictionaryAPI dictionaryAPI;

    // protected GroupsAPI groupsAPI;

    
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static AlfrescoClientSwagger getInstance()
    {
        synchronized (LOCK)
        {
            return mInstance;
        }
    }

    private AlfrescoClientSwagger(RestClient restClient, OkHttpClient okHttpClient)
    {
        super(restClient, okHttpClient);        
    }

    // ///////////////////////////////////////////////////////////////////////////
    // API REGISTRY
    // ///////////////////////////////////////////////////////////////////////////
        
    // Core Api
    
    public ActivitiesApi getActivitiesAPI()
    {
        if (activitiesAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	activitiesAPI = apiClientSwagger.createService(com.alfresco.swagger.api.ActivitiesApi.class);
            //activitiesAPI = getAPI(ActivitiesApi.class);
        }
        return activitiesAPI;
    }
    
    public AuditApi getAuditAPI()
    {
        if (activitiesAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	auditAPI = apiClientSwagger.createService(com.alfresco.swagger.api.AuditApi.class);
        }
        return auditAPI;
    }

    public CommentsApi getCommentsAPI()
    {
        if (commentsAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	commentsAPI = apiClientSwagger.createService(com.alfresco.swagger.api.CommentsApi.class);
            //commentsAPI = getAPI(CommentsApi.class);
        }
        return commentsAPI;
    }
    
    public DownloadsApi getDownloadsAPI()
    {
        if (downloadsAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	downloadsAPI = apiClientSwagger.createService(com.alfresco.swagger.api.DownloadsApi.class);
        }
        return downloadsAPI;
    }
    
    public FavoritesApi getFavoritesAPI()
    {
        if (favoritesAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	favoritesAPI = apiClientSwagger.createService(com.alfresco.swagger.api.FavoritesApi.class);
            //favoritesAPI = getAPI(FavoritesApi.class);
        }
        return favoritesAPI;
    }
    
    public GroupsApi getGroupsAPI()
    {
        if (groupsAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	groupsAPI = apiClientSwagger.createService(com.alfresco.swagger.api.GroupsApi.class);
            //favoritesAPI = getAPI(FavoritesApi.class);
        }
        return groupsAPI;
    }
    
    public NetworksApi getNetworksAPI()
    {
        if (networksAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	networksAPI = apiClientSwagger.createService(com.alfresco.swagger.api.NetworksApi.class);
        }
        return networksAPI;
    }
    
    public NodesApi getNodesAPI()
    {
        if (nodesAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	nodesAPI = apiClientSwagger.createService(com.alfresco.swagger.api.NodesApi.class);
            //nodesAPI = getAPI(NodesApi.class);
        }
        return nodesAPI;
    }
       
    public PeopleApi getPeopleAPI()
    {
        if (peopleAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	peopleAPI = apiClientSwagger.createService(com.alfresco.swagger.api.PeopleApi.class);
            //peopleAPI = getAPI(PeopleApi.class);
        }
        return peopleAPI;
    }
    
    public PreferencesApi getPreferencesAPI()
    {
        if (preferencesAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	preferencesAPI = apiClientSwagger.createService(com.alfresco.swagger.api.PreferencesApi.class);           
        }
        return preferencesAPI;
    }
       
    public QueriesApi getQueriesAPI()
    {
        if (queriesAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	queriesAPI = apiClientSwagger.createService(com.alfresco.swagger.api.QueriesApi.class);       
        	//queriesAPI = getAPI(QueriesApi.class);
        }
        return queriesAPI;
    }
    
    public RatingsApi getRatingsAPI()
    {
        if (ratingsAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	ratingsAPI = apiClientSwagger.createService(com.alfresco.swagger.api.RatingsApi.class);
            //ratingsAPI = getAPI(RatingsApi.class);
        }
        return ratingsAPI;
    }
    
    public RenditionsApi getRenditionsAPI()
    {
        if (renditionsAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	renditionsAPI = apiClientSwagger.createService(com.alfresco.swagger.api.RenditionsApi.class);
            //renditionsAPI = getAPI(RenditionsApi.class);
        }
        return renditionsAPI;
    }
    
    public SharedLinksApi getSharedLinksAPI()
    {
        if (sharedLinksAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	sharedLinksAPI = apiClientSwagger.createService(com.alfresco.swagger.api.SharedLinksApi.class);
            //sharedLinksAPI = getAPI(SharedLinksApi.class);
        }
        return sharedLinksAPI;
    }
    
    public SitesApi getSitesAPI()
    {
        if (sitesAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	sitesAPI = apiClientSwagger.createService(com.alfresco.swagger.api.SitesApi.class);
            //sitesAPI = getAPI(SitesApi.class);
        }
        return sitesAPI;
    }

    public TagsApi getTagsAPI()
    {
        if (tagsAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	tagsAPI = apiClientSwagger.createService(com.alfresco.swagger.api.TagsApi.class);
            //tagsAPI = getAPI(TagsApi.class);
        }
        return tagsAPI;
    }
    
    public TrashcanApi getTrashcanAPI()
    {
        if (trashcanAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	trashcanAPI = apiClientSwagger.createService(com.alfresco.swagger.api.TrashcanApi.class);
            //trashcanAPI = getAPI(TrashcanApi.class);
        }
        return trashcanAPI;
    }
    
    public VersionsApi getVersionAPI()
    {
        if (versionAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
        	versionAPI = apiClientSwagger.createService(com.alfresco.swagger.api.VersionsApi.class);
            //versionAPI = getAPI(VersionsApi.class);
        }
        return versionAPI;
    }
    
    // Search Api
    
    public SearchApi getSearchAPI()
    {
        if (searchAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointSearchApi);        
        	searchAPI = apiClientSwagger.createService(com.alfresco.swagger.api.SearchApi.class);        	
            //searchAPI = getAPI(SearchApi.class);
        } 
        return searchAPI;
    }   
    
    // Authentication API
    
    public AuthenticationApi getAuthenticationAPI()
    {
        if (authenticationAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointAuthenticationApi);        
        	authenticationAPI = apiClientSwagger.createService(com.alfresco.swagger.api.AuthenticationApi.class);
            //authenticationAPI = getAPI(AuthenticationApi.class);
        }
        return authenticationAPI;
    }
    
    // Discovery API
    
    public DiscoveryApi getDiscoveryAPI()
    {
        if (discoveryAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointDiscoveryApi);        
        	discoveryAPI = apiClientSwagger.createService(com.alfresco.swagger.api.DiscoveryApi.class);
            //discoveryAPI = getAPI(DiscoveryApi.class);
        }
        return discoveryAPI;
    }
    
    // Workflow API
    
    public DeploymentsApi getDeploymentsAPI()
    {
        if (deploymentsAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointWorkflowApi);        
        	deploymentsAPI = apiClientSwagger.createService(com.alfresco.swagger.api.DeploymentsApi.class);
        }
        return deploymentsAPI;
    }

    public ProcessDefinitionsApi getProcessDefinitionsAPI()
    {
        if (processDefinitionsAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointWorkflowApi);        
        	processDefinitionsAPI = apiClientSwagger.createService(com.alfresco.swagger.api.ProcessDefinitionsApi.class);
        }
        return processDefinitionsAPI;
    }
    
    public ProcessesApi getProcessesAPI()
    {
        if (processesAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointWorkflowApi);        
        	processesAPI = apiClientSwagger.createService(com.alfresco.swagger.api.ProcessesApi.class);
        }
        return processesAPI;
    }
    
    public TasksApi getTasksAPI()
    {
        if (tasksAPI == null)
        {
        	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointWorkflowApi);        
        	tasksAPI = apiClientSwagger.createService(com.alfresco.swagger.api.TasksApi.class);
        }
        return tasksAPI;
    }

    // ENTERPRISE ACTIVITI


//    public DictionaryAPI getDictionaryAPI()
//    {
//        if (dictionaryAPI == null)
//        {
//            dictionaryAPI = getAPI(DictionaryAPI.class);
//        }
//        return dictionaryAPI;
//    }
    
    // CUSTOM REST API
    
    public Service getCustomNodesAPI()
    {
    	this.apiClientSwagger.getAdapterBuilder().baseUrl(this.restClient.endpointCoreApi);        
    	Service service = apiClientSwagger.createService(Service.class);       
        return service;
    }

    @Override
    public RestClient getRestClient(){
    	return this.restClient;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // AUTHENTICATION
    // ///////////////////////////////////////////////////////////////////////////
    public void setTicket(String ticket)
    {

        OkHttpClient.Builder builder = getOkHttpClient().newBuilder();

        // Remove old interceptor
        int index = 0;
        for (int i = 0; i < builder.interceptors().size(); i++)
        {
            if (builder.interceptors().get(i) instanceof BasicAuthInterceptor
                    || builder.interceptors().get(i) instanceof TicketInterceptor)
            {
                index = i;
            }
        }

        builder.interceptors().remove(index);
        builder.interceptors().add(new TicketInterceptor(ticket));
        okHttpClient = builder.build();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends AbstractClient.Builder<AlfrescoClientSwagger>
    {

        @Override
        public String getUSerAgent()
        {
            return "Alfresco-ECM-Client/" + Version.SDK;
        }

        @Override
        public AlfrescoClientSwagger create(RestClient restClient, OkHttpClient okHttpClient)
        {
            return new AlfrescoClientSwagger(new RestClient(endpoint, retrofit, username), okHttpClient);
        }

        @Override
        public GsonBuilder getDefaultGsonBuilder()
        {
            return null;
        }

        // ////////////////////////////////////////////////////////////////////////////
        // BUILD
        // ///////////////////////////////////////////////////////////////////////////
        public AlfrescoClientSwagger build()
        {
            // Create Client
            mInstance = super.build();
            return mInstance;
        }
    }

    
    
}
