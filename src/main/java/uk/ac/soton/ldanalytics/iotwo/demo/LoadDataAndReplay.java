package uk.ac.soton.ldanalytics.iotwo.demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.sql2o.Sql2o;
import org.zeromq.ZMQ;

import com.espertech.esper.client.EPServiceProvider;
import com.google.gson.Gson;

public class LoadDataAndReplay implements Runnable {
	private long startTime;
	private Sql2o sql2o;
	private Boolean loadDB = false;
	private String fileName;
	private Map<String,Object> dataSchema;
	private int timestampColIndex;
	private EPServiceProvider epService;
	private String name;
	private int speed=1;
	private ZMQ.Socket sender;
	
	public LoadDataAndReplay(long startTime, Sql2o sql2o, EPServiceProvider epService, ZMQ.Socket sender) {
		this.startTime = startTime;
		this.sql2o = sql2o;
		this.epService = epService;
		this.sender = sender;
	}
	
	public void setLoadDB(Boolean loadDB) {
		this.loadDB = loadDB;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line="";
			long previousTime = startTime;
			while((line=br.readLine())!=null) {
				String[] parts = line.split(",");
				if(parts.length>timestampColIndex) {
					long rowTime = Long.parseLong(parts[timestampColIndex]);
					if(rowTime >= startTime) {
						Thread.sleep((rowTime-previousTime)*1000/speed);
						previousTime = rowTime;
						Map<String, Object> data = new LinkedHashMap<String, Object>();
			            int i=0;
			            for(Entry<String,Object> row:dataSchema.entrySet()) {
			            	data.put(row.getKey(), convertStrToObject(parts[i++],row.getValue()));
			            }
//			            epService.getEPRuntime().sendEvent(data, name);
			            Gson gson = new Gson();
			            sender.sendMore(name);
			            sender.send(gson.toJson(data));
					} else if(loadDB) {
						
					}
				}
			}
			br.close();
		} catch(IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void loadFile(String fileName) {
		this.fileName = fileName;
	}
	
	public void setTimestampCol(String colName) {
		int i=0;
		for(String key:dataSchema.keySet()) {
			if(key.equals(colName)) {
				timestampColIndex = i;
				break;
			}
			i++;
		}
	}

	public void loadSchema(String fileName) {
		dataSchema = new LinkedHashMap<String,Object>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line="";
			int i = 0;
			while((line=br.readLine())!=null) {
				String[] parts = line.split(",");
				if(parts.length>1) {
					dataSchema.put(parts[0], classMap(parts[1]));
					if(parts[1].toLowerCase().equals("timestamp")) {
						timestampColIndex = i;
					}
				}
				i++;
			}
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		Path p = Paths.get(fileName);
		name = FilenameUtils.removeExtension(p.getFileName().toString());
		epService.getEPAdministrator().getConfiguration().addEventType(name, dataSchema);
	}
	
	private Object classMap(String className) {
		Object object = null;
		switch(className.toLowerCase()) {
			case "string":
				object = String.class;
				break;
			case "float":
				object = Float.class;
				break;
			case "double":
				object = Double.class;
				break;
			case "integer":
				object = Integer.class;
				break;
			case "timestamp":
				object = Timestamp.class;
				break;
		}
		return object;
	}
	
	private static Object convertStrToObject(String val, Object className) {
		Object object = null;
		if(!val.trim().equals("")) {
			if(className.equals(String.class)) {
				object = val;
			} else if(className.equals(Float.class)) {
				object = Float.parseFloat(val);
			} else if(className.equals(Timestamp.class)) {
				object = new Timestamp(Long.parseLong(val));
			} else if(className.equals(Integer.class)) {
				object = Integer.parseInt(val);
			}
		}
		return object;
	}

}
