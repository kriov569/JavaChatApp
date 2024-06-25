import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    // Хранит соединения с клиентами по их идентификаторам
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        // Создаем серверный сокет, прослушивающий порт 6000
        ServerSocket serverSocket = new ServerSocket(6000);
        System.out.println("Server started. Waiting for clients...");

        while (true) {
            // Принимаем входящее соединение от клиента
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket);

            // Создаем новый поток для обработки клиента и добавляем его в карту клиентов
            ClientHandler clientThread = new ClientHandler(clientSocket, clients);
            clients.put(clientThread.getClientId(), clientThread);
            new Thread(clientThread).start(); // Запускаем поток для клиента
        }
    }

    // Отправляет сообщение конкретному клиенту по его идентификатору
    public static void sendMessageToClient(String clientId, String message) {
        ClientHandler client = clients.get(clientId);
        if (client != null) {
            client.sendMessage(message);
        } else {
            System.out.println("Client " + clientId + " not found.");
        }
    }
}

// Обработчик клиента, реализует интерфейс Runnable для многопоточности
class ClientHandler implements Runnable {
    private Socket clientSocket; // Сокет клиента
    private Map<String, ClientHandler> clients; // Ссылка на карту клиентов на сервере
    private PrintWriter out; // Поток вывода для отправки сообщений клиенту
    private BufferedReader in; // Поток ввода для чтения сообщений от клиента
    private String clientId; // Уникальный идентификатор клиента

    // Конструктор ClientHandler инициализирует потоки ввода-вывода для клиента
    public ClientHandler(Socket socket, Map<String, ClientHandler> clients) throws IOException {
        this.clientSocket = socket;
        this.clients = clients;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true); // Поток вывода
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Поток ввода
        this.clientId = UUID.randomUUID().toString(); // Генерация уникального идентификатора клиента
    }

    // Возвращает уникальный идентификатор клиента
    public String getClientId() {
        return clientId;
    }

    // Отправляет сообщение клиенту через поток вывода
    public void sendMessage(String message) {
        out.println(message);
    }

    // Основная логика обработки клиента в методе run
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Обработка приватных сообщений
                if (inputLine.startsWith("/private")) {
                    String[] parts = inputLine.split(" ", 3);
                    String recipient = parts[1];
                    String message = parts[2];
                    ChatServer.sendMessageToClient(recipient, message); // Отправка приватного сообщения
                } else {
                    // Отправка сообщения всем клиентам
                    for (ClientHandler aClient : clients.values()) {
                        aClient.sendMessage(inputLine);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            try {
                // Закрытие потоков и сокета при завершении работы с клиентом
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
