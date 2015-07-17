import java.io.*;
import java.util.*;
import java.net.*;

import arenaapi.*;
import json.*;

public class ItemUpdater {

    static ArenaAPI arenaAPI = null;
    static boolean loggedIn = false;

    static void updateItem(Map<String, String> map, String[] fields, String[] parts) throws IOException, URISyntaxException {
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
                if (workspaceID.length() > 0)
                    args.put("workspaceId", workspaceID);
                arenaAPI.apiCall("login", null, args);
                loggedIn = true;
            }

            String number = null;
            for (int i = 0; i < parts.length; ++i) {
                String arenaField = map.get(fields[i]);
                if ("number".equals(arenaField))
                    number = parts[i];
            }

            if (number == null)
                doExit("number field is missing in data file");

            JSONObject props = new JSONObject();
            props.put("number", number);
            JSONObject result = (JSONObject) arenaAPI.apiCall("getItems", props, null);

            int n = result.get("count").intValue();
            if (n == 1) {
                String guid = result.get("results").get(0).get("guid").stringValue();
                JSONObject args = new JSONObject();
                args.put("guid", guid);
                args.put("includeEmptyAdditionalAttributes", true);

                Map<String, JSONObject> aaMap = new HashMap<String, JSONObject>();
                JSONObject item = (JSONObject) arenaAPI.apiCall("getItem", args, null);
                JSONValue aa = item.get("additionalAttributes");
                if (aa != null) {
                    JSONArray ja = (JSONArray) aa;
                    for (int i = 0; i < ja.size(); ++i)
                        aaMap.put(ja.get(i).get("name").stringValue(), (JSONObject) ja.get(i));
                }

                JSONArray addAttrJA = new JSONArray();
                props = new JSONObject();
                for (int i = 0; i < parts.length; ++i) {
                    String arenaField = map.get(fields[i]);
                    if (!"number".equals(arenaField)) {
                        if (arenaField == null)
                            arenaField = fields[i];       // if not in mapping file , assume mapped to itself
                        JSONObject addAttr = aaMap.get(arenaField);
                        if (addAttr != null) {
                            JSONObject jo = new JSONObject();
                            jo.put("apiName", addAttr.get("apiName").stringValue());
                            jo.put("value", parts[i]);
                            addAttrJA.add(jo);
                        } else {
                            try {
                                props.put(arenaField, new JSONNumber(Double.parseDouble(parts[i])));   // try as number first
                            } catch (NumberFormatException nfe) {
                                props.put(arenaField, parts[i]);
                            }
                        }
                    }
                }
                if (addAttrJA.size() > 0)
                    props.put("additionalAttributes", addAttrJA);

                args = new JSONObject();
                args.put("guid", guid);
                result = (JSONObject) arenaAPI.apiCall("updateItem", args, props);
                System.out.println("Updated : " + number);

            } else if (n > 1) {
                System.out.println("Multiple items found for " + number + " - skipping update");
            } else {
                System.out.println(number + " not found.");
            }

        } catch (ArenaAPIException e) {
            String error = e.errors.get("errors").get(0).get("message").stringValue();
            if (!loggedIn)    // keep going unless there was a login problem
                doExit(error);
            else
                System.out.println(error);
        }

    }

    static void doExit(String error) {
        if (error != null && error.length() > 0)
            System.out.println(error);
        if (loggedIn) {
            try {
                arenaAPI.apiCall("logout", null, null);
            } catch (Exception e) {
            }
        }
        System.exit(error == null ? 0 : 1);
    }

    public static void main(String[] arguments) throws IOException, URISyntaxException {

        String mappingFile = arguments.length > 0 ? arguments[0] : "mapping.txt";
        System.out.println("Attribute mapping file for items to update: " + mappingFile);
        Map<String, String> map = new HashMap<String, String>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(mappingFile));
        try {
            String line;
            do {
                line = bufferedReader.readLine();
                if (line != null && line.length() > 0) {
                    String[] parts = line.split("\t");
                    if (parts.length >= 2)  // might be header but no harm if so
                        map.put(parts[0], parts[1]);
                    else
                        doExit("Error in mapping file at line: " + line);
                }
            } while (line != null);
        } finally {
            bufferedReader.close();
        }

        String dataFile = arguments.length > 1 ? arguments[1] : "dataupdate.txt";
        System.out.println("Data file for items to update: " + dataFile);
        bufferedReader = new BufferedReader(new FileReader(dataFile));
        try {
            String[] fields = null;
            String line;
            do {
                line = bufferedReader.readLine();
                if (line != null && line.length() > 0) {
                    String[] parts = line.split("\t");
                    if (fields == null)
                        fields = parts;
                    else
                        updateItem(map, fields, parts);
                }
            } while (line != null);
        } finally {
            bufferedReader.close();
        }

        doExit(null);
    }
}
