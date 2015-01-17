package files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mapping.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

/**
 * Created by samuelsmith on 17/01/2015.
 */
public class MappingFileManager {

    Logger logger = LoggerFactory.getLogger(MappingFileManager.class);

    private final String mappingDirectory;

    public MappingFileManager(String mappingDirectory) {
        this.mappingDirectory = mappingDirectory;
    }

    public Mapping loadColumnMap(String sheetType) throws Exception {

        /*{
	    "Opportunity" : "Company",
	    "Deal Status" : "Company Status",
	    "Analyst 1" : "Deal Team",
	    "Analyst 2" : "Secondary Deal Team",
	    "Country of Incorporation": "Country",
        "Deal Description": "Business Description",
        "Target Funding": "Target Closed",
        "Est. KAM Size 'MM": "KKR Equity Size ($m) Expected",
        "Total Trans Size 'MM": "Total  Transaction Size",
        "Update Date": "Last Update Date"
}
         */
        Mapping.MappingBuilder mb = new Mapping.MappingBuilder();

        String json = getFileAsJSON(sheetType);

        if (json == null) throw new Exception("Error generating column mapping");

        JsonParser parser = new JsonParser();
        JsonObject o = (JsonObject) parser.parse(json);

        Set<Map.Entry<String, JsonElement>> entrySet = o.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String toMapTo = entry.getKey();
            String toBeMapped = entry.getValue().getAsString();

            mb.addColumnMap(toMapTo, toBeMapped);
        }


        return mb.build();
    }

    private String getFileAsJSON(String sheetType) {
        try {
            byte[] encodedJSON;
            encodedJSON = Files.readAllBytes(Paths.get(mappingDirectory + sheetType + ".json"));
            String json = new String(encodedJSON, Charset.defaultCharset());
            return json;
        } catch (IOException e) {
            logger.error("ERROR: Error loading mapping file - " + sheetType + " error: " + e.getMessage(), e);
            return null;
        }
    }

}
