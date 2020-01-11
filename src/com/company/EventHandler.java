package com.company;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Класс обработки событий.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public class EventHandler {
    ReentrantLock m_locker;
    private EventListener m_listener;

    public EventHandler() {
        m_locker = new ReentrantLock();
    }

    /**
     * Добавление обработчика события.
     * @param listener      - объект, принимающий событие
     */
    public void  addListener(EventListener listener) {
        m_listener = listener;
    }

    /**
     * Отправка события.
     * @param threadID        - логический идентификатор потока, передающего событие;
     * @param fileURL         - URL закачиваемого файла;
     * @param fileSize        - объём загруженных данных;
     * @param contentLength   - полный объём файла;
     * @param opTime          - текущее время загрузки;
     * @param loadingStatus   - статус загрузки файла:
     *                          EVLST_LDCOMPLETE - загрузка завершена;
     *                          EVLST_LDCONTINUE - загрузка продолжается;
     *                          EVLST_LDFROZEN   - остановка загрузки по неизвестной причине
     */
    public void sendEvent(int threadID, String fileURL, long fileSize, long contentLength, long opTime, int loadingStatus) {
        m_locker.lock();
        m_listener.getEvent(threadID, fileURL, fileSize, contentLength, opTime, loadingStatus);
        m_locker.unlock();
    }
}

