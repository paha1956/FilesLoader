package com.company;

/**
 * Интерфейс реализации механизма обработки межпотоковых событий.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public interface EventListener {

    /**
     * Отправка события.
     * @param threadID        - логический идентификатор потока, передающего событие;
     * @param fileURL         - URL закачиваемого файла;
     * @param fileSize        - объём загруженных данных;
     * @param opTime          - текущее время загрузки;
     * @param loadingComplete - флаг завершения загрузки
     */
    public void getEvent(int threadID, String fileURL, long fileSize, long opTime, boolean loadingComplete);
}
