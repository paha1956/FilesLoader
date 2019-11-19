package com.company;

import java.io.IOException;
import java.util.ArrayList;

public class ThreadsManager {

    private ArgsParser m_argsParser;
    private int m_numThreads;
    private ArrayList<Thread> m_threadList;
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

    public void threadsManage() {

    }
}
