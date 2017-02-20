package runnings;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import com.StatisticsPkg;

class AsyncRun implements Runnable {

    private final CloseableHttpClient httpClient;
    private final HttpContext context;
    private final HttpGet httpget;
    private final StatisticsPkg stats;
    private final ConcurrentHashMap<String,Integer> aggregateFreqMap;
    private final int index;

    public AsyncRun(CloseableHttpClient httpClient, HttpGet httpget, int index, 
    		ConcurrentHashMap<String,Integer> aggregateFreqMap, StatisticsPkg stats) {

    	this.httpClient = httpClient;
        this.context = HttpClientContext.create();
        this.httpget = httpget;
        this.index = index;
        this.aggregateFreqMap = aggregateFreqMap;
        this.stats = stats;
    }

    @Override
    public void run() {
        try {
        	Instant start = Instant.now();
            CloseableHttpResponse response = httpClient.execute(
                    httpget, context);
        	Instant end = Instant.now();
        	stats.storeBnF(index, start, end);
            try {
            	start = Instant.now();
                List<String> parsed = RunningTools.parseToPlainText(response.getEntity().getContent());

                for(String word : parsed) {
	    			aggregateFreqMap.merge(word, 1, (oldValue, one) -> oldValue + one);
	    		}
	    		
	    		end = Instant.now();
	    		stats.storeLPT(index, start, end);
	    					    	
            } catch (UnsupportedOperationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (TikaException e) {
				e.printStackTrace();
			} finally {
                response.close();
            }
        } catch (ClientProtocolException ex) {
        	ex.printStackTrace();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
    }
}