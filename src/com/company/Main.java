/**
 * Утилита параллельной загрузки файлов по HTTP протоколу.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */

package com.company;

import java.util.ArrayList;

import static com.company.ArgsParser.ARGS_OK;
import static com.company.ArgsParser.ARGS_ERROR;
import static com.company.ArgsParser.ARGS_HELP;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        ArgsParser argsParser = new ArgsParser(args);

        switch (argsParser.getArgsProperty()) {
            case ARGS_OK: {
                System.out.println("Установленное количество потоков: " + argsParser.getNumThreads());
                System.out.println("Каталог закачки: " + argsParser.getOutDirName());
                System.out.println("Файл со списком закачки: " + argsParser.getLinksFileName());

                ThreadsManager threadsManager = new ThreadsManager(argsParser);
                int runingThreads = threadsManager.threadsStart();
                System.out.println("Запущено потоков: " + runingThreads + "\nНачало загрузки файлов...");

                threadsManager.threadsMonitoring();
                break;
            }

            case ARGS_ERROR: {
                System.out.println("Ошибка параметров:");
                ArrayList<String> errorList = argsParser.getErrors();
                for (String error : errorList) {
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
