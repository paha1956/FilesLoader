package com.company;

/**
 * Интерфейс реализации механизма обработки межпотоковых событий.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public interface EventListener {

    public static final int EVLST_LDBEGINNING = 1;
    public static final int EVLST_LDCOMPLETE = 2;
    public static final int EVLST_LDCONTINUE = 3;
    public static final int EVLST_LDFROZEN = 4;

    /**
     * Получение события.
     *
     * @param threadID              - логический идентификатор потока, передающего событие;
     * @param fileURL               - URL закачиваемого файла;
     * @param fileSize              - объём загруженных данных;
     * @param contentLength         - полный объём файла;
     * @param opTime                - текущее время загрузки;
     * @param loadingStatus         - статус загрузки файла:
     *                                EVLST_LDCOMPLETE - загрузка завершена;
     *                                EVLST_LDCONTINUE - загрузка продолжается;
     *                                EVLST_LDFROZEN   - остановка загрузки по неизвестной причине
     */
    public void getEvent(int threadID, String fileURL, long fileSize, long contentLength, long opTime, int loadingStatus);
}
