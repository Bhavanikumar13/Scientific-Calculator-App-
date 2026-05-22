package src;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    public static class Record {
        public String expression;
        public String result;

        public Record(String expression, String result) {
            this.expression = expression;
            this.result = result;
        }

        @Override
        public String toString() {
            return expression + " = " + result;
        }
    }

    private final List<Record> records = new ArrayList<>();

    public void addRecord(String expr, String result) {
        records.add(new Record(expr, result));
        if (records.size() > 50) {
            records.remove(0);
        }
    }

    public List<Record> getRecords() {
        return new ArrayList<>(records);
    }

    public void clear() {
        records.clear();
    }
}
