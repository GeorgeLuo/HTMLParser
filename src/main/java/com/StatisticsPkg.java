package com;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// Intended use: 
	// package = new StatisticsPkg(5);
	
	// Instant start = Instant.now();
	// for(int i = 0; i < 5; i++)
		// Instant tempStart = Instant.now();
		// MakecallusingHTTPClient();
		// Instant tempEnd = Instant.now();
		//package.storeBnF(i, tempStart, tempEnd);
	
		// tempStart = Instant.now();
		// ProcessHTML()
		// tempEnd = Instant.now();
		// package.storeLPT(i, tempStart, tempEnd);

	// tempStart = Instant.now();
	// AggregateMaps();
	// tempEnd = Instant.now();
	// package.storeAPT(tempStart, tempEnd);
	// Instant end = Instant.now();
	// package.storeNR(start, end);

public class StatisticsPkg {
	public int numlinks;
	public Duration initTime;
	public Duration[] httpClientTimeBackAndForth;
	public Duration[] linkProcessTime;
	public Duration aggregationProcessTime;
	public Duration netRuntime;
	public List<String> top10;
	
	
	public StatisticsPkg(int numlinks) {
		this.numlinks = numlinks;
		top10 = new ArrayList<String>();
		httpClientTimeBackAndForth = new Duration[numlinks];
		linkProcessTime = new Duration[numlinks];
		aggregationProcessTime = null;
		initTime = null;
	}
	
	public void storeBnF(int index, Instant start, Instant end) {
		httpClientTimeBackAndForth[index] = Duration.between(start, end);
	}
	
	public void storeLPT(int index, Instant start, Instant end) {
		linkProcessTime[index] = Duration.between(start, end);
	}
	
	public void storeAPT(Instant start, Instant end) {
		aggregationProcessTime = Duration.between(start, end);
	}
	
	public void storeNR(Instant start, Instant end) {
		netRuntime = Duration.between(start, end);
	}
	
	public void storeIT(Instant start, Instant end) {
		initTime = Duration.between(start, end);
	}

	public String printStats() {
		StringBuilder BNF = new StringBuilder();
		for(int i = 0; i < numlinks; i++) {
			BNF.append("	Link " + (i + 1) + ": " + httpClientTimeBackAndForth[i].toNanos() + " nanoseconds\n");
		}
		StringBuilder LPT = new StringBuilder();
		for(int i = 0; i < numlinks; i++) {
			LPT.append("	Link " + (i + 1) + ": " + linkProcessTime[i].toNanos() + " nanoseconds\n");
		}
		
		return "Initialization Time: " + initTime.toNanos() + " nanoseconds\n\nHTTP Request Back and Forth Time: \n" 
				+ BNF.toString() +"\nLink Process Time: \n" + LPT.toString() 
				+ "\nStart to Finish: " + netRuntime.toNanos() + " nanoseconds";		
	}

	public void setTop10words(List<String> top10) {
		this.top10 = top10;
	}
	
	public String getTop10String(Map<String, Integer> aggregate) {
    	StringBuilder ag = new StringBuilder();
    	ag.append("Top 10 Words by Frequency of Appearance:\n\n");
    	
		int i = 1;
		for(String key : top10) {
			ag.append(i + ". '" + key + "' appears " + aggregate.get(key) + " times\n");
			i++;
		}
		
		return ag.toString();
	}
}
