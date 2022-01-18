package com.sochat.soulter.sophoneinfor;

/**
 * Created by Administrator on 2018/6/29.
 */
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the network.
 *
 * 这个工具类将会被用于与网络进行交流.
 */
public class NetworkUtils extends MainActivity{

    final static String GITHUB_BASE_URL =
            "https://api.github.com/search/repositories";

    final static String PARAM_QUERY = "q";

    /*
     * The sort field. One of stars, forks, or updated.
     * Default: results are sorted by best match if no field is specified.
     */
    final static String PARAM_SORT = "sort";
    final static String sortBy = "stars";

    /**
     * Builds the URL used to query Github.
     * 用于Github请求
     *
     * @return The URL to use to query the weather server.
     */
    public static URL buildUrl(String webSearchQuery) {
        // TODO (1) Fill in this method to build the proper Github query URL
        Uri buildUri = Uri.parse(webSearchQuery).buildUpon().build();

        String str = buildUri.toString();


        URL url = null;
        try {
            url = new URL(buildUri.toString());
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}