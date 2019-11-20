package com.company;

import java.io.IOException;
import java.util.ArrayList;

public class ThreadsManager {

    private ArgsParser m_argsParser;
    private int m_numThreads;
    private ArrayList<FileLoader> m_threadList;
    private LinksParser m_linksParser;

    public ThreadsManager(ArgsParser argsParser) {
        m_argsParser = argsParser;
        m_threadList = new ArrayList<>();
        m_linksParser = new LinksParser();
    }

    public int threadsStart() {
        try {
            m_linksParser.loadLinksFile(m_argsParser.getLinksFileName());
        } catch (IOException e) {
            System.out.println("Ошибка загрузки файла " + m_argsParser.getLinksFileName() + " : файл отсутствует или повреждён");
            return 0;
        }

        int numThreads = Math.min(m_argsParser.getNumThreads(), m_linksParser.getLinksList().size());

        int threadCounter;
        for (threadCounter = 0; threadCounter < numThreads; threadCounter++) {
            FileLoader fileLoader = new FileLoader(threadCounter + 1, m_linksParser, m_argsParser);
            m_threadList.add(fileLoader);

            fileLoader.start();
        }
        return threadCounter;
    }

    public void threadsManage() throws InterruptedException {
        long totalTime = 0;
        long totalSize  = 0;
        for (FileLoader loader:m_threadList) {
            loader.join();
            totalSize += loader.getFilesSize();
            totalTime += loader.getFilesLoadTime();
        }

        System.out.println("Загружено: " + 10 + " файлов, " + totalSize + " байт");
        System.out.println("Время: " + totalTime/1000 + " секунд ");
        System.out.println("Средняя скорость: " + (totalSize * 8 * 1000)/totalTime + " бит/сек");

    }
}
