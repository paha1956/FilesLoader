package com.company;

/**
 * Класс обработки событий.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public class EventHandler {
    private Object m_lock = new Object();
    private ThreadsManager m_listener;

    /**
     * Добавление обработчика события.
     * @param listener      - объект, принимающий событие
     */
    public void  addListener(ThreadsManager listener) {
        m_listener = listener;
    }

    /**
     * Отправка события.
     * @param threadID        - логический идентификатор потока, передающего событие;
     * @param fileURL         - URL закачиваемого файла;
     * @param fileSize        - объём загруженных данных;
     * @param opTime          - текущее время загрузки;
     * @param loadingComplete - флаг завершения загрузки
     */
    public void sendEvent(int threadID, String fileURL, long fileSize, long opTime, boolean loadingComplete) {
        synchronized (m_lock) {
            m_listener.getEvent(threadID, fileURL, fileSize, opTime, loadingComplete);
        }
    }
}

