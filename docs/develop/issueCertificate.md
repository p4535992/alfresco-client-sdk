### Added two new costruct to Builder for explicit avoid HOstname and certifctae conflicts like javax.net.ssl.SSLPeerUnverifiedExceptionr

```java

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
            }
            if(acceptAllCertificates){
				try {				
	        		this.sslContext = WsdlUtils.setSystemTrustAllCertificateSSLContext();
				} catch (NoSuchAlgorithmException | KeyManagementException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					return null;
				}
        		
            }
            return this;
        }

```

Where the methods for build hostname are these:

```java

	public static HostnameVerifier setSystemTrustAllHost(){
		//TRUST ALL HOST Ignore differences between given hostname and certificate hostname
	    HostnameVerifier hv = new HostnameVerifier(){
	    	public boolean verify(String hostname, SSLSession session) { return true; }
	    };
	    //hv.verify(wsdlURL.getHost(),sc.getClientSessionContext().getSession());
	    //HttpsURLConnection.setDefaultHostnameVerifier(hv);
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
		//SSLContext sc = SSLContext.getInstance(sslProtocol);
		//sc.init(null, trustAllCerts, new SecureRandom());
		//HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		return trustAllCerts;
	}

```
