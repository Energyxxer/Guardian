package com.energyxxer.guardian.util;

import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.util.logger.Debug;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class NetworkUtil {
    private static final String AUTHENTICATED_REQUEST_API = "https://energyxxer.com/api/guardian/request.php?url=";

    public static final String IP_REGEX = "(?:[0-9]{1,3}\\.){3}[0-9]{1,3}";

    @Contract("_, false -> !null")
    public static InputStream retrieveStreamForURLAuth(String url, boolean accept404Null) throws IOException {

        InputStream authenticatedResult = null;
        try {
            authenticatedResult = retrieveStreamForURL((AUTHENTICATED_REQUEST_API + url).replace("&", "%26"), true, 4);
        } catch (IOException x) {
            Debug.log("Failed authenticated request (and it wasn't 404). Please notify Energyxxer.", Debug.MessageType.ERROR);
            GuardianWindow.showError("Failed authenticated request (and it wasn't 404). Please notify Energyxxer.");
            x.printStackTrace();
        }
        if(authenticatedResult != null) {
            //First try
            return authenticatedResult;
        }

        //Call to authenticated request API failed. As a backup, do the call to the URL directly.

        return retrieveStreamForURL(url, accept404Null, 4);
    }

    public static InputStream retrieveStreamForURL(String url, boolean accept404Null, int attempts) throws IOException {
        for(int i = 0; i < attempts; i++) {
            try {
                return retrieveStreamForURL(url, accept404Null);
            } catch(SocketTimeoutException x) {
                Debug.log("Timed out (" + (i + 1) + "/" + attempts + ")");
                if(i == attempts-1) {
                    GuardianWindow.showError("Authenticated request timed out after " + attempts + ".");
                    throw x;
                }
            }
        }
        throw new IllegalArgumentException("Impossible Exception (or attempts <= 0)");
    }

    @Contract("_, false -> !null")
    public static InputStream retrieveStreamForURL(String url, boolean accept404Null) throws IOException {
        URL latestURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) latestURL.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setInstanceFollowRedirects(true);

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        connection.connect();
        int status = connection.getResponseCode();

        if(200 <= status && status <= 299) {
            // OK
            return connection.getInputStream();
        } else if(accept404Null && status == 404) {
            // 404
            return null;
        } else {
            // ERROR
            JsonObject errorObj = new Gson().fromJson(new InputStreamReader(connection.getErrorStream()), JsonObject.class);
            String message = errorObj.get("message").getAsString();
            throw new IOException(message.replaceAll(IP_REGEX, "[IP REDACTED]"));
        }
    }

    public static String inputStreamToString(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(is));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    private NetworkUtil() {}
}
