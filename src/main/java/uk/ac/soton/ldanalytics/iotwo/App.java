package uk.ac.soton.ldanalytics.iotwo;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.webSocket;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.server.Request;
import org.sql2o.Sql2o;

import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;
import uk.ac.soton.ldanalytics.iotwo.CEP.QueryListener;
import uk.ac.soton.ldanalytics.iotwo.demo.LoadDataAndReplay;
import uk.ac.soton.ldanalytics.iotwo.model.Model;
import uk.ac.soton.ldanalytics.iotwo.model.Replay;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationDBRef;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.google.gson.Gson;

import freemarker.cache.ClassTemplateLoader;

public class App {
	public static void main(String[] args) {		
		FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
		freemarker.template.Configuration freeMarkerConfiguration = new freemarker.template.Configuration();
		freeMarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(App.class, "/templates"));
		freeMarkerEngine.setConfiguration(freeMarkerConfiguration);
		Spark.staticFileLocation("/public");
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Sql2o sql2o = new Sql2o(prop.getProperty("jdbcUrl"), prop.getProperty("dbUser"), prop.getProperty("dbPass"));
		Model model = new Model(sql2o);
		Gson gson = new Gson(); 
		
		ConfigurationDBRef dbConfig = new ConfigurationDBRef();
		dbConfig.setDriverManagerConnection("org.h2.Driver",
											prop.getProperty("jdbcUrl"), 
											prop.getProperty("dbUser"), 
											prop.getProperty("dbPass"));

		Configuration engineConfig = new Configuration();
		engineConfig.addDatabaseReference("hist", dbConfig);
		
		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(engineConfig);
		
		LoadDataAndReplay envReplay = new LoadDataAndReplay(Long.parseLong(prop.getProperty("timestampNow")), sql2o, epService);
		envReplay.setLoadDB(true);
		envReplay.setSpeed(Integer.parseInt(prop.getProperty("speed")));
		envReplay.loadFile("/Users/eugene/Documents/workspace/iotwo/data/all-environmental-sort.csv");
		envReplay.loadSchema("/Users/eugene/Documents/workspace/iotwo/schema/environmental.map");
		(new Thread(envReplay)).start();
		
		LoadDataAndReplay meterReplay = new LoadDataAndReplay(Long.parseLong(prop.getProperty("timestampNow")), sql2o, epService);
		meterReplay.setLoadDB(true);
		meterReplay.setSpeed(Integer.parseInt(prop.getProperty("speed")));
		meterReplay.loadFile("/Users/eugene/Documents/workspace/iotwo/data/all-meter-replace.csv");
		meterReplay.loadSchema("/Users/eugene/Documents/workspace/iotwo/schema/meter.map");
		(new Thread(meterReplay)).start();
		
		LoadDataAndReplay motionReplay = new LoadDataAndReplay(Long.parseLong(prop.getProperty("timestampNow")), sql2o, epService);
		motionReplay.setLoadDB(true);
		motionReplay.setSpeed(Integer.parseInt(prop.getProperty("speed")));
		motionReplay.loadFile("/Users/eugene/Documents/workspace/iotwo/data/all-motion-replace.csv");
		motionReplay.loadSchema("/Users/eugene/Documents/workspace/iotwo/schema/motion.map");
		(new Thread(motionReplay)).start();
		
//		String stmtStr = "    SELECT\n" + 
//				"        avg(environmental.insideTemp) AS averageTemp ,\n" + 
//				"        max(environmental.insideTemp) AS maxTemp ,\n" +
//				"        min(environmental.insideTemp) AS minTemp, URI \n" +
//				"   FROM\n" + 
//				"        environmental.win:time(1 hour),"
//				+ "sql:hist [' select URI from replay ']";
		String stmtStr = "    SELECT\n" + 
		"        environmental.insideTemp AS currentTemp \n" + 
		"   FROM\n" + 
		"        environmental.std:lastevent()";
//		EPStatement statement = epService.getEPAdministrator().createEPL(stmtStr);
//		statement.addListener(new QueryListener("tempQuery"));
		
//		String stmtStr = "SELECT \n" + 
//				"	t1.LOCATION,\n" + 
//				"	meter.MeterName, avg(meter.RealPowerWatts) ,sum(motion.MotionOrNoMotion)\n" + 
//				"FROM\n" + 
//				"	motion.win:time(10 min),\n" + 
//				"	meter.win:time(10 min),\n" + 
//				"	sql:hist [' select SENSINGDEVICE, LOCATION from sensors '] as t1,\n" + 
//				"	sql:hist [' select SENSINGDEVICE, LOCATION from sensors '] as t2\n" + 
//				"WHERE \n" + 
//				"	motion.MotionSensorName=t1.SENSINGDEVICE AND\n" + 
//				"	meter.MeterName=t2.SENSINGDEVICE AND\n" + 
//				"	t1.LOCATION=t2.LOCATION\n" + 
//				"GROUP BY\n" + 
//				"	t1.LOCATION,\n" + 
//				"	meter.MeterName,\n" + 
//				"	motion.MotionSensorName\n" + 
//				"HAVING\n" + 
//				"	sum(motion.MotionOrNoMotion)=0 AND\n" + 
//				"	sum(meter.RealPowerWatts)>0";
		EPStatement statement = epService.getEPAdministrator().createEPL(stmtStr);
		statement.addListener(new QueryListener("tempQuery"));
		
		stmtStr = "    SELECT\n" + 
		"        environmental.insideHumidity AS currentHumidity \n" + 
		"   FROM\n" + 
		"        environmental.std:lastevent()";
		EPStatement hstatement = epService.getEPAdministrator().createEPL(stmtStr);
		hstatement.addListener(new QueryListener("humidityQuery"));
		
		webSocket("/events", EventsWebSocket.class);
		
        get("/", (req, res) -> {        	
        	Map<String, Object> attributes = new HashMap<>();
        	return freeMarkerEngine.render(new ModelAndView(attributes, "index.ftl"));
        });
        
        get("/sensors/replay", (req, res) -> {
	       	Map<String, Object> attributes = new HashMap<>();
	       	return freeMarkerEngine.render(new ModelAndView(attributes, "replay.ftl"));
        });
        
        post("/sensors/replay/upload", "application/json", (req, res) -> {
        	MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
        	req.raw().setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
            Upload upload = new Upload(req.raw().getPart("file"));
            return gson.toJson(upload);
        });
        
        get("api/sensors/replay", "application/json", (req, res) -> {        	
        	return gson.toJson(model.getAllReplays());
        });
        
        post("api/sensors/replay", "application/json", (req, res) -> {    
        	Replay replay = gson.fromJson(req.body(), Replay.class);
        	replay.generateUUID();
        	replay.generateDate();
//        	return gson.toJson(replay);
        	return model.createReplay(replay);
        });
    }
}
