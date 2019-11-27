package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Классы, предназначенные для получения и хранения ссылок скачиваемых из сети файлов, и имён файлов для сохранения на диск.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */

/**
 * Класс хранения ссылки для скачивания.
 * Для работы потоков используется список объектов класса LinkList.
 */
class LinkList {
    private String m_link;
    private ArrayList<String> m_filesList;

    public LinkList(String link, String fileName) {
        m_link = link;
        m_filesList = new ArrayList<>();
        m_filesList.add(fileName);
    }

    public String getLink() {
        return m_link;
    }

    public ArrayList<String> getFilesList() {
        return m_filesList;
    }

    public void addFileName(String fileName) {
        if (m_filesList == null) return;
        m_filesList.add(fileName);
    }
}

/**
 * Класс парсинга ссылок для скачивания из файла ссылок.
 * Из полученных ссылок формирует список, элементами которого являются объекты класса LinkList
 */
public class LinksParser {
    private Object m_lock = new Object();
    private LinkedList<LinkList> m_linksList;

    public LinksParser() {
        m_linksList = new LinkedList<>();
    }

    public LinkedList<LinkList> getLinksList() {
        return m_linksList;
    }

    /**
     * Метод загрузки и парсинга файла ссылок.
     * @param linksFileName - имя файла, содержащего ссылки на скачиваемые файлы
     */
    public void loadLinksFile(String linksFileName) throws IOException {
        BufferedReader fileReader;
        fileReader = new BufferedReader(new FileReader(linksFileName));
        String line;
        do {
            line = fileReader.readLine();
            if (line != null) {
                String[] frames = line.split(" ");
                if (frames.length == 2) {
                    if (isValidURL(frames[0]))
                        addLink(frames[0], frames[1]);
                }
            }
        }
        while (line != null);
    }

    /**
     * Метод добавления к ссылке имени файла для сохранения.
     * Если указанная ссылка уже существует в списке, то происходит подключение имени файла к уже имеющейся ссылке.
     * @param link          - URL файла для скачивания;
     * @param fileName      - имя файла для сохранения данных
     */
    private void addLink(String link, String fileName) {
        int linkCounter = 0;
        for (LinkList linkUnit : m_linksList) {
            if (linkUnit.getLink().equalsIgnoreCase(link)) {
                m_linksList.get(linkCounter).addFileName(fileName);
                return;
            }
            linkCounter++;
        }
        m_linksList.add(new LinkList(link, fileName));
    }

    /**
     * Метод получения очередной ссылки. Производит чтение и удаление очередной ссылки из списка ссылок.
     * Для предотвращения конфликтов при обращении к списку ссылок несколькими потоками используется синхронизация
     * @return - объект класса LinkList, содержащий очередную ссылку и список файлов для сохранения на диске
     */
    public LinkList getNextLink() {
        LinkList link = null;
        synchronized (m_lock) {
            if (m_linksList.size() > 0) {
                link = m_linksList.get(0);
                m_linksList.remove(0);
            }
        }
        return link;
    }

    /**
     * Метод проверки URL на корректность.
     * @param url           - URL для проверки на корректность
     * @return               - результат проверки URL: true - URL корректен, false - URL неправильный
     */
    private boolean isValidURL(String url) {
        try {
            new URI(url).parseServerAuthority();
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
