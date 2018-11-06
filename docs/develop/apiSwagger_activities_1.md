# API CORE - ACTIVITIES SWAGGER ALFRESCO 5.2

### OLD API (com.alfresco.client.api) CHECK OUT THE OLD DOCUEMTNATION


## NEW API WITH SWAGGER  (com.alfresco.swagger.api)

```java
		  		   AlfrescoClientSwagger clientAlfresco2 = new AlfrescoClientSwagger.Builder().connect(urlAlfresco, username, password,true,false).build();
		   com.alfresco.swagger.api.model.RequestQuery requestQuery = new com.alfresco.swagger.api.model.RequestQuery();
	      
	       retrofit2.Response<ActivityPaging> response2 = clientAlfresco2.getActivitiesAPI().listActivitiesForPerson("marco.tenti", null, null, null, null, null).execute();
	    	
	    		   
	       
	       if(response2==null || !response2.isSuccessful()){
	    	   throw new IOException();
	       }
	
	       // Check Response
	       ActivityPaging resultSet2 = response2.body();
	       if(resultSet2==null) 
	    	   throw new IOException("Response is empty");
	       if(resultSet2.getList()==null) 
	    	   throw new IOException("Response has no entries");
	       if(resultSet2.getList()==null)
	    	   throw new IOException("Response has no Pagination Info");
	       if(resultSet2.getList().getPagination()==null) 
	    	   throw new IOException("Response has no Context");
	       
	       ActivityPagingList resultSetPagingList = resultSet2.getList(); 
		   for(ActivityEntry resultSetRowEntry : resultSetPagingList.getEntries()){
			   Activity node = resultSetRowEntry.getEntry();
			   LinkedTreeMap<String, String> props = new LinkedTreeMap<>();
			   if(node.getActivitySummary()!=null){
				   props = (LinkedTreeMap<String, String>) node.getActivitySummary();
	    		   logger.info(Arrays.toString(props.entrySet().toArray()));
	    	   }else{
	    		   //TODO somecode
	    	   }
			   String nodeId = node.getId().toString();
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