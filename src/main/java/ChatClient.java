import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket; // Сокет клиента
    private BufferedReader in; // Поток ввода для чтения сообщений от сервера
    private PrintWriter out; // Поток вывода для отправки сообщений серверу
    private String clientId; // Уникальный идентификатор клиента

    // Конструктор ChatClient инициализирует соединение с сервером
    public ChatClient(String serverAddress, int serverPort, String clientId) throws IOException {
        this.socket = new Socket(serverAddress, serverPort); // Подключение к серверу
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Поток ввода
        this.out = new PrintWriter(socket.getOutputStream(), true); // Поток вывода
        this.clientId = clientId;
        out.println(clientId); // Отправка идентификатора клиента на сервер
    }

    // Метод отправки сообщения серверу
    public void sendMessage(String msg) {
        out.println(msg);
    }

    // Запускает поток для приема сообщений от сервера
    public void startClient(Consumer<String> onMessageReceived) {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onMessageReceived.accept(line); // Передача полученного сообщения обработчику
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
