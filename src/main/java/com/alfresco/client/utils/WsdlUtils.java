package com.alfresco.client.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLStreamHandler;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.wsdl.Port;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Questa classe contiene metodi di utility per sfruttare in modo gia' testato
 * le operazioni piu' comuni delle librerie SOAP native di java o di Apache CXF e Apache Axis2
 *
 */
public class WsdlUtils {
	
	 private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WsdlUtils.class);
	 private static final String CHARSET = "UTF-8";
	 private static final MessageFactory MSG_FACTORY;
	 
	 static {
		 try {
			 MSG_FACTORY = MessageFactory.newInstance();
		 } catch (SOAPException e) {
			 throw new IllegalStateException(e);
		 }
	 }
	 
	/**
     * Costruisci un client JAXB da un documento WSDL con CXF e/o in alternativa con JAXB
     * NOTA: testato sule classi generati da cxf
	 * @param <T> è un interfaccia di classe di servizio xml generate da cxf o xjt 
     * @param endpointWsdl stringa url di endpoint del servizio wsdl puo essere sia il file che la risorsa online
     * @param username stringa username autenticazione
     * @param password stringa password autenticazione
     * @param serviceClass la classe di servizio generata da CXF
     * @param ignoreSSLCertificate se true disabilita la verifica certificati SSL
     * @param useAuthorizationBasic se true inserisce gli header per authentication basic
     * @param supplierheaders gli header aggiuntivi per la richiesta SOAP
     * @param useMTOM se true abiltia uso modalita MTOM
     * @param forcePrintSOAPOnLog se true scrive sulla console le richieste e risposte SOAP 
     * @param forcePrintSOAPOnFile  se true scrive su file temporanei le richieste e risposte SOAP
     * @param SERVICE_NAME qname del servizio client
     * @param SERVICE_NAME_PORT qname del servizio server (getPort)
     * @param proxyHost indirizzo di rete del proxy
     * @param proxyPort numero della porta del proxy
     * @return 
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws SOAPException 
     * @throws IOException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "restriction" })
	public static <T> T buildServerWsdl(String endpointWsdl,final String username,final String password,
    		final Class<T> serviceClass,boolean ignoreSSLCertificate,boolean useAuthorizationBasic,
    		Map<String,String> supplierheaders,boolean useMTOM,boolean forcePrintSOAPOnLog,boolean forcePrintSOAPOnFile,
    		javax.xml.namespace.QName SERVICE_NAME,javax.xml.namespace.QName SERVICE_NAME_PORT,
    		String proxyHost,String proxyPort) 
    	    throws NoSuchAlgorithmException, KeyManagementException, SOAPException, IOException{
    	
    	
    	logger.info("BuildClientWsdl ["
    	+ "endpointWsdl=" + endpointWsdl 
    	+ ", username=" + username 
    	+ ", password=" + StringUtils.isNotBlank(password)
		+ ", serviceClass=" + serviceClass 
		+ ", ignoreSSLCertificate=" + ignoreSSLCertificate
		+ ", useAuthorizationBasic=" + useAuthorizationBasic 
		+ ", supplierheaders=" + supplierheaders
		+ ", useMTOM=" + useMTOM 
		+ ", forcePrintSOAPOnLog=" + forcePrintSOAPOnLog 
		+ ", forcePrintSOAPOnFile=" + forcePrintSOAPOnFile
		+ ", SERVICE_NAME=" + SERVICE_NAME
		+ ", SERVICE_NAME_PORT=" + SERVICE_NAME_PORT 
		+ ", proxyHost=" + proxyHost 
		+ ", proxyPort=" + proxyPort
		+ "]");
  	
    	System.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
    	Long timeout = new Long(600000); //60 sec
    	//Controllo wsdlurl
    	URL wsdlURL;
        java.io.File wsdlFile = new java.io.File(endpointWsdl);

        if (wsdlFile.exists()) {
            wsdlURL = wsdlFile.toURI().toURL();
        } else {
            wsdlURL = new URL(endpointWsdl);
        }

    	T server = null;
    	try{	    	
    		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();			
        	factory.setServiceClass(serviceClass); //factory.setServiceClass(javax.xml.ws.Service.class);	 		
        	factory.setAddress(endpointWsdl); //factory.setAddress("http://server.service.core.eng.it/");
        	//factory.setServiceBean(implementor);
        	//Abilita il loggin in ingresco ed uscita dei messaggi soap!
        	//TODO qui
        	if(forcePrintSOAPOnLog){
        		//STAMPA SU LOG LA RICHIESTA
	        	factory.getInInterceptors().add(new LoggingInInterceptor(4*1024));        	
	        	//STAMPA SU LOG LA RISPOSTA
	        	factory.getOutInterceptors().add(new LoggingOutInterceptor(4*1024));	        	
        	}
        	if(forcePrintSOAPOnFile){
        		//STAMPA SU FILE TEMPORANEO LA RICHIESTA
	        	factory.getInInterceptors().add(new SOAPRequestFileLoggerInterceptor());
        		//STAMPA SU FILE TEMPORANEO LA RISPOSTA
	        	factory.getOutInterceptors().add(new SOAPResponseFileLoggerInterceptor());
        	}
        	
	    	server = (T) factory.create();	
	    	//Client cl = ClientProxy.getClient(server);    
	    	Client cl = ClientProxy.getClient(server);
	    	
	    	HTTPConduit httpConduit = (HTTPConduit) cl.getConduit();

	    	//disable ssl certificate handshake
	    	if(ignoreSSLCertificate){
	    		String targetAddr = httpConduit.getTarget().getAddress().getValue();
	    		if (targetAddr.toLowerCase().startsWith("https:")) {
	    			TLSClientParameters tlsParams = new TLSClientParameters();
	    			tlsParams.setTrustManagers(setSystemTrustAllCertificateSSL());//TRUST ALL CERTIFICATE    			
	    			tlsParams.setDisableCNCheck(true); //TRUST ALL CN
	    			tlsParams.setHostnameVerifier(setSystemTrustAllHost());; //TRUST ALL HOST
	    			httpConduit.setTlsClientParameters(tlsParams);
	    		}
	    	}
	    	
	    	//SETTAGGIO PROXY
			if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null) {
				httpConduit.getClient().setProxyServer(proxyHost);
				httpConduit.getClient().setProxyServerPort(Integer.parseInt(proxyPort));
			}
			if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)){
		    	AuthorizationPolicy authorizationPolicy = httpConduit.getAuthorization();
		    	authorizationPolicy.setUserName(username);
		    	authorizationPolicy.setPassword(password);
			}

	    	HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
	    	httpClientPolicy.setConnectionTimeout(timeout);//60sec  con 0 setti timeout infinito
	    	httpClientPolicy.setReceiveTimeout(timeout);//60sec  con 0 setti timeout infinito
	    	    	
	    	//ATTENZIONE SE ARRIVA ERROR CON CODICE 415 sid eve settare un content type qui
	    	//httpClientPolicy.setContentType("application/soap+xml"); 
	    	httpClientPolicy.setContentType("text/xml"); 
	    	
	    	//Attenzione per errore  org.apache.cxf.transport.http.HTTPException: HTTP response '415: Unsupported Media Type'
	    	//qualcosa non va con encoding
	    	//httpClientPolicy.setAcceptEncoding("UTF-8");

	    	//httpClientPolicy.setConnection(ConnectionType.CLOSE);
	    	//httpClientPolicy.setMaxRetransmits(1);

	    	httpConduit.setClient(httpClientPolicy);   
	    	
	    	//=============================================================================================
	    	// Set up WS-Security Encryption, Reference: https://ws.apache.org/wss4j/using.html
	        Map<String, Object> outProps = new HashMap<String, Object>();
	        //props.put(WSHandlerConstants.USER, "testkey");
	        //props.put(WSHandlerConstants.ACTION, WSHandlerConstants.ENCRYPT);
	        //props.put(WSHandlerConstants.PASSWORD_TYPE, "PasswordText");
	        //props.put(WSHandlerConstants.ENC_PROP_FILE, "clientKeystore.properties");
	        //props.put(WSHandlerConstants.ENCRYPTION_PARTS, "{Content}{http://schemas.xmlsoap.org/soap/envelope/}Body");
	        //props.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordCallback.class.getName());
	        //props.put(WSHandlerConstants.ADD_INCLUSIVE_PREFIXES,false);
	        //props.put(ConfigurationConstants.EXPAND_XOP_INCLUDE_FOR_SIGNATURE, false);
	        
	        //inProps.put("expandXOPIncludeForSignature", false);
	        //inProps.put("expandXOPInclude", false);
	        //WSS4JOutInterceptor wss4jOut = new WSS4JOutInterceptor(inProps);

	        //ClientProxy.getClient(client).getOutInterceptors().add(wss4jOut);
	        //cl.getInInterceptors().add(wss4jOut);
	        //cl.getOutInterceptors();
	        //==============================================================================================
	    	
	        //org.apache.cxf.endpoint.Endpoint cxfEndpoint = cl.getEndpoint();
		    //Map<String, Object> outProps= new HashMap<String, Object>();
		    //outProps.put(WSHandlerConstants.ACTION,WSHandlerConstants.USERNAME_TOKEN + ' ' + WSHandlerConstants.TIMESTAMP);
		    //outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		    //outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordHandler.class.getName());
		    //outProps.put(WSHandlerConstants.USER, username);

		    //PhaseInterceptor<SoapMessage> wssOut = new WSS4JOutInterceptor(outProps);
		    //cxfEndpoint.getOutInterceptors().add(wssOut);
		    //cxfEndpoint.getOutInterceptors().add(new SAAJOutInterceptor());
	        
	        // Alternative CXF interceptor config method https://glenmazza.net/blog/entry/cxf-usernametoken-profile
	        if(StringUtils.isNotBlank(username)){	        	
	        	outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
	        	outProps.put(WSHandlerConstants.USER, username);	        
		        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
		        outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordHandler.class.getName());
				//Add this to your properties if you are using WSS4J < 2.0:			
			    //outProps.put(WSHandlerConstants.ADD_UT_ELEMENTS, WSConstants.NONCE_LN + " " + WSConstants.CREATED_LN);			
				//if using WSS4J >= 2.0 then it should be:
		        outProps.put(WSHandlerConstants.ADD_USERNAMETOKEN_NONCE, "true");
		        outProps.put(WSHandlerConstants.ADD_USERNAMETOKEN_CREATED, "true");
			    
		        outProps.put(WSHandlerConstants.MUST_UNDERSTAND, "false");
		        outProps.put(WSHandlerConstants.TTL_USERNAMETOKEN, "900");
		        outProps.put(WSHandlerConstants.TTL_FUTURE_USERNAMETOKEN, "900"); 
    		}
	        if(StringUtils.isNotBlank(password)){
		        ClientPasswordHandler handler = new ClientPasswordHandler(password);
			    outProps.put(WSHandlerConstants.PW_CALLBACK_REF, handler);
	        }
		    Endpoint cxfEndpoint = cl.getEndpoint();	    
		    WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
		    cxfEndpoint.getOutInterceptors().add(wssOut);
    	}catch(java.lang.NoSuchMethodError ne){
    		//GETISCE ERRORI PROBLEMI MAVEN
   		    //EVITA I PROBLEMI DERIVANTI DA COLLISIONI CON CXF E ALFRESCO (org.apache.ws.xmlschema.xml-schema-core e org.apache.ws.xmlschema.XmlSchema)
    		if(SERVICE_NAME != null && SERVICE_NAME_PORT != null){
        		 logger.warn("Problemi collisione dipendenze cxf si utilizza la libreria java standard");
		    	 //https://www.programcreek.com/java-api-examples/index.php?source_dir=jbossws-cxf-master/modules/testsuite/cxf-tests/src/test/java/org/jboss/test/ws/jaxws/cxf/jms/DeploymentTestServlet.java
		         javax.xml.ws.Service service = javax.xml.ws.Service.create(wsdlURL, SERVICE_NAME);    	         
//	    	         java.lang.reflect.Field delegateField = javax.xml.ws.Service.class.getDeclaredField("delegate");
//	    	         delegateField.setAccessible(true);
//	    	         javax.xml.ws.spi.ServiceDelegate previousDelegate = (javax.xml.ws.spi.ServiceDelegate)delegateField.get(service);
//	    	         if(!previousDelegate.getClass().getName().contains("cxf")) {
//	    	        	 javax.xml.ws.spi.ServiceDelegate serviceDelegate = ((javax.xml.ws.spi.Provider) Class.forName("org.apache.cxf.jaxws.spi.ProviderImpl").newInstance())
//	    	                 .createServiceDelegate(wsdlURL, SERVICE_NAME, service.getClass());
//	    	             System.out.println("The " + service.getClass().getSimpleName() + " delegate is changed from " + "[" + previousDelegate + "] to [" + serviceDelegate +  "]");
//	    	             delegateField.set(service, serviceDelegate);
//	    	         }     	        		   	         
		    	  server = (T) service.getPort(SERVICE_NAME_PORT, serviceClass);    
    		
    		//DA TESTARE
    		//}else if(SERVICE_NAME != null && wsServiceClass != null){    			
    		//	 javax.xml.ws.Service service = buildServiceWsdl(endpointWsdl, SERVICE_NAME.getNamespaceURI(), SERVICE_NAME.getLocalPart(), wsServiceClass);
    		//	 
    		}else{
        		throw new SOAPException("Problemi collisione dipendenze cxf con alfresco (schema-core,XMlSchema) setta i servizi QNAME per utilizzare la java standard");
    		}
    		//disable ssl certificate handshake
		    if(ignoreSSLCertificate){
	    		String targetAddr = wsdlURL.toString();
	    		if (targetAddr.toLowerCase().startsWith("https:")) {
	    			SSLContext sc = SSLContext.getInstance("SSL");
	    			sc.init(null, setSystemTrustAllCertificateSSL(), new SecureRandom());//TRUST ALL CERTIFICATE 
	    		    HttpsURLConnection.setDefaultHostnameVerifier(setSystemTrustAllHost());//TRUST ALL HOST
	    		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    			
	    		}
		   }
    	}//FINE CATCH NO APACHE CXF

    	// The BindingProvider interface provides access to the protocol binding and
    	// to the associated context objects for request and response message processing.
    	BindingProvider prov = (BindingProvider)server;
    	
    	SOAPBinding binding = (SOAPBinding) prov.getBinding();     	
    	//DISABILIAT/ABILITA MTOM
    	binding.setMTOMEnabled(useMTOM);   	
    	//Add handlers to the binding jaxb 
    	java.util.List<javax.xml.ws.handler.Handler> handlers = binding.getHandlerChain();
    	if(forcePrintSOAPOnLog){    	
	    	handlers.add(new JaxWsLoggingHandler());	    	  	
    	}
    	if(forcePrintSOAPOnFile){
    		handlers.add(new JaxWsLoggingFileHandler());
    	}
    	binding.setHandlerChain(handlers);
    	
    	Map<String, Object> req_ctx = prov.getRequestContext();
    	req_ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointWsdl);
        req_ctx.put("com.sun.xml.ws.developer.JAXWSProperties.CONNECT_TIMEOUT", timeout);
    	req_ctx.put("com.sun.xml.ws.connect.timeout", timeout);
    	req_ctx.put("com.sun.xml.ws.internal.connect.timeout", timeout);
    	req_ctx.put("com.sun.xml.ws.request.timeout", timeout); 
    	req_ctx.put("com.sun.xml.internal.ws.request.timeout", timeout);//https://www.javatips.net/blog/cxf-java-net-sockettimeoutexception-read-timed-out
    	
    	//SETTAGGIO PROXY https://www.ibm.com/support/knowledgecenter/en/SSEQTP_9.0.0/com.ibm.websphere.base.doc/ae/twbs_configwbsclient2webproxy.html
    	if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null) {
	    	req_ctx.put("https.proxyHost", proxyHost);
	    	req_ctx.put("https.proxyPort", proxyPort);
	    	req_ctx.put("http.proxyHost", proxyHost);
	    	req_ctx.put("http.proxyPort", proxyPort);
    	}
		
    	Map<String, List<String>> headers = new HashMap<String, List<String>>();
    	//headers.put("Content-Type", Arrays.asList("text/xml")); //necessario specificare se si usa schema-core invece di XmlSchema

    	// WS-SecurityPolicy configuration method https://glenmazza.net/blog/entry/cxf-usernametoken-profile
    	if(StringUtils.isNotBlank(username)){	
    		headers.put("Username", Arrays.asList(username));
    		//headers.put("Password", Arrays.asList(password)); 		
    		req_ctx.put(BindingProvider.USERNAME_PROPERTY, username);
    		//req_ctx.put(BindingProvider.PASSWORD_PROPERTY, password);	
    		req_ctx.put("ws-security.username", username);
    		req_ctx.put("ws-security.callback-handler", ClientPasswordHandler.class.getName());
            // instead of above line can also do:
    		//req_ctx.put("ws-security.password", password);
    	}
    	if(StringUtils.isNotBlank(password)){
    		headers.put("Password", Arrays.asList(password));
    		req_ctx.put(BindingProvider.PASSWORD_PROPERTY, password);
    		req_ctx.put("ws-security.password", password);
    	}
    	
    	//OTHER PROPERTIES
    	
    	//FILE TO ELEMENT
        //InputStream clientPolicy = serviceClass.getResourceAsStream("webservices-client.xml");
		// DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    // builderFactory.setValidating(false);
	    // builderFactory.setNamespaceAware(true);
	    // builderFactory.setIgnoringElementContentWhitespace(true);
	    // builderFactory.setIgnoringComments(true);
	    // Element element = builderFactory.newDocumentBuilder().parse(clientPolicy).getDocumentElement();
        //prov.put(ClientConstants.CLIENT_CONFIG, element);
		
		//Add some configuration 
	 
		//prov.put(ClientConstants.WSS_KEYSTORE_TYPE, "JKS");
		//prov.put(ClientConstants.WSS_KEYSTORE_LOCATION, "D:\\default-keystore.jks");
		//prov.put(ClientConstants.WSS_KEYSTORE_PASSWORD, "welcome1");
		
		//req_ctx.put("ws-security.store.bytes.in.attachment", "false");
		//prov.getRequestContext().put("mtom-enabled", "false");
		
		//prov.getRequestContext().put("org.apache.cxf.http.no_io_exceptions", "true");
		//prov.getRequestContext().put("org.apache.cxf.transport.no_io_exceptions", "true"); //for the latest cxf version
    		
    	if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)){	
    		Authenticator myAuth = new Authenticator() {
    			@Override
    			protected PasswordAuthentication getPasswordAuthentication() {
    				return new PasswordAuthentication(username, password.toCharArray());
    			}
    		};
    		Authenticator.setDefault(myAuth);		    		    
    	}
    	    	
    	//ADD HEADERS
    	
    	if(supplierheaders !=null &&  supplierheaders.size() > 0){
			prov.getRequestContext().putAll(supplierheaders);
			for(Map.Entry<String, String> entry : supplierheaders.entrySet()){
				headers.put(entry.getKey(), Arrays.asList(entry.getValue()));
			}
		}

    	if(useAuthorizationBasic && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)){
    		String authorization = new String(Base64.getEncoder().encode((username+":"+password).getBytes()));
    		headers.put("Authorization", Arrays.asList("Basic " + authorization));
    		req_ctx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    		//MessageContext mctx = wsctx.getMessageContext();
    		Map<String, List<String>> http_headers = (HashMap<String, List<String>>) req_ctx.get(MessageContext.HTTP_REQUEST_HEADERS);
    		List list = (List) http_headers.get("Authorization");
    		if (list == null || list.size() == 0) {
    			throw new RuntimeException("Authentication failed! This WS needs BASIC Authentication!");
    		}

    		String userpass = (String) list.get(0);
    		userpass = userpass.substring(5);
    		byte[] buf = org.apache.commons.codec.binary.Base64.decodeBase64(userpass.getBytes());
    		String credentials = new String(buf);		  
    		String usernamex = null;
    		String passwordx = null;
    		int p = credentials.indexOf(":");
    		if (p > -1) {
    			usernamex = credentials.substring(0, p);
    			passwordx = credentials.substring(p+1);
    		}   
    		else {
    			throw new RuntimeException("There was an error while decoding the Authentication!");
    		}
    		// This should be changed to a DB / Ldap authentication check 
    		if (usernamex.equals(username) && passwordx.equals(password)) { 			 
    			//logger.debug("============== Authentication Basic OK =============");
    		}
    		else {
    			throw new RuntimeException("Authentication failed! Wrong username / password!");
    		}
    	} 
    	  
    	return server;
	 		
    }
    
    /**
	 * Costruisci un servizio javax.xml.ws.Service per il servizio wsdl
	 */
	public static <T extends javax.xml.ws.Service> T buildServiceWsdl(String endpointWsdl,String namespaceUri,String localPart,Class<T> serviceClass) {
		try {
			URL wsdlUrl = new URL(endpointWsdl);
			javax.xml.namespace.QName SERVICE_NAME = new javax.xml.namespace.QName(namespaceUri, localPart);		

			Constructor<?> cons = serviceClass.getConstructor(URL.class,javax.xml.namespace.QName.class);
			@SuppressWarnings("unchecked")
			T service = (T) cons.newInstance(wsdlUrl,SERVICE_NAME);
			return service;
		} catch (MalformedURLException e) {			
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (java.lang.IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	/**
	 * Metodo molto utile epr caricare in modo forzato wsdl da macchine esterne all'esb
	 * @param clazz la classe del servizio web
	 * @param relativePathUnderMetaInf la path relativa al file wsdl fisico sotto la cartella META-INF del
	 * @return  url of the wsdl
	 */
	public static URL forceToLoadUrlFromWebService(Class<? extends Service> clazz,String relativePathUnderMetaInf){
		URL url = null;
		String urls = null;
		try {
			//urls = "classpath:META-INF/wsdl/ArubaSignService.wsdl";
			urls ="classpath:META-INF/"+relativePathUnderMetaInf;
			try{
				//URL.setURLStreamHandlerFactory(ConfigurableStreamHandlerFactory.class);
				url = new URL(urls);
			}catch(Throwable e){
				url = new URL(null, 
						urls, 
						new Handler(java.lang.ClassLoader.getSystemClassLoader()));
			}      	
		} catch (MalformedURLException e) {
			logger.error("Can not initialize the default wsdl from {0} to {1}", new Object[]{urls,url});
		}
		return url;
	}
	
	// =======================================================================
	// CONVERTER
	// =========================================================================
	
    public static <T> void toConsole(T jaxbObject) throws JAXBException{
    	JAXBContext jaxbContext = JAXBContext.newInstance(jaxbObject.getClass());
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter stringWriter = new StringWriter();
		marshaller.marshal(jaxbObject, System.out );
		logger.debug(stringWriter.toString());
    }
    
    public static <T> java.io.File toFile(T jaxbObject,java.io.File fileOutput) throws JAXBException, IOException{
    	//JAXBContext jaxbContext = JAXBContext.newInstance(jaxbObject.getClass());
		//Marshaller marshaller = jaxbContext.createMarshaller();
		//marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		//marshaller.marshal(jaxbObject, fileOutput);

		JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());
        Marshaller m = context.createMarshaller();          
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);       
        FileOutputStream fos = new FileOutputStream(fileOutput); 
        try{
        	m.marshal(jaxbObject, fos);   
        }finally{
        	fos.close();
        }
		return fileOutput;
    }
    
    public static <T> String toXML(T jaxbObject) throws JAXBException{
    	JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		//marshaller.marshal(sip, System.out);
		StringWriter stringWriter = new StringWriter();
		marshaller.marshal(jaxbObject, stringWriter );
		return stringWriter.toString();
    }       
    
    @SuppressWarnings("unchecked")
	public static <T> T toXmlJavaObject(File xmlfile,Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller un = context.createUnmarshaller();
            T emp = (T) un.unmarshal(xmlfile);
            return emp;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @href https://stackoverflow.com/questions/17853541/java-how-to-convert-a-xml-string-into-an-xml-file
     */
	public static File toXMLFile(String xmlSource,String filename) 
	        throws SAXException, ParserConfigurationException, IOException, TransformerException {
		File tmpDir = TempFileProvider.getTempDir();
		String outputFolderName = getTodaysFolderName("dumps");	        
		String absolutefolderName = tmpDir.getAbsolutePath() + File.separator + outputFolderName;
		File outputFolder = new File(absolutefolderName);
		if(!outputFolder.exists())outputFolder.mkdirs();        
		File outputFile = TempFileProvider.createTempFile(filename,".xml",outputFolder);        
	    // Parse the given input
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(new InputSource(new StringReader(xmlSource)));
	    // Write the parsed document to an xml file
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer tf = transformerFactory.newTransformer();
	    tf.setOutputProperty(OutputKeys.INDENT, "yes");
		tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    DOMSource source = new DOMSource(doc);	    
	    StreamResult result =  new StreamResult(outputFile);
	    tf.transform(source, result);
	    return outputFile;
	}

	
	
	@SuppressWarnings("unchecked")
	public static <T> JAXBElement<T> toJaxbElement(javax.xml.namespace.QName qName,T objectJabx){
		 return new JAXBElement<T>(qName, (Class<T>) objectJabx.getClass(), null, objectJabx);
	}

	// ==============================================================================
	// STATIC CLASS
	// ===============================================================================
	
	/**
	 * SOAP logging interceptor that extends the functionality provided by
	 * LoggingIn    Interceptor, and logs to a designated folder
	 * @href https://gist.github.com/barbietunnie/c4902df00937d3894da702cb6f45dfbf
	 * @author babatunde.adeyemi
	 */
	public static class SOAPRequestFileLoggerInterceptor extends LoggingInInterceptor {

	    @Override
	    public boolean isPrettyLogging() {
	        return true;
	    }
	    
	    @Override
	    protected void writePayload(StringBuilder builder, CachedOutputStream cos,String encoding, String contentType) throws Exception {
	        // Save the outputstream to file
	        String filename = "soap.request-" + TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis());
	        saveLogToFile("dumps", filename, ".xml", cos);        
	        super.writePayload(builder, cos, encoding, contentType); // dumps to sysout
	    }
	    
	    /**
	     * Gets the recursive folder path that represents today
	     * 
	     * @param rootFolder The root folder
	     * @return The recursive folder name
	     */
	    private static String getTodaysFolderName(String rootFolder) {
	        Calendar cal = Calendar.getInstance();	        
	        StringBuilder sb = new StringBuilder();
	        sb.append(rootFolder);
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.YEAR));
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.MONTH));
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.DAY_OF_MONTH));
	        sb.append(File.separator);
	        return sb.toString();
	    }
	    
	    /**
	     * Save the contents in the CachedOutputSTream to file
	     * 
	     * @param rootFolder The root folder
	     * @param filename The file to write to
	     * @param fileExt The file extension, with the preceding dot included
	     * @param cos The CahedOutputStream reference
	     * @throws FileNotFoundException
	     * @throws IOException 
	     * @throws SOAPException 
	     * @throws TransformerException 
	     */
	    private static void saveLogToFile(String rootFolder, String filename, String fileExt, CachedOutputStream cos) throws FileNotFoundException, IOException, SOAPException, TransformerException {	        
	    	try{	    	
		    	File tmpDir = TempFileProvider.getTempDir();
		    	String outputFolderName = getTodaysFolderName(rootFolder);	        
		        String absolutefolderName = tmpDir.getAbsolutePath() + File.separator + outputFolderName;
		        File outputFolder = new File(absolutefolderName);
		        if(!outputFolder.exists())outputFolder.mkdirs();        
		        File outputFile = TempFileProvider.createTempFile(filename,fileExt,outputFolder);        
		        CloseShieldOutputStream fos = new CloseShieldOutputStream(new FileOutputStream(outputFile));        
		        try {
		            cos.writeCacheTo(fos);	            
		            SOAPMessage soapMessage = toSoapMessage(new FileInputStream(outputFile));
		            //SOAPMessage soapMessage = toSoapMessage(new BOMInputStream(new FileInputStream(outputFile)));
		            TransformerFactory tff = TransformerFactory.newInstance();
					Transformer tf = tff.newTransformer();
					// Set formatting
					tf.setOutputProperty(OutputKeys.INDENT, "yes");
					tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
					tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					Source sc = soapMessage.getSOAPPart().getContent();
					StreamResult result = new StreamResult(outputFile);
					tf.transform(sc, result);	
		        } finally {
		            if(fos != null)
		                fos.close(); // close the file
		        }
	    	}catch(javax.xml.transform.TransformerException xe){
	        	if(xe.getMessage().contains("Content is not allowed in prolog")){
	        		logger.error(xe.getMessage(),xe);
	        	}else{
	        		xe.printStackTrace();
	        	}
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }      
	    }
	}
	

	/**
	 * SOAP logging interceptor that extends the functionality provided by
	 * LoggingOutInterceptor, and logs to a designated folder
	 * @href https://gist.github.com/barbietunnie/c4902df00937d3894da702cb6f45dfbf
	 * @author babatunde.adeyemi
	 */
	public static class SOAPResponseFileLoggerInterceptor extends LoggingOutInterceptor {
	
	    @Override
	    public boolean isPrettyLogging() {
	        return true;
	    }
	    
	    @Override
	    protected void writePayload(StringBuilder builder, CachedOutputStream cos,String encoding, String contentType)throws Exception {    
	        // Save the outputstream to file
	        String filename = "soap.response-" + TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis());
	        saveLogToFile("dumps", filename, ".xml", cos);
	        super.writePayload(builder, cos, encoding, contentType); // dumps to sysout
	    }
	    
	    /**
	     * Gets the recursive folder path that represents today
	     * 
	     * @param rootFolder The root folder
	     * @return The recursive folder name
	     */
	    private static String getTodaysFolderName(String rootFolder) {
	        Calendar cal = Calendar.getInstance();	        
	        StringBuilder sb = new StringBuilder();
	        sb.append(rootFolder);
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.YEAR));
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.MONTH));
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.DAY_OF_MONTH));
	        sb.append(File.separator);
	        return sb.toString();
	    }
	    
	    /**
	     * Save the contents in the CachedOutputSTream to file
	     * 
	     * @param rootFolder The root folder
	     * @param filename The file to write to
	     * @param fileExt The file extension, with the preceding dot included
	     * @param cos The CahedOutputStream reference
	     * @throws FileNotFoundException
	     * @throws IOException 
	     * @throws SOAPException 
	     * @throws TransformerException 
	     */
	    private static void saveLogToFile(String rootFolder, String filename, String fileExt, CachedOutputStream cos) throws FileNotFoundException, IOException, SOAPException, TransformerException {	        
	    	try{
		    	File tmpDir = TempFileProvider.getTempDir();
		    	String outputFolderName = getTodaysFolderName(rootFolder);	        
		        String absolutefolderName = tmpDir.getAbsolutePath() + File.separator + outputFolderName;
		        //System.out.println("Output Location: " + absolutefolderName + "\n\n\n"); // debug
		        // Create the directory if it doesn't already exist
		        File outputFolder = new File(absolutefolderName);
		        if(!outputFolder.exists())outputFolder.mkdirs();        
		        File outputFile = TempFileProvider.createTempFile(filename,fileExt,outputFolder);        
		        CloseShieldOutputStream fos = new CloseShieldOutputStream(new FileOutputStream(outputFile));  
		        try {
		            cos.writeCacheTo(fos);		           
		            SOAPMessage soapMessage = toSoapMessage(new FileInputStream(outputFile));
		            //SOAPMessage soapMessage = toSoapMessage(new BOMInputStream(new FileInputStream(outputFile)));
		            TransformerFactory tff = TransformerFactory.newInstance();
					Transformer tf = tff.newTransformer();
					tf.setOutputProperty(OutputKeys.INDENT, "yes");
					tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
					tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					Source sc = soapMessage.getSOAPPart().getContent();
				
					StreamResult result = new StreamResult(outputFile);
					tf.transform(sc, result);	
		        } finally {
		            if(fos != null)
		                fos.close(); // close the file
		        }
	    	}catch(javax.xml.transform.TransformerException xe){
	        	if(xe.getMessage().contains("Content is not allowed in prolog")){
	        		logger.error(xe.getMessage(),xe);
	        	}else{
	        		xe.printStackTrace();
	        	}
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }      
	    }
	}
	
//  NON CANCELLARE	
//	@SuppressWarnings("unused")
//	static class JaxWsHandlerResolver implements HandlerResolver {
//		 
//		private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JaxWsHandlerResolver.class);
//	   
//		@SuppressWarnings("rawtypes")
//	    @Override
//	    public List<Handler> getHandlerChain(PortInfo arg0) {
//	        List<Handler> hchain = new ArrayList<Handler>();
//	        hchain.add(new JaxWsLoggingHandler());
//	        return hchain;
//	    }
//	 
//	}

// NON CANCELLARE
//	/**
//	 * It logs Request and Response SOAP Messages.
//	 * Create your own Logging Handler class.
//	 * This class is actual class that gets invoked every time Request comes in and goes out. Sample
//	 * @href http://jayeshpokar.blogspot.com/2013/11/log-request-and-response-soap-using_8330.html
//	 * @author jayesh.patel
//	 *
//	 */
//	public class JaxWSLoggingAxis2Handler extends org.apache.axis2.handlers.AbstractHandler implements org.apache.axis2.engine.Handler {
//		org.slf4j.Logger incomingLogger = org.slf4j.LoggerFactory.getLogger("SOAP_REQUEST");
//		org.slf4j.Logger outgoingLogger = org.slf4j.LoggerFactory.getLogger("SOAP_REQUEST_RESPONSE");
//
//		private String name;
//
//		public String getName() {
//			return name;
//		}
//
//		public InvocationResponse invoke(org.apache.axis2.context.MessageContext msgContext) throws org.apache.axis2.AxisFault {
//			incomingLogger.debug("Received Request.");
//
//			StringBuilder stringBuilder=new StringBuilder();        
//			org.apache.axis2.context.OperationContext operationContext = msgContext.getOperationContext();
//
//			org.apache.axis2.context.MessageContext inMessage = operationContext.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
//			String inSoapRequest = inMessage.getEnvelope().toString();
//
//			org.apache.axis2.context.MessageContext outMessage = operationContext.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
//			org.apache.axis2.context.MessageContext faultMessage = operationContext.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_FAULT_VALUE);
//
//			if(outMessage==null&&faultMessage==null){
//				incomingLogger.debug("\n--------------Request Received--------------\n"+inSoapRequest);
//			}
//
//			if(outMessage!=null || faultMessage!=null){
//				stringBuilder.append("\n-------IN MESSAGE-------\n");                                                   
//				stringBuilder.append(inSoapRequest);
//				stringBuilder.append("\n-------OUT MESSAGE-------\n");
//
//				if(outMessage!=null){                                                                                                                      
//					String outMessageEnvelope = outMessage.getEnvelope().toString();
//					stringBuilder.append(outMessageEnvelope);
//				}
//
//				if(faultMessage!=null){
//					stringBuilder.append("\n-------FAULT MESSAGE-------\n");
//					String faultMessagerEnvelope = faultMessage.getEnvelope().toString();
//					stringBuilder.append(faultMessagerEnvelope);
//					//	                                                                                            stringBuilder.append("\n Fault Envelope : "+faultMessagerEnvelope+"\n");
//					//	                                                                                            stringBuilder.append("\n Fault Reason : "+faultMessage.getFailureReason().getMessage()+"\n");
//					//	                                                                                            faultMessage.getFailureReason().printStackTrace();
//				}
//
//				outgoingLogger.debug("\nTRANSACTION START :"+stringBuilder.toString()+ "\nTRANSACTION END\n");
//			}
//			return InvocationResponse.CONTINUE;       
//		}
//
//		public void revoke(org.apache.axis2.context.MessageContext msgContext) {
//			incomingLogger.debug("Revoking Started");
//			incomingLogger.info(msgContext.getEnvelope().toString());
//			incomingLogger.debug("Revoking completed");
//		}
//
//		public void setName(String name) {
//			incomingLogger.debug("Setting name as :"+name);
//			this.name = name;
//		}
//	}

	public static class JaxWsLoggingHandler implements SOAPHandler<SOAPMessageContext> {
		 
		private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JaxWsLoggingHandler.class);
		   		
	    @Override
	    public void close(MessageContext arg0) {
	    }
	 
	    @Override
	    public boolean handleFault(SOAPMessageContext arg0) {
	        SOAPMessage message = arg0.getMessage();
	        try {
	            message.writeTo(System.out);
	        } catch (SOAPException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return true;
	    }
	 
	    @Override
	    public boolean handleMessage(SOAPMessageContext arg0) {
	        SOAPMessage message = arg0.getMessage();
	        boolean isOutboundMessage = (Boolean) arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);	     
	        if (isOutboundMessage) {
	            logger.debug("OUTBOUND MESSAGE");
	        } else {
	            logger.debug("INBOUND MESSAGE");
	        }
	        try {
	        	//message.writeTo(System.out);
	        	try(OutputStream outStream = new ByteArrayOutputStream()) {
	                 message.writeTo(outStream);
	                 logger.debug(outStream.toString());
	            }	        	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }         
	        return true;
	    }
	 
	    @Override
	    public Set<QName> getHeaders() {
	        return null;
	    }
	    	    
		public String prettyFormat(SOAPMessage soapMessage, int indent) throws SOAPException {
	    	Source xmlInput = soapMessage.getSOAPPart().getContent();
	    	return prettyFormat(xmlInput,indent);
	    }
	    
	    public String prettyFormat(String input, int indent) {
	    	Source xmlInput = new StreamSource(new StringReader(input));
	    	return prettyFormat(xmlInput,indent);
	    }
	    
	    public String prettyFormat(Source xmlInput, int indent) {
	        try
	        {
	            //Source xmlInput = new StreamSource(new StringReader(input));
	        	
	            //StringWriter stringWriter = new StringWriter();            
	            ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
	            StreamResult xmlOutput = new StreamResult(streamOut);
	            //StreamResult xmlOutput = new StreamResult(stringWriter);
	            
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            // This statement works with JDK 6
	            transformerFactory.setAttribute("indent-number", indent);
	             
	            Transformer tf = transformerFactory.newTransformer();
	            tf.setOutputProperty(OutputKeys.INDENT, "yes");
	            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
	            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	            tf.transform(xmlInput, xmlOutput);
	            //return xmlOutput.getWriter().toString();            
	            String strMessage = streamOut.toString();
	            return strMessage;
	        }
	        catch (Throwable e)
	        {
	            // You'll come here if you are using JDK 1.5
	            // you are getting an the following exeption
	            // java.lang.IllegalArgumentException: Not supported: indent-number
	            // Use this code (Set the output property in transformer.
	            try
	            {
	                //Source xmlInput = new StreamSource(new StringReader(input));
	                StringWriter stringWriter = new StringWriter();
	                StreamResult xmlOutput = new StreamResult(stringWriter);
	                TransformerFactory transformerFactory = TransformerFactory.newInstance();
	                Transformer tf = transformerFactory.newTransformer();
	                tf.setOutputProperty(OutputKeys.INDENT, "yes");
	                tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
	                tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	                tf.transform(xmlInput, xmlOutput);
	                return xmlOutput.getWriter().toString();
	            }
	            catch(Throwable t)
	            {
	                return xmlInput.toString();
	            }
	        }
	    }
	 
		public String prettyFormat(String input) {
	        return prettyFormat(input, 2);
	    }
	 
	}
	
	public static class JaxWsLoggingFileHandler implements SOAPHandler<SOAPMessageContext> {
		 
		private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JaxWsLoggingFileHandler.class);
		   		
	    @Override
	    public void close(MessageContext arg0) {
	    }
	 
	    @Override
	    public boolean handleFault(SOAPMessageContext arg0) {
	        SOAPMessage message = arg0.getMessage();
	        try {	        	
	            message.writeTo(System.out);
	        } catch (SOAPException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return true;
	    }
	 
	    @Override
	    public boolean handleMessage(SOAPMessageContext arg0) {
	        SOAPMessage message = arg0.getMessage();
	        boolean isOutboundMessage = (Boolean) arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);	       
	        if (isOutboundMessage) {
	            logger.debug("OUTBOUND MESSAGE");
	        } else {
	            logger.debug("INBOUND MESSAGE");
	        }
	        try {	        	
	        	File tmpDir = TempFileProvider.getTempDir();
		    	String outputFolderName = getTodaysFolderName("dumps");	        
		        String absolutefolderName = tmpDir.getAbsolutePath() + File.separator + outputFolderName;		       
		        File outputFolder = new File(absolutefolderName);
		        if(!outputFolder.exists())outputFolder.mkdirs();   
		        File outputFile = null;
		        if(isOutboundMessage){
		        	outputFile = TempFileProvider.createTempFile("soap.request-"+ TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis()),".xml",outputFolder); 
		        }else{
		        	outputFile = TempFileProvider.createTempFile("soap.response-"+ TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis()),".xml",outputFolder); 
		        }
		        //File outputFile = TempFileProvider.createTempFile("soap.request-"+ TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis()),".xml",outputFolder);        
		        CloseShieldOutputStream fos = new CloseShieldOutputStream(new FileOutputStream(outputFile));        
		        try {	           
		        	message.writeTo(fos);
		            SOAPMessage soapMessage = toSoapMessage(new FileInputStream(outputFile));
		        	//SOAPMessage soapMessage = toSoapMessage(new BOMInputStream(new FileInputStream(outputFile)));
		            TransformerFactory tff = TransformerFactory.newInstance();
					Transformer tf = tff.newTransformer();
					// Set formatting
					tf.setOutputProperty(OutputKeys.INDENT, "yes");
					tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
					tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					Source sc = soapMessage.getSOAPPart().getContent();					
					StreamResult result = new StreamResult(outputFile);
					tf.transform(sc, result);	
		        } finally {
		            if(fos != null)
		                fos.close(); // close the file
		        }        	
	        } 
	        catch(javax.xml.transform.TransformerException xe){
	        	if(xe.getMessage().contains("Content is not allowed in prolog")){
	        		logger.error(xe.getMessage(),xe);
	        	}else{
	        		xe.printStackTrace();
	        	}
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }         
	        return true;
	    }
	    
	    
	    /**
	     * Gets the recursive folder path that represents today
	     * 
	     * @param rootFolder The root folder
	     * @return The recursive folder name
	     */
	    private static String getTodaysFolderName(String rootFolder) {
	        Calendar cal = Calendar.getInstance();	        
	        StringBuilder sb = new StringBuilder();
	        sb.append(rootFolder);
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.YEAR));
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.MONTH));
	        sb.append(File.separator);
	        sb.append(cal.get(Calendar.DAY_OF_MONTH));
	        sb.append(File.separator);
	        return sb.toString();
	    }	    
	 
	    @Override
	    public Set<QName> getHeaders() {
	        return null;
	    }
	    	    
		public String prettyFormat(SOAPMessage soapMessage, int indent) throws SOAPException {
	    	Source xmlInput = soapMessage.getSOAPPart().getContent();
	    	return prettyFormat(xmlInput,indent);
	    }
	    
	    public String prettyFormat(String input, int indent) {
	    	Source xmlInput = new StreamSource(new StringReader(input));
	    	return prettyFormat(xmlInput,indent);
	    }
	    
	    public String prettyFormat(Source xmlInput, int indent) {
	        try
	        {
	            //Source xmlInput = new StreamSource(new StringReader(input));
	        	
	            //StringWriter stringWriter = new StringWriter();            
	            ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
	            StreamResult xmlOutput = new StreamResult(streamOut);
	            //StreamResult xmlOutput = new StreamResult(stringWriter);
	            
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            // This statement works with JDK 6
	            transformerFactory.setAttribute("indent-number", indent);
	             
	            Transformer tf = transformerFactory.newTransformer();
	            tf.setOutputProperty(OutputKeys.INDENT, "yes");
	            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
	            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	            tf.transform(xmlInput, xmlOutput);
	            //return xmlOutput.getWriter().toString();            
	            String strMessage = streamOut.toString();
	            return strMessage;
	        }
	        catch (Throwable e)
	        {
	            // You'll come here if you are using JDK 1.5
	            // you are getting an the following exeption
	            // java.lang.IllegalArgumentException: Not supported: indent-number
	            // Use this code (Set the output property in transformer.
	            try
	            {
	                //Source xmlInput = new StreamSource(new StringReader(input));
	                StringWriter stringWriter = new StringWriter();
	                StreamResult xmlOutput = new StreamResult(stringWriter);
	                TransformerFactory transformerFactory = TransformerFactory.newInstance();
	                Transformer tf = transformerFactory.newTransformer();
	                tf.setOutputProperty(OutputKeys.INDENT, "yes");
	                tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
	                tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	                tf.transform(xmlInput, xmlOutput);
	                return xmlOutput.getWriter().toString();
	            }
	            catch(Throwable t)
	            {
	                return xmlInput.toString();
	            }
	        }
	    }
	 
		public String prettyFormat(String input) {
	        return prettyFormat(input, 2);
	    }
	 
	}
	
//	static class SOAPBodyHandler implements SOAPHandler<SOAPMessageContext> {
//
//		static final String DESIRED_NS_PREFIX = "customns";
//		static final String DESIRED_NS_URI = "http://test/";
//		static final String UNWANTED_NS_PREFIX = "ns";
//
//		@Override
//		public Set<QName> getHeaders() {
//		   //do nothing
//		   return null;
//		}
//
//		@Override
//		public boolean handleMessage(SOAPMessageContext context) {
//			
//			Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
//			
//		    if (isRequest) { //Check here that the message being intercepted is an outbound message from your service, otherwise ignore.
//		        try {
//		            SOAPEnvelope msg = context.getMessage().getSOAPPart().getEnvelope(); //get the SOAP Message envelope
//		            SOAPBody body = msg.getBody();
//		            body.removeNamespaceDeclaration(UNWANTED_NS_PREFIX);
//		            body.addNamespaceDeclaration(DESIRED_NS_PREFIX, DESIRED_NS_URI); 
//		        } catch (SOAPException ex) {
//		            logger.error(ex.getMessage(),ex);
//		        }
//		    }
//		    return true; //indicates to the context to proceed with (normal)message processing
//		}
//
//		@Override
//		public boolean handleFault(SOAPMessageContext context) {
//		      //do nothing
//			  return false;
//		}
//
//		@Override
//		public void close(MessageContext context) {
//		      //do nothing
//		}
//
//	}
	
    /** 
     * A {@link URLStreamHandler} that handles resources on the classpath. 
     * @href https://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java
     */
	public static class Handler extends java.net.URLStreamHandler {
        /** The classloader to find resources from. */
        private final ClassLoader classLoader;

        public Handler() {
            this.classLoader = getClass().getClassLoader();
        }

        public Handler(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        protected java.net.URLConnection openConnection(URL u) throws java.io.IOException {
            final URL resourceUrl = classLoader.getResource(u.getPath());
            return resourceUrl.openConnection();
        }
    }
    
    /** 
     * @href https://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java
     */
    public static class ConfigurableStreamHandlerFactory implements java.net.URLStreamHandlerFactory {
        private final java.util.Map<String, java.net.URLStreamHandler> protocolHandlers;

        public ConfigurableStreamHandlerFactory(String protocol, java.net.URLStreamHandler urlHandler) {
            protocolHandlers = new java.util.HashMap<String, java.net.URLStreamHandler>();
            addHandler(protocol, urlHandler);
        }

        public void addHandler(String protocol, java.net.URLStreamHandler urlHandler) {
            protocolHandlers.put(protocol, urlHandler);
        }

        public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
            return protocolHandlers.get(protocol);
        }
    }
	
	public static class ClientPasswordHandler implements CallbackHandler 
	{
		String password;
		
		public ClientPasswordHandler(String password) {
			this.password = password;
		}
		
		public ClientPasswordHandler() {
			this.password = "";
		}
		
		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			//VECCHIO CODICE
		    //WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
		    //pc.setPassword(password);	    
			  
			//NUOVO CODICE
		    for (Callback callback : callbacks) {
	            if (callback instanceof WSPasswordCallback) {
	                WSPasswordCallback passwordCallback = (WSPasswordCallback) callback;
	                passwordCallback.setPassword(password);
	            } else {
	                throw new UnsupportedCallbackException(callback, "Unrecognized callback!");
	            }
	        }
	    }
	}
	
	/**
	 * @href https://stackoverflow.com/questions/17222902/remove-namespace-prefix-while-jaxb-marshalling
	 * @href https://stackoverflow.com/questions/2816176/how-to-marshal-without-a-namespace/29945934#29945934
	 * Filter the output to remove namespaces.
     * m.marshal(it, NoNamesWriterHandler.filter(writer));
	 *
	 */
	public static class NoNamesWriterHandler extends org.apache.cxf.staxutils.DelegatingXMLStreamWriter {

		  private static final NamespaceContext emptyNamespaceContext = new NamespaceContext() {

		    @Override
		    public String getNamespaceURI(String prefix) {
		      return null;
		    }

		    @Override
		    public String getPrefix(String namespaceURI) {
		      return "";
		    }

		    @Override
		    public Iterator getPrefixes(String namespaceURI) {
		      return null;
		    }
		  };
		  
		  public static XMLStreamWriter filter(XMLStreamWriter writer) throws XMLStreamException {
		    return new NoNamesWriterHandler(writer);
		  }

		  public static XMLStreamWriter filter(Writer writer) throws XMLStreamException {
		    return new NoNamesWriterHandler(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
		  }
		  
		  public static XMLStreamWriter filter(OutputStream writer) throws XMLStreamException {
		    return new NoNamesWriterHandler(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
		  }
		  
		  public static XMLStreamWriter filter(Result writer) throws XMLStreamException {
		    return new NoNamesWriterHandler(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
		  }

		  public NoNamesWriterHandler(XMLStreamWriter writer) throws XMLStreamException {			  
		    super(writer);
		  }

		  @Override
		  public NamespaceContext getNamespaceContext() {
		    return emptyNamespaceContext;
		  }
		  
		  @Override
		  public void writeNamespace(String prefix, String uri) throws XMLStreamException {
		    // intentionally doing nothing
		  }

		  @Override
		  public void writeDefaultNamespace(String uri) throws XMLStreamException {
		    // intentionally doing nothing
		  }

		  @Override
		  public void writeStartElement(String prefix, String local, String uri) throws XMLStreamException {
		    super.writeStartElement(null, local, null);
		  }

		  @Override
		  public void writeStartElement(String uri, String local) throws XMLStreamException {
		    super.writeStartElement(null, local);
		  }

		  @Override
		  public void writeEmptyElement(String uri, String local) throws XMLStreamException {
		    super.writeEmptyElement(null, local);
		  }

		  @Override
		  public void writeEmptyElement(String prefix, String local, String uri) throws XMLStreamException {
		    super.writeEmptyElement(null, local, null);
		  }

		  @Override
		  public void writeAttribute(String prefix, String uri, String local, String value) throws XMLStreamException {
		    super.writeAttribute(null, null, local, value);
		  }

		  @Override
		  public void writeAttribute(String uri, String local, String value) throws XMLStreamException {
		    super.writeAttribute(null, local, value);
		  }
		  
	}
	
	// ==========================================================================
	
	public static HostnameVerifier setSystemTrustAllHost(){
		//TRUST ALL HOST Ignore differences between given hostname and certificate hostname
	    HostnameVerifier hv = new HostnameVerifier(){
	    	public boolean verify(String hostname, SSLSession session) { return true; }
	    };
	    //hv.verify(wsdlURL.getHost(),sc.getClientSessionContext().getSession());
	    HttpsURLConnection.setDefaultHostnameVerifier(hv);
	    return hv;
	}
	
	public static SSLContext  setSystemTrustAllCertificateSSLContext()throws KeyManagementException, NoSuchAlgorithmException{
		return setSystemTrustAllCertificateSSLContext("SSL");
	}	
	
	public static SSLContext setSystemTrustAllCertificateSSLContext(String sslProtocol)throws KeyManagementException, NoSuchAlgorithmException{
		SSLContext sc = SSLContext.getInstance(sslProtocol);
		TrustManager[] trustAllCerts = setSystemTrustAllCertificateSSL(sslProtocol);
		sc.init(null, trustAllCerts, new SecureRandom());
		return sc;
	}
	
	public static TrustManager[] setSystemTrustAllCertificateSSL() throws KeyManagementException, NoSuchAlgorithmException{
		return setSystemTrustAllCertificateSSL("SSL");
	}
	
	public static TrustManager[] setSystemTrustAllCertificateSSL(String sslProtocol) throws KeyManagementException, NoSuchAlgorithmException{
		//TRUST ALL CERTIFICATE Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { 
				new X509TrustManager() {			
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {return new java.security.cert.X509Certificate[0];}
					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
				} 
		};
		SSLContext sc = SSLContext.getInstance(sslProtocol);
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		return trustAllCerts;
	}
	
	public static void setSystemTrustAllCertificateAndHost() throws KeyManagementException, NoSuchAlgorithmException{
		setSystemTrustAllCertificateSSL();
		setSystemTrustAllHost();
	}
	
	
	// ======================================
	// SOAP MESSAGE UTILITIES
	// ========================================

	/**
	 * @href http://wpcertification.blogspot.com/2011/10/pretty-printing-soap-messages.html
	 * @href adamish.com/blog/archives/707
	 */
	public static String toStringPretty(SOAPMessage soapMessage) {
		try {
			TransformerFactory tff = TransformerFactory.newInstance();
			Transformer tf = tff.newTransformer();
			// Set formatting
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			Source sc = soapMessage.getSOAPPart().getContent();
			ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(streamOut);
			tf.transform(sc, result);		
			String strMessage = streamOut.toString();
			return strMessage;
		} catch (Exception e) {
			logger.error("Exception in getSOAPMessageAsString "+ e.getMessage());
			return null;
		}
	}
	
	/**
	 * @href http://wpcertification.blogspot.com/2011/10/pretty-printing-soap-messages.html
	 */
	public static File toFile(SOAPMessage soapMessage,File output) {
		try {
			TransformerFactory tff = TransformerFactory.newInstance();
			Transformer tf = tff.newTransformer();
			// Set formatting
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			Source sc = soapMessage.getSOAPPart().getContent();
			StreamResult result = new StreamResult(output);
			tf.transform(sc, result);	
			return output;
		} catch (Exception e) {
			logger.error("Exception in getSOAPMessageAsString "+ e.getMessage());
			return null;
		}
	}
	
	/**
	 * @href https://stackoverflow.com/questions/13614508/how-to-convert-a-string-to-a-soapmessage-in-java
	 * @href adamish.com/blog/archives/707
	 */
	public static SOAPMessage toSoapMessage(String soapMessage) throws IOException, SOAPException{
		CloseShieldInputStream is = new CloseShieldInputStream(new ByteArrayInputStream(soapMessage.getBytes()));
		try{
			SOAPMessage request = null;
			try{
				request = MessageFactory.newInstance().createMessage(null, is);
			}catch (Exception e) {
				request = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(new MimeHeaders(), is);
			}
			return request;
		}finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	/**
	 * @href https://stackoverflow.com/questions/13614508/how-to-convert-a-string-to-a-soapmessage-in-java
	 * @href adamish.com/blog/archives/707
	 */
	public static SOAPMessage toSoapMessage(InputStream soapMessage) throws IOException, SOAPException{
		CloseShieldInputStream is = new CloseShieldInputStream(soapMessage);
		try{
			SOAPMessage request = null;
			try{
				request = MessageFactory.newInstance().createMessage(null, is);
			}catch (Exception e) {
				request = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(new MimeHeaders(), is);
			}
			return request;
		}finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	// =============================
	// SOAP UTITLITY
	// @href https://www.programcreek.com/java-api-examples/?code=vrk-kpa/xrd4j/xrd4j-master/src/common/src/main/java/fi/vrk/xrd4j/common/util/SOAPHelper.java#
	// =============================
	
    /**
     * Goes through all the child nodes of the given node and returns the first
     * child that matches the given name. If no child with the given is found,
     * null is returned.
     *
     * @param node parent node
     * @param nodeName name of the node to be searched
     * @return node with the given name or null
     */
    public static Node getNode(Node node, String nodeName) {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE
                    && node.getChildNodes().item(i).getLocalName().equals(nodeName)) {
                return (Node) node.getChildNodes().item(i);
            }
        }
        return null;
    }

    

    /**
     * Converts the given Node to String.
     *
     * @param node Node object to be converted
     * @return String presentation of the given Node
     */
    public static String toString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, CHARSET);
            t.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "";
        }
    }

    /**
     * Converts the given attachment part to string.
     *
     * @param att attachment part to be converted
     * @return string presentation of the attachment or null
     */
    public static String toString(AttachmentPart att) {
        try {
            return new Scanner(att.getRawContent(), CHARSET).useDelimiter("\\A").next();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Converts the given String to SOAPMessage. The String must contain a valid
     * SOAP message, otherwise null is returned.
     *
     * @param soap SOAP string to be converted
     * @return SOAPMessage or null
     */
    public static SOAPMessage toSOAP(String soap) {
        try {
            InputStream is = new ByteArrayInputStream(soap.getBytes(CHARSET));
            return toSOAP(is);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Converts the given InputStream to SOAPMessage. The stream must contain a
     * valid SOAP message, otherwise null is returned.
     *
     * @param is InputStream to be converted
     * @return SOAPMessage or null
     */
    public static SOAPMessage toSOAP(InputStream is) {
        try {
            return createSOAPMessage(new MimeHeaders(), is);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Converts the given InputStream to SOAPMessage. The stream must contain a
     * valid SOAP message, otherwise null is returned.
     *
     * @param is InputStream to be converted
     * @param mh MIME headers of the SOAP request
     * @return SOAPMessage or null
     */
    public static SOAPMessage toSOAP(InputStream is, MimeHeaders mh) {
        try {
            return createSOAPMessage(mh, is);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Transfers the given NodeList to a Map that contains all the list items as
     * key-value-pairs, localName as the key and NodeValue as the value. The
     * given NodeList is parsed recursively.
     *
     * @param list NodeList to be transfered
     * @return Map that contains all the list items as key-value-pairs
     */
    public static Map<String, String> nodesToMap(NodeList list) {
        return nodesToMap(list, false);
    }

    /**
     * Transfers the given NodeList to a Map that contains all the list items as
     * key-value-pairs, localName as the key and NodeValue as the value. Each
     * key can have only one value. The given NodeList is parsed recursively.
     *
     * @param list NodeList to be transfered
     * @param upperCase store all keys in upper case
     * @return Map that contains all the list items as key-value-pairs
     */
    public static Map<String, String> nodesToMap(NodeList list, boolean upperCase) {
        Map<String, String> map = new HashMap<>();
        nodesToMap(list, upperCase, map);
        return map;
    }

    /**
     * Transfers the given NodeList to a Map that contains all the list items as
     * key-value-pairs, localName as the key and NodeValue as the value. Each
     * key can have only one value. The given NodeList is parsed recursively.
     *
     * @param list NodeList to be transfered
     * @param upperCase store all keys in upper case
     * @param map Map for the results
     */
    public static void nodesToMap(NodeList list, boolean upperCase, Map<String, String> map) {
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == javax.xml.soap.Node.ELEMENT_NODE && list.item(i).hasChildNodes()) {
                nodesToMap(list.item(i).getChildNodes(), upperCase, map);
            } else {
                processMapNode(list, i, upperCase, map);
            }
        }
    }

    /**
     * Transfers the given Node to a Map as key - value pair.
     *
     * @param list NodeList containing the node to be transfered
     * @param index index of the node to be transfered
     * @param upperCase store all keys in upper case
     * @param map Map for the results
     */
    private static void processMapNode(NodeList list, int index, boolean upperCase, Map<String, String> map) {
        if (list.item(index).getNodeType() == javax.xml.soap.Node.ELEMENT_NODE && !list.item(index).hasChildNodes()) {
            String key = list.item(index).getLocalName();
            map.put(upperCase ? key.toUpperCase() : key, "");
        } else if (list.item(index).getNodeType() == javax.xml.soap.Node.TEXT_NODE) {
            String key = list.item(index).getParentNode().getLocalName();
            String value = list.item(index).getNodeValue();
            value = value.trim();
            if (!value.isEmpty()) {
                map.put(upperCase ? key.toUpperCase() : key, value);
            }
        }
    }

    /**
     * Transfers the given NodeList to a MultiMap that contains all the list
     * items as key - value list pairs. Each key can have multiple values that
     * are stored in a list. The given NodeList is parsed recursively.
     *
     * @param list NodeList to be transfered
     * @return Map that contains all the list items as key - value list pairs
     */
    public static Map<String, List<String>> nodesToMultiMap(NodeList list) {
        Map<String, List<String>> map = new HashMap<>();
        nodesToMultiMap(list, map);
        return map;
    }

    /**
     * Transfers the given NodeList to a MultiMap that contains all the list
     * items as key - value list pairs. Each key can have multiple values that
     * are stored in a list. The given NodeList is parsed recursively.
     *
     * @param list NodeList to be transfered
     * @param map Map for the results
     */
    public static void nodesToMultiMap(NodeList list, Map<String, List<String>> map) {
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == javax.xml.soap.Node.ELEMENT_NODE && list.item(i).hasChildNodes()) {
                nodesToMultiMap(list.item(i).getChildNodes(), map);
            } else {
                processMultiMapNode(list, i, map);
            }
        }
    }

    /**
     * Transfers the given Node to a MultiMap as key - value list pair.
     *
     * @param list NodeList containing the Node to be transfered
     * @param index index of the Node to be transfered
     * @param map Map for the results
     */
    private static void processMultiMapNode(NodeList list, int index, Map<String, List<String>> map) {
        if (list.item(index).getNodeType() == javax.xml.soap.Node.ELEMENT_NODE && !list.item(index).hasChildNodes()) {
            String key = list.item(index).getLocalName();
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<String>());
            }
            map.get(key).add("");
        } else if (list.item(index).getNodeType() == javax.xml.soap.Node.TEXT_NODE) {
            String key = list.item(index).getParentNode().getLocalName();
            String value = list.item(index).getNodeValue();
            value = value.trim();
            if (!value.isEmpty()) {
                if (!map.containsKey(key)) {
                    map.put(key, new ArrayList<String>());
                }
                map.get(key).add(value);
            }
        }
    }

    /**
     * Adds the namespace URI and prefix of the ProvideMember related to the
     * given Message to the given Node and all its children. If the Node should
     * have another namespace, the old namespace is first removed and the new
     * namespace is added after that.
     *
     * @param node Node to be modified
     * @param message Message that contains the ProviderMember which namespace
     * URI and prefix are used
     */
    public static void addNamespace(Node node, SOAPMessage message) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            node.getOwnerDocument().renameNode(node, null, node.getLocalName());
//            if (message.getProducer().getNamespacePrefix() != null
//                    && !message.getProducer().getNamespacePrefix().isEmpty()) {
//                node = (Node) node.getOwnerDocument().renameNode(node, message.getProducer().getNamespaceUrl(),
//                    message.getProducer().getNamespacePrefix() + ":" + node.getNodeName());
//            } else {
//                node = (Node) node.getOwnerDocument().renameNode(node, message.getProducer().getNamespaceUrl(), node.getNodeName());
//            }
        }

        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            addNamespace((Node) list.item(i), message);
        }
    }

    /**
     * Removes the namespace from the given Node and all its children.
     *
     * @param node Node to be modified
     */
    public static void removeNamespace(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            node.getOwnerDocument().renameNode(node, null, node.getLocalName());
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            removeNamespace((Node) list.item(i));
        }
    }

    /**
     * Searches the attachment with the given content id and returns its string
     * contents. If there's no attachment with the given content id or its value
     * is not a string, null is returned.
     *
     * @param contentId content id of the attachment
     * @param attachments list of attachments to be searched
     * @return string value of the attachment or null
     */
    public static String getStringAttachment(String contentId, Iterator attachments) {
        if (attachments == null) {
            return null;
        }
        try {
            while (attachments.hasNext()) {
                AttachmentPart att = (AttachmentPart) attachments.next();
                if (att.getContentId().equals(contentId)) {
                    return new Scanner(att.getRawContent(), CHARSET).useDelimiter("\\A").next();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns the content type of the first SOAP attachment or null if there's
     * no attachments.
     *
     * @param message SOAP message
     * @return content type of the first attachment or null
     */
    public static String getAttachmentContentType(SOAPMessage message) {
        if (message.countAttachments() == 0) {
            return null;
        }
        AttachmentPart att = (AttachmentPart) message.getAttachments().next();
        return att.getContentType();

    }

    /**
     * Checks if the given SOAP message has attachments. Returns true if and
     * only if the message has attachments. Otherwise returns false.
     *
     * @param message SOAP message to be checked
     * @return true if and only if the message has attachments; otherwise false
     */
    public static boolean hasAttachments(SOAPMessage message) {
        if (message == null) {
            return false;
        }
        if (message.countAttachments() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Converts the given XML string to SOAPElement.
     *
     * @param xml XML string
     * @return given XML string as a SOAPElement or null if the conversion
     * failed
     */
    public static SOAPElement xmlStrToSOAPElement(String xml) {
        logger.debug("Convert XML string to SOAPElement. XML : \"{}\"", xml);
        // Try to conver XML string to XML Document
        Document doc = xmlStrToDoc(xml);
        if (doc == null) {
            logger.warn("Convertin XML string to SOAP element failed.");
            return null;
        }

        try {
            // Use SAAJ to convert Document to SOAPElement
            // Create SoapMessage
            SOAPMessage message = createSOAPMessage();
            SOAPBody soapBody = message.getSOAPBody();
            // This returns the SOAPBodyElement
            // that contains ONLY the Payload
            SOAPElement payload = soapBody.addDocument(doc);
            if (payload == null) {
                logger.warn("Converting XML string to SOAPElement failed.");
            } else {
                logger.debug("Converting XML string to SOAPElement succeeded.");
            }
            return payload;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.warn("Converting XML document to SOAPElement failed.");
            return null;
        }
    }

    /**
     * Converts the given XML string to XML document. If the conversion fails,
     * null is returned.
     *
     * @param xml XML string to be converted
     * @return XML document
     */
    public static Document xmlStrToDoc(String xml) {
        logger.debug("Convert XML string to XML document.");
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        InputStream stream = null;
        Document doc = null;
        try {
            stream = new ByteArrayInputStream(xml.getBytes());
            doc = builderFactory.newDocumentBuilder().parse(stream);
            logger.debug("Converting XML string to XML document succeeded.");
        } catch (Exception e) {
            // If exception starts with "Invalid byte", it means that ISO-8859-1
            // character set is probably used. Try to convert the string to
            // UTF-8.
            if (e.getLocalizedMessage().startsWith("Invalid byte")) {
                logger.warn("Invalid characters detected.");
                try {
                    logger.debug("Try to convert XML string from ISO-8859-1 to UTF-8.");
                    stream = new ByteArrayInputStream(new String(xml.getBytes(), "ISO-8859-1").getBytes(CHARSET));
                    doc = builderFactory.newDocumentBuilder().parse(stream);
                    logger.debug("Converting XML string from ISO-8859-1 to UTF-8 succeeded.");
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    logger.warn("Converting XML string to XML document failed.");
                    logger.warn("Converting XML string from ISO-8859-1 to UTF-8 failed.");
                    return null;
                }
            } else {
                logger.error(e.getMessage());
                logger.warn("Converting XML string to XML document failed.");
                return null;
            }
        }
        return doc;
    }

    /**
     * Removes all the child nodes from the given node.
     *
     * @param node node to be modified
     */
    public static void removeAllChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    /**
     * Move all the children under from SOAPElement to under to SOAPElement. If
     * updateNamespaceAndPrefix is true and from elements do not have namespace
     * URI yet, to elements namespace URI and prefix are recursively copied to
     * them.
     *
     * @param from source element
     * @param to target element
     * @param updateNamespaceAndPrefix should elements namespace URI and prefix
     * be applied to all the copied elements if they do not have namespace URI
     * yet
     * @throws SOAPException if there's an error
     */
    public static void moveChildren(SOAPElement from, SOAPElement to, boolean updateNamespaceAndPrefix) throws SOAPException {
        NodeList children = from.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = (Node) children.item(i);
            if (updateNamespaceAndPrefix && (child.getNamespaceURI() == null || child.getNamespaceURI().isEmpty())) {
                child = updateNamespaceAndPrefix(child, to.getNamespaceURI(), to.getPrefix());
                updateNamespaceAndPrefix(child.getChildNodes(), to.getNamespaceURI(), to.getPrefix());
            }
            child.setParentElement(to);
        }
    }

    /**
     * Updates the namespace URI and prefix of all the nodes in the list, if
     * node does not have namespace URI yet. The list is updated recursively, so
     * also the children of children (and so on) will be updated.
     *
     * @param list list of nodes to be updated
     * @param namespace target namespace
     * @param prefix target prefix
     */
    public static void updateNamespaceAndPrefix(NodeList list, String namespace, String prefix) {
        for (int i = 0; i < list.getLength(); i++) {
            Node node = (Node) list.item(i);
            if (node.getNamespaceURI() == null || node.getNamespaceURI().isEmpty()) {
                node = updateNamespaceAndPrefix(node, namespace, prefix);
            }
            updateNamespaceAndPrefix(node.getChildNodes(), namespace, prefix);
        }
    }

    /**
     * Updates the namespace URI and prefix of the given node with the given
     * values. If prefix is null or empty, only namespace URI is updated.
     *
     * @param node Node to be updated
     * @param namespace target namespace
     * @param prefix target prefix
     * @return updated Node
     */
    public static Node updateNamespaceAndPrefix(Node node, String namespace, String prefix) {
        if (node.getNodeType() == javax.xml.soap.Node.ELEMENT_NODE) {
            if (prefix != null && !prefix.isEmpty()) {
                node = (Node) node.getOwnerDocument().renameNode(node, namespace, prefix + ":" + node.getLocalName());
            } else if (namespace != null && !namespace.isEmpty()) {
                node = (Node) node.getOwnerDocument().renameNode(node, namespace, node.getLocalName());
            }
        }
        return node;
    }

//    /**
//     * Reads installed X-Road packages and their version info from environmental
//     * monitoring metrics.
//     *
//     * @param metrics NodeList containing "metricSet" element returned by
//     * environmental monitoring service
//     * @return installed X-Road packages as key-value pairs
//     */
//    public static Map<String, String> getXRdVersionInfo(NodeList metrics) {
//        logger.trace("Start reading X-Road version info from metrics.");
//        Map<String, String> results = new HashMap<>();
//        // Check for null and empty
//        if (metrics == null || metrics.getLength() == 0) {
//            logger.trace("Metrics set is null or empty.");
//            return results;
//        }
//        // Loop through metrics
//        for (int i = 0; i < metrics.getLength(); i++) {
//            Node node = getNode((Node) metrics.item(i), "name");
//            // Jump to next element if this is not Packages
//            if (node == null || !Constants.NS_ENV_MONITORING_ELEM_PACKAGES.equals(node.getTextContent())) {
//                continue;
//            }
//            // Loop through packages and add X-Road packages to results
//            getXRdPackages(metrics.item(i).getChildNodes(), results);
//        }
//        logger.trace("Metrics info read. {} X-Road packages found.", results.size());
//        return results;
//    }

    /**
     * Helper function for creating new SOAP messages
     *
     * @return New SOAP message
     * @throws SOAPException on soap error
     */
    public static SOAPMessage createSOAPMessage() throws SOAPException {
        synchronized (MSG_FACTORY) {
            return MSG_FACTORY.createMessage();
        }
    }

    /**
     * Helper function for creating new SOAP messages
     * 
     * @param mimeHeaders needed for creating SOAP message
     * @param is needed for creating SOAP message
     * @return New SOAP message
     * @throws IOException on IO error
     * @throws SOAPException on soap error
     */
    public static SOAPMessage createSOAPMessage(MimeHeaders mimeHeaders, InputStream is)
            throws IOException, SOAPException {
        synchronized (MSG_FACTORY) {
            return MSG_FACTORY.createMessage(mimeHeaders, is);
        }
    }

//    /**
//     * Reads installed X-Road packages and their version info from environmental
//     * monitoring metrics.
//     *
//     * @param packages NodeList containing "metricSet" element which children
//     * all the installed packages are
//     * @param results Map object for results
//     */
//    private static void getXRdPackages(NodeList packages, Map<String, String> results) {
//        // Loop through packages
//        for (int j = 0; j < packages.getLength(); j++) {
//            // We're looking for "stringMetric" elements
//            if (Constants.NS_ENV_MONITORING_ELEM_STRING_METRIC.equals(packages.item(j).getLocalName())) {
//                // Get name and value
//                Node name = getNode((Node) packages.item(j), "name");
//                Node value = getNode((Node) packages.item(j), "value");
//                // X-Road packages start with "xroad-prefix"
//                if (name != null && value != null && name.getTextContent().startsWith("xroad-")) {
//                    results.put(name.getTextContent(), value.getTextContent());
//                    logger.debug("X-Road package version info found: \"{}\" = \"{}\".", name.getTextContent(),
//                            value.getTextContent());
//                }
//            }
//        }
//    }
    
    /**
     * Gets the recursive folder path that represents today
     * 
     * @param rootFolder The root folder
     * @return The recursive folder name
     */
    private static String getTodaysFolderName(String rootFolder) {
        Calendar cal = Calendar.getInstance();	        
        StringBuilder sb = new StringBuilder();
        sb.append(rootFolder);
        sb.append(File.separator);
        sb.append(cal.get(Calendar.YEAR));
        sb.append(File.separator);
        sb.append(cal.get(Calendar.MONTH));
        sb.append(File.separator);
        sb.append(cal.get(Calendar.DAY_OF_MONTH));
        sb.append(File.separator);
        return sb.toString();
    }
    
    /**
     * Return the endpoint address from a <soap:address location="..."> tag
     * da WsdlUtils form axis
     */
    public static String getAddressFromPort(Port p) {
        // Get the endpoint for a port
        List extensibilityList = p.getExtensibilityElements();
        for (ListIterator li = extensibilityList.listIterator(); li.hasNext();) {
            Object obj = li.next();
            if (obj instanceof SOAPAddress) {
                return ((SOAPAddress) obj).getLocationURI();
            } else if (obj instanceof UnknownExtensibilityElement){
                //TODO: After WSDL4J supports soap12, change this code
                UnknownExtensibilityElement unkElement = (UnknownExtensibilityElement) obj;
                QName name = unkElement.getElementType();
                if(name.getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/soap12/") && 
                   name.getLocalPart().equals("address")) {
                    return unkElement.getElement().getAttribute("location");
                }
            }
        }
        // didn't find it
        return null;
    } // getAddressFromPort

}
