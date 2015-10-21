package uk.ac.soton.ldanalytics.iotwo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.Part;

public class Upload {
	private final int MAX_SAMPLE_LINES = 1;
	private String uri = "http://iotobservatory.com/node/stream/";
	private String tableName = ""; 
	private String fileName = "";
	private String size = "";
	private String actualPath = "";
	private List<Map<String,String>> sample = new ArrayList<Map<String,String>>();
	
	public Upload(Part file) {
		String fileId = UUID.randomUUID().toString();
		//create a placeholder uri for the stream
		uri += fileId;
        try {
			file.write(fileId + ".csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
        actualPath = System.getProperty("java.io.tmpdir") + fileId + ".csv";
        fileName = file.getSubmittedFileName();
        tableName = fileName.replace(".csv", "");
        size = Long.toString(file.getSize());
        
        try {
			getSampleSchema(file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getSampleSchema(InputStream file) {
		Map<String,String> sample = new LinkedHashMap<String,String>(); 
		Map<String,String> sampleType = new HashMap<String,String>();
		try {			
			BufferedReader br = new BufferedReader(new InputStreamReader(file));
			String separator = "";
			String header = br.readLine();
			if(header.contains(";"))
				separator = ";";
			if(header.contains(","));
				separator = ",";
			String[] headerParts = header.split(separator);
			String line = "";
			int i=1;
			while((line=br.readLine())!=null) {
				String[] lineParts = line.split(separator);
				if(lineParts.length>=headerParts.length) {
					for(int j=0;j<headerParts.length;j++) {
						String headerCol = headerParts[j];
						String val = sample.get(headerCol);
						if(Util.isNumeric(lineParts[j])) {
							sampleType.put(headerCol, "Numeric");
						} else {
							String format = Util.determineDateFormat(lineParts[j]);
							if(format==null)
								sampleType.put(headerCol, "String");
							else
								sampleType.put(headerCol, "Time ("+format+")");
						}
						if(val!=null) 
							sample.put(headerCol, val+","+lineParts[j]);
						else
							sample.put(headerCol, lineParts[j]);						
					}
				}
				if(i++>=MAX_SAMPLE_LINES)
					break;
			}
			br.close();
			
			//convert linkedhashmap to arraylist
			for(Entry<String,String> entry:sample.entrySet()) {
				Map<String,String> props = new HashMap<String,String>();
				props.put("name", entry.getKey());
				props.put("type", sampleType.get(entry.getKey()));
				props.put("eg", entry.getValue());
				this.sample.add(props);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
