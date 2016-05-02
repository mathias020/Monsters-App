package com.jonashr.monsters;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Jonas on 12-11-2015.
 */
public class MonstersDB extends Activity {
    private static final String HTTP_POST_PRIVATE_KEY = "049b94094509gfd0203493020203240234fkdlkfldkf3002";

    public static ArrayList<Map<String, Object>>  httpPost(String action, AbstractMap.SimpleEntry<String, String>... args) {
        String response = "";
        try {
            URL url = new URL("https://www.skainet.dk/monsters_db/functions.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();

            params.add(new AbstractMap.SimpleEntry<String, String>("private_key", HTTP_POST_PRIVATE_KEY));
            params.add(new AbstractMap.SimpleEntry<String, String>("action", action));

            if(args != null) {
                if(args.length > 0) {
                    for(int i = 0; i < args.length; i++) {
                        params.add(args[i]);
                    }
                }
            }

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("skainet_dk", "HTTP OK.. reading input");
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                while((line=br.readLine()) != null)
                    response += line;
            } else {
                response = "";
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return jsonToArray(response);
    }

    private static String getQuery(List<AbstractMap.SimpleEntry<String, String>> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for(AbstractMap.SimpleEntry<String, String> pair : params) {
            if(first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static ArrayList<Map<String, Object>> jsonToArray(String jsonString) {

        ArrayList<Map<String, Object>> oArray = new ArrayList<>();

        if(jsonString.equals(""))
            return oArray;

        try {
            JSONArray jArray = new JSONArray(jsonString);

            for(int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                Iterator<String> keys = jObject.keys();
                Map<String, Object> row = new HashMap<String, Object>();
                while(keys.hasNext()) {
                    String key = keys.next();
                    row.put(key, jObject.get(key));
                }

                oArray.add(row);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return oArray;


    }
}
