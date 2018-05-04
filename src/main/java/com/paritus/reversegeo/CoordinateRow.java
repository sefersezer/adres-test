package com.paritus.reversegeo;

public class CoordinateRow {
	private Double latitude;
	private Double longitude;
	private Double defaultRadius;
	private Double streetRadius;
	private String comment;
	private String target;
	private String expected;
	private String jira;

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getDefaultRadius() {
		return defaultRadius;
	}

	public Double getStreetRadius() {
		return streetRadius;
	}

	public String getComment() {
		return comment;
	}

	public String getTarget() {
		return target;
	}

	public String getExpected() {
		return expected;
	}

	public String getJira() {
		return jira;
	}

	public CoordinateRow(Double latitude, Double longitude, Double defaultRadius, Double streetRadius, 
			String target, String expected, String jira, String comment) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.defaultRadius = defaultRadius;
		this.streetRadius = streetRadius;
		this.comment = comment;
		this.target = target;
		this.expected = expected;
		this.jira = jira;
	}

}
