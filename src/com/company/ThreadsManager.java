
package com.company;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

/**
 * Класс запуска потоков и мониторинга их работы.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public class ThreadsManager implements EventListener {

    private ArgsParser m_argsParser;
    private ArrayList<FileLoader> m_threadList;
    private LinksParser m_linksParser;
    private EventHandler m_eventHandler;
    private CountDownLatch m_threadLatch;

    private long m_filesSize;
    private long m_filesLoadTime;
    private int m_filesCounter;
    private long m_totalOpTime;

    public ThreadsManager(ArgsParser argsParser) {
        m_argsParser = argsParser;
        m_threadList = new ArrayList<>();
        m_linksParser = new LinksParser();
        m_eventHandler = new EventHandler();
        m_eventHandler.addListener(this);
        m_filesSize = 0;
        m_filesLoadTime = 0;
        m_filesCounter = 0;
        m_totalOpTime = 0;
    }

    /**
     * Метод приёма события от потока
     * @param threadID        - логический идентификатор потока, вызвавшего событие;
     * @param fileURL         - URL текущего загружаемого потоком файла;
     * @param opTime          - текущее время события в формате UTC;
     * @param loadingComplete - флаг окончания загрузки
     */
    @Override
    public void getEvent(int threadID, String fileURL, long fileSize, long opTime, boolean loadingComplete) {
        DateFormat timeFormat = new SimpleDateFormat("HHч mmмин ssсек.");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        opTime = System.currentTimeMillis() - opTime;
        if (loadingComplete) {
            m_filesSize += fileSize;
            m_filesLoadTime += opTime;
            m_filesCounter++;
            System.out.println("Поток " + threadID + ". Файл " + fileURL + " загружен: " + sizeConverter(fileSize) + "Байт за " + timeFormat.format(new Date(opTime)));
        } else
            System.out.println("Поток " + threadID + ". Файл " + fileURL + ", загружено " + sizeConverter(fileSize) + "Байт, время загрузки " + timeFormat.format(new Date(opTime)));
    }

    /**
     * Метод запуска потоков
     * @return - количество запущенных потоков
     */
    public int threadsStart() {
        try {
            m_linksParser.loadLinksFile(m_argsParser.getLinksFileName());
        } catch (IOException e) {
            System.out.println("Ошибка загрузки файла " + m_argsParser.getLinksFileName() + " : файл отсутствует или повреждён");
            return 0;
        }

        int numThreads = Math.min(m_argsParser.getNumThreads(), m_linksParser.getLinksList().size());
        m_totalOpTime = System.currentTimeMillis();
        m_threadLatch = new CountDownLatch(numThreads);

        int threadCounter;
        for (threadCounter = 0; threadCounter < numThreads; threadCounter++) {
            FileLoader fileLoader = new FileLoader(threadCounter + 1, m_linksParser, m_argsParser, m_eventHandler, m_threadLatch);
            m_threadList.add(fileLoader);

            fileLoader.start();
        }
        return threadCounter;
    }

    /**
     * Метод мониторинга работы потоков
     *
     * @throws InterruptedException
     */
    public void threadsMonitoring() throws InterruptedException {
        if (m_threadLatch == null) return;
        m_threadLatch.await();

        m_totalOpTime = System.currentTimeMillis() - m_totalOpTime;
        System.out.println("Загружено файлов: " + m_filesCounter + " объёмом " + sizeConverter(m_filesSize) + "Байт");
        DateFormat timeFormat = new SimpleDateFormat("HHч mmмин ssсек");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println("Время загрузки: " + timeFormat.format(new Date(m_totalOpTime)));
        if (m_totalOpTime > 0)
            System.out.println("Скорость многопоточной загрузки: " + sizeConverter((m_filesSize * 8 * 1000) / m_totalOpTime) + "Бит в секунду");
        else
            System.out.println("Скорость многопоточной загрузки: очень быстро! На самом деле, что-то пошло не так и время загрузки оказалось нулевым.");

        if (m_filesLoadTime > 0)
            System.out.println("Средняя физическая скорость загрузки: " + sizeConverter((m_filesSize * 8 * 1000) / m_filesLoadTime) + "Бит в секунду");
        else
            System.out.println("Средняя скорость загрузки: неимоверно быстро! На самом деле, что-то пошло не так и время загрузки оказалось нулевым.");
    }

    /**
     * Метод для приведения объёма файла к сокращённому формату (байты - в килобайты, мегабайты и гигабайты)
     *
     * @param size - размер файла, байт
     * @return строка, содержащая приведённый к сокращённому формату размер файла
     */
    private String sizeConverter(long size) {
        if (size < 0) return "-1 ";
        String[] lb = {" ", " к", " М", " Г"};
        int index = 0;
        while (size > 1024 && index < lb.length - 1) {
            size = size >> 10;
            index++;
        }
        return size + lb[index];
    }
}
