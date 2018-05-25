package ca.jimlong.FolderSync.Models;

public class GoogleGeoCodeResponse {

	public class Results {
		public String formatted_address ;
		public Geometry geometry;
		public String[] types;
		public Address_component[] address_components;
	}

	public class Geometry {
		public Bounds bounds;
		public String location_type;
		public Location location;
		public Bounds viewport;
	}

	public class Bounds {

		public Location northeast;
		public Location southwest;
	}

	public class Location {
		public String lat;
		public String lng;
	}

	public class Address_component {
		public String long_name;
		public String short_name;
		public String[] types;
	}
	
	public String status ;
	public Results[] results;
	public GoogleGeoCodeResponse() {

	}
}
