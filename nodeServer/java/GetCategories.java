import java.io.*;
import java.net.*;

import arenaapi.*;
import json.*;

public class GetCategories {

    public static void main(String[] arguments) throws IOException, URISyntaxException {

        // Parameters could be hard coded, from command line, etc
        String url = System.getenv("ARENA_API_URL");
        String email = System.getenv("ARENA_API_EMAIL");
        String password = System.getenv("ARENA_API_PASSWORD");
        String workspaceID = System.getenv("ARENA_API_WORKSPACEID");
        String log = System.getenv("ARENA_API_LOG");

        ArenaAPI arenaAPI = new ArenaAPI();
        arenaAPI.url = url;
        arenaAPI.log = log.length() > 0 && "1TtYy".indexOf(log.substring(0, 1)) != -1;
        boolean loggedIn = false;
        try {
            JSONObject args = new JSONObject();
            args.put("email", email);
            args.put("password", password);
            if (workspaceID.length() > 0)
                args.put("workspaceId", workspaceID);
            arenaAPI.apiCall("login", null, args);
            loggedIn = true;
            JSONObject result = (JSONObject) arenaAPI.apiCall("getItemCategories", null, null);
            System.out.println(result.toStringIndent(""));
        } catch (ArenaAPIException e) {
            arenaAPI.showResult(e.getMessage(), e.statusCode, e.errors, null);
        } finally {
            if (loggedIn) {
                try {
                    arenaAPI.apiCall("logout", null, null);
                } catch (ArenaAPIException e) {
                }
            }
        }
    }
}
