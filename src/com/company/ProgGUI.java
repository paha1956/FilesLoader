package com.company;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class ProgGUI extends JFrame {

    private static final int UNITS_X_POS = 10;
    private static final int UNITS_dY = 55;
    private static final int UNITS_Y_OFFSET = 10;

    private final String REFERENCE_TEXT = "Текст1\nТекст2\nТекст3\nТекст4\nТекст5\nТекст6\nТекст7\nТекст8\nТекст9\n";
    private final String ABOUT_TEXT = "Текст1\nТекст2\nТекст3\nТекст4\nТекст5\nТекст6\nТекст7\nТекст8\nТекст9\n";

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

    private HashMap<Integer, ProgressBarData> m_barList;
    private ThreadsManager m_threadsManager;
    static private JTextArea m_console;

    public ProgGUI(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("Файл");
        JMenu setupMenu = new JMenu("Настройки");
        JMenu runMenu = new JMenu("Запуск");
        JMenu helpMenu = new JMenu("Помощь");

        runMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                int runningThreads = m_threadsManager.threadsStart();
                ProgGUI.dataOut("Запущено потоков: " + runningThreads + "\nНачало загрузки файлов...");
            }

            @Override
            public void menuDeselected(MenuEvent menuEvent) { }

            @Override
            public void menuCanceled(MenuEvent menuEvent) { }
        });

        menuBar.add(fileMenu);
        menuBar.add(setupMenu);
        menuBar.add(runMenu);
        menuBar.add(helpMenu);

        JMenuItem linksItem = new JMenuItem("Ссылки");
        fileMenu.add(linksItem);
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Выход");
        fileMenu.add(exitItem);

        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
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
        JTextArea referenceText = new JTextArea();
        referenceText.setText(text);
        referenceFrame.add(referenceText);
        referenceFrame.pack();
        referenceFrame.setLocationRelativeTo(null);
        referenceFrame.setResizable(false);
        referenceFrame.setVisible(true);
    }

    static public void dataOut(String outData) {
        System.out.println(outData);
        m_console.append(outData + "\n");
        m_console.setCaretPosition(m_console.getText().length());
    }
}
