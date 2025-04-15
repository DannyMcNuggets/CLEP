package CLEP;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Packet implements Serializable {
    private final List<List<String>> data;

    public Packet(ResultSet rs) throws SQLException {

        this.data = new ArrayList<>();

        if (rs == null) return;

        while (rs.next()) {
            List<String> row = new ArrayList<>();
            int columnsCount = rs.getMetaData().getColumnCount();

            for (int i = 1; i <= columnsCount; i++) {
                String value = rs.getString(i);
                if (value != null) {
                    row.add(value);
                } else {
                    row.add("NULL");
                }
            }
            data.add(row);
        }
    }

    public List<List<String>> getData() {
        return data;
    }

    /* old version
        public class Packet implements Serializable {
        private List<String> data;

        public Packet(List<String> data) {
            this.data = data;
        }

        public List<String> getData() {
            return data;
        }
    }
     */
}
