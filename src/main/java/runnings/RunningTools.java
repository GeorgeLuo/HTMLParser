package runnings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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
}
