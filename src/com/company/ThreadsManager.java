
package com.company;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.*;

/**
 * Класс запуска потоков и мониторинга их работы.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public class ThreadsManager implements EventListener {

    private ArgsParser m_argsParser;
    private ProgGUI m_progGUI;
    ExecutorService m_executor;
    private ArrayList<Future> m_threadList;
    private LinksParser m_linksParser;
    private EventHandler m_eventHandler;

    private long m_filesSize;
    private long m_filesLoadTime;
    private int m_filesCounter;
    private long m_totalOpTime;

    public ThreadsManager(ArgsParser argsParser, ProgGUI progGUI) {
        m_argsParser = argsParser;
        m_progGUI = progGUI;
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
     *
     * @param threadID      - логический идентификатор потока, вызвавшего событие;
     * @param fileURL       - URL текущего загружаемого потоком файла;
     * @param fileSize      - объём загруженных данных;
     * @param contentLength - полный объём файла;
     * @param opTime        - текущее время события в формате UTC;
     * @param loadingStatus - статус загрузки файла:
     *                      EVLST_LDCOMPLETE - загрузка завершена;
     *                      EVLST_LDCONTINUE - загрузка продолжается;
     *                      EVLST_LDFROZEN   - остановка загрузки по неизвестной причине
     */
    @Override
    public void getEvent(int threadID, String fileURL, long fileSize, long contentLength, long opTime, int loadingStatus) {
        DateFormat timeFormat = new SimpleDateFormat("HHч mmмин ssсек.");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        opTime = System.currentTimeMillis() - opTime;
        switch (loadingStatus) {
            case EVLST_LDBEGINNING: {
                ProgGUI.dataOut("Поток " + threadID + ". Начата загрузка файла " + fileURL);
                m_progGUI.progressBarOut(threadID, fileURL, 0);
                break;
                }

            case EVLST_LDCOMPLETE: {
                m_filesSize += fileSize;
                m_filesLoadTime += opTime;
                m_filesCounter++;
                ProgGUI.dataOut("Поток " + threadID + ". Файл " + fileURL + " загружен: " + sizeConverter(fileSize) + "Байт за " + timeFormat.format(new Date(opTime)));
                m_progGUI.progressBarOut(threadID, fileURL, 100);

                try {
                    URL url = new URL(m_argsParser.getServerURL());

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    GregorianCalendar calendar = new GregorianCalendar();
                    SimpleDateFormat dFormat = new SimpleDateFormat("dd.MM.yyyy");
                    String fileDate = dFormat.format(calendar.getTime());
                    SimpleDateFormat tFormat = new SimpleDateFormat("HH:mm:ss");
                    String fileTime = tFormat.format(calendar.getTime());

                    PostRecord postRecord = new PostRecord();
                    postRecord.setUserName(m_argsParser.getUserName());
                    postRecord.setFileDate(fileDate);
                    postRecord.setFileTime(fileTime);
                    postRecord.setFileName(fileURL);
                    postRecord.setFileSize(fileSize);
                    postRecord.setLoadTime(opTime);

                    Gson gson = new Gson();
                    String JSON = gson.toJson(postRecord);

                    writer.write(JSON);

                    writer.flush();
                    writer.close();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    ProgGUI.dataOut("POST запрос к серверу " + m_argsParser.getServerURL() + " отправлен. Код ответа " + responseCode);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                break;
            }

            case EVLST_LDCONTINUE: {
                ProgGUI.dataOut("Поток " + threadID + ". Файл " + fileURL + ", загружено " + sizeConverter(fileSize) + "Байт, время загрузки " + timeFormat.format(new Date(opTime)));
                m_progGUI.progressBarOut(threadID, fileURL, (int)(fileSize * 100 / contentLength));
                break;
            }

            case EVLST_LDFROZEN: {
                ProgGUI.dataOut("Поток " + threadID + ". Ой, всё... Что-то пошло не так и файл " + fileURL + " устал. Было загружено " + sizeConverter(fileSize) + "Байт за время " + timeFormat.format(new Date(opTime)));
                break;
            }

            default: {
                ProgGUI.dataOut("Поток " + threadID + ". Неизвестный код события " + loadingStatus);
                break;
            }
        }
    }

    /**
     * Метод запуска потоков загрузки файлов
     *
     * @return - количество запущенных потоков
     */
    public int threadsStart() {
        try {
            m_linksParser.loadLinksFile(m_argsParser.getLinksFileName());
        } catch (IOException e) {
            ProgGUI.dataOut("Ошибка загрузки файла " + m_argsParser.getLinksFileName() + " : файл отсутствует или повреждён");
            return 0;
        }

        int numThreads = Math.min(m_argsParser.getNumThreads(), m_linksParser.getLinksList().size());
        m_totalOpTime = System.currentTimeMillis();

        m_executor = Executors.newFixedThreadPool(numThreads);

        int threadCounter;
        for (threadCounter = 1; threadCounter <= numThreads; threadCounter++) {
            m_threadList.add(m_executor.submit(new FileLoader(threadCounter, m_linksParser, m_argsParser, m_eventHandler)));
        }

        return threadCounter;
    }

    /**
     * Метод мониторинга работы потоков
     *
     * @throws InterruptedException, ExecutionException
     */
    public void threadsMonitoring() throws InterruptedException, ExecutionException {
        while (m_threadList.size() == 0){;}
        m_executor.shutdown();
        for (Future unit : m_threadList) {
            unit.get();
        }

        m_totalOpTime = System.currentTimeMillis() - m_totalOpTime;
        ProgGUI.dataOut("Загружено файлов: " + m_filesCounter + " объёмом " + sizeConverter(m_filesSize) + "Байт");
        DateFormat timeFormat = new SimpleDateFormat("HHч mmмин ssсек");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        ProgGUI.dataOut("Время загрузки: " + timeFormat.format(new Date(m_totalOpTime)));
        if (m_totalOpTime > 0)
            ProgGUI.dataOut("Скорость многопоточной загрузки: " + sizeConverter((m_filesSize * 8 * 1000) / m_totalOpTime) + "Бит в секунду");
        else
            ProgGUI.dataOut("Скорость многопоточной загрузки: очень быстро! На самом деле, что-то пошло не так и время загрузки оказалось нулевым.");

        if (m_filesLoadTime > 0)
            ProgGUI.dataOut("Средняя физическая скорость загрузки: " + sizeConverter((m_filesSize * 8 * 1000) / m_filesLoadTime) + "Бит в секунду");
        else
            ProgGUI.dataOut("Средняя скорость загрузки: неимоверно быстро! На самом деле, что-то пошло не так и время загрузки оказалось нулевым.");
    }

    /**
     * Метод для приведения объёма файла к сокращённому формату (байты - в килобайты, мегабайты и гигабайты)
     *
     * @param size - размер файла, байт
     * @return строка, содержащая приведённый к сокращённому формату размер файла
     */
    static public String sizeConverter(long size) {
        if (size < 0) return "-1 ";
        String[] lb = {" ", " к", " М", " Г"};
        int index = 0;
        double dSize = (double) size;
        while (dSize >= 1024.0 && index < lb.length - 1) {
            dSize = dSize / 1024.0;
            index++;
        }
        String formattedDouble = new DecimalFormat("#0.00").format(dSize);
        return formattedDouble + lb[index];
    }
}
