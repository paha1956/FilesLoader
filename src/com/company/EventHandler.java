package com.company;

public class EventHandler {

    private EventListener m_listener = new ThreadsManager();

    public void addListener(ThreadsManager listener) {
        m_listener = listener;
    }

    public void sendEvent(int threadID, String fileURL, long fileSize, long opTime) {
        m_listener.getEvent(threadID, fileURL, fileSize, opTime);
    }

}

