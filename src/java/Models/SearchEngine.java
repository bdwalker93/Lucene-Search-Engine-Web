package Models;

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.NotFoundException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.apache.lucene.search.IndexSearcher;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchEngine {
	final private  boolean GET_CONTENT_URL = false;
	final private  boolean PRINT_CONTENT_STRING = false;
	final private  boolean PRINT_CONTENT_BODY = false;
	final private  boolean PRINT_CONTENT_TEXT = false;

	final private  boolean USE_REAL_FILES = true;
	final private  int REAL_FILE_INDEX_LIMIT = -1;
	
	final private static  String REAL_INDEX = "index";
	final private  String HUMAN_READABLE_INDEX = "index.txt";
	
	final private  String INDEX_METRIC_SIZE_KEY = "indexSize";
	final private  String INDEX_METRIC_SIZE_COUNT_KEY = "indexFileCount";
	final private  String INDEX_METRIC_UNIQUE_KEY = "uniqueKeyCount";
	final private  String INDEX_METRIC_DOC_CT_KEY = "numberOfDocsRead";
	final private  String INDEX_METRIC_UNPARSABLE_CT = "numberOfUnparsableFiles";
        
        /*Need to set this when using a new computer*/
	final private  String PROJECT_FILE_LOCATION = "/Users/brettwalker/workspace/netbeans/Lucene-Search-Engine-Web/";
//	final private  String PROJECT_FILE_LOCATION = "C:\\Users\\Brett\\Documents\\NetBeansProjects\\Lucene-Search-Engine-Web\\";
	
	private enum operation { INDEX, SEARCH, PRINT_INDEX, PRINT_METRICS }  
	private int numberOfUnparsableFiles = 0;
	
	public static void main(String[] args) throws IOException, ParseException, Exception{		 		
		SearchEngine se = new SearchEngine();
		Directory index = null;

		operation op = operation.INDEX;
//		operation op = operation.SEARCH;
		
		File indexFile = new File(REAL_INDEX);
		 
		 //Check to make sure the index directory exists
		 if(!indexFile.isDirectory())
		 {
			indexFile.mkdir(); 
		 }
		
		 
		 index = new SimpleFSDirectory(indexFile.toPath());	
		 PrintWriter out = new PrintWriter(System.out, true); 
		 switch(op){
			 case INDEX:
				 se.indexCorpus("index", out, false);
				 se.printIndexMetrics("index",out);
				 break;
				 
			default:
				System.out.println("UNKNOWN OPERATION");
			 
		 }
		 
  
		 index.close();
	}
	
	public void indexCorpus(String indexLocation, PrintWriter out, boolean verbose){
	    //our map for link boosting
            HashMap<String, Integer> inlinks = new HashMap<>();
	
            out.println("***Beginning Indexing***<br>");

                try{
                    Directory index = null;
                    File indexFile = new File(PROJECT_FILE_LOCATION + indexLocation);
                     
                    out.println("This is our index location: " + indexFile.getAbsolutePath());
                    
                    //Check to make sure the index directory exists
                    if(!indexFile.isDirectory())
                    {
                        indexFile.mkdir();
                    }
                    
                    
                    index = new SimpleFSDirectory(indexFile.toPath());
                    
                    StandardAnalyzer analyzer = new StandardAnalyzer();
                    IndexWriterConfig config = new IndexWriterConfig(analyzer);
                    
                    //Sets how we handles an existing index
                    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                    
                    try{
                        
                        IndexWriter w = new IndexWriter(index, config);
                        
                        //Loading our courpus guide
                        File bookKeeping = new File(PROJECT_FILE_LOCATION + "WEBPAGES_RAW/bookkeeping.json");
                        JSONObject jsonObj = new JSONObject(String.join("", Files.readAllLines(bookKeeping.toPath(), StandardCharsets.UTF_8)));
                        
                        File inputFile = null;
                        
                        //THIS CHECK IS ONLY FOR DEVELOPMENT
                        if(USE_REAL_FILES)
                        {
                            JSONArray nameArr = jsonObj.names();
                            
                            // Traverse our bookeeping JSON file that has all of the paths of the files for us to index
                            for(int i = 0; i < nameArr.length() && (i < REAL_FILE_INDEX_LIMIT || REAL_FILE_INDEX_LIMIT == -1); i++)
                            {
                                if(verbose)
                                    out.println("<br>Currently Parsing #" + (i + 1) + " : WEBPAGES_RAW/" + (String)nameArr.get(i) + (GET_CONTENT_URL ? " -- This is the URL: " + jsonObj.getString((String)nameArr.get(i)) : ""));
                                
                                inputFile = new File(PROJECT_FILE_LOCATION + "WEBPAGES_RAW/" + (String)nameArr.get(i));
                                
                                try{
                                    
                                    if(addDoc(w, jsonObj.getString((String)nameArr.get(i)), inputFile, inlinks) == -1)
                                    {
                                        numberOfUnparsableFiles++;
                                        if(verbose)
                                            out.println("<br>***COULDNT BE ADDED TO DOC***(" + numberOfUnparsableFiles + ")");
                                        
                                    }
                                }
                                //catch(IllegalArgumentException e)
                                catch(Exception e)
                                {
                                    numberOfUnparsableFiles++;  
                                    if(verbose)
                                        out.println("<br>***ILLEGAL ARGUMENTS FOUND***(" + numberOfUnparsableFiles + ") :" + e.getMessage());
                                }
                                
                                //flush the buffer
                                out.flush();
                            }
                        }
                        else
                        {
                            //***TEST CODE***
                            
                            //for crista lopes new article
                            inputFile = new File(PROJECT_FILE_LOCATION + "WEBPAGES_RAW/5/218");
                            addDoc(w, "www2", inputFile, inlinks);
                        }
                         
//                        for(String key : inlinks.keySet())
//                        {
//                            out.print("<br>key: " + key + " value: " + inlinks.get(key));
//                        }
                        
                        //Bost all docs that were linked to 
//                        handleBoost(w, index, inlinks);
                        
                        //Close or commit IndexWriter to push changes for IndexReader
                        w.close();
                    }
                    catch (Exception e) {
                        out.println("There was some exception thrown during indexing: " + e.getMessage());
                    }
                }
		catch (IOException ex) {
                        out.println("Index File IOException");
		}
                
                
                out.println("<br>***Indexing Complete***");
	}
		
	/* Searches the passed index
	 * 
	 * */
	public void searchIndex(String indexLocation, String searchString, PrintWriter out){
                try {                   
                    Directory index = null;
                    
                    File indexFile = new File(PROJECT_FILE_LOCATION + indexLocation);
                    
                    //Check to make sure the index directory exists
                    if(!indexFile.exists() || !indexFile.isDirectory())
                    {
                        out.print("Where is the index?!?");
                        throw new NotFoundException("Index cannot be found!");
                    }
                    
                    index = new SimpleFSDirectory(indexFile.toPath());

                    IndexReader indexReader = DirectoryReader.open(index);
                    
                    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                    
                    Analyzer analyzer = new StandardAnalyzer();

                    String query_string = "title:" + searchString +  " OR content:" + searchString +  "OR important:" + searchString  + "OR h1:" + searchString +
                            "OR h2:" + searchString + "OR h3:" + searchString + "OR h4:" + searchString + "OR h5:" + searchString + "OR h6:" + searchString;
                    QueryParser queryParser = new QueryParser("title", analyzer);
                    
                    TopDocs docs = indexSearcher.search(queryParser.parse(query_string), 999999999);
                    
                    ScoreDoc[] hits = docs.scoreDocs;
                    
                    out.print("<h3 style='text-align: center' >Query Results</h3>");
                    out.print("<p style='text-align: center' >There were <span style='color: red'>" + hits.length + "</span> results found!</p>");

                    //beginning of results list
                    out.print("<ul>");

                    for(int i=0;i<hits.length;++i) {
                        int docId = hits[i].doc;
                        Document d = indexSearcher.doc(docId);
                        
                        String resultTitle = (i + 1) + ". " + d.get("title"); 
                        String urlToPage = d.get("url");
                        String hitScore = String.valueOf(hits[i].score);
                        
                        // Need to use the "//" before the url to show its not relative
                        out.print("<li style='list-style-type: none'>" + "<a href=\"//" + urlToPage + "\">" + resultTitle + "</a>" + "</li>");
                        
                    }
                    
                    //end of results list... should probably move into a finally
                    out.print("</ul>");

                } 
                catch (IOException ex) {
                    out.print("<br> IO Exception: " + ex.getMessage());
                } 
                catch (ParseException ex) {
                    out.print("<br> Parse Exception: " + ex.getMessage());
                }
	}
	
	/* Prints an inverted index of the corpus 
	 * 
	 * */
	public void printInvertedIndex(Directory index, boolean printToScreen, boolean printToFile){
		 
		try{
			 //Creating our index
			 IndexReader reader = DirectoryReader.open(index);

			 HashMap<String, HashSet<String>> hmap = convertIndexToMap(reader);
			 printIndexMap(hmap, printToScreen, printToFile);

			 reader.close();  
		}
		catch(Exception e){
			System.out.println("There was some exception thrown during printing: " + e.getStackTrace());
		}
	}
	/* Prints the metrics of the index 
	 * 
	 * */
	public void printIndexMetrics(String indexLocation, PrintWriter out){
		try{
                        Directory index = null;

                        File indexFile = new File(PROJECT_FILE_LOCATION + indexLocation);

                        //Check to make sure the index directory exists
                        if(!indexFile.exists() || !indexFile.isDirectory())
                        {
                            out.print("Where is the index?!?");
                            throw new NotFoundException("Index cannot be found!");
                        }

                        index = new SimpleFSDirectory(indexFile.toPath());

			 //Creating our index
			 IndexReader reader = DirectoryReader.open(index);

                         
			 //Need this map for metrics
			 HashMap<String, HashSet<String>> hmap = convertIndexToMap(reader);

                         
			 HashMap<String, String> metrics = calculateMetrics(hmap, reader); 

                         
			 //add the bad file metric... should probably just make this map global
			 metrics.put(INDEX_METRIC_UNPARSABLE_CT, String.valueOf(numberOfUnparsableFiles));

                         
			 printMetrics(metrics, out);
	
			 reader.close();  
		}
		catch(IOException e){
			out.println("There was some exception thrown during index printing: " + e.getMessage());
		}
	}
	
        private void boostDoc(IndexWriter w, Document doc, String uniqueField, String uniqueID, int inlink_count) throws IOException, ParseException{
                //{"title":2, "important":.2, "heading1-heading6":1, "content":.1, "url"}

                float iterativeBoost = (float)1.05;

                //doc object to be put into index and field type object for fields that can queried against
                Document newDoc = new Document();
                Field field = null;
                FieldType type = new FieldType();
                type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                type.setStored(true); 
                type.setStoreTermVectors(true);
                type.setTokenized(true);

                //Set title field in newDoc
                String title = doc.get("title");
                field = new Field("title", title, type);
                float titleboost = (float)2;
                field.setBoost(getNewBoost(inlink_count, titleboost, iterativeBoost));
                newDoc.add(field);

                //Set important field in newDoc
                String importantText = doc.get("important");
                field = new Field("important", importantText, type);
                float importantboost = (float).2;
                field.setBoost(getNewBoost(inlink_count, importantboost, iterativeBoost));
                newDoc.add(field);

                //Set content field in newDoc
                String contentText = doc.get("content");
                field = new Field("content", contentText, type);
                float contentBoost = (float).1;
                field.setBoost(getNewBoost(inlink_count, contentBoost, iterativeBoost));
                newDoc.add(field);

                //Set header fields in newDoc
                for(int i = 0; i<6; i++){
                        String headerTag = "heading" + i; //Set as [header0 - header6] according to addDoc
                        String headerText = doc.get(headerTag);
                        field = new Field(headerTag, headerText, type);
                        float headerBoost = (float).5;
                        field.setBoost(getNewBoost(inlink_count, headerBoost, iterativeBoost));
                        newDoc.add(field);
                }

                //create fieldType for fields that should not be queried against
                type = new FieldType();
                type.setStored(true);
                type.setTokenized(false);
                type.setStoreTermVectors(false);


                //Set url in newDoc
                newDoc.add(new Field("url", doc.get("url"), type));

                //Delete existing doc before adding new one
                Term term = new Term(uniqueField, uniqueID);
                w.deleteDocuments(term);

                //Add in new doc and commit changes
                w.addDocument(newDoc);
                w.commit();
        }

        private float getNewBoost(int inlink_count, float fieldBoost, float iterativeBoost){
                float newBoost = fieldBoost;

                for(int i = 0; i<inlink_count; i++){
                        newBoost *= iterativeBoost;
                }

                return newBoost;
        }

        private Document getDoc(Directory index, String field_name, String text) throws IOException, ParseException{
                Term term = new Term(field_name, text);
                Query q = new TermQuery(term);

                int hitsPerPage = 10;
                IndexReader reader = DirectoryReader.open(index);
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs docs = searcher.search(q, hitsPerPage);
                ScoreDoc[] hits = docs.scoreDocs;

                if(hits.length == 0)
                        return null;
                else
                        return searcher.doc(hits[0].doc); //Should only be 1 result if done correctly
        }

        private void handleBoost(IndexWriter w, Directory index, HashMap<String, Integer> inlinks) throws IOException, ParseException{
                Document doc;
                for(String key: inlinks.keySet()){
                        doc = getDoc(index, "url", key);
                        if(doc == null)
                                continue;
                        else
                                boostDoc(w, doc, "url", key, inlinks.get(key));
                }

        }
        
        private int addDoc(IndexWriter w, String url, File file, HashMap<String, Integer> inlinks) throws IOException, IllegalArgumentException {
            //File parsing
            org.jsoup.nodes.Document html = Jsoup.parse(String.join("",Files.readAllLines(file.toPath())));
            //If we cant parse the html
            if(html == null)
                    return -1;

            //Document Creation
            Document doc = new Document();
            Field field = null;
            FieldType type = new FieldType();
            type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            type.setStored(true); 
            type.setStoreTermVectors(true);
            type.setTokenized(true);
            
            Element body = html.body();

            String title = null;
            Element head = html.head();
            if(head != null)
                    title = head.text();

            //If there is text in the head, it is probably a title
            if(title != null && !title.isEmpty()){
                    field = new Field("title", title, type);
                    field.setBoost(2); //Set weight for the field when query matches to string in field here
                    doc.add(field);

            }
            
            //Grab the important text tags
            Elements linkTags = null;
            if(body != null)
                linkTags = body.select("a");

            if(linkTags != null && !linkTags.isEmpty())
            {
                for(Element e : linkTags)
                {
                    String extractedLink = e.absUrl("href");
                    if(inlinks.containsKey(extractedLink)){
                        inlinks.put(url, inlinks.get(extractedLink) + 1);
                    }
                    else
                    {
                        inlinks.put(url, 1);  
                    }
                }
            }
            
            //Grab the important text tags
            Elements importantTags = null;
            if(body != null)
                importantTags = body.select("b, strong, em");

            if(importantTags != null && !importantTags.isEmpty())
            {
                    field = new Field("important", importantTags.html(), type);

                    //Setting the bold tag boost
                    field.setBoost((float).2); 
                    doc.add(field);

                    //We remove any content in these tags so there is no duplicate counting
                    importantTags.remove();
            }

            //Grab all heading tags
            Elements headingTags = null;
            if(body != null)
                headingTags = body.select("h1, h2, h3, h4, h5, h6");

            for(int headingNum = 0; headingNum < 6; headingNum++)
            {
                    //Attempt to index all the heading tags
                    Elements hTags = null;
                    if(headingTags != null)
                        hTags = headingTags.select("h" + (headingNum + 1));
                    
                    if(hTags != null && !hTags.isEmpty())
                    {
                            field = new Field("heading" + headingNum, hTags.html(), type);

                            //Setting the heading tag boost
                            field.setBoost(1); 
                            doc.add(field);

                            //We remove any content in these tags so there is no duplicate counting
                            hTags.remove();
                    }

            }
            
            //Get the rest of the text in the body
            String content = null;
            if(body != null)
                    content = body.text();

            //Need to parse the remaining content after all the important tags have been deleted 
            if(content != null && !content.isEmpty()){
                    field = new Field("content", content, type);

                    //Setting the default boost
                    field.setBoost((float).1); 
                    doc.add(field);
            }

            //Need to make sure we have content before attempting to add a link to a document
            if(doc.getFields().size() > 0)
            {
                    //fileID field should not be used for finding terms within document, only for uniquely identifying this doc amongst others in index
                    type = new FieldType();
                    type.setStored(true);
                    type.setTokenized(false);
                    type.setStoreTermVectors(false);
                    doc.add(new Field("url", url, type));	

                    w.addDocument(doc);
                    return 0;
            }

            return -1;
        }
	
	/*
	Will iterate through all documents held within the index and append to a map with key representing the term and value being all documents that have
	the term within it. This will assume terms can be within any field even the title of the document
	*/
	private HashMap<String, HashSet<String>> convertIndexToMap(IndexReader reader) throws IOException{
		 HashMap<String, HashSet<String>> hmap = new HashMap<String, HashSet<String>>();
		 HashSet<String> docIdSet;
		 
		 for(int i=0; i<reader.numDocs(); i++){
			 Fields termVect = reader.getTermVectors(i);

			 if(termVect == null)
				 continue;
			 
	         for (String field : termVect) {
	        	 
	             Terms terms = termVect.terms(field);
	             TermsEnum termsEnum = terms.iterator();
	             Document doc = reader.document(i);
	             
	             while(termsEnum.next() != null){
	            	 BytesRef term = termsEnum.term();
	            	 String strTerm = term.utf8ToString();
	            	 
	            	 if (hmap.containsKey(strTerm)){
	            		 docIdSet = hmap.get(strTerm);
	            		 docIdSet.add(doc.get("url"));
	            	 } else{
	            		 docIdSet = new HashSet<String>();
	            		 docIdSet.add(doc.get("url"));
	            		 hmap.put(strTerm, docIdSet);
	            	 }
	             }
	         }
		 }
		 
		 return hmap;
	}
	
	/*
        ****************Needs to be fixed for UI*****
        */
	//Iterates through map and prints out as a postings as seen in lectures
	private void printIndexMap(HashMap<String, HashSet<String>> hmap, boolean printToScreen, boolean printToFile){
		
		try{
			//False overwrites old data
			FileWriter writer = new FileWriter(HUMAN_READABLE_INDEX, false); 
		
			//creating output string
			String output = new String();
	        for(String key: hmap.keySet()){
		       	output = key + " -> " + String.join(", ", hmap.get(key));
		       	
		        //Printing to screen
		        if(printToScreen)
		        	System.out.println(output);
		        
		        if(printToFile){
					try {
						writer.write(output + "\n");
					} catch (IOException e){}	
		        } 	
	        }
	        
	        writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public  void printMetrics(HashMap<String, String> metrics, PrintWriter out){
            out.print("<h2>Index Metrics</h2>");
            out.print("<ul>");
            
            //list contents
            out.println("<li>Total number of flat (.cfs files) files storing the index: " + metrics.get(INDEX_METRIC_SIZE_COUNT_KEY) + "</li>");
            out.println("<li>Size of the complete index size: " + metrics.get(INDEX_METRIC_SIZE_KEY) +  " MB" + "</li>");
            out.println("<li>Total Unique Terms: " + metrics.get(INDEX_METRIC_UNIQUE_KEY) + "</li>");
            out.println("<li>Total number of documents: " + metrics.get(INDEX_METRIC_DOC_CT_KEY) + "</li>");
            out.println("<li>Total number of unparsable documents: " + metrics.get(INDEX_METRIC_UNPARSABLE_CT) + "</li>");		
	    out.print("</ul>");

        }

	private HashMap<String, String> calculateMetrics(HashMap<String, HashSet<String>> hmap, IndexReader reader)
	{
		double totalIndexSize = 0;
		int numOfCfsFiles = 0;

		File indexFile = new File(PROJECT_FILE_LOCATION + REAL_INDEX);
                
		 for(File file : indexFile.listFiles())
		 {
                    if(file.getName().endsWith(".cfs"))
                    {
                            numOfCfsFiles++;
                            totalIndexSize += file.length() / (double)1024 / 1024;
                    }
		 }

		 //Puts the metric results in a hashmap
		HashMap<String, String> results = new HashMap<>(); 
		results.put(INDEX_METRIC_SIZE_KEY, String.format("%.2f", totalIndexSize));
		results.put(INDEX_METRIC_SIZE_COUNT_KEY, String.valueOf(numOfCfsFiles));
		results.put(INDEX_METRIC_UNIQUE_KEY, String.valueOf(hmap.size()));
		results.put(INDEX_METRIC_DOC_CT_KEY, String.valueOf(reader.numDocs()));

		return results;
	}
}
