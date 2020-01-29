package com.company;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Класс парсинга входных параметров программы.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */
public class ArgsParser {

    public static final int THREADS_NUM = 7;

    public static final int ARGS_OK = 1;
    public static final int ARGS_ERROR = 2;
    public static final int ARGS_HELP = 3;

    public static final String CONFIG_URL = "D:\\Share\\config.json";

    private String m_args[];
    private ArrayList<String> m_errors;
    private String m_userName;
    private String m_serverURL;
    private int m_numThreads;
    private String m_outDirName;
    private String m_linksFileName;

    public ArgsParser(String[] args) {
        m_args = args;
        m_errors = new ArrayList<>();
        m_numThreads = THREADS_NUM;
        m_outDirName = "";
        m_linksFileName = "";
        m_errors.clear();
    }

    public ArrayList getErrors() {
        return m_errors;
    }
    public String getUserName() { return m_userName; }
    public String getServerURL() { return m_serverURL; }
    public int getNumThreads() {
        return m_numThreads;
    }
    public String getOutDirName() {
        return m_outDirName;
    }
    public String getLinksFileName() {
        return m_linksFileName;
    }

    /**
     * Метод парсинга параметров
     *
     * @return - результат расшифровки входных параметров:
     * ARGS_OK - параметры расшифровались без ошибок;
     * ARGS_ERROR - параметры содержат ошибку, содержание ошибки записывается в поле m_errors;
     * ARGS_HELP - требуется вывод справки по программе
     */
    public int getArgsProperty() {
        int res = ARGS_OK;

        BufferedReader fileReader;
        String configData = "";
        try {
            fileReader = new BufferedReader(new FileReader(CONFIG_URL));
            String line;
            do {
                line = fileReader.readLine();
                if (line != null) {
                    configData += line;
                }
            }
            while (line != null);
        } catch (Exception e) {
            m_errors.add("  - ошибка чтения файла конфигурации");
            return ARGS_ERROR;
        }

        Gson jsonConfig = new Gson();
        try {
            ConfigList config = jsonConfig.fromJson(configData, ConfigList.class);
            m_userName = config.getUserName();
            m_serverURL = config.getServerURL();
            m_outDirName = config.getOutDirName();
            m_linksFileName = config.getLinksFileName();
        } catch (JsonSyntaxException e){
            m_errors.add("  - неправильная структура файла конфигурации");
            return ARGS_ERROR;
        }

        if (m_userName == null)      { m_errors.add("  - ошибка поля userName файла конфигурации"); }
        if (m_serverURL == null)     { m_errors.add("  - ошибка поля serverURL файла конфигурации"); }
        if (m_outDirName == null)    { m_errors.add("  - ошибка поля outDirName файла конфигурации"); }
        if (m_linksFileName == null) { m_errors.add("  - ошибка поля linksFileName файла конфигурации"); }

        if (m_errors.size() > 0) {
            return ARGS_ERROR;
        }

        if (m_args.length == 1) {
            if (m_args[0].equalsIgnoreCase("?") || m_args[0].equalsIgnoreCase("help")) {
                return ARGS_HELP;
            }
        }

        File file = new File(m_linksFileName);
        if (!file.isFile()) {
            m_errors.add("  - указанного файла со списком ссылок не существует");
            res = ARGS_ERROR;
        }

        return res;
    }

    /**
     * Метод вывода справки по программе
     *
     * @param help - определяет формат выводимого текста:
     *             true - вывод формата командной строки запуска программы;
     *             false - вывод списка управляющих ключей
     */
    public void printHelp(boolean help) {
        if (help) {
            ProgGUI.dataOut("\nФормат командной строки:\n" +
                    "java -jar floader.jar <thread_num> <output_folder> <links_file>\n" +
                    "где:  thread_num    - максимальное количество запускаемых потоков;\n" +
                    "      output_folder - каталог для сохранения файлов;\n" +
                    "      links_file    - текстовый файл, содержащий ссылки на скачиваемые файлы.");
        } else {
            ProgGUI.dataOut("\nДля вывода помощи запустите программу с параметром ? или help\n" +
                    "Например:    java -jar floader.jar ?\n" +
                    "или          java -jar floader.jar help");
        }
    }
}
