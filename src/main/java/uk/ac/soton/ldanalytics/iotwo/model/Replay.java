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
}
