package com.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;

public class ProgGUI extends JFrame {

    private static final int UNITS_X_POS = 10;
    private static final int UNITS_dY = 55;
    private static final int UNITS_Y_OFFSET = 10;
    private static final String SAVE_PRESSED = "Save";
    private static final String CANCEL_PRESSED = "Cancel";

    private final String REFERENCE_TEXT = "<html><b><u>Настройки программы.</b></u><b><br><br>" +
            "Путь к файлу coonfig.json:</b><br>" + ArgsParser.CONFIG_URL +
            "<b><br><br>Формат файла config.json:</b>" +
            "<br>{<br>\"\"userName\": \"имя пользователя\",<br>" +
            "\"serverURL\": \"http://URL сервера:8080\",<br>" +
            "\"outDirName\": \"путь к выходной директории\",<br>" +
            "\"linksFileName\": \"путь к файлу со списком загрузки\"<br>" +
            "}</html>";
    private final String ABOUT_TEXT = "<html><b><u>Файловый загрузчик<br><br></u></b>" +
            "Кого ругать: Фёдоров Павел<br>" +
            "Где ругать: ИТМО, группа 124/21<br>" +
            "Когда ругать: 2020 г.</html>";

    class ProgressBarData {
        private JLabel m_label;
        private JProgressBar m_progressBar;

        public ProgressBarData(JLabel label, JProgressBar progressBar) {
            this.m_label = label;
            this.m_progressBar = progressBar;
        }

        public void setTextValue(String text, int value) {
            m_label.setText(text);
            m_progressBar.setValue(value);
        }
    }

    class LinksLoaderActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getActionCommand().equalsIgnoreCase("Save")) {
                try {
                    OutputStream outputStream = new FileOutputStream(m_argsParser.getLinksFileName());
                    outputStream.write(m_setLinksTextArea.getText().getBytes(), 0, m_setLinksTextArea.getText().length());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            m_setLinksFrame.dispose();
        }
    }

    class ConfigActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getActionCommand().equalsIgnoreCase("Save")) {
                ConfigList configRecord = new ConfigList();
                configRecord.setUserName(m_userNameField.getText());
                configRecord.setServerURL(m_serverURLField.getText());
                configRecord.setOutDirName(m_outDirNameField.getText());
                configRecord.setLinksFileName(m_linksFileNameField.getText());

                Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
                String JSON = gsonBuilder.toJson(configRecord);

                try {
                    OutputStream outputStream = new FileOutputStream("F:\\Share\\config.json");
                    String data = JSON;
                    outputStream.write(data.getBytes(), 0, data.length());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            m_configFrame.dispose();
        }
    }

    private HashMap<Integer, ProgressBarData> m_barList;
    private ArgsParser m_argsParser;
    private ThreadsManager m_threadsManager;
    static private JTextArea m_console;
    private JFrame m_setLinksFrame;
    private JFrame m_configFrame;
    private JTextArea m_setLinksTextArea;
    private JMenuItem m_startItem;

    private JTextField m_userNameField;
    private JTextField m_serverURLField;
    private JTextField m_outDirNameField;
    private JTextField m_linksFileNameField;

    public ProgGUI(String title, ArgsParser argsParser) {
        super(title);
        m_argsParser = argsParser;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        m_setLinksFrame = new JFrame(title);
        m_configFrame = new JFrame(title);

        m_userNameField = new JTextField();
        m_serverURLField = new JTextField();
        m_outDirNameField = new JTextField();
        m_linksFileNameField = new JTextField();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("Файл");
        JMenu toolsMenu = new JMenu("Инструменты");
        JMenu helpMenu = new JMenu("Помощь");
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        JMenuItem linksItem = new JMenuItem("Ссылки");
        fileMenu.add(linksItem);
        linksItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                linksLoadFrame(800, 400, "Загружаемые ссылки");
            }
        });

        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Выход");
        fileMenu.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        m_startItem = new JMenuItem("Запуск");

        toolsMenu.add(m_startItem);
        m_startItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int runningThreads = m_threadsManager.threadsStart();
                ProgGUI.dataOut("Запущено потоков: " + runningThreads + "\nНачало загрузки файлов...");
                m_startItem.setEnabled(false);
             }
        });

        JMenuItem configItem = new JMenuItem("Настройки");
        toolsMenu.add(configItem);
        configItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                configFrame(700, 200, "Настройки программы");
            }
        });

        JMenuItem referenceItem = new JMenuItem("Вывод справки");
        helpMenu.add(referenceItem);
        referenceItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                serviceFrame(500, 400, "Справочная информация", REFERENCE_TEXT);
            }
        });

        JMenuItem aboutItem = new JMenuItem("О программе");
        helpMenu.add(aboutItem);
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                serviceFrame(400, 200, "О программе", ABOUT_TEXT);
            }
        });

        m_console = new JTextArea();
        m_console.setLineWrap(true);
        m_console.setWrapStyleWord(true);
        m_console.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(m_console);
        scrollPane.setBounds(UNITS_X_POS, 400, 570, 130);
        add(scrollPane);

        setLayout(null);
        m_barList = new HashMap<>();

        setPreferredSize(new Dimension(600, 600));
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void createProgressBar(int id) {
        if (id <= 0) return;

        JLabel label = new JLabel();
        label.setLocation(UNITS_X_POS, UNITS_dY * (id - 1) + UNITS_Y_OFFSET);
        label.setSize(new Dimension(570, 20));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setLocation(UNITS_X_POS, UNITS_dY * (id - 1) + UNITS_Y_OFFSET + 25);
        progressBar.setSize(570, 20);
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(1);

        ProgressBarData progressBarData = new ProgressBarData(label, progressBar);
        m_barList.put(id, progressBarData);

        add(label);
        add(progressBar);

        setVisible(true);
    }

    public void attach(ThreadsManager threadsManager) {
        m_threadsManager = threadsManager;
    }

    public void progressBarOut(int id, String text, int value) {
        if (!m_barList.containsKey(id)) {
            createProgressBar(id);
        }

        ProgressBarData progressBarData = m_barList.get(id);
        progressBarData.setTextValue(text, value);

        setVisible(true);
    }

    private void serviceFrame(int width, int height, String title, String text) {
        JFrame referenceFrame = new JFrame(title);
        referenceFrame.setPreferredSize(new Dimension(width, height));
        JTextPane referenceText = new JTextPane();
        referenceText.setContentType("text/html");
        referenceText.setText(text);
        referenceText.setEditable(false);
        referenceFrame.add(referenceText);
        referenceFrame.pack();
        referenceFrame.setLocationRelativeTo(null);
        referenceFrame.setResizable(false);
        referenceFrame.setVisible(true);
    }

    private void linksLoadFrame(int width, int height, String title) {
        if (width < 300) {
            width = 300;
        }
        if (height < 90) {
            height = 90;
        }
        m_setLinksFrame.setPreferredSize(new Dimension(width, height));

        m_setLinksTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(m_setLinksTextArea);
        scrollPane.setBounds(5, 5, width - 20, height - 90);

        BufferedReader fileReader;
        String line, links = "";
        try {
            fileReader = new BufferedReader(new FileReader(m_argsParser.getLinksFileName()));
            do {
                line = fileReader.readLine();
                if (line != null) {
                    links += line + "\n";
                }
            }
            while (line != null);
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_setLinksTextArea.setText(links);
        m_setLinksFrame.add(scrollPane);

        ActionListener buttonsActionListener = new LinksLoaderActionListener();
        postfixFrame(m_setLinksFrame, width, height, buttonsActionListener);
    }

    private void configFrame(int width, int height, String title) {
        if (width < 300) {
            width = 300;
        }
        if (height < 90) {
            height = 90;
        }
        m_configFrame.setPreferredSize(new Dimension(width, height));

        JLabel userNameLabel = new JLabel("Имя пользователя:");
        JLabel serverURLLabel = new JLabel("URL сервера:");
        JLabel outDirNameLabel = new JLabel("Выходной директорий:");
        JLabel linksFileNameLabel = new JLabel("Файл со списком ссылок:");

        Gson jsonConfig = new Gson();
        BufferedReader fileReader;
        String configData = "";
        try {
            fileReader = new BufferedReader(new FileReader("F:\\Share\\config.json"));
            String line;
            do {
                line = fileReader.readLine();
                if (line != null) {
                    configData += line;
                }
            }
            while (line != null);

            ConfigList config = jsonConfig.fromJson(configData, ConfigList.class);
            m_userNameField.setText(config.getUserName());
            m_serverURLField.setText(config.getServerURL());
            m_outDirNameField.setText(config.getOutDirName());
            m_linksFileNameField.setText(config.getLinksFileName());
        } catch (Exception e) {
            m_userNameField.setText("");
            m_serverURLField.setText("");
            m_outDirNameField.setText("");
            m_linksFileNameField.setText("");
        }

        userNameLabel.setBounds(UNITS_X_POS, UNITS_Y_OFFSET, 200, 20);
        serverURLLabel.setBounds(UNITS_X_POS, UNITS_Y_OFFSET * 3, 200, 20);
        outDirNameLabel.setBounds(UNITS_X_POS, UNITS_Y_OFFSET * 5, 200, 20);
        linksFileNameLabel.setBounds(UNITS_X_POS, UNITS_Y_OFFSET * 7, 200, 20);

        m_userNameField.setBounds(UNITS_X_POS + 200, UNITS_Y_OFFSET, 470, 20);
        m_serverURLField.setBounds(UNITS_X_POS + 200, UNITS_Y_OFFSET * 3, 470, 20);
        m_outDirNameField.setBounds(UNITS_X_POS + 200, UNITS_Y_OFFSET * 5, 470, 20);
        m_linksFileNameField.setBounds(UNITS_X_POS + 200, UNITS_Y_OFFSET * 7, 470, 20);

        m_configFrame.add(userNameLabel);
        m_configFrame.add(serverURLLabel);
        m_configFrame.add(outDirNameLabel);
        m_configFrame.add(linksFileNameLabel);
        m_configFrame.add(m_userNameField);
        m_configFrame.add(m_serverURLField);
        m_configFrame.add(m_outDirNameField);
        m_configFrame.add(m_linksFileNameField);

        ActionListener buttonsActionListener = new ConfigActionListener();
        postfixFrame(m_configFrame, width, height, buttonsActionListener);
    }

    private void postfixFrame(JFrame frame, int width, int height, ActionListener buttonsActionListener) {
        JButton buttonSave = new JButton();
        buttonSave.setText("Сохранить");
        buttonSave.setBounds(width - 250, height - 80, 100, 25);

        JButton buttonCancel = new JButton();
        buttonCancel.setText("Отмена");
        buttonCancel.setBounds(width - 115, height - 80, 100, 25);

        buttonSave.setActionCommand(SAVE_PRESSED);
        buttonSave.addActionListener(buttonsActionListener);
        buttonCancel.setActionCommand(CANCEL_PRESSED);
        buttonCancel.addActionListener(buttonsActionListener);

        frame.setLayout(null);
        frame.add(buttonSave);
        frame.add(buttonCancel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public void enableStart() {
        if (m_startItem != null) {
            m_startItem.setEnabled(true);
        }
    }

    static public void dataOut(String outData) {
        System.out.println(outData);
        m_console.append(outData + "\n");
        m_console.setCaretPosition(m_console.getText().length());
    }
}
