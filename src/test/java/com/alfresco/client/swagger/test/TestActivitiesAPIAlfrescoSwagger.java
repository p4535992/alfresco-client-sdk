package com.alfresco.client.swagger.test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.soap.SOAPException;

import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;

import com.alfresco.client.AlfrescoClientSwagger;
import com.alfresco.client.utils.DateUtils;
import com.alfresco.swagger.api.model.Activity;
import com.alfresco.swagger.api.model.ActivityEntry;
import com.alfresco.swagger.api.model.ActivityPaging;
import com.alfresco.swagger.api.model.ActivityPagingList;
import com.alfresco.swagger.api.model.ResultNode;
import com.alfresco.swagger.api.model.ResultSetPaging;
import com.alfresco.swagger.api.model.ResultSetPagingList;
import com.alfresco.swagger.api.model.ResultSetRowEntry;


import okhttp3.Response;

public class TestActivitiesAPIAlfrescoSwagger {
	
	public static String DATE_FORMAT_LUCENEQUERY = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestActivitiesAPIAlfrescoSwagger.class);
	
	public static void main(String[] args) throws IOException, GeneralSecurityException, SOAPException {

		Properties props = loadOvverrides();

		//SET PROXY
		if(StringUtils.isNotBlank(props.getProperty("proxyServer"))){
			  	System.setProperty("http.proxySet", "true");
		        System.setProperty("http.proxyHost", props.getProperty("proxyServer"));	       
		        System.setProperty("https.proxyHost",props.getProperty("proxyServer"));
			if(StringUtils.isNotBlank(props.getProperty("proxyPort"))){

				System.setProperty("http.proxyPort", props.getProperty("proxyPort"));
				System.setProperty("https.proxyPort", props.getProperty("proxyPort"));
			}else{
				System.setProperty("http.proxyPort", "80");
				System.setProperty("https.proxyPort", "80");
			}
		}
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
		System.setProperty("jsse.enableSNIExtension","false");
		String numeroRepertorio =  "262";
		Date dataReperorio = new Date();
		String registroRepertorio = "AT";
		String dateHalley = DateUtils.dateToString(dataReperorio,"yyyy-MM-dd");
		List<String> docPrincipaliTrovati  = queryDocPrincipale(numeroRepertorio, dateHalley,registroRepertorio);
		
		
		logger.info(Arrays.toString(docPrincipaliTrovati.toArray()));
	}

	private static List<String> queryDocPrincipale(String numeroRepertorio,String dataRepertorio, String registroRepertorio) throws IOException, KeyManagementException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException{
		String username = "YYY";
		String password = "XXX";
		String urlAlfresco = "https://localhost:8443/alfresco"; //ovverride.getProperty("url.alfresco");

		List<String> refs = new ArrayList<>();
		try{

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
			   Map<String, String> props = new HashMap<>();
			   if(node.getActivitySummary()!=null){
				   props = (Map<String, String>) node.getActivitySummary();
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
	
		}catch(Exception ex){
			throw new IOException("[QUERY PRINCIPALE] Errore in fase di invio della query verso alfresco",ex);
		}
	   return refs;
	}
	
	
	
	
	private static Properties ovverride;
	public static String loadOvverrideProperty(String propertyKey){
		loadOvverrides();
		return ovverride.getProperty(propertyKey);
	}

	public static Properties loadOvverrides(){
		if(ovverride==null){
			ovverride = new Properties();
			String evironment = System.getProperty("nome.installazione");
			try {
				ovverride.load(ClassLoader.getSystemResourceAsStream("test.properties"));
			} catch(Exception e) {
				logger.error("Impossibile trovare il file di configurazione dell'evironment '"+evironment+"'");
				e.printStackTrace();			
			}
		}
		return ovverride;
	}

}
