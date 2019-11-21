package com.company;

public interface EventListener {
    public void getEvent(int threadID, String fileURL, long fileSize, long opTime);
}
