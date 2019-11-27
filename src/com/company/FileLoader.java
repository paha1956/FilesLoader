package com.company;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Класс загрузки файла из сети и записи его на диск в папку, записанную в m_linksParser.
 * Запускается как отдельный поток.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public class FileLoader extends Thread {

    public static final int LOADING_BUFFER_SIZE = 128000;

    private int m_threadID;
    private LinksParser m_linksParser;
    private ArgsParser m_argsParser;
    private EventHandler m_eventHandler;
    private CountDownLatch m_threadLatch;

    /**
     * Конструктор класса FileLoader
     * @param threadID        - логический идентификатор потока;
     * @param linksParser     - объект, хранящий ссылки для скачивания и имена сохраняемых файлов;
     * @param argsParser      - объект входных параметров;
     * @param eventHandler    - объект обработки событий;
     * @param latch           - объект слежения за окончанием выполнения потоков
     */
    public FileLoader(int threadID, LinksParser linksParser, ArgsParser argsParser, EventHandler eventHandler, CountDownLatch latch) {
        m_threadID = threadID;
        m_linksParser = linksParser;
        m_argsParser = argsParser;
        m_eventHandler = eventHandler;
        m_threadLatch = latch;
    }

    /**
     * Метод, реализующий закачку файла из сети и запись его на диск. Запускается как параллельный поток
     */
    @Override
    public void run() {
        List<OutputStream> outputStreamList = new ArrayList<>();
        LinkList loadLink;

        while ((loadLink = m_linksParser.getNextLink()) != null) {
            long begTime = System.currentTimeMillis();
            long fileSize = 0;

            ArrayList<String> filesList = loadLink.getFilesList();
            try {
                String collectNames = "";
                for (String fileName : filesList) {
                    collectNames += " " + fileName;
                    outputStreamList.add(new FileOutputStream((m_argsParser.getOutDirName() + "\\" + fileName)));
                }
                System.out.println("Поток " + m_threadID + ". Загрузка файла: " + loadLink.getLink() + ((filesList.size() > 1) ? " в файлы" : " в файл") + collectNames);

                URL url = new URL(loadLink.getLink());
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoInput(true);
                InputStream inputStream = urlConnection.getInputStream();

                byte[] readBuffer = new byte[LOADING_BUFFER_SIZE];
                long fileLoadTime = System.currentTimeMillis();
                do {
                    int availableSize = inputStream.available();
                    if (availableSize > LOADING_BUFFER_SIZE) availableSize = LOADING_BUFFER_SIZE;
                    int readSize = inputStream.read(readBuffer, 0, availableSize);
                    if (readSize < 0) break;

                    long currentTime = System.currentTimeMillis();
                    if ((currentTime - fileLoadTime) > 10000) {
                        fileLoadTime = currentTime;
                        m_eventHandler.sendEvent(m_threadID, loadLink.getLink(), fileSize, begTime, false);
                   }

                    fileSize += readSize;
                    for (OutputStream outputStream : outputStreamList) {
                        outputStream.write(readBuffer, 0, readSize);
                    }
                } while (true);

                for (OutputStream outputStream : outputStreamList) {
                    outputStream.flush();
                    outputStream.close();
                }

                inputStream.close();

                m_eventHandler.sendEvent(m_threadID, loadLink.getLink(), fileSize, begTime, true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        m_threadLatch.countDown();
    }
}