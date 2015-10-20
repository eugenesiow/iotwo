package uk.ac.soton.ldanalytics.iotwo;

import static spark.Spark.get;
import static spark.Spark.post;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.eclipse.jetty.server.Request;

import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;
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
        	System.out.println(System.getProperty("java.io.tmpdir"));
        	
        	Map<String, Object> attributes = new HashMap<>();
        	return freeMarkerEngine.render(new ModelAndView(attributes, "index.ftl"));
        });
        
        get("/sensors/replay", (req, res) -> {
	       	Map<String, Object> attributes = new HashMap<>();
	       	return freeMarkerEngine.render(new ModelAndView(attributes, "replay.ftl"));
        });
        
        post("/sensors/replay/upload", (req, res) -> {
        	MultipartConfigElement multipartConfigElement = new MultipartConfigElement("tmp");
        	req.raw().setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
        	
        	String fileId = UUID.randomUUID().toString();
            Part file = req.raw().getPart("file"); //file is name of the upload form
            file.write(fileId + ".csv");
            System.out.println(file.getSubmittedFileName());
            System.out.println(file.getSize());
            //return new id, process csv
            return fileId;
        });
        
        
    }
}
