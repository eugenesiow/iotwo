package uk.ac.soton.ldanalytics.iotwo.CEP;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.soton.ldanalytics.iotwo.EventsWebSocket;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.JsonObject;

public class QueryListener implements UpdateListener {
	private String queryName;
	
	public QueryListener(String queryName) {
		this.queryName = queryName;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if(newEvents.length>0) {
			JsonObject message = new JsonObject();
			message.addProperty("queryName", queryName);
			for(Object map:((Map<?, ?>)newEvents[0].getUnderlying()).entrySet()) {
				Entry<?, ?> entry = ((Entry<?, ?>)map);
				System.out.println(entry.getKey()+":"+entry.getValue());
				message.addProperty(entry.getKey().toString(),entry.getValue().toString());
			}
			try {
				EventsWebSocket.sendMessage(message.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
