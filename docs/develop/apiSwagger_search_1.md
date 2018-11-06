# API SEARCH SWAGGER ALFRESCO 5.2

## CHECK DIFFERENCE BETWEEN THE OLD API CLIENT EMBEDDED AND THE NEW API CLIENT WITH SWAGGER

### OLD API (com.alfresco.client.api)

```java

   AlfrescoClient clientAlfresco = new AlfrescoClient.Builder().connect(urlAlfresco, username, password,true,false).build();
   com.alfresco.client.api.search.body.RequestQuery query = new com.alfresco.client.api.search.body.RequestQuery().query(queryLucene).language(com.alfresco.client.api.search.body.RequestQuery.LanguageEnum.LUCENE);
   com.alfresco.client.api.search.body.QueryBody body = new com.alfresco.client.api.search.body.QueryBody().query(query);

   // Request
   retrofit2.Response<com.alfresco.client.api.search.model.ResultSetRepresentation<com.alfresco.client.api.search.model.ResultNodeRepresentation>> response = clientAlfresco.getSearchAPI().searchCall(body).execute();
   if(response==null || !response.isSuccessful()){
	   throw new IOException();
   }

   // Check Response
   com.alfresco.client.api.search.model.ResultSetRepresentation<com.alfresco.client.api.search.model.ResultNodeRepresentation> resultSet = response.body();
   if(resultSet==null) 
	   throw new IOException("Response is empty");
   if(resultSet.getList()==null) 
	   throw new IOException("Response has no entries");
   if(resultSet.getPagination()==null)
	   throw new IOException("Response has no Pagination Info");
   if(resultSet.getContext()==null) 
	   throw new IOException("Response has no Context");


   // Check Pagination & Entries
   List<com.alfresco.client.api.search.model.ResultNodeRepresentation> results = resultSet.getList();
  
   for (com.alfresco.client.api.search.model.ResultNodeRepresentation node : results)
   {
	   if(node.getProperties()!=null){
		   logger.info(Arrays.toString(node.getProperties().entrySet().toArray()));
	   }else{
		   //TODO somecode
	   }
	   String nodeId = node.getId();
	   NodeRef nodoDaConservare = null;			
		try{
			nodoDaConservare = new NodeRef(nodeId);
		}catch(MalformedNodeRefException aex){
			try{
				nodoDaConservare = new NodeRef("workspace://SpacesStore/"+nodeId);
			}catch(MalformedNodeRefException aex2){
				throw new MalformedNodeRefException("Errore di riferimento a un nodo  <"+nodeId+">, verifica il nodo : "+ nodeId,aex);				
			}
		}
		refs.add(nodoDaConservare.toString());

   }		

```

## NEW API WITH SWAGGER  (com.alfresco.swagger.api)

```java
	  		   AlfrescoClientSwagger clientAlfresco2 = new AlfrescoClientSwagger.Builder().connect(urlAlfresco, username, password,true,false).build();
		   com.alfresco.swagger.api.model.RequestQuery requestQuery = new com.alfresco.swagger.api.model.RequestQuery();
	       requestQuery.query(queryLucene).language(com.alfresco.swagger.api.model.RequestQuery.LanguageEnum.LUCENE);	       
	       com.alfresco.swagger.api.model.SearchRequest sea = new com.alfresco.swagger.api.model.SearchRequest().query(requestQuery);
	       retrofit2.Response<ResultSetPaging> response2 = clientAlfresco2.getSearchAPI().search(sea).execute();
	   
	   if(response2==null || !response2.isSuccessful()){
		   throw new IOException();
	   }
	
	   // Check Response
	   ResultSetPaging resultSet2 = response2.body();
	   if(resultSet2==null) 
		   throw new IOException("Response is empty");
	   if(resultSet2.getList()==null) 
		   throw new IOException("Response has no entries");
	   if(resultSet2.getList()==null)
		   throw new IOException("Response has no Pagination Info");
	   if(resultSet2.getList().getContext()==null) 
		   throw new IOException("Response has no Context");
	   
	   ResultSetPagingList resultSetPagingList = resultSet2.getList(); 
	   for(ResultSetRowEntry resultSetRowEntry : resultSetPagingList.getEntries()){
		   ResultNode node = resultSetRowEntry.getEntry();
		   LinkedTreeMap<String, Object> props = new LinkedTreeMap<>();
		   if(node.getProperties()!=null){
			   props = (LinkedTreeMap<String, Object>) node.getProperties();
			   logger.info(Arrays.toString(props.entrySet().toArray()));
		   }else{
			   //TODO somecode
		   }
		   String nodeId = node.getId();
		   NodeRef nodoDaConservare = null;			
		   try{
			   nodoDaConservare = new NodeRef(nodeId);
		   }catch(MalformedNodeRefException aex){
			   try{
				   nodoDaConservare = new NodeRef("workspace://SpacesStore/"+nodeId);
			   }catch(MalformedNodeRefException aex2){
				   throw new MalformedNodeRefException("Errore di riferimento a un nodo  <"+nodeId+">, verifica il nodo : "+ nodeId,aex);				
			   }
		   }
		   refs.add(nodoDaConservare.toString());
	   }  


```