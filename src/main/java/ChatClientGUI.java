import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClientGUI extends JFrame {
    private JTextArea messageArea; // Область для отображения сообщений
    private JTextField textField; // Поле для ввода текста
    private JButton exitButton; // Кнопка для выхода из чата
    private ChatClient client; // Клиентское соединение

    // Конструктор создает GUI чата
    public ChatClientGUI() {
        super("Chat Application");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Color backgroundColor = new Color(240, 240, 240); // Цвет фона
        Color buttonColor = new Color(75, 75, 75); // Цвет кнопок
        Color textColor = new Color(50, 50, 50); // Цвет текста
        Font textFont = new Font("Arial", Font.PLAIN, 14); // Шрифт текста
        Font buttonFont = new Font("Arial", Font.BOLD, 12); // Шрифт кнопок

        // Настройка области для отображения сообщений
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setBackground(backgroundColor);
        messageArea.setForeground(textColor);
        messageArea.setFont(textFont);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);

        // Диалоговое окно для ввода имени пользователя
        String name = JOptionPane.showInputDialog(this, "Enter your name:", "Name Entry", JOptionPane.PLAIN_MESSAGE);
        this.setTitle("Chat Application - " + name);

        // Поле ввода текста для отправки сообщений
        textField = new JTextField();
        textField.setFont(textFont);
        textField.setForeground(textColor);
        textField.setBackground(backgroundColor);
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Формирование сообщения и его отправка при нажатии Enter
                String message = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + name + ": "
                        + textField.getText();
                client.sendMessage(message); // Отправка сообщения серверу
                textField.setText(""); // Очистка поля ввода
            }
        });

        // Кнопка для выхода из чата
        exitButton = new JButton("Exit");
        exitButton.setFont(buttonFont);
        exitButton.setBackground(buttonColor);
        exitButton.setForeground(Color.BLACK);
        exitButton.addActionListener(e -> {
            // Отправка сообщения о выходе и завершение программы
            String departureMessage = name + " has left the chat.";
            client.sendMessage(departureMessage);
            try {
                Thread.sleep(1000); // Задержка перед выходом
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            System.exit(0); // Завершение приложения
        });

        // Нижняя панель с полем ввода и кнопкой выхода
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(exitButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        try {
            this.client = new ChatClient("127.0.0.1", 6000, "client1"); // здесь "client1" это идентификатор клиента
            client.startClient(this::onMessageReceived); // передаем Consumer<String> для обработки сообщений
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the server", "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI().setVisible(true);
        });
    }
}