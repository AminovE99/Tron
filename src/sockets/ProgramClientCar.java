package sockets;


import java.util.Scanner;

/**
 * 12.02.2019
 * ProgramClientCar
 *
 * @author Sidikov Marsel (First Software Engineering Platform)
 * @version v1.0
 */
public class ProgramClientCar {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SocketClient client = new SocketClient();
        client.startConnection("127.0.0.1", 6666);
        while (true) {
            String message = scanner.nextLine();
            //client.sendData(message);
        }
    }
}
