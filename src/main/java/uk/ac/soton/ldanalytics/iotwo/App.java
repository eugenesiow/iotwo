package uk.ac.soton.ldanalytics.iotwo;

import static spark.Spark.get;
import static spark.Spark.post;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.server.Request;

import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

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
        	Gson gson = new Gson(); 
            Upload upload = new Upload(req.raw().getPart("file"));
            return gson.toJson(upload);
        });
        
        
    }
}
