package uk.ac.soton.ldanalytics.iotwo.model;

import java.util.List;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Model {
	private Sql2o sql2o;
	
	public Model(Sql2o sql2o) {
        this.sql2o = sql2o;
    }
	
	public List<Replay> getAllReplays() {
        try (Connection conn = sql2o.open()) {
            List<Replay> replays = conn.createQuery("select * from REPLAY")
                    .executeAndFetch(Replay.class);
            return replays;
        }
    }
	
	public String createReplay(Replay model) {
		// Give the parameters the same names as the corresponding properties in your model class
		String sql = 
			"insert into REPLAY(replay_uuid, uri, name, source, model, rate, publishing_date) "+
			"values (:replay_uuid, :uri, :name, :source, :model, :rate, :publishing_date)";

		try (Connection conn = sql2o.open()) {
		    conn.createQuery(sql).bind(model).executeUpdate();
		}
		return "success";
	}
}
