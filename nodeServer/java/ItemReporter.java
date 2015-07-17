import arenaapi.ArenaAPI;
import arenaapi.ArenaAPIException;
import json.JSONArray;
import json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ItemReporter {

    static ArenaAPI arenaAPI = null;
    static boolean loggedIn = false;
    static Map<String, String> categoryMap = new HashMap<String, String>();

    static void report(String category) throws IOException, URISyntaxException {
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

                JSONObject result = (JSONObject) arenaAPI.apiCall("getItemCategories", null, null);
                JSONArray results = (JSONArray) result.get("results");
                for (int i = 0; i < results.size(); ++i) {
                    JSONObject cat = (JSONObject) results.get(i);
                    categoryMap.put(cat.get("name").stringValue(), cat.get("guid").stringValue());
                }
            }

            String guid = categoryMap.get(category);
            if (guid != null) {
                JSONObject props = new JSONObject();
                JSONObject categoryAttr = new JSONObject();
                categoryAttr.put("guid", guid);
                props.put("category", categoryAttr);
                JSONObject result = (JSONObject) arenaAPI.apiCall("getItems", props, null);
                JSONArray results = (JSONArray) result.get("results");
                for (int i = 0; i < results.size(); ++i) {
                    JSONObject args = new JSONObject();
                    args.put("guid", results.get(i).get("guid").stringValue());
                    JSONObject item = (JSONObject) arenaAPI.apiCall("getItem", args, null);
                    System.out.println(item.get("number").stringValue() + '\t' + item.get("name").stringValue() + '\t' +
                                       item.get("description").stringValue() + '\t' +
                                       item.get("category").stringValue());
                }
            }
            else {
                System.out.println("No items found for category: " + category);
            }
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

        String dataFile = arguments.length > 1 ? arguments[1] : "datareport.txt";
        System.out.println("CSV data file containing item categories of items to show: " + dataFile);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile));
        try {
            String line;
            do {
                line = bufferedReader.readLine();
                if (line != null && line.length() > 0) {
                    String[] parts = line.split(",");
                    for (int j = 0; j < parts.length; ++j) {
                        String category = parts[j].trim();
                        if (category.length() > 0) {
                            report(category);
                        }
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
