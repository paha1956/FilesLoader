package com.company;

import java.util.ArrayList;

import static com.company.ArgsParser.ARGS_OK;
import static com.company.ArgsParser.ARGS_ERROR;
import static com.company.ArgsParser.ARGS_HELP;

public class Main {

    public static void main(String[] args) {
        // write your code here

        ArgsParser argsParser = new ArgsParser(args);

        switch (argsParser.getArgsProperty()) {
            case ARGS_OK: {
                System.out.println("Количество потоков: " + argsParser.getNumThreads());
                System.out.println("Каталог закачки: " + argsParser.getOutDirName());
                System.out.println("Файл со списком закачки: " + argsParser.getLinksFileName());

                System.out.println("Загрузка файлов...");
                ThreadsManager threadsManager = new ThreadsManager(argsParser);

                int runingThreads = threadsManager.threadsStart();
                System.out.println("Запущено "+ runingThreads + " потоков");

                threadsManager.threadsManage();

                break;
            }

            case ARGS_ERROR: {
                System.out.println("Ошибка параметров:");
                ArrayList <String> errorList = new ArrayList <> ();
                errorList = argsParser.getErrors();
                for (String error: errorList) {
                    System.out.println(error);
                }
                argsParser.printHelp(false);
                break;
            }

            default:
            case ARGS_HELP: {
                argsParser.printHelp(true);
                break;
            }
        }


    }
}
