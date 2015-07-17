package arenaapi;

import java.io.*;
import java.util.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;

import json.*;

public class ArenaAPI {

    public String url = "";
    public boolean log = false;
    public String sessionID = null;

    static Map<String, String[]> apiMap = new HashMap<String, String[]>();

    static {
        apiMap.put("login", new String[]{"POST", "login"});
        apiMap.put("logout", new String[]{"GET", "logout"});
        apiMap.put("getItemNumberFormats", new String[]{"GET", "item/numberformats"});
        apiMap.put("getItemCategories", new String[]{"GET", "item/categories"});
        apiMap.put("getFileCategories", new String[]{"GET", "file/categories"});
        apiMap.put("getItemAttributes", new String[]{"GET", "item/attributes"});
        apiMap.put("getItemNumberFormat", new String[]{"GET", "item/numberformats/<guid>"});
        apiMap.put("getItemRevisions", new String[]{"GET", "items/<guid>/revisions"});
        apiMap.put("getItemRequirements", new String[]{"GET", "items/<guid>/requirements"});
        apiMap.put("getItemRelationships", new String[]{"GET", "items/<guid>/relationships"});
        apiMap.put("getCategoryAttributes", new String[]{"GET", "item/categories/<guid>/attributes"});
        apiMap.put("getItems", new String[]{"GET", "items"});
        apiMap.put("getItem", new String[]{"GET", "items/<guid>"});
        apiMap.put("updateItem", new String[]{"PUT", "items/<guid>"});
        apiMap.put("createItem", new String[]{"POST", "items"});
        apiMap.put("deleteItem", new String[]{"DELETE", "items/<guid>"});
        apiMap.put("getItemBOM", new String[]{"GET", "items/<guid>/bom"});
        apiMap.put("getItemFiles", new String[]{"GET", "items/<guid>/files"});
        apiMap.put("getItemFileContent", new String[]{"GET", "items/<guid>/files/<fileguid>/content"});
        apiMap.put("addItemFile", new String[]{"POST", "items/<guid>/files"});
        apiMap.put("addItemFileContent", new String[]{"POST", "items/<guid>/files/<fileguid>/content"});
        apiMap.put("deleteItemFile", new String[]{"DELETE", "items/<guid>/files/<fileguid>"});
    }

    public ArenaAPI() {
        disableCertificateValidation();
    }

    public Object apiCall(String name, JSONObject args, Object body) throws ArenaAPIException, IOException, URISyntaxException {
        HttpURLConnection connection = null;
        try {
            String[] apiInfo = apiMap.get(name);
            String method = apiInfo[0];
            String urlPart = apiInfo[1];
            String query = "";
            if (args != null) {
                char joinChar = '?';
                for (Map.Entry<String, JSONValue> arg : args.getValue().entrySet()) {
                    String key = arg.getKey();
                    String value = arg.getValue().stringValue();
                    int k = urlPart.indexOf("<" + key + ">");
                    if (k != -1) {
                        urlPart = urlPart.substring(0, k) + value + urlPart.substring(k + key.length() + 2);
                    } else {
                        query += joinChar + key + '=' + value;
                        joinChar = '&';
                    }
                }
            }

            Map<String, List<String>> requestProperties = null;

            URL u = new URL(url + urlPart);   // maybe need to escape query part
            URL urlObj = new URI(u.getProtocol(), u.getAuthority(), u.getPath(), query, null).toURL();

//      URL urlObj = new URL(url + urlPart + query);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod(method);

            if (sessionID != null)
                connection.addRequestProperty("Cookie", "arena_session_id=" + sessionID);

            if (body != null) {
                byte[] bodyBuffer = body instanceof byte[] ? (byte[]) body : body.toString().getBytes("UTF-8");
                if (bodyBuffer.length > 0) {
                    connection.addRequestProperty("Content-Length", Integer.toString(bodyBuffer.length));
                    connection.addRequestProperty("Content-Type", body instanceof byte[] ? "application/octet-stream" : "application/json");
                    if (log)
                        requestProperties = connection.getRequestProperties();
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(bodyBuffer);
                    os.close();
                }
            }

            if (log) {
                if (requestProperties == null)
                    requestProperties = connection.getRequestProperties();
                String b = body == null ? "null" : body instanceof byte[] ? "<byte[" + ((byte[]) body).length + "]>" : body.toString();
                System.out.println("--------------------------------------------\n" + method + " " + urlObj.getPath() + query + "\nheaders: " + requestProperties.toString() + "\nbody: " + b);
            }

            int statusCode = connection.getResponseCode();

            List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
            if (cookies != null) {
                for (String cookie : cookies) {
                    int k = cookie.indexOf("arena_session_id=");
                    if (k != -1)
                        sessionID = URLDecoder.decode(cookie.substring(k + 17, cookie.indexOf(';')), "UTF-8");
                }
            }

            InputStream is = connection.getErrorStream();
            if (is == null)
                is = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int length;
            while ((length = is.read(buffer)) != -1)
                baos.write(buffer, 0, length);
            is.close();
            byte[] resultBytes = baos.toByteArray();

            JSONValue resultJSON = null;
            String contentType = connection.getHeaderField("Content-Type");
            if (contentType != null && contentType.toUpperCase().indexOf("JSON") != -1)
                resultJSON = JSONValue.parseJSONValue(new String(resultBytes, "UTF-8"));

            Object result = null;
            JSONObject errors = null;
            if (statusCode >= 200 && statusCode < 300) {
                result = resultJSON != null ? resultJSON : resultBytes;
            } else {
                if (resultJSON instanceof JSONObject)
                    errors = (JSONObject) resultJSON;
                else
                    errors = buildError(statusCode, 9999, "API RESPONSE ERROR: " + name + " statusCode: " + statusCode);
            }

            if (log)
                showResult(method + ' ' + urlObj.getPath() + query + ' ' + name, statusCode, errors, result);

            if (errors != null)
                throw new ArenaAPIException(statusCode, errors);

            return result;

        } catch (UnsupportedEncodingException e) {
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    JSONObject buildError(int statusCode, int code, String message) {
        JSONObject errors = new JSONObject();
        errors.put("status", new JSONNumber(statusCode));
        JSONObject info = new JSONObject();
        info.put("code", new JSONNumber(code));
        info.put("message", new JSONString(message));
        JSONArray ja = new JSONArray(1);
        ja.add(info);
        errors.put("errors", ja);
        return errors;
    }

    void disableCertificateValidation() {
        try {
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    public void showResult(String info, int statusCode, JSONObject errors, Object result) {
        if (errors != null) {
            System.out.println("-> " + statusCode + ' ' + info + " ERRORS " + errors.toStringIndent("  "));
        } else {
            String s = result instanceof JSONObject ? ((JSONObject) result).toStringIndent("  ") : "<byte[" + ((byte[]) result).length + "]>";
            System.out.println("-> " + statusCode + ' ' + info + " RESULT " + s);
        }
    }

}
