package com.company;

import java.io.File;
import java.util.ArrayList;

public class ArgsParser {

    public static final int ARGS_OK = 1;
    public static final int ARGS_ERROR = 2;
    public static final int ARGS_HELP = 3;

    private String m_args[];
    private ArrayList<String> m_errors;
    private int m_numThreads;
    private String m_outDirName;
    private String m_linksFileName;

    public ArgsParser(String[] args) {
        m_args = args;
        m_errors = new ArrayList<>();
        m_numThreads = 0;
        m_outDirName = "";
        m_linksFileName = "";

    }

    public ArrayList getErrors() {
        return m_errors;
    }

    public int getNumThreads() {
        return m_numThreads;
    }

    public String getOutDirName() {
        return m_outDirName;
    }

    public String getLinksFileName() {
        return m_linksFileName;
    }

    public int getArgsProperty() {
        int res = ARGS_OK;

        if (m_args.length == 0) {
            m_errors.add("  - параметры отсутствуют");
            return ARGS_ERROR;
        }

        if (m_args.length != 3) {
            if (m_args.length == 1) {
                if (m_args[0].equalsIgnoreCase("?") || m_args[0].equalsIgnoreCase("help"))
                    return ARGS_HELP;
            }

            m_errors.add("  - неправильное количество параметров");
            return ARGS_ERROR;
        }

        try {
            m_numThreads = Integer.parseInt(m_args[0]);
        } catch (NumberFormatException e) {
            m_errors.add("  - количество потоков неопределено");
            res = ARGS_ERROR;
        }

        if (m_numThreads <= 0) {
            m_errors.add("  - количество потоков должно быть больше нуля");
            res = ARGS_ERROR;
        }

        m_outDirName = m_args[1];

        File file = new File(m_args[2]);
        if (file.isFile())
            m_linksFileName = m_args[2];
        else {
            m_errors.add("  - указанного файла со списком ссылок не существует");
            res = ARGS_ERROR;
        }

        return res;
    }

    public void printHelp(boolean help) {
        if (help) {
            System.out.println("\nФормат командной строки:\n" +
                    "java -jar floader.jar <thread_num> <output_folder> <links_file>\n" +
                    "где:  thread_num    - максимальное количество запускаемых потоков;\n" +
                    "      output_folder - каталог для сохранения файлов;\n" +
                    "      links_file    - текстовый файл, содержащий ссылки на скачиваемые файлы.");
        } else {
            System.out.println("\nДля вывода помощи запустите программу с параметром ? или help\n" +
                    "Например:    java -jar floader.jar ?\n" +
                    "или          java -jar floader.jar help");
        }
    }
}
