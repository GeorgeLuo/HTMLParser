package runnings;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class RunningTools {
    public static List<String> parseToPlainText(InputStream in) throws IOException, SAXException, TikaException {
        BodyContentHandler handler = new BodyContentHandler();
     
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        InputStream stream = in; 
        parser.parse(stream, handler, metadata);
        return Arrays.asList(handler.toString().replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+"));
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
