package com.company;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class FileLoader extends Thread {

    public static final int LOADING_BUFFER_SIZE = 64000;

    private int m_treadID;
    private LinksParser m_linksParser;
    private ArgsParser m_argsParser;
    private EventHandler m_eventHandler;
    private CountDownLatch m_threadLatch;


    public FileLoader(int treadID, LinksParser linksParser, ArgsParser argsParser, EventHandler eventHandler, CountDownLatch latch) {
        m_treadID = treadID;
        m_linksParser = linksParser;
        m_argsParser = argsParser;
        m_eventHandler = eventHandler;
        m_threadLatch = latch;
    }

    @Override
    public void run() {
        List<OutputStream> outputStreamList = new ArrayList<>();
        LinkList loadLink;

        while ((loadLink = m_linksParser.getNextLink()) != null) {
            long opTime = 0;
            long begTime = System.currentTimeMillis();
            long fileSize = 0;

            ArrayList<String> filesList = loadLink.getFilesList();
            try {
                String collectNames = "";
                for (String fileName : filesList) {
                    collectNames += " " + fileName;
                    outputStreamList.add(new FileOutputStream((m_argsParser.getOutDirName() + "\\" + fileName)));
                }
                System.out.println("Поток " + m_treadID + ". Загрузка файла: " + loadLink.getLink() + ((filesList.size() > 1) ? " в файлы" : " в файл") + collectNames);

                URL url = new URL(loadLink.getLink());
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoInput(true);
                InputStream inputStream = urlConnection.getInputStream();

                byte[] readBuffer = new byte[LOADING_BUFFER_SIZE];
                do {
                    int availableSize = inputStream.available();
                    if (availableSize > LOADING_BUFFER_SIZE) availableSize = LOADING_BUFFER_SIZE;
                    int readSize = inputStream.read(readBuffer, 0, availableSize);
                    if (readSize < 0) break;
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

                opTime = System.currentTimeMillis() - begTime;
                m_eventHandler.sendEvent(m_treadID, loadLink.getLink(), fileSize, opTime);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        m_threadLatch.countDown();
    }
}