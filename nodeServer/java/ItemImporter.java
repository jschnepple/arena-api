import arenaapi.ArenaAPI;
import arenaapi.ArenaAPIException;
import json.JSONArray;
import json.JSONNumber;
import json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ItemImporter {

    static ArenaAPI arenaAPI = null;
    static boolean loggedIn = false;
    static Map<String, JSONObject> numberMap = new HashMap<String, JSONObject>();
    static Map<String, String> categoryMap = new HashMap<String, String>();

    static void importItem(Map<String, String> map, String[] fields, String[] parts)
            throws IOException, URISyntaxException {
        try {
            if (arenaAPI == null) {
                // Parameters could be hard coded, from command line, etc
                String url = System.getenv("ARENA_API_URL");
                String email = System.getenv("ARENA_API_EMAIL");
                String password = System.getenv("ARENA_API_PASSWORD");
                String workspaceID = System.getenv("ARENA_API_WORKSPACEID");
                String log = System.getenv("ARENA_API_LOG");

                arenaAPI = new ArenaAPI();
                arenaAPI.url = url;
                arenaAPI.log = log.length() > 0 && "1TtYy".indexOf(log.substring(0, 1)) != -1;
                JSONObject args = new JSONObject();
                args.put("email", email);
                args.put("password", password);
                if (workspaceID.length() > 0) {
                    args.put("workspaceId", workspaceID);
                }
                arenaAPI.apiCall("login", null, args);
                loggedIn = true;

                JSONObject result = (JSONObject) arenaAPI.apiCall("getItemNumberFormats", null, null);
                JSONArray results = (JSONArray) result.get("results");
                for (int i = 0; i < results.size(); ++i) {
                    JSONObject props = new JSONObject();
                    props.put("guid", results.get(i).get("guid").stringValue());
                    JSONObject numberFormat = (JSONObject) arenaAPI.apiCall("getItemNumberFormat", props, null);
                    numberMap.put(numberFormat.get("name").stringValue(), numberFormat);
                }

                result = (JSONObject) arenaAPI.apiCall("getItemCategories", null, null);
                results = (JSONArray) result.get("results");
                for (int i = 0; i < results.size(); ++i) {
                    JSONObject category = (JSONObject) results.get(i);
                    categoryMap.put(category.get("name").stringValue(), category.get("guid").stringValue());
                }
            }

            String saveNumber = null;
            JSONObject props = new JSONObject();
            for (int i = 0; i < parts.length; ++i) {
                String arenaField = map.get(fields[i]);
                if (arenaField == null) {
                    arenaField = fields[i];       // if not in mapping file , assume mapped to itself
                }
                if (arenaField == null) {
                    doExit(fields[i] + " is not properly mapped to an Arena property");
                }
                if (arenaField.equals("numberFormat")) {
                    JSONObject jo = new JSONObject();
                    String guid = numberMap.get(parts[i]).get("guid").stringValue();
                    if (guid == null) {
                        doExit(parts[i] + " is not a valid Number Format");
                    }
                    jo.put("guid", guid);
                    JSONArray fieldsProp = new JSONArray();
                    JSONObject f = new JSONObject();
                    f.put("apiName", numberMap.get(parts[i]).get("fields").get(0).get("apiName")
                                              .stringValue()); // ok for free text number format
                    f.put("value", saveNumber);
                    fieldsProp.add(f);
                    jo.put("fields", fieldsProp);
                    props.put(arenaField, jo);
                }
                else if (arenaField.equals("category")) {
                    String guid = categoryMap.get(parts[i]);
                    if (guid == null) {
                        doExit(parts[i] + " is not a valid Category Name");
                    }
                    JSONObject categoryAttr = new JSONObject();
                    categoryAttr.put("guid", guid);
                    props.put("category", categoryAttr);
                }
                else if (arenaField.equals("number")) {
                    saveNumber = parts[i];
                }
                else {
                    try {
                        props.put(arenaField, new JSONNumber(Double.parseDouble(parts[i])));   // try as number first
                    }
                    catch (NumberFormatException nfe) {
                        props.put(arenaField, parts[i]);
                    }
                }
            }
            JSONObject result = (JSONObject) arenaAPI.apiCall("createItem", null, props);
            System.out.println("Created item: " + result.get("number").stringValue());
        }
        catch (ArenaAPIException e) {
            String error = e.errors.get("errors").get(0).get("message").stringValue();
            if (!loggedIn)    // keep going unless there was a login problem
            {
                doExit(error);
            }
            else {
                System.out.println(error);
            }
        }
    }

    static void doExit(String error) {
        if (error != null && error.length() > 0) {
            System.out.println(error);
        }
        if (loggedIn) {
            try {
                arenaAPI.apiCall("logout", null, null);
            }
            catch (Exception e) {
            }
        }
        System.exit(error == null ? 0 : 1);
    }

    public static void main(String[] arguments) throws IOException, URISyntaxException {

        String mappingFile = arguments.length > 0 ? arguments[0] : "mapping.txt";
        System.out.println("Attribute mapping file for items to import: " + mappingFile);
        Map<String, String> map = new HashMap<String, String>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(mappingFile));
        try {
            String line;
            do {
                line = bufferedReader.readLine();
                if (line != null && line.length() > 0) {
                    String[] parts = line.split("\t");
                    if (parts.length >= 2)  // might be header but no harm if so
                    {
                        map.put(parts[0], parts[1]);
                    }
                    else {
                        doExit("Error in mapping file at line: " + line);
                    }
                }
            }
            while (line != null);
        }
        finally {
            bufferedReader.close();
        }

        String dataFile = arguments.length > 1 ? arguments[1] : "dataimport.txt";
        System.out.println("Data file for items to import: " + dataFile);
        bufferedReader = new BufferedReader(new FileReader(dataFile));
        try {
            String[] fields = null;
            String line;
            do {
                line = bufferedReader.readLine();
                if (line != null && line.length() > 0) {
                    String[] parts = line.split("\t");
                    if (fields == null) {
                        fields = parts;
                    }
                    else {
                        importItem(map, fields, parts);
                    }
                }
            }
            while (line != null);
        }
        finally {
            bufferedReader.close();
        }

        doExit(null);
    }
}
