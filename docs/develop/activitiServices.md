# Alfresco Activiti - Java Client (Early Access)

## NOTE: All the credit for the activiti service go to jmpascal checkout the original [project](https://github.com/Alfresco/alfresco-client-sdk/tree/DBP)

## Introduction
The idea behind this project is to enable developers to interact easily with Alfresco Activiti REST API.
To achieve this goal this project provide a **single JAR** file that can be used in Java or Android applications.

### Status
**Please note this project is currently under development and is available only on Early Access Mode**

## Requirements

### Alfresco
Activiti REST API is available with **Activiti 5.20** and onwards

## Installation

### MAVEN

```xml
<dependency>
  <groupId>org.alfresco.client</groupId>
  <artifactId>activiti</artifactId>
  <version>1.0.0-beta2</version>
</dependency>
```

### GRADLE
```gradle
compile 'org.alfresco.client:activiti:1.0.0-beta2'
```

## Usage

### Create Client/Session Object

**Basic Auth**
```java
ActivitiClient client = new ActivitiClient.Builder().connect(endpoint, username, password).build();
```

**Ticket based (like SAML or Alfresco Ticket)**
```java
ActivitiClient client =  new ActivitiClient.Builder().connectWithTicket(baseUrl, ticketValue).build();
```

**Token based (like oAuth)**
```java
ActivitiClient client =  new ActivitiClient.Builder().connectWithToken(baseUrl, tokenValue).build();
```


### Create Services
```java

 DatabaseTablesAPI databaseTablesAPI = client.getDatabaseTablesAPI();

 DeploymentsAPI deploymentsAPI = client.getDeploymentsAPI();

 EngineAPI engineAPI = client.getEngineAPI();

 ExecutionsAPI executionsAPI = client.getExecutionsAPI();

 FormsAPI formsAPI = client.getFormsAPI();

 GroupsAPI groupsAPI = client.getGroupsAPI();

 HistoryAPI historyAPI = client.getHistoryAPI();

 JobsAPI jobsAPI = client.getJobsAPI();

 ModelsAPI modelsAPI = client.getModelsAPI();

 ProcessDefinitionsAPI processDefinitionsAPI = client.getProcessDefinitionsAPI();

 ProcessInstancesAPI processInstancesAPI = client.getProcessInstancesAPI();

 RuntimeAPI runtimeAPI = client.getRuntimeAPI();

 TasksAPI tasksAPI = client.getTasksAPI();

 UsersAPI usersAPI = client.getUsersAPI();

```

### Retrieve Task by Id

**Synchronuously**
```java
Response<TaskResponse> response = client.getTasksAPI().getTaskCall("123").execute();
```

**Asynchronuously**
```java
 client.getTasksAPI().getTaskCall("123").enqueue(new Callback<TaskResponse>() {
             @Override
             public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                 
             }
 
             @Override
             public void onFailure(Call<TaskResponse> call, Throwable throwable) {
 
             }
         });
```

**RxJava**
```java
client.getTasksAPI().getTaskObservable("123").subscribe(root -> Assert.assertEquals(root.getName(), "Task"));
```

### Task Actions
```java
    // CREATE STANDALONE TASK
    TaskResponse task = tasksAPI.createTaskCall(new TaskRequest("Task 1", "Description 1", "kermit", "kermit")).execute().body();
    
    // ATTACH DOCUMENT
    File file = getResourceFile("/org/alfresco/client/services/process/tests/test.txt");
    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), file);
    MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
    multipartBuilder.addFormDataPart("filedata", "test.txt", requestBody);
    RequestBody fileRequestBody = multipartBuilder.build();
    HashMap<String, RequestBody> map = new HashMap<>();
    map.put("filedata", fileRequestBody);
    map.put("name", RequestBody.create(MediaType.parse("multipart/form-data"), "my-file.txt"));

    Response<AttachmentResponse> attachment = tasksAPI.createAttachmentCall(task.getId(), map).execute();

    //ATTACH EXTERNAL LINK
    AttachmentRequest request = new AttachmentRequest("ExtenalFile", "External", "simpleType", "http://www.activiti.org");
    attachment = tasksAPI.createAttachmentCall(task.getId(), request).execute();
            
    // COMPLETE STANDALONE TASK
    tasksAPI.executeTaskActionCall(task.getId(), new TaskActionRequest(TaskActionRequest.ACTION_COMPLETE));         

```

## Running tests

### Requirements

Tests can be executed only with **Activiti 5.20** and onwards

### Usage 

Tests can be executed with a fresh install on localhost:8080 with user admin/test or kermit/kermit

Want to manage other tests ?

Replace all TEST_X in [Client API: ActivitiAPITestCase](src/test/java/org/alfresco/client/services/process/activiti/core/ActivitiAPITestCase.java)


# Alfresco Digital Business Platform Java Client

## Introduction
The idea behind this project is to enable developers to interact easily with Alfresco Digital Business Platform REST API.
To achieve this goal this project provide a **single JAR** file that can be used in Java or Android applications.

### Status
**Please note this project is currently under development and is available only on Early Access Mode**

## Installation

### MAVEN

```xml
<dependency>
  <groupId>org.alfresco.client</groupId>
  <artifactId>alfresco-platform-client</artifactId>
  <version>1.0.0-beta2</version>
</dependency>
```

### GRADLE
```gradle
compile 'org.alfresco.client:alfresco-platform-client:1.0.0-beta2'
```

## Usage
```java
//Connect to Alfresco CS
AlfrescoClient client = new AlfrescoClient.Builder()
        .connectTo("http://cmis.alfresco.com", ServicesEnum.CONTENT_SERVICES)
        .withUser("admin", "admin")
        .build();

//Connect to Alfresco CS with CSClient Builder
AlfrescoClient client = new AlfrescoClient.Builder()
        .addServiceClientBuilder(new CSClient.Builder().connect("http://cmis.alfresco.com", "admin", "admin"))
        .build();

//Connect to Alfresco CS + GS
AlfrescoClient client = new AlfrescoClient.Builder()()
        .connectTo("http://cmis.alfresco.com", ServicesEnum.CONTENT_SERVICES)
        .addService(ServicesEnum.GOVERNANCE_SERVICES)
        .withUser("admin", "admin")
        .build();

//Connect to Alfresco CS + GS
AlfrescoClient client = new AlfrescoClient.Builder()
        .connectTo("http://cmis.alfresco.com")
        .addService(ServicesEnum.CONTENT_SERVICES)
        .addService(ServicesEnum.GOVERNANCE_SERVICES)
        .withUser("admin", "admin")
        .build();

//Connect to Alfresco CS + PS with same user
AlfrescoClient client = new AlfrescoClient.Builder()
        .addServiceClientBuilder(new CSClient.Builder().connect("http://cmis.alfresco.com", "admin", "admin"))
        .addServiceClientBuilder(new PSClient.Builder().connect("http://localhost:8080/activiti-app", "admin@app.activiti.com", "admin"))
        .build();
        
 //Connect to Alfresco CS + PS with different user      
 AlfrescoClient client = new AlfrescoClient.Builder()
         .addServiceClientBuilder(new CSClient.Builder().connect("http://cmis.alfresco.com", "admin", "admin"))
         .addServiceClientBuilder(new PSClient.Builder().connect("http://localhost:8080/activiti-app", "admin@app.activiti.com", "admin"))
         .build();
```

