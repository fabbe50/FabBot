package com.fabbe50.teemobeats.handlers;

import com.fabbe50.teemobeats.Main;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Created by fabbe on 13/05/2018 - 4:18 PM.
 */
public class Console extends WindowAdapter implements WindowListener, ActionListener, Runnable {
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    JScrollBar vertical;
    private Thread reader;
    private Thread reader2;
    private boolean quit;

    Highlighter highlighter;
    Highlighter.HighlightPainter painter;

    private final PipedInputStream pin = new PipedInputStream();
    private final PipedInputStream pin2 = new PipedInputStream();

    public Console() {
        frame = new JFrame("Fab Bot " + Main.getVersion());
        frame.getContentPane().setBackground(new Color(35, 39, 42));
        frame.setMinimumSize(new Dimension(400, 300));

        JPanel content = new JPanel();
        content.setPreferredSize(new Dimension(1000, 800));
        content.setBackground(new Color(35, 39, 42));
        content.setLayout(new BorderLayout());

        Border lineBorder = BorderFactory.createLineBorder(new Color(35, 39, 42));

        //Console viewport.
        textArea = new JTextArea();
        textArea.setBackground(new Color(44, 47, 51));
        textArea.setForeground(Color.WHITE);
        textArea.setBorder(null);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(lineBorder);
        scrollPane.setBackground(new Color(44, 47, 51));
        scrollPane.setForeground(new Color(153, 170, 181));
        vertical = scrollPane.getVerticalScrollBar();
        scrollPane.setPreferredSize(new Dimension(400, 900));
        scrollPane.setMinimumSize(new Dimension(300, 300));
        scrollPane.setMaximumSize(new Dimension(4000, 4000));
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());

        //Console Input
        textField = new JTextField();
        textField.setBackground(new Color(44, 47, 51));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setBorder(lineBorder);
        textField.addActionListener(e -> {
            textArea.append(textField.getText() + "\n");
            ConsoleHandler.readConsoleCommand(textField.getText());
            textField.setText("");
            if (vertical.getValue() < vertical.getMaximum())
                vertical.setValue( vertical.getMaximum() );
        });
        textField.setPreferredSize(new Dimension(400, 30));
        textField.setMinimumSize(new Dimension(300, 30));
        textField.setMaximumSize(new Dimension(4000, 30));
        bottom.add(textField, BorderLayout.NORTH);

        JPanel panelButtons = new JPanel(new GridLayout(1, 2));
        panelButtons.setPreferredSize(new Dimension(300, 30));

        //Buttons
        JButton clearButton = new JButton("clear");
        clearButton.setBackground(new Color(44, 47, 51));
        clearButton.setForeground(Color.WHITE);
        clearButton.setBorder(lineBorder);
        panelButtons.add(clearButton);

        JButton quitButton = new JButton("quit");
        quitButton.setBackground(new Color(44, 47, 51));
        quitButton.setForeground(Color.WHITE);
        quitButton.setBorder(lineBorder);
        panelButtons.add(quitButton);

        bottom.add(panelButtons, BorderLayout.SOUTH);
        content.add(bottom, BorderLayout.SOUTH);

        frame.add(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(this);
        clearButton.addActionListener(this);
        quitButton.addActionListener(e -> System.exit(0));

        try {
            PipedOutputStream pout = new PipedOutputStream(this.pin);
            System.setOut(new PrintStream(pout, true));
        } catch (IOException | SecurityException io) {
            textArea.append("Couldn't redirect STDOUT to this console\n" + io.getMessage());
        }

        try {
            PipedOutputStream pout2 = new PipedOutputStream(this.pin2);
            System.setErr(new PrintStream(pout2, true));
        } catch (IOException | SecurityException io) {
            textArea.append("Couldn't redirect STDERR to this console\n" + io.getMessage());
        }

        quit = false;

        reader = new Thread(this);
        reader.setDaemon(true);
        reader.start();
        reader2 = new Thread(this);
        reader2.setDaemon(true);
        reader2.start();
    }

    public synchronized void windowClosed(WindowEvent evt) {
        quit = true;
        this.notifyAll();
        try {
            reader.join(1000);
            pin.close();
        } catch (Exception ignored) {
        }
        try {
            reader2.join(1000);
            pin2.close();
        } catch (Exception ignored) {
        }
        System.exit(0);
    }

    public synchronized void windowClosing(WindowEvent evt) {
        frame.setVisible(false);
        frame.dispose();
    }

    public synchronized void actionPerformed(ActionEvent evt) {
        textArea.setText("");
    }

    public void addMessageToConsole(String name, String channel, String guild, String text, Color color) throws BadLocationException {
        highlighter = textArea.getHighlighter();
        painter = new DefaultHighlighter.DefaultHighlightPainter(color);
        String string = "[DiscordMessage]: {'" + name + "' in channel '" + channel + "' in guild '" + guild + "'}: " + text;
        textArea.append(string);
        textArea.append("\n");
        highlighter.addHighlight(textArea.getText().length() - string.length() + 19, textArea.getText().length() - string.length() + 19 + name.length(), painter);
        if (vertical.getValue() < vertical.getMaximum())
            vertical.setValue( vertical.getMaximum() );
    }

    public synchronized void run() {
        try {
            while (Thread.currentThread() == reader) {
                try {
                    this.wait(100);
                } catch (InterruptedException ignored) {
                }
                if (pin.available() != 0) {
                    String input = this.readLine(pin);
                    textArea.append(input);
                    if (vertical.getValue() < vertical.getMaximum())
                        vertical.setValue( vertical.getMaximum() );
                }
                if (quit) return;
            }
            while (Thread.currentThread() == reader2) {
                try {
                    this.wait(100);
                } catch (InterruptedException ignored) {
                }
                if (pin2.available() != 0) {
                    String input = this.readLine(pin2);
                    textArea.append(input);
                    if (vertical.getValue() < vertical.getMaximum())
                        vertical.setValue( vertical.getMaximum() );
                }
                if (quit) return;
            }
        } catch (Exception e) {
            textArea.append("\nConsole reports an Internal error.");
            textArea.append("The error is: " + e);
            if (vertical.getValue() < vertical.getMaximum())
                vertical.setValue( vertical.getMaximum() );
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private synchronized String readLine(PipedInputStream in) throws IOException {
        StringBuilder input = new StringBuilder();
        do {
            int available = in.available();
            if (available == 0) break;
            byte b[] = new byte[available];
            in.read(b);
            input.append(new String(b, 0, b.length));
        }
        while (!input.toString().endsWith("\n") && !input.toString().endsWith("\r\n") && !quit);
        return input.toString();
    }
}