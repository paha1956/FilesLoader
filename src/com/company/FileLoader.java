package com.company;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Класс загрузки файла из сети и записи его на диск в папку, записанную в m_linksParser.
 * Запускается как отдельный поток.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public class FileLoader extends Thread {

    public static final int LOADING_BUFFER_SIZE = 128000;
    public static final int MAX_LOADTRY_COUNTER = 3;
    public static final int LOAD_MONITORING_PERIOD_MS = 1000;

    private int m_threadID;
    private ArgsParser m_argsParser;
    private LinksParser m_linksParser;
    private EventHandler m_eventHandler;

    /**
     * Конструктор класса FileLoader
     *
     * @param threadID     - логический идентификатор потока;
     * @param linksParser  - объект, хранящий ссылки для скачивания и имена сохраняемых файлов;
     * @param argsParser   - объект входных параметров;
     * @param eventHandler - объект обработки событий;
     */
    public FileLoader(int threadID, LinksParser linksParser, ArgsParser argsParser, EventHandler eventHandler) {
        m_threadID = threadID;
        m_linksParser = linksParser;
        m_argsParser = argsParser;
        m_eventHandler = eventHandler;
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
            long previousFileSize = 0;
            int frozenLoadingCounter = 0;
            outputStreamList.clear();

            ArrayList<String> filesList = loadLink.getFilesList();
            try {
                String collectNames = "";
                for (String fileName : filesList) {
                    collectNames += " " + fileName;
                    outputStreamList.add(new FileOutputStream((m_argsParser.getOutDirName() + "\\" + fileName)));
                }

                URL url = new URL(loadLink.getLink());
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoInput(true);

                long loadFileSize;
                loadFileSize = urlConnection.getContentLength();
                InputStream inputStream = urlConnection.getInputStream();

                ProgGUI.dataOut("Поток " + m_threadID + ". Загрузка файла: " + loadLink.getLink() + "(" + ThreadsManager.sizeConverter(loadFileSize) + "Байт) " + ((filesList.size() > 1) ? " в файлы" : " в файл") + collectNames);
                m_eventHandler.sendEvent(m_threadID, loadLink.getLink(), 0, loadFileSize,0, EventListener.EVLST_LDBEGINNING);

                byte[] readBuffer = new byte[LOADING_BUFFER_SIZE];
                long fileLoadTime = System.currentTimeMillis();
                do {
                    int availableSize = inputStream.available();
                    if (availableSize > LOADING_BUFFER_SIZE) availableSize = LOADING_BUFFER_SIZE;
                    int readSize = inputStream.read(readBuffer, 0, availableSize);
                    if (readSize < 0){
                        m_eventHandler.sendEvent(m_threadID, loadLink.getLink(), fileSize, loadFileSize, begTime, EventListener.EVLST_LDCOMPLETE);
                        break;
                    }

                    long currentTime = System.currentTimeMillis();
                    if ((currentTime - fileLoadTime) > LOAD_MONITORING_PERIOD_MS) {
                        fileLoadTime = currentTime;
                        if (previousFileSize != fileSize) {
                            frozenLoadingCounter = 0;
                            previousFileSize = fileSize;
                        } else {
                            frozenLoadingCounter++;
                            if (frozenLoadingCounter > MAX_LOADTRY_COUNTER) {
                                m_eventHandler.sendEvent(m_threadID, loadLink.getLink(), fileSize, loadFileSize, begTime, EventListener.EVLST_LDFROZEN);
                                break;
                            }
                        }
                        m_eventHandler.sendEvent(m_threadID, loadLink.getLink(), fileSize, loadFileSize, begTime, EventListener.EVLST_LDCONTINUE);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}