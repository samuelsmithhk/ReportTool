package files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mapping.CRGMapping;
import mapping.ICDateMapping;
import mapping.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
        logger.info("Initializing mapping file manager");
        this.mappingDirectory = mappingDirectory;
    }

    public Mapping loadColumnMap(String sheetType) throws Exception {
        logger.info("Loading column map for sheetType " + sheetType);

        /*{"headers": {
	    "Opportunity" : "Company",
	    "Deal Status" : "Company Status",
	    "Analyst 1" : "Deal Team",
	    "Analyst 2" : "Secondary Deal Team",
	    "Country of Incorporation": "Country",
        "Deal Description": "Business Description",
        "Target Funding": "Target Closed",
        "Est. KAM Size 'MM": "KKR Equity Size ($m) Expected",
        "Total Trans Size 'MM": "Total  Transaction Size",
        "Update Date": "Last Update Date",
        "Country of Origin": "Country"
        },

        "values":{
            "Country": {
                "Australia and New Zealand": "Australia"
            }
        }}
         */
        Mapping.MappingBuilder mb = new Mapping.MappingBuilder();

        String json = getFileAsJSON(sheetType);

        if (json == null) throw new Exception("Error generating column mapping");

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(json).getAsJsonObject();
        JsonObject headers = o.get("headers").getAsJsonObject();
        JsonObject values = o.get("values").getAsJsonObject();

        Set<Map.Entry<String, JsonElement>> headerEntrySet = headers.entrySet();
        for (Map.Entry<String, JsonElement> entry : headerEntrySet) {
            String toMapTo = entry.getKey();
            String toBeMapped = entry.getValue().getAsString();

            mb.addColumnMap(toMapTo, toBeMapped);
        }

        Set<Map.Entry<String, JsonElement>> valueEntrySet = values.entrySet();
        for (Map.Entry<String, JsonElement> entry : valueEntrySet) {
            String header = entry.getKey();
            JsonObject valuesJSON = entry.getValue().getAsJsonObject();

            Set<Map.Entry<String, JsonElement>> mappingSet = valuesJSON.entrySet();
            for (Map.Entry<String, JsonElement> subEntry : mappingSet) {
                String toMapTo = subEntry.getKey();
                String toBeMapped = subEntry.getValue().getAsString();

                mb.addValueMap(header, toMapTo, toBeMapped);
            }
        }


        return mb.build();
    }

    public CRGMapping loadCagMap() throws Exception {
        String json = getFileAsJSON("countryAndGrouping");
        CRGMapping.CagMappingBuilder cmb = new CRGMapping.CagMappingBuilder();

        if (json == null) throw new Exception("Error generating country and grouping mapping");

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(json).getAsJsonObject();
        JsonArray maps = o.get("mapping").getAsJsonArray();

        for (JsonElement map : maps) {
            JsonObject mapO = map.getAsJsonObject();
            String coi = mapO.get("coi").getAsString();
            String geography = mapO.get("geography").getAsString();
            String grouping = mapO.get("grouping").getAsString();
            String region = mapO.get("region").getAsString();

            cmb = cmb.withCag(coi, geography, grouping, region);
        }

        return cmb.build();
    }

    public ICDateMapping loadICDateMap() throws Exception {
        String json = getFileAsJSON("icDates");
        ICDateMapping.ICDateMappingBuilder idmb = new ICDateMapping.ICDateMappingBuilder();

        if (json == null) throw new Exception("Error generating IC date mapping");

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(json).getAsJsonObject();
        JsonArray maps = o.get("mapping").getAsJsonArray();

        for (JsonElement map : maps) {
            JsonObject mapO = map.getAsJsonObject();
            String dealCode = mapO.get("dealCode").getAsString();
            String date = mapO.get("dateShown").getAsString();

            idmb = idmb.withDate(dealCode, date);
        }

        return idmb.build();
    }

    private String getFileAsJSON(String sheetType) {
        logger.info("Loading json file into string");
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

    private void saveJSONAsFile(String sheetType, String json) {

        PrintWriter out;
        try {
            out = new PrintWriter(mappingDirectory + sheetType + ".json");
            out.print(json);
            out.close();
        } catch (FileNotFoundException e) {
            logger.error("Error saving mapping file: " + e.getLocalizedMessage());
        }
    }

    public void addNewICDate(String dealCode, String datestamp) {
        logger.info("Saving new date into the ic dates map");

        StringBuilder sb = new StringBuilder(getFileAsJSON("icDates"));
        sb.deleteCharAt(sb.lastIndexOf("}")).deleteCharAt(sb.lastIndexOf("]")).append(",{\"dealCode\":\"")
                .append(dealCode).append("\",\"dateShown\":\"").append(datestamp).append("\"}]}");

        saveJSONAsFile("icDates", sb.toString());
    }
}
