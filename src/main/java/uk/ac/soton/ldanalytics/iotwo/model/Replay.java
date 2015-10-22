package uk.ac.soton.ldanalytics.iotwo.model;

import java.sql.Date;
import java.util.UUID;

public class Replay {
	private UUID replay_uuid;
    private String uri;
    private String name;
    private String source;
    private String model;
    private String mapping;
    private Float rate;
    private Date publishing_date;
	public void generateUUID() {
		replay_uuid = UUID.randomUUID();		
	}
	public void generateDate() {
		publishing_date = new Date((new java.util.Date()).getTime());
	}
	public UUID getReplay_uuid() {
		return replay_uuid;
	}
	public String getUri() {
		return uri;
	}
	public String getName() {
		return name;
	}
	public String getSource() {
		return source;
	}
	public String getModel() {
		return model;
	}
	public Float getRate() {
		return rate;
	}
	public String getMapping() {
		return mapping;
	}
	public Date getPublishing_date() {
		return publishing_date;
	}
}
