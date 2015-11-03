package uk.ac.soton.ldanalytics.iotwo.CEP;

import java.util.Map;
import java.util.Map.Entry;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class QueryListener implements UpdateListener {
	private String queryName;
	
	public QueryListener(String queryName) {
		this.queryName = queryName;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if(newEvents.length>0) {
			for(Object map:((Map<?, ?>)newEvents[0].getUnderlying()).entrySet()) {
				Entry<?, ?> entry = ((Entry<?, ?>)map);
				System.out.println(entry.getKey()+":"+entry.getValue());
			}
		}
	}

}
