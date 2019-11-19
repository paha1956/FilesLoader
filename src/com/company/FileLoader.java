package com.company;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileLoader extends Thread {
    private int m_treadID;
    private LinksParser m_linksParser;
    private ArgsParser m_argsParser;

    public FileLoader(int treadID, LinksParser linksParser, ArgsParser argsParser) {
        m_treadID = treadID;
        m_linksParser = linksParser;
        m_argsParser = argsParser;
    }

    @Override
    public void run()    //Этот метод будет выполнен в побочном потоке
    {
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
                System.out.println("Поток" + m_treadID + ". Загрузка файла: " + loadLink.getLink() + " в файлы" + collectNames);

                URL url = null;
                url = new URL(loadLink.getLink());
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoInput(true);
                InputStream inputStream = urlConnection.getInputStream();

                int bufferSize = 64000;
                byte readBuffer[] = new byte[bufferSize];
                do {
                    int availableSize = inputStream.available();
                    if (availableSize > bufferSize) availableSize = bufferSize;
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
                DateFormat timeFormat = new SimpleDateFormat("HHч mmмин ssсек.");
                timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                System.out.println("Файл " + loadLink.getLink() + " загружен: " + sizeConverter(fileSize) + " за " + timeFormat.format(new Date(System.currentTimeMillis() - begTime)));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String sizeConverter(long size) {
        if (size < 0) return "-1 байт";
        String[] lb = {" байт", " кБайт", " МБайт", " ГБайт"};
        int index = 0;
        while (size > 1024 && index < lb.length-1) {
            size = size >> 10;
            index++;
        }
        return size + lb[index];
    }
}