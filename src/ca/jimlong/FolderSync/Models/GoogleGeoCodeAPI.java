package ca.jimlong.FolderSync.Models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.drew.lang.GeoLocation;
import com.google.gson.Gson;

public class GoogleGeoCodeAPI {
	public static GoogleGeoCodeResponse getGeoCodeForCoordinates(String baseUrl, String key, GeoLocation geoLocation) {
		
		String json = "";
		
		URL url;
		try {
			String urlString = baseUrl + "/maps/api/geocode/json?latlng=" + getLatLongString(geoLocation) + "&sensor=false&key=" + key;
			System.out.println(urlString);
			url = new URL(baseUrl + "/maps/api/geocode/json?latlng=" + getLatLongString(geoLocation) + "&sensor=false&key=" + key);

			URLConnection connection = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				json += inputLine;
			}
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

        Gson gson = new Gson();
        return gson.fromJson(json, GoogleGeoCodeResponse.class);
        
	}
	
	public static String getLatLongString(GeoLocation g) {
		return String.format("%.6f", g.getLatitude()) + "," + String.format("%.6f",g.getLongitude());
	}
}
