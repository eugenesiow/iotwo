package uk.ac.soton.ldanalytics.iotwo;

import static spark.Spark.get;
import static spark.Spark.post;

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
import uk.ac.soton.ldanalytics.iotwo.demo.LoadDataAndReplay;
import uk.ac.soton.ldanalytics.iotwo.model.Model;
import uk.ac.soton.ldanalytics.iotwo.model.Replay;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.google.gson.Gson;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

public class App {
	public static void main(String[] args) {		
		FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
		Configuration freeMarkerConfiguration = new Configuration();
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
		
		EPServiceProvider epService = EPServiceProviderManager.getProvider("stream_engine");
		LoadDataAndReplay loadDataAndReplay = new LoadDataAndReplay(Long.parseLong(prop.getProperty("timestampNow")), sql2o, epService);
		loadDataAndReplay.setLoadDB(true);
		loadDataAndReplay.loadFile("/Users/eugene/Documents/workspace/iotwo/data/all-environmental-sort.csv");
		loadDataAndReplay.loadSchema("/Users/eugene/Documents/workspace/iotwo/schema/environmental.map");
		(new Thread(loadDataAndReplay)).start();
		
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
