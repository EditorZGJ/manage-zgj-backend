package com.example.common;

import java.util.concurrent.ConcurrentHashMap;

public class ImportProgress {
    private static final ConcurrentHashMap<String, Progress> map = new ConcurrentHashMap<>();

    public static class Progress {
        private int total;
        private int processed;
        private int success;
        private int failed;
        private boolean done;

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getProcessed() { return processed; }
        public void setProcessed(int processed) { this.processed = processed; }

        public int getSuccess() { return success; }
        public void setSuccess(int success) { this.success = success; }

        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }

        public boolean isDone() { return done; }
        public void setDone(boolean done) { this.done = done; }
    }

    public static void put(String taskId, Progress p) {
        map.put(taskId, p);
    }

    public static Progress get(String taskId) {
        return map.get(taskId);
    }

    public static void remove(String taskId) {
        map.remove(taskId);
    }
}