package parse;

import com.google.common.collect.Lists;
import com.google.gson.*;

import java.util.List;

public class ParserConfig {
    public final String jarPath, inputPath, regex, sourceSystem;
    public final String[] constructorArguments;


    public ParserConfig(String jarPath, String inputPath, String regex, String sourceSystem,
                        String[] constructorArguments) {
        this.jarPath = jarPath;
        this.inputPath = inputPath;
        this.regex = regex;
        this.sourceSystem = sourceSystem;
        this.constructorArguments = constructorArguments;
    }

    public static List<ParserConfig> loadConfigs(String json) {
        List<ParserConfig> retList = Lists.newArrayList();

        JsonParser jsonParser = new JsonParser();
        JsonObject jo = jsonParser.parse(json).getAsJsonObject();
        JsonArray parsers = jo.getAsJsonArray("parsers");

        for (JsonElement  jsonElement : parsers) {
            JsonObject o = jsonElement.getAsJsonObject();

            String jarPath = o.get("jarPath").getAsString();
            String inputPath = o.get("inputPath").getAsString();
            String regex = o.get("regex").getAsString();
            String sourceSystem = o.get("sourceSystem").getAsString();

            JsonArray caJSONArray  = o.get("constructorArguments").getAsJsonArray();
            String[] constructorArguments = new String[caJSONArray.size()];

            int i = 0;
            for (JsonElement ca : caJSONArray) {
                constructorArguments[i] = ca.getAsString();
                i++;
            }

            retList.add(new ParserConfig(jarPath, inputPath, regex, sourceSystem, constructorArguments));
        }

        return retList;
    }
}
