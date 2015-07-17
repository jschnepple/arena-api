import java.io.*;
import java.util.*;
import java.net.*;

import arenaapi.*;
import json.*;

public class ItemDeleter {

    static ArenaAPI arenaAPI = null;
    static boolean loggedIn = false;

    static void deleteItem(String deleteNumber) throws IOException, URISyntaxException {
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

            JSONObject props = new JSONObject();
            props.put("number", deleteNumber);
            JSONObject result = (JSONObject) arenaAPI.apiCall("getItems", props, null);

            int n = result.get("count").intValue();
            if (n == 1) {
                JSONObject args = new JSONObject();
                args.put("guid", result.get("results").get(0).get("guid").stringValue());
                arenaAPI.apiCall("deleteItem", args, null);
                System.out.println("Deleted : " + deleteNumber);
            } else if (n > 1) {
                System.out.println("Multiple items found for " + deleteNumber + " - skipping delete");
            } else {
                System.out.println(deleteNumber + " not found.");
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

        String dataFile = arguments.length > 1 ? arguments[1] : "datadelete.txt";
        System.out.println("Data file for items to delete: " + dataFile);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile));
        try {
            String line;
            do {
                line = bufferedReader.readLine();
                if (line != null && line.length() > 0) {
                    deleteItem(line);
                }
            } while (line != null);
        } finally {
            bufferedReader.close();
        }

        doExit(null);
    }
}
