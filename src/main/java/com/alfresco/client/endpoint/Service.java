package com.alfresco.client.endpoint;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.alfresco.client.samizerouta.retrofit2.adapter.download.Download;
import com.alfresco.swagger.api.client.CollectionFormats.CSVParams;
import com.alfresco.swagger.api.model.AssociationBody;
import com.alfresco.swagger.api.model.ChildAssociationBody;
import com.alfresco.swagger.api.model.NodeBodyCreate;
import com.alfresco.swagger.api.model.NodeBodyCreateAssociation;
import com.alfresco.swagger.api.model.NodeEntry;
import com.google.gson.annotations.SerializedName;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

public interface Service {
	
	 
	  /**
	   * Async download url
	   * @param url
	   * @return
	   */
	  @Streaming
	  @GET
	  com.alfresco.client.samizerouta.retrofit2.adapter.download.Download.Builder download(@Url String url);
	  
	  
	  //https://github.com/codexpedia/android_retrofit_download_zip/blob/master/app/src/main/java/com/example/retrofitdownloadzip/RetrofitInterface.java
	  
	  /**
	   * Regular Retrofit 2 GET request Sync
	   * @href https://www.codexpedia.com/android/retrofit-2-and-rxjava-for-file-downloading-in-android/
	   * @param fileUrl
	   * @return
	   */
	  @Streaming
	  @GET
	  Call<ResponseBody> downloadFileByUrl(@Url String fileUrl);


	  /**Retrofit 2 GET request for rxjava Sync
	   * @href https://www.codexpedia.com/android/retrofit-2-and-rxjava-for-file-downloading-in-android/
	   * @param fileUrl
	   * @return
	   */
	  @Streaming
	  @GET
	  Observable<Response<ResponseBody>> downloadFileByUrlRx(@Url String fileUrl);

	  //https://stackoverflow.com/questions/44549121/upload-multipart-form-data-to-alfresco-with-php-5-4-3?rq=1
//	  I had a similar issue uploading a file with JavaScript in a browser environment where status 409 with and errorKey No disk space available was returned from the alfresco backend, but the same POST request in Postman or CURL worked without problems.
//
//	  The solution was to not add the HTTP header "content-type: multipart/form-data", because it already contained an multipart/form-data; boundary=---------------------------41184676334 header, if no custom content-type header was added.
//
//	  Refrences:
//
//	      Why it workeds in Postman: Postman adds content-type with boundry automatically
//	      multipart/form-data Syntax with boundary Definition

	  //https://community.alfresco.com/thread/234557-rest-use-a-curfile-object-with-json
	  //UPDATE : Error 400 is normal we need to send the request with 'Content-Type' => 'multipart/form-data'
	  //If i use 'Content-Type' => 'multipart/form-data' in the headers i receive : "errorKey":"No disk space available","statusCode":409,"briefSummary":"10290099 No disk space available" (there is space left i can upload using another script)
	  
	  //@Headers({ HttpHeaders.CONTENT_TYPE+":"+MediaType.MULTIPART_FORM_DATA})
	  /**
	   * Create a node
	   * **Note:** this endpoint is available in Alfresco 5.2 and newer versions.  Create a node and add it as a primary child of node **nodeId**.  This endpoint supports both JSON and multipart/form-data (file upload).  **Using multipart/form-data**  Use the **filedata** field to represent the content to upload, for example, the following curl command will create a node with the contents of test.txt in the test user&#39;s home folder.  &#x60;&#x60;&#x60;curl -utest:test -X POST host:port/alfresco/api/-default-/public/alfresco/versions/1/nodes/-my-/children -F filedata&#x3D;@test.txt&#x60;&#x60;&#x60;  You can use the **name** field to give an alternative name for the new file.  You can use the **nodeType** field to create a specific type. The default is cm:content.  You can use the **renditions** field to create renditions (e.g. doclib) asynchronously upon upload. Note that currently only one rendition can be requested. Also, as requesting rendition is a background process, any rendition failure (e.g. No transformer is currently available) will not fail the whole upload and has the potential to silently fail.  Use **overwrite** to overwrite an existing file, matched by name. If the file is versionable, the existing content is replaced.  When you overwrite existing content, you can set the **majorVersion** boolean field to **true** to indicate a major version should be created. The default for **majorVersion** is **false**. Setting  **majorVersion** enables versioning of the node, if it is not already versioned.  When you overwrite existing content, you can use the **comment** field to add a version comment that appears in the version history. This also enables versioning of this node, if it is not already versioned.  You can set the **autoRename** boolean field to automatically resolve name clashes. If there is a name clash, then the API method tries to create a unique name using an integer suffix.  You can use the **relativePath** field to specify the folder structure to create relative to the node **nodeId**. Folders in the **relativePath** that do not exist are created before the node is created.  Any other field provided will be treated as a property to set on the newly created node.  **Note:** setting properties of type d:content and d:category are not supported.  **Using JSON**  You must specify at least a **name** and **nodeType**. For example, to create a folder: &#x60;&#x60;&#x60;JSON {   \&quot;name\&quot;:\&quot;My Folder\&quot;,   \&quot;nodeType\&quot;:\&quot;cm:folder\&quot; } &#x60;&#x60;&#x60;  You can create an empty file like this: &#x60;&#x60;&#x60;JSON {   \&quot;name\&quot;:\&quot;My text file.txt\&quot;,   \&quot;nodeType\&quot;:\&quot;cm:content\&quot; } &#x60;&#x60;&#x60; You can update binary content using the &#x60;&#x60;&#x60;PUT /nodes/{nodeId}&#x60;&#x60;&#x60; API method.  You can create a folder, or other node, inside a folder hierarchy: &#x60;&#x60;&#x60;JSON {   \&quot;name\&quot;:\&quot;My Special Folder\&quot;,   \&quot;nodeType\&quot;:\&quot;cm:folder\&quot;,   \&quot;relativePath\&quot;:\&quot;X/Y/Z\&quot; } &#x60;&#x60;&#x60; The **relativePath** specifies the folder structure to create relative to the node **nodeId**. Folders in the **relativePath** that do not exist are created before the node is created.  You can set properties when you create a new node: &#x60;&#x60;&#x60;JSON {   \&quot;name\&quot;:\&quot;My Other Folder\&quot;,   \&quot;nodeType\&quot;:\&quot;cm:folder\&quot;,   \&quot;properties\&quot;:   {     \&quot;cm:title\&quot;:\&quot;Folder title\&quot;,     \&quot;cm:description\&quot;:\&quot;This is an important folder\&quot;   } } &#x60;&#x60;&#x60; Any missing aspects are applied automatically. For example, **cm:titled** in the JSON shown above. You can set aspects explicitly, if needed, using an **aspectNames** field.  **Note:** setting properties of type d:content and d:category are not supported.  Typically, for files and folders, the primary children are created within the parent folder using the default \&quot;cm:contains\&quot; assocType.  If the content model allows then it is also possible to create primary children with a different assoc type. For example: &#x60;&#x60;&#x60;JSON {   \&quot;name\&quot;:\&quot;My Node\&quot;,   \&quot;nodeType\&quot;:\&quot;my:specialNodeType\&quot;,   \&quot;association\&quot;:   {     \&quot;assocType\&quot;:\&quot;my:specialAssocType\&quot;   } } &#x60;&#x60;&#x60;   Additional associations can be added after creating a node. You can also add associations at the time the node is created. This is  required, for example, if the content model specifies that a node has mandatory associations to one or more existing nodes. You can optionally  specify an array of **secondaryChildren** to create one or more secondary child associations, such that the newly created node acts as a parent node.  You can optionally specify an array of **targets** to create one or more peer associations such that the newly created node acts as a source node.  For example, to associate one or more secondary children at time of creation: &#x60;&#x60;&#x60;JSON {   \&quot;name\&quot;:\&quot;My Folder\&quot;,   \&quot;nodeType\&quot;:\&quot;cm:folder\&quot;,   \&quot;secondaryChildren\&quot;:     [ {\&quot;childId\&quot;:\&quot;abcde-01234-...\&quot;, \&quot;assocType\&quot;:\&quot;my:specialChildAssocType\&quot;} ] } &#x60;&#x60;&#x60;  For example, to associate one or more targets at time of creation: &#x60;&#x60;&#x60;JSON {   \&quot;name\&quot;:\&quot;My Folder\&quot;,   \&quot;nodeType\&quot;:\&quot;cm:folder\&quot;,   \&quot;targets\&quot;:     [ {\&quot;targetId\&quot;:\&quot;abcde-01234-...\&quot;, \&quot;assocType\&quot;:\&quot;my:specialPeerAssocType\&quot;} ] } &#x60;&#x60;&#x60;  **Note:** You can create more than one child by  specifying a list of nodes in the JSON body. For example, the following JSON body creates two folders inside the specified **nodeId**, if the **nodeId** identifies a folder:  &#x60;&#x60;&#x60;JSON [   {     \&quot;name\&quot;:\&quot;My Folder 1\&quot;,     \&quot;nodeType\&quot;:\&quot;cm:folder\&quot;   },   {     \&quot;name\&quot;:\&quot;My Folder 2\&quot;,     \&quot;nodeType\&quot;:\&quot;cm:folder\&quot;   } ] &#x60;&#x60;&#x60; If you specify a list as input, then a paginated list rather than an entry is returned in the response body. For example:  &#x60;&#x60;&#x60;JSON {   \&quot;list\&quot;: {     \&quot;pagination\&quot;: {       \&quot;count\&quot;: 2,       \&quot;hasMoreItems\&quot;: false,       \&quot;totalItems\&quot;: 2,       \&quot;skipCount\&quot;: 0,       \&quot;maxItems\&quot;: 100     },     \&quot;entries\&quot;: [       {         \&quot;entry\&quot;: {           ...         }       },       {         \&quot;entry\&quot;: {           ...         }       }     ]   } } &#x60;&#x60;&#x60; 
	   * @param nodeId The identifier of a node. You can also use one of these well-known aliases: * -my- * -shared- * -root-  (required)
	   * @param nodeBodyCreate The node information to create. (required)
	   * @param autoRename If true, then  a name clash will cause an attempt to auto rename by finding a unique name using an integer suffix. (optional)
	   * @param include Returns additional information about the node. The following optional fields can be requested: * allowableOperations * association * isLink * isLocked * path * permissions  (optional)
	   * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
	   * @return Call&lt;NodeEntry&gt;
	   */
	  @Multipart
	  @POST("nodes/{nodeId}/children")
	  Call<NodeEntry> createNode(	
			  @retrofit2.http.Path("nodeId") String nodeId, 
			  //@retrofit2.http.Body NodeBodyCreate nodeBodyCreate, 	    
			  @retrofit2.http.Query("autoRename") Boolean autoRename, 
			  @retrofit2.http.Query("include") CSVParams include, 
			  @retrofit2.http.Query("fields") CSVParams fields,
			  @retrofit2.http.PartMap() Map<String, RequestBody> partMap,
			  @retrofit2.http.Part MultipartBody.Part filedata
	  );
	  
}