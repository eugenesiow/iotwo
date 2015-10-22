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
}
