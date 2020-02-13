/**
 * Утилита параллельной загрузки файлов по HTTP протоколу.
 *
 * @version 0.1
 * @autor Федоров Павел, гр. 124/21 ИТМО 25.11.2019
 */

package com.company;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.company.ArgsParser.*;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException{

        ProgGUI.setDefaultLookAndFeelDecorated(true);

        ArgsParser argsParser = new ArgsParser(args);
        ProgGUI progGUI = new ProgGUI("Файловый загрузчик", argsParser);

        switch (argsParser.getArgsProperty()) {
            case ARGS_OK: {
                ProgGUI.dataOut("Установленное количество потоков: " + argsParser.getNumThreads());
                ProgGUI.dataOut("Каталог закачки: " + argsParser.getOutDirName());
                ProgGUI.dataOut("Файл со списком закачки: " + argsParser.getLinksFileName());

                ThreadsManager threadsManager = new ThreadsManager(argsParser, progGUI);
                progGUI.attach(threadsManager);

                threadsManager.threadsMonitoring();
                break;
            }

            case ARGS_ERROR: {
                ProgGUI.dataOut("Ошибка параметров:");
                ArrayList<String> errorList = argsParser.getErrors();
                for (String error : errorList) {
                    ProgGUI.dataOut(error);
                }
                argsParser.printHelp(true);
                break;
            }

            default:
            case ARGS_HELP: {
                argsParser.printHelp(false);
                break;
            }
        }

    }
}
