package com.company;

/**
 * Класс обработки событий.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public class EventHandler {
    private Object m_lock = new Object();
    private EventListener m_listener;

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
     * @param opTime          - текущее время загрузки;
     * @param loadingStatus   - статус загрузки файла:
     *                          EVLST_LDCOMPLETE - загрузка завершена;
     *                          EVLST_LDCONTINUE - загрузка продолжается;
     *                          EVLST_LDFROZEN   - остановка загрузки по неизвестной причине
     */
    public void sendEvent(int threadID, String fileURL, long fileSize, long opTime, int loadingStatus) {
        synchronized (m_lock) {
            m_listener.getEvent(threadID, fileURL, fileSize, opTime, loadingStatus);
        }
    }
}

