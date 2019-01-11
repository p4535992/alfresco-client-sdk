package com.alfresco.client.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;

import com.alfresco.client.AlfrescoClientSwagger;
import com.alfresco.client.constant.ContentModel;
import com.alfresco.swagger.api.client.CollectionFormats.CSVParams;
import com.alfresco.swagger.api.model.NodeEntry;
import com.alfresco.swagger.api.model.PathElement;
import com.alfresco.swagger.api.model.PathInfo;



/**
 * Alfresco has ability to contain information, documents and their metadata – we can say that holding data in structured and organized way is important but here is one thing that we can not live without and that is retrieving the information.<br>
	<br>
	In this tutorial we are going to explain how to use lucene search and get nodes that you need in fast and easy.<br>
	<br>
	In alfresco everything is a node, node can be of certain type, with aspects applied to it and can have number of properties. Nodes that have content are interesting too as we can search the content as well, limited to those nodes that can be converted to text.<br>
	Type Search<br>
	<br>
	Syntax : TYPE:”{type name}”<br>
	<br>
	Examples for searching folders<br>
	<br>
	TYPE:"cm:folder"<br>
	<br>
	TYPE:"{http://www.alfresco.org/model/content/1.0}folder"<br>
	<br>
	Property Search<br>
	<br>
	Syntax: @{prefix}\:{property Name}:”Value to match”<br>
	<br>
	Examples:<br>
	<br>
	@cm\:name:"Buyer"<br>
	<br>
	@cm\:name:"Buy*"<br>
	<br>
	Aspect Search<br>
	<br>
	Syntax: ASPECT:”{aspect name}”<br>
	<br>
	Examples:<br>
	<br>
	ASPECT:"ab:invoice"<br>
	<br>
	ASPECT:"{http://www.alfrescoblog.com/model/data/1.0}invoice"<br>
	<br>
	Text Search<br>
	<br>
	Syntact: TEXT:”query”<br>
	<br>
	Examples:<br>
	<br>
	TEXT:"about"<br>
	<br>
	TEXT:"ab0*"<br>
	<br>
	Path Search<br>
	<br>
	Using the path search we can search for for example all nodes that are childs direct or not to defined folder. Lets see few examples.<br>
	<br>
	    To select folder test that is under Company Home use:<br>
	<br>
	    PATH:"/app:company_home/cm:test"<br>
	<br>
	    All directly below folder test<br>
	<br>
	    PATH:"/app:company_home/cm:test/*"<br>
	<br>
	    All nodes below folder test on any depth<br>
	<br>
	    PATH:"/app:company_home/cm:test//*"<br>
	<br>
	    All nodes below folder test on any depth and include folder text<br>
	<br>
	    PATH:"/app:company_home/cm:test//."<br>
	<br>
	Search Combinations<br>
	<br>
	This is great but it is not enough, we need search combinations. Node with certain type and name and so on.<br>
	<br>
	Combinations are achieved using AND, OR and NOT keywords. Also there is a way for using ‘+’ and ‘-‘.<br>
	<br>
	To match one or other<br>
	<br>
	TEXT:"query"   @cm\:name:"Buy*<br>
	<br>
	TEXT:"query" OR  @cm\:name:"Buy*<br>
	<br>
	To match both<br>
	<br>
	+TEXT:"query"   +@cm\:name:"Buy*<br>
	<br>
	TEXT:"query" AND  @cm\:name:"Buy*<br>
	<br>
	To match one attribute and not other<br>
	<br>
	+ASPECT:"ab:invoice"  AND  -@cm\:description:"Buy*<br>
	<br>
	ASPECT:"ab:invoice"  AND NOT -@cm\:description:"Buy*<br>
	<br>
	To NOT match one criteria we can not put -@cm\:description:”Buy*, for this we must add one criteria to match and other not to match like so<br>
	<br>
	 +TYPE:"sys:base"  -@cm\:description:"Buy*<br>
	<br>
	 Summary<br>
	<br>
	We have shown how to use Lucene and query your nodes, for any questions let us know, we will be happy to help.<br>

 */
public class LuceneUtils {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LuceneUtils.class);
	
	public static void main(String[] args){
		Map<String,String> map = new HashMap<>();
		map.put(ContentModel.PROP_NAME, "name Test");
		map.put(ContentModel.PROP_TITLE, "title Test");
		map.put(ContentModel.PROP_DESCRIPTION, "description Test");
		map.put(ContentModel.TYPE_CONTENT, "content");
		
		//+PATH:"/app:company_home/cm:ASL1/cm:Archivio/cm:Spazio_x0020_Corrente//*" AND +@asl1cc\:codice_ospedale:"26" AND TYPE:"cm:folder"
		String luceneQueryFull = buildLuceneQuery(map,"165e7340-9f53-422c-b619-e66f238d2dd6",TypeSearch.FULL,false,null,null);		
		System.out.println(luceneQueryFull);
		System.out.println();
		
		String luceneQueryAny = buildLuceneQuery(map,"165e7340-9f53-422c-b619-e66f238d2dd6",TypeSearch.ANY,false,null,null);		
		System.out.println(luceneQueryAny);
		System.out.println();
		
		String luceneQueryExact = buildLuceneQuery(map,"165e7340-9f53-422c-b619-e66f238d2dd6",TypeSearch.EXACT,false,null,null);		
		System.out.println(luceneQueryExact);
		System.out.println();
	}
	
	private static String getPathFromNodeRef(AlfrescoClientSwagger alfrescoClientSwagger,String nodeRefnodeID) throws WebApplicationException{
		try{
			
			String nodeID = "";
			if(nodeRefnodeID.startsWith("workspace://SpacesStore/")){
				nodeID = nodeRefnodeID.replace("workspace://SpacesStore/", "");
			}else{
				nodeID = nodeRefnodeID;
			}
			
			CSVParams includes = new CSVParams();
			includes.setParams(Arrays.asList("allowableOperations", "association","isLink", "isLocked","path", "permissions","properties","aspectNames"));

			retrofit2.Response<NodeEntry> resp = alfrescoClientSwagger.getNodesAPI().getNode(
					nodeID, 
					includes, 
					null, 
					null).execute();
			
			if(!resp.isSuccessful()){
				logger.error(resp.errorBody().source().readUtf8());				
				throw new WebApplicationException(resp.errorBody().source().readUtf8(),500);
			}				
			NodeEntry nodeEntry = resp.body();

			Map<String, Object> props = (Map<String, Object>) nodeEntry.getEntry().getProperties();
			String path = nodeEntry.getEntry().getPath().getName();
			
			return getRelativePathFromNodeEntry(nodeEntry.getEntry().getPath(),true,false);
			
		}catch(Throwable ex){
			throw (WebApplicationException)ex;
		}
	}
	
//	/**
//	 * Method to convert a noderef to a relative path for use in lucene query
//	 * @param nodeEntry
//	 * @param encoded
//	 * @return
//	 */
//	private static String getRelativePathFromNodeEntry(NodeEntry nodeEntry,boolean encoded){
//		return getRelativePathFromNodeEntry(nodeEntry.getEntry().getPath(), encoded);
//	}
//	
	/**
	 * Method to convert a noderef to a relative path for use in lucene query
	 * @param pathInfo
	 * @param encoded
	 * @return
	 */
	public static String getRelativePathFromNodeEntry(PathInfo pathInfo,boolean encoded,boolean includedName){
		StringBuilder relativePath = new StringBuilder();
		if(pathInfo!=null){
			relativePath.append(File.separator);
			//TODO verify a better way to do this
			
			boolean flagCompanyHome = true;
			boolean flagUserHome = true;
			boolean flagDictionary = true;
			boolean flagGuestHome = true;
			boolean flagSites = true;
			boolean flagShared = true;
			
			for(PathElement elem : pathInfo.getElements()){
				if(flagCompanyHome && elem.getName().equalsIgnoreCase("Company Home")){
					relativePath.append("app:company_home"+File.separator);
					flagCompanyHome = false;
				}else if(flagUserHome && elem.getName().equalsIgnoreCase("User Homes")){
					relativePath.append("app:user_homes"+File.separator);
					flagUserHome = false;
				}else if(flagDictionary && elem.getName().equalsIgnoreCase("Data Dictionary")){
					relativePath.append("app:dictionary"+File.separator);
					flagDictionary = false;
				}else if(flagGuestHome && elem.getName().equalsIgnoreCase("Guest Home")){
					relativePath.append("app:guest_home"+File.separator);
					flagGuestHome = false;
				}else if(flagSites && elem.getName().equalsIgnoreCase("Sites")){
					relativePath.append("st:sites"+File.separator);
					flagSites = false;
				}else if(flagShared && (elem.getName().equalsIgnoreCase("Condiviso") || elem.getName().equalsIgnoreCase("Shared"))){
					relativePath.append("app:shared"+File.separator);
					flagShared = false;
				}
				else{
					if(encoded){
						relativePath.append("cm:"+ISO9075Utils.encode(elem.getName())+File.separator);
					}else{
						relativePath.append("cm:"+elem.getName()+File.separator);
					}
				}
			}				
		}
		
		if(includedName){
			if(encoded){
				relativePath.append("cm:"+ISO9075Utils.encode(pathInfo.getName())+File.separator);
			}else{
				relativePath.append("cm:"+pathInfo.getName()+File.separator);
			}
		}
		
		String s = relativePath.toString();
		if(s.endsWith(File.separator)){
			s = s.substring(0, s.length()-File.separator.length());
		}
		return s;
	}
	
	/**
	 * NOTA: questa ricerca sara' sempre non ricorsiva, ma limitaita ai filgi della cartella specificata
	 * @param paramsToSearch
	 * @param nodeRefolderID
	 * @param typeSearch
	 * @return
	 */
	public static String buildLuceneQuery(Map<String,String> paramsToSearch, String nodeRefolderID,TypeSearch typeSearch){
		return buildLuceneQuery(paramsToSearch, nodeRefolderID, typeSearch,false,null,null);
	}
	
	/**
	 * @href http://alfrescoblog.com/2014/07/09/alfresco-lucene-tutorial/
	 * @param paramsToSearch
	 * @param folderID
	 * @param typeSearch
	 * @return
	 */
	public static String buildLuceneQuery(Map<String,String> paramsToSearch, String nodeRefolderID,TypeSearch typeSearch,boolean recursively,AlfrescoClientSwagger alfrescoClientSwagger){
		return  buildLuceneQuery(paramsToSearch, nodeRefolderID, typeSearch,false,null,alfrescoClientSwagger);
	}
	
	/**
	 * @href http://alfrescoblog.com/2014/07/09/alfresco-lucene-tutorial/
	 * @param paramsToSearch
	 * @param folderID
	 * @param typeSearch
	 * @return
	 */
	public static String buildLuceneQuery(Map<String,String> paramsToSearch, String nodeRefolderID,TypeSearch typeSearch,boolean recursively,String folderPath){
		return  buildLuceneQuery(paramsToSearch, nodeRefolderID, typeSearch,false,folderPath,null);
	}
	
	/**
	 * @href http://alfrescoblog.com/2014/07/09/alfresco-lucene-tutorial/
	 * @param paramsToSearch
	 * @param folderID
	 * @param typeSearch
	 * @return
	 */
	public static String buildLuceneQuery(Map<String,String> paramsToSearch, String nodeRefolderID,TypeSearch typeSearch,boolean recursively,String folderPath, AlfrescoClientSwagger alfrescoClientSwagger){
			Map<String,String> params = new HashMap<>(paramsToSearch);
			String folderID = "";
			if(nodeRefolderID.startsWith("workspace://SpacesStore/")){
				folderID = nodeRefolderID.replace("workspace://SpacesStore/", "");
			}else{
				folderID = nodeRefolderID;
			}
		
			StringBuilder luceneQuery = new StringBuilder();	        
		    String condition = " AND ";
		    //TODO DA RIVEDERE LA GESTIONE DELLE CONDIZIONI
        	if(typeSearch.equals(TypeSearch.FULL)){
        		condition = " OR ";       		
 		    }else if(typeSearch.equals(TypeSearch.ANY)){
 		    	condition = " OR ";	    	
 		    }else if(typeSearch.equals(TypeSearch.EXACT)){
 		    	condition = " OR ";
 		    }else{
 		    	throw new WebApplicationException("The type search <"+typeSearch+"> is not supported");
 		    }
	    
		    //FILTER TYPE	
		    if(params.containsKey(ContentModel.TYPE_CONTENT)){
		    	 luceneQuery.append("+TYPE:\"").append(ContentModel.TYPE_CONTENT).append("\" ");
		    	 params.remove(ContentModel.TYPE_CONTENT);
		    }else if(params.containsKey(ContentModel.TYPE_FOLDER)){
		    	luceneQuery.append("+TYPE:\"").append(ContentModel.TYPE_FOLDER).append("\" ");
		    	params.remove(ContentModel.TYPE_FOLDER);
		    }
		    
		    //Indeed, PARENT is not searching recursively. This what PATH is intended for: to search recursively.
		    //And this is also why PATH is slower than PARENT: it is slower because it searches recursively.
	    
		    //FILTER PARENT
		    //PARENT:\"" + STORE.getScheme() + "://" + STORE.getAddress() + "/" + "eec7d9d6-8470-11db-85c2-01d4040f47aa" + "\"";	 
		    
		    String path = "";
		    if(folderPath!=null && !folderPath.isEmpty()){
		    	path = folderPath;
		    }else{
		    	if(alfrescoClientSwagger==null){
		    		throw new WebApplicationException("The alfrescoClientSwagger is NULL for the node ID : <"+folderID+"> ");
		    	}else{
				    try{		    	
				    	path = getPathFromNodeRef(alfrescoClientSwagger,folderID);
				    }catch(Throwable ex){
				    	throw new WebApplicationException("The alfresco is not reachable OR the node ID : <"+folderID+"> do not exists OR you don't have permissions" ,ex);
				    }
		    	}
		    }
		    
		    if(recursively){	
		    	if(path!=null && !path.isEmpty()){
			    	luceneQuery.append("+PATH:\"").append(path);
			    	
				    //if(params.containsKey(ContentModel.TYPE_CONTENT)){
				    //	luceneQuery.append("//*"); //All nodes below folder test on any depth
				    //}else if(params.containsKey(ContentModel.TYPE_FOLDER)){
				    //	luceneQuery.append("//."); //All nodes below folder test on any depth and include folder text
				    //}
				    luceneQuery.append("//."); 
				    
			    	luceneQuery.append("\" ");  
		    	}else{
		    		throw new WebApplicationException("The path is NULL for the node ID : <"+folderID+"> ");
		    	}
		    }else{
		    	if(path!=null && !path.isEmpty()){
		    		luceneQuery.append("+PATH:\"").append(path).append("/*").append("\" ");
		    	}else{
		    		luceneQuery.append("+PARENT:\"").append("workspace").append("://").append("SpacesStore").append("/").append(folderID).append("\" ");  
		    	}
		    }
		    	
		    //FILTER path
		    
		    //"PATH:\"/company_home.//*\"" + "@cm\abstract:mimetype:" + "application/pdf");
		    //.append("+PATH:\"")
		    //.append(ISO9075.encode(relativePath)
		    //.append("//.\" ");
		    //luceneQuery.append("+PATH:\"/cm:taggable/cm:").append(ISO9075.encode(tag)).append("/member\"");
		    
		    luceneQuery.append(" AND ").append("( ");
		    StringJoiner joinerRoot = new StringJoiner(condition);	
	        for(Map.Entry<String, String> entry : params.entrySet()){	
	        	StringBuilder luceneQueryEntry = new StringBuilder();
	        	String key = entry.getKey();	        	
	        	key = key.replaceAll("cm:", "@cm\\\\:");
	        	

	        	List<Object> values = new ArrayList<Object>();
	        	if(typeSearch.equals(TypeSearch.FULL)){	        		
	        		values.add(entry.getValue());
	        		//values.add(entry.getValue().toLowerCase());
	        		//values.add(entry.getValue().toUpperCase());
	 		    }else if(typeSearch.equals(TypeSearch.ANY)){	 		    	
	 		    	values.addAll(Arrays.asList(entry.getValue().split("\\s+")));
	 		    }else if(typeSearch.equals(TypeSearch.EXACT)){
	 		    	values.add(entry.getValue());
	 		    }else{
	 		    	throw new WebApplicationException("Il tipo di ricerca <"+typeSearch+"> non e' supportato");
	 		    }
	         	
	        	StringJoiner joiner = new StringJoiner(condition);	        	
	        	for(Object valueS : values){	 
	        		String valueF = null;
	        		if(valueS instanceof String){
		        		valueF = ISO9075Utils.encode(String.valueOf(valueS));	
		        	}
		        	if(valueS instanceof Date){
		        		valueF = getLuceneDateString((Date) valueS);
		        	}	        		
		        	StringBuilder lucenequeryPart = new StringBuilder();
		        	lucenequeryPart.append(key).append(":").append("\"");
		        	if(typeSearch.equals(TypeSearch.ANY)){
		        		
		        	}
		        	if(typeSearch.equals(TypeSearch.FULL)){	        		
		        		lucenequeryPart.append("*"+valueF+"*");
		 		    }else if(typeSearch.equals(TypeSearch.ANY)){	 		    	
		 		    	lucenequeryPart.append("*"+valueF+"*");
		 		    }else if(typeSearch.equals(TypeSearch.EXACT)){
		 		    	lucenequeryPart.append(valueF);
		 		    }else{
		 		    	throw new WebApplicationException("Il tipo di ricerca <"+typeSearch+"> non e' supportato");
		 		    }
		        	lucenequeryPart.append("\"");
		        	joiner.add(lucenequeryPart.toString());
	        	}	
	        	luceneQueryEntry.append(joiner.toString());
	        	//luceneQuery.append(" AND ");
	        	joinerRoot.add(luceneQueryEntry);
	        }
	        luceneQuery.append(joinerRoot.toString());
	        luceneQuery.append(" )");
	        logger.info("EXECUTE LUCENE QUERY:\n" + luceneQuery.toString());
	        return luceneQuery.toString();
	}
	
	
    /**
     * This is the date string format as required by Lucene e.g. "1970\\-01\\-01T00:00:00"
     * @since 4.0
     */
    private static final SimpleDateFormat LUCENE_DATETIME_FORMAT = new SimpleDateFormat("yyyy\\-MM\\-dd'T'HH:mm:ss");
    
//    public static boolean fieldHasTerm(IndexReader indexReader, String field) throws IOException
//    {
//        try
//        {
//            TermEnum termEnum = indexReader.terms(new Term(field, ""));
//            try
//            {
//                if (termEnum.next())
//                {
//                    Term first = termEnum.term();
//                    return first.field().equals(field);
//                }
//                else
//                {
//                    return false;
//                }
//            }
//            finally
//            {
//                termEnum.close();
//            }
//        }
//        catch (IOException e)
//        {
//            throw new IOException("Could not find terms for sort field ", e);
//        }
//    }
    
    /**
     * Returns a date string in the format required by Lucene.
     * 
     * @since 4.0
     */
    public static String getLuceneDateString(Date date)
    {
        return LUCENE_DATETIME_FORMAT.format(date);
    }
    
    
    
//    /**
//     * This method creates a Lucene query fragment which constrains the specified dateProperty to a range
//     * given by the fromDate and toDate parameters.
//     * 
//     * @param fromDate     the start of the date range (defaults to 1970-01-01 00:00:00 if null).
//     * @param toDate       the end of the date range (defaults to 3000-12-31 00:00:00 if null).
//     * @param dateProperty the Alfresco property value to check against the range (must be a valid Date or DateTime property).
//     * 
//     * @return the Lucene query fragment.
//     * 
//     * @throws NullPointerException if dateProperty is null or if the dateProperty is not recognised by the system.
//     * @throws IllegalArgumentException if dateProperty refers to a property that is not of type {@link DataTypeDefinition#DATE} or {@link DataTypeDefinition#DATETIME}.
//     */
//    public static String createDateRangeQuery(Date fromDate, Date toDate, QName dateProperty, 
//          DictionaryService dictionaryService, NamespaceService namespaceService)
//    {
//        // Some sanity checking of the date property.
//        if (dateProperty == null)
//        {
//            throw new NullPointerException("dateProperty cannot be null");
//        }
//        PropertyDefinition propDef = dictionaryService.getProperty(dateProperty);
//        if (propDef == null)
//        {
//            throw new NullPointerException("dateProperty '" + dateProperty + "' not recognised.");
//        }
//        else
//        {
//            final QName propDefType = propDef.getDataType().getName();
//            if ( !DataTypeDefinition.DATE.equals(propDefType) &&
//                    !DataTypeDefinition.DATETIME.equals(propDefType))
//            {
//                throw new IllegalArgumentException("Illegal property type '" + dateProperty + "' [" + propDefType + "]");
//            }
//        }
//        
//        QName propertyName = propDef.getName();
//        final String shortFormQName = propertyName.toPrefixString(namespaceService);
//        final String prefix = shortFormQName.substring(0, shortFormQName.indexOf(QName.NAMESPACE_PREFIX));
//        final String localName = propertyName.getLocalName();
//        
//        
//        // I can see potential issues with using 1970 and 3000 as default dates, but this is what the previous
//        // JavaScript controllers/libs did and I'll reproduce it here.
//        final String ZERO_DATE = "1970\\-01\\-01T00:00:00";
//        final String FUTURE_DATE = "3000\\-12\\-31T00:00:00";
//        
//        StringBuilder luceneQuery = new StringBuilder();
//        luceneQuery.append(" +@").append(prefix).append("\\:").append(localName).append(":[");
//        if (fromDate != null)
//        {
//            luceneQuery.append(LuceneUtils.getLuceneDateString(fromDate));
//        }
//        else
//        {
//            luceneQuery.append(ZERO_DATE);
//        }
//        luceneQuery.append(" TO ");
//        if (toDate != null)
//        {
//            luceneQuery.append(LuceneUtils.getLuceneDateString(toDate));
//        }
//        else
//        {
//            luceneQuery.append(FUTURE_DATE);
//        }
//        luceneQuery.append("] ");
//        return luceneQuery.toString();
//    }
		
//	/**
//	 * This method creates a Lucene query fragment which constrains the specified dateProperty to a range
//	 * given by the fromDate and toDate parameters.
//	 * 
//	 * @param fromDate     the start of the date range (defaults to 1970-01-01 00:00:00 if null).
//	 * @param toDate       the end of the date range (defaults to 3000-12-31 00:00:00 if null).
//	 * @param dateProperty the Alfresco property value to check against the range (must be a valid Date or DateTime property).
//	 * 
//	 * @return the Lucene query fragment.
//	 * 
//	 * @throws NullPointerException if dateProperty is null or if the dateProperty is not recognised by the system.
//	 * @throws IllegalArgumentException if dateProperty refers to a property that is not of type {@link DataTypeDefinition#DATE} or {@link DataTypeDefinition#DATETIME}.
//	 */
//	private String createDateRangeQuery(Date fromDate, Date toDate, QName dateProperty)
//	{
//	   return LuceneUtils.createDateRangeQuery(fromDate, toDate, dateProperty, dictionaryService, namespaceService);
//	}
    
    
    public static enum TypeSearch{
    	
    	ANY("any"),
    	FULL("full"),
    	EXACT("exact");
    	
    	private String reference;

		private TypeSearch(String reference) {
			this.reference = reference;
		}

		public String getReference() {
			return reference;
		}
		
		public static TypeSearch fromString(String text) {
		    for (TypeSearch b : values()) {
		      if (b.reference.equalsIgnoreCase(text)) {
		        return b;
		      }
		    }		    
		    return null;
		}
    }

}
