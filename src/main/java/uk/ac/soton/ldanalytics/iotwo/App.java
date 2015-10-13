package uk.ac.soton.ldanalytics.iotwo;

import static spark.Spark.get;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

public class App {
	public static void main(String[] args) {
		FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
		Configuration freeMarkerConfiguration = new Configuration();
		freeMarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(App.class, "/templates"));
		freeMarkerEngine.setConfiguration(freeMarkerConfiguration);
		
        get("/hello", (req, res) -> {
        	 Map<String, Object> attributes = new HashMap<>();
        	return freeMarkerEngine.render(new ModelAndView(attributes, "posts.ftl"));
        });
    }
}
