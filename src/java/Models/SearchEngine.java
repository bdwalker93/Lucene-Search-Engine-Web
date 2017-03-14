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
    	final private static boolean PRINT_INDEX_TO_SCREEN = false;
	final private static boolean PRINT_INDEX_TO_FILE = false;
	final private static boolean PRINT_METRIC_TO_SCREEN = true;
	final private static boolean PRINT_METRIC_TO_FILE = false;
	
	final private static boolean PRINT_INDEX_TO_PARSING = false;

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
	
	private enum operation { INDEX, SEARCH, PRINT_INDEX, PRINT_METRICS }  
	private int numberOfUnparsableFiles = 0;
	
	public static void main(String[] args) throws IOException, ParseException, Exception{		 		
//		SearchEngine se = new SearchEngine();
//		Directory index = null;
//
//		operation op = operation.INDEX;
////		operation op = operation.SEARCH;
//		
//		File indexFile = new File(REAL_INDEX);
//		 
//		 //Check to make sure the index directory exists
//		 if(!indexFile.isDirectory())
//		 {
//			indexFile.mkdir(); 
//		 }
//		
//		 
//		 index = new SimpleFSDirectory(indexFile.toPath());	
//		 
//		 switch(op){
//			 case INDEX:
//				 se.indexCorpus(index);
//				 System.out.println("***INDEXING COMPLETE***");
//				 se.printInvertedIndex(index, PRINT_INDEX_TO_SCREEN, PRINT_INDEX_TO_FILE);
//
//				 break;
//				 
//			 case SEARCH:
//				 se.searchIndex(index);
//				 break;
//				 
//			 case PRINT_INDEX:
//				 se.printInvertedIndex(index, PRINT_INDEX_TO_SCREEN, PRINT_INDEX_TO_FILE);
//				 break;
//				 
//			 case PRINT_METRICS:
//				 se.printIndexMetrics(index, PRINT_METRIC_TO_SCREEN, PRINT_METRIC_TO_FILE);
//				 break;
//				 
//			default:
//				System.out.println("UNKNOWN OPERATION");
//			 
//		 }
//		 
//  
//		 index.close();
	}
	
	public void indexCorpus(String indexLocation, PrintWriter out){
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
//                                out.println("<br>Currently Parsing #" + (i + 1) + " : WEBPAGES_RAW/" + (String)nameArr.get(i) + (GET_CONTENT_URL ? " -- This is the URL: " + jsonObj.getString((String)nameArr.get(i)) : ""));
                                
                                inputFile = new File(PROJECT_FILE_LOCATION + "WEBPAGES_RAW/" + (String)nameArr.get(i));
                                
                                try{
                                    
                                    if(addDoc(w, jsonObj.getString((String)nameArr.get(i)), inputFile) == -1)
                                    {
                                        numberOfUnparsableFiles++;
                                    }
                                }
                                //catch(IllegalArgumentException e)
                                catch(Exception e)
                                {
                                    out.println("<br>***ILLEGAL ARGUMENTS FOUND***: " + e.getMessage());
                                    numberOfUnparsableFiles++;
                                    
                                    out.print(e.getStackTrace());
                                }
                            }
                        }
                        else
                        {
                            //***TEST CODE***
//				 inputFile = new File("SampleTextDoc.txt");
//				 addDoc(w, "www1", inputFile);
//
//				 inputFile = new File("secondSampleTextDoc.txt"); 
//				 addDoc(w, "www2", inputFile);
//
//				 inputFile = new File("WEBPAGES_RAW/0/189"); 
//				 addDoc(w, "www2", inputFile);
//				 
//				 //crista lopes home page
//				 inputFile = new File("WEBPAGES_RAW/51/46"); 
//				 addDoc(w, "www2", inputFile);
                            
                            //for crista lopes new article
                            inputFile = new File("WEBPAGES_RAW/57/392");
                            addDoc(w, "www2", inputFile);
                        }
                        
                        
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
	
	/* Reads user input to get search string
	 * 
	 * */
	private String getSearchString(){
		Scanner scanner = new Scanner(System.in);
		String phrase = null;
		
		System.out.print("Please enter the search phrase: ");
		phrase = scanner.nextLine();
		
		scanner.close();
		
		return phrase;
	}
	
	/* Searches the passed index
	 * 
	 * */
	public void searchIndex(Directory index) throws Exception{
		String searchString = getSearchString();
		
		System.out.println("Searching for '" + searchString + "'");

		IndexReader indexReader = DirectoryReader.open(index);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		Analyzer analyzer = new StandardAnalyzer();
		
		String query_string = "title:" + searchString +  " OR content:" + searchString +  "OR important:" + searchString  + "OR h1:" + searchString +
				"OR h2:" + searchString + "OR h3:" + searchString + "OR h4:" + searchString + "OR h5:" + searchString + "OR h6:" + searchString;
		QueryParser queryParser = new QueryParser("title", analyzer);
		
		TopDocs docs = indexSearcher.search(queryParser.parse(query_string), 10);
		
		
//		String[] fields = {"content", "title", "important", "h1", "h2", "h3", "h4", "h5", "h6"};
//		BooleanClause.Occur[] flags = 
//		{
//			BooleanClause.Occur.SHOULD,
//			BooleanClause.Occur.SHOULD,
//			BooleanClause.Occur.SHOULD,
//			BooleanClause.Occur.SHOULD,
//			BooleanClause.Occur.SHOULD,
//			BooleanClause.Occur.SHOULD,
//			BooleanClause.Occur.SHOULD,
//			BooleanClause.Occur.SHOULD,
//			BooleanClause.Occur.SHOULD,
//         };
//		
//		TopDocs docs = indexSearcher.search(MultiFieldQueryParser.parse(searchString, fields, flags, analyzer), 10);

		ScoreDoc[] hits = docs.scoreDocs;

	    System.out.println("Found " + hits.length + " hits.");
	    for(int i=0;i<hits.length;++i) {
	    	int docId = hits[i].doc;
	        Document d = indexSearcher.doc(docId);
	        System.out.println((i + 1) + ". " + d.get("title") + "\t" + d.get("url") + "\t" + hits[i].score);
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
	public void printIndexMetrics(Directory index, boolean printMetricsToScreen, boolean printMetricsToFile){
		 
		try{
			 //Creating our index
			 IndexReader reader = DirectoryReader.open(index);
			 
			 //Need this map for metrics
			 HashMap<String, HashSet<String>> hmap = convertIndexToMap(reader);
			 
			 HashMap<String, String> metrics = calculateMetrics(hmap, reader); 

			 //add the bad file metric... should probably just make this map global
			 metrics.put(INDEX_METRIC_UNPARSABLE_CT, String.valueOf(numberOfUnparsableFiles));
			 printMetrics(metrics, printMetricsToScreen, printMetricsToFile);
	
			 reader.close();  
		}
		catch(Exception e){
			System.out.println("There was some exception thrown during printing: " + e.getStackTrace());
		}
	}
	
        private static void boostDoc(IndexWriter w, Document doc, String uniqueField, String uniqueID) throws IOException, ParseException{
            //{"title":15, "important":1, "heading1-heading6":5, "content":1, "url", "inlinks_count"}
            
            float iterativeBoost = (float)1.05;

            //Get inlink count from doc, change to int value for later use and increment by 1
            String inlink = doc.get("inlink");
            int inlink_int = Integer.valueOf(inlink);
            inlink_int = inlink_int + 1;


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
            int titleboost = 15;
            field.setBoost(getNewBoost(inlink_int, titleboost, iterativeBoost));
            newDoc.add(field);

            //Set important field in newDoc
            String importantText = doc.get("important");
            field = new Field("important", importantText, type);
            int importantboost = 1;
            field.setBoost(getNewBoost(inlink_int, importantboost, iterativeBoost));
            newDoc.add(field);
            
            //Set content field in newDoc
            String contentText = doc.get("content");
            field = new Field("content", contentText, type);
            int contentBoost = 1;
            field.setBoost(getNewBoost(inlink_int, contentBoost, iterativeBoost));
            newDoc.add(field);
            
            //Set header fields in newDoc
            for(int i = 0; i<6; i++){
                String headerTag = "heading" + i; //Set as [header0 - header6] according to addDoc
                String headerText = doc.get(headerTag);
                field = new Field(headerTag, headerText, type);
                int headerBoost = 5;
                field.setBoost(getNewBoost(inlink_int, headerBoost, iterativeBoost));
                newDoc.add(field);
            }

            //create fieldType for fields that should not be queried against
            type = new FieldType();
            type.setStored(true);
            type.setTokenized(false);
            type.setStoreTermVectors(false);
            
            //Set inlink_count in newDoc
            newDoc.add(new Field("inlink", Integer.toString(inlink_int), type));
            
            //Set url in newDoc
            newDoc.add(new Field("url", doc.get("url"), type));

            //Delete existing doc before adding new one
            Term term = new Term(uniqueField, uniqueID);
            w.deleteDocuments(term);

            //Add in new doc and commit changes
            w.addDocument(newDoc);
            w.commit();
        }
    
    private static float getNewBoost(int inlink_count, float fieldBoost, float iterativeBoost){
    	float newBoost = fieldBoost;
    	
    	for(int i = 0; i<inlink_count; i++){
    		newBoost *= iterativeBoost;
    	}
    	
    	return newBoost;
    }
    
    private static Document getDoc(Directory index, String field_name, String text) throws IOException, ParseException{
    	QueryParser queryParser = new QueryParser("title", new StandardAnalyzer());

    	String query_string = field_name + ":" + text;
        
        Query q = queryParser.parse(query_string);

        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        
        return searcher.doc(hits[0].doc); //Should only be 1 result if done correctly
    }
        
	/* Adds a new document to to the index.
	 *
	 * Use the following link for setting up help with increasing the scoring of certain terms:
	 * https://lucene.apache.org/core/3_5_0/scoring.html#Fields and Documents
	 * */
	private int addDoc(IndexWriter w, String url, File file) throws IOException, IllegalArgumentException {
		
		//TODO: needs to be able to parse HTML pages here
		//File parsing
		org.jsoup.nodes.Document html = Jsoup.parse(String.join("",Files.readAllLines(file.toPath())));
		
		//If we cant parse the html
		if(html == null)
			return -1;
		
		
		if(PRINT_CONTENT_STRING)
			System.out.println("***This is the body***\n" + String.join("",Files.readAllLines(file.toPath())));
		
		if(PRINT_CONTENT_BODY)
			System.out.println("***This is the body***\n" + html.body());
		
		String content = null;
		Element body = html.body();
		
		//Get the rest of the text in the body
		if(body != null)
			content = body.text();
		
		if(PRINT_CONTENT_TEXT)
			System.out.println("***This is the BODY text***\n" + content);

		String title = null;
		Element head = html.head();
		if(head != null)
			title = head.text();
		
		if(PRINT_CONTENT_TEXT)
			System.out.println("***This is the TITLE***\n" + title);
				
		//Document Creation
		Document doc = new Document();
		Field field = null;
		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		type.setStored(true); 
		type.setStoreTermVectors(true);
		type.setTokenized(true);
		
		//If there is text in the head, it is probably a title
		if(title != null && !title.isEmpty()){
			field = new Field("title", title, type);
			field.setBoost(15); //Set weight for the field when query matches to string in field here
			doc.add(field);
                        
		}
		
		if(PRINT_INDEX_TO_PARSING)
			System.out.println("This is title: " + title);

		//Grab the important text tags
		Elements importantTags = body.select("b, strong, em");

		if(importantTags != null && !importantTags.isEmpty())
		{
			field = new Field("important", importantTags.html(), type);
			
			//Setting the bold tag boost
			field.setBoost(1); 
			doc.add(field);
			
			//We remove any content in these tags so there is no duplicate counting
			importantTags.remove();
		}
		if(PRINT_INDEX_TO_PARSING)
			System.out.println("This is Bolding: " + importantTags.text());

		
		//Grab all heading tags
		Elements headingTags = body.select("h1, h2, h3, h4, h5, h6");

		for(int headingNum = 0; headingNum < 6; headingNum++)
		{
			//Attempt to index all the heading tags
			Elements hTags = headingTags.select("h" + (headingNum + 1));
			if(hTags != null && !hTags.isEmpty())
			{
				field = new Field("heading" + headingNum, hTags.html(), type);
				
				//Setting the heading tag boost
				field.setBoost(5); 
				doc.add(field);
				
				//We remove any content in these tags so there is no duplicate counting
				hTags.remove();
			}
			if(PRINT_INDEX_TO_PARSING)
				System.out.println("This is heading: " + headingNum + " - " + hTags.text());

		}
		
		//Need to parse the remaining content after all the important tags have been deleted 
		if(content != null && !content.isEmpty()){
			field = new Field("content", content, type);
			
			//Setting the default boost
			field.setBoost(1); 
			doc.add(field);
		}
		if(PRINT_INDEX_TO_PARSING)
			System.out.println("This is content: " + content);

		//Need to make sure we have content before attempting to add a link to a document
		if(doc.getFields().size() > 0)
		{
			//fileID field should not be used for finding terms within document, only for uniquely identifying this doc amongst others in index
			type = new FieldType();
			type.setStored(true);
			type.setTokenized(false);
			type.setStoreTermVectors(false);
			doc.add(new Field("url", url, type));
                        
                        //inlink field representing number of times doc has alink from another doc pointing to it. Initialized to 0
                        doc.add(new Field("inlink", "0", type));
			
                        
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
	
	public  void printMetrics(HashMap<String, String> metrics, boolean printMetricsToScreen, boolean printMetricsToFile){
		if(printMetricsToScreen)
		{
			System.out.println("\nTotal number of flat (.cfs files) files storing the index: " + metrics.get(INDEX_METRIC_SIZE_COUNT_KEY));
			System.out.println("Size of the complete index size: " + metrics.get(INDEX_METRIC_SIZE_KEY) +  " MB");
			System.out.println("Total Unique Terms: " + metrics.get(INDEX_METRIC_UNIQUE_KEY));
			System.out.println("Total number of documents: " + metrics.get(INDEX_METRIC_DOC_CT_KEY));
			System.out.println("Total number of unparsable documents: " + metrics.get(INDEX_METRIC_UNPARSABLE_CT) );

		}
	
		if(printMetricsToFile)
		{
			try 
			{
				FileWriter writer = new FileWriter(HUMAN_READABLE_INDEX, true); //the true will append the new data
				writer.write("\n");
				writer.write("Total number of flat files (.cfs files) storing the index: " + metrics.get(INDEX_METRIC_SIZE_COUNT_KEY) + "\n");
				writer.write("Size of the complete index size: " + metrics.get(INDEX_METRIC_SIZE_KEY) + " MB\n");
				writer.write("Total Unique Terms: " + metrics.get(INDEX_METRIC_UNIQUE_KEY) + "\n");
				writer.write("Total number of documents encountered: " + metrics.get(INDEX_METRIC_DOC_CT_KEY) + "\n");
				writer.write("Total number of unparsable documents: " + metrics.get(INDEX_METRIC_UNPARSABLE_CT) + "\n");
				
				writer.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private HashMap<String, String> calculateMetrics(HashMap<String, HashSet<String>> hmap, IndexReader reader)
	{
		double totalIndexSize = 0;
		int numOfCfsFiles = 0;
		
		File indexFile = new File(REAL_INDEX);
		 	
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
