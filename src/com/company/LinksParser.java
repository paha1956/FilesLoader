package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;

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

public class LinksParser {
    private LinkedList<LinkList> m_linksList;

    public LinksParser() {
        m_linksList = new LinkedList<>();
    }

    public LinkedList<LinkList> getLinksList() {
        return m_linksList;
    }

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

    public LinkList getNextLink() {
        LinkList link = null;
        synchronized (this) {
            if (m_linksList.size() > 0) {
                link = m_linksList.get(0);
                m_linksList.remove(0);
            }
        }
        return link;
    }

    boolean isValidURL(String url) {
        try {
            new URI(url).parseServerAuthority();
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
