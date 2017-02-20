package com;

import com.StatisticsPkg;

import runnings.AsyncRun;
import runnings.GetThread;
import runnings.RunningTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import org.apache.tika.parser.AutoDetectParser;

import java.time.Instant;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


public class Main {
	
	// For asynchronous calls in Part 3
	static ExecutorService exe = null;
    static CompletableFuture<Void> exeFutureList = null;
	
	public static void main(String[] args) throws ClientProtocolException, IOException, Exception, SAXException, TikaException {
    			
    	PrintWriter out = new PrintWriter("runtime-stats.txt");
    	out.println("Part 1: \n");
    	System.out.println("Part 1: \n");
    	
    	StatisticsPkg stats1 = new StatisticsPkg(5);
    	
    	Instant start = Instant.now();
    	Instant tempStart = Instant.now();
    	
	    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
	    InputStream stream;
	    BufferedReader in;
	    try{
	    	stream = classloader.getResourceAsStream(args[0]);
		    in = new BufferedReader(new InputStreamReader(stream));

	    } catch (NullPointerException e) {
	    	System.out.println("Check to make sure input file is in directory.");
	    	return;
	    } catch (IndexOutOfBoundsException e) {
	    	System.out.println("No input file designated.");
	    	return;
	    }
	    String line = null;

	    CloseableHttpClient httpclient = HttpClients.custom()
	            .setDefaultRequestConfig(RequestConfig.custom()
	                    .setCookieSpec(CookieSpecs.STANDARD).build())
	                .build(); // one client, multiple connections
	    HttpGet httpGetReuseable = new HttpGet();	    
	    CloseableHttpResponse responseReuseable = null;
	    	    
	    Map<String, Integer> aggregate = new HashMap<String, Integer>();
	    
	    Instant tempEnd = Instant.now();
	    stats1.storeIT(tempStart, tempEnd);
	    
	    int counter = 0;
	    
	    while((line = in.readLine()) != null) {
	    	tempStart = Instant.now();
	    	
	    	httpGetReuseable = new HttpGet(line);
	    	try{
	    	responseReuseable = httpclient.execute(httpGetReuseable);
	    	} catch (UnknownHostException e) {
	    		System.out.println("Check URLs, at least one host can not be reached.");
	    		return;
	    	}
	    	
	    	tempEnd = Instant.now();
	    	stats1.storeBnF(counter, tempStart, tempEnd);

	    	try {
	    		tempStart = Instant.now();
	    		
	    		List<String> parsed = RunningTools.parseToPlainText(responseReuseable.getEntity().getContent());
	    		for(String word : parsed) {
	    			aggregate.merge(word, 1, (oldValue, one) -> oldValue + one);
	    		}
	    		
	    		tempEnd = Instant.now();
		    	stats1.storeLPT(counter, tempStart, tempEnd);
	    		counter++;
	    		
	    	} finally {
	    		responseReuseable.close();
	    	}
	    }
	    
	    stats1.setTop10words(getTop10words(aggregate));
	    
    	Instant end = Instant.now();
    	stats1.storeNR(start, end);
    	
    	out.println(stats1.getTop10String(aggregate));
    	out.println(stats1.printStats());
		System.out.println(stats1.getTop10String(aggregate));
    	System.out.println(stats1.printStats());
	    
    	// Part 2 - Multithreading
    	// Uses ConcurrentHashMap for thread safe. PoolingHttpClientConnectionManager used for multi-thread
    	// HTTPClient requests. 
    	
    	out.println("\nPart 2: \n");
    	System.out.println("\nPart 2: \n");
    	
    	StatisticsPkg stats2 = new StatisticsPkg(5);
    	
    	start = Instant.now();
    	tempStart = Instant.now();
    	
	    classloader = Thread.currentThread().getContextClassLoader();
	    stream = classloader.getResourceAsStream(args[0]);
	    in = new BufferedReader(new InputStreamReader(stream));
	    line = null;
	    	    	    
	    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	    ConcurrentHashMap<String,Integer> aggregateFreqMap = new ConcurrentHashMap<String, Integer>();
	    CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
	            .setCookieSpec(CookieSpecs.STANDARD).build())
	            .setConnectionManager(cm)
	            .build();
	    
	    // create a thread for each URI
	    GetThread[] threads = new GetThread[5];
	    
	    tempEnd = Instant.now();
	    stats2.storeIT(tempStart, tempEnd);
	    
	    counter = 0;
	    while((line = in.readLine()) != null) {
	    	HttpGet httpget = new HttpGet(line);
	    	threads[counter] = new GetThread(httpClient, httpget, counter, aggregateFreqMap, stats2);
	    	counter++;
	    }
	    
	    // start the threads
	    for (int j = 0; j < threads.length; j++) {
	        threads[j].start();
	    }

	    // join the threads
	    for (int j = 0; j < threads.length; j++) {
	        threads[j].join();
	    }
	    
    	end = Instant.now();
//		System.out.println("all joined at: " + Instant.now().toString());
    	stats2.storeNR(start, end);
	    
	    stats2.setTop10words(getTop10words(aggregateFreqMap));
	    out.println(stats2.getTop10String(aggregateFreqMap));
	    out.println(stats2.printStats());
		System.out.println(stats2.getTop10String(aggregateFreqMap));
    	System.out.println(stats2.printStats());
    	
    	// Part 3 - Asynchronous 
    	// Uses ConcurrentHashMap once again, Executors and CompletableFuture
    	// to spin asynchronous calls.
    	
    	out.println("\nPart 3: \n");
    	System.out.println("\nPart 3: \n");
    	
    	StatisticsPkg stats3 = new StatisticsPkg(5);
    	
    	start = Instant.now();
    	tempStart = Instant.now();
    	
	    classloader = Thread.currentThread().getContextClassLoader();
	    stream = classloader.getResourceAsStream(args[0]);
	    in = new BufferedReader(new InputStreamReader(stream));
	    line = null;

	    httpGetReuseable = new HttpGet();	    
	    responseReuseable = null;
	    	    
	    ConcurrentHashMap<String, Integer> freqMap = new ConcurrentHashMap<String, Integer>();
	    
        exe = Executors.newFixedThreadPool(1);
	    
	    tempEnd = Instant.now();
	    stats3.storeIT(tempStart, tempEnd);
	    
	    counter = 0;
	    while((line = in.readLine()) != null) {
	    	tempStart = Instant.now();

	    	httpGetReuseable = new HttpGet(line);
	        exeFutureList = CompletableFuture.runAsync(new AsyncRun(httpclient, httpGetReuseable, counter, freqMap, stats3), exe);
	    	counter++;
	    }
	    CompletableFuture.allOf(exeFutureList).join();

    	end = Instant.now();
    	stats3.storeNR(start, end);
    	
	    stats3.setTop10words(getTop10words(freqMap));
		System.out.println(stats3.getTop10String(freqMap));
    	System.out.println(stats3.printStats());
    	out.println(stats2.getTop10String(aggregateFreqMap));
	    out.println(stats2.printStats());
	    
	    out.close();
    	
    	exe.shutdown();
    }
    
    // generate mapping of distinct words to frequency of appearance given an InputStream
    
    public static Map<String, Integer> freqMap(List<String> parsed) throws IOException, SAXException, TikaException {
	   
	   Map<String, Integer> frequencyMap = parsed.stream()
		         .collect(toMap(
		                s -> s, // key is the word
		                s -> 1, // value is 1
		                Integer::sum)); // merge function counts the identical words

		return frequencyMap;
    }
    
    // print the top 10 most frequently occurring words in a Map of distinct words to Integer frequency, 
    //called by main thread once freqMaps are aggregated
    
	public static List<String> getTop10words (Map<String, Integer> in) {
		List<String> top10 = in.keySet().stream()
		        .sorted(comparing(in::get).reversed()) // sort by descending frequency
		        .distinct() // take only unique values
		        .limit(10)   // take only the first 10
		        .collect(toList()); // put it in a returned list

		return top10;
	}
	
	
}
