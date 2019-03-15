package sockets;

import javafx.scene.shape.Line;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class SocketClient {
    // поле, содержащее сокет-клиента
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private double[] respCoord = new double[4];
    ArrayList<Line> linesP1;
    ArrayList<Line> linesP2;
    Line tail1, tail2;


    // начало сессии - получаем ip-сервера и его порт
    public void startConnection(String ip, int port) {
        try {
            linesP1 = new ArrayList<Line>();
            linesP2 = new ArrayList<Line>();
            // создаем подключение
            clientSocket = new Socket(ip, port);
            // получили выходной поток
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            // входной поток
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // запустили слушателя сообщений
            new Thread(receiverMessagesTask).start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void sendData2(double p1x, double p1y, double p2x, double p2y) {
        out.println("p1x:" + p1x + ";" + "p1y:" + p1y + ";" + "p2x:" + p2x + ";" + "p2y:" + p2y + ";");
    }


    private Runnable receiverMessagesTask = new Runnable() {
        @Override
        public void run() {
            String[] str = new String[5];
            while (true) {
                try {
                    String response = in.readLine();
                    if (response != null) {
                        if (response.charAt(0) != 'p') {
                            str = response.split(",");
                            respCoord[0] = Double.parseDouble(str[0]);
                            respCoord[1] = Double.parseDouble(str[1]);
                            respCoord[2] = Double.parseDouble(str[2]);
                            respCoord[3] = Double.parseDouble(str[3]);
                            if (str[4].equals("1")) {
                                tail1 = new Line();
                                tail1.setStartX(respCoord[0]);
                                tail1.setStartY(respCoord[1]);
                                tail1.setEndX(respCoord[2]);
                                tail1.setEndY(respCoord[3]);
                                linesP1.add(tail1);
                            } else {
                                tail2 = new Line();
                                tail2.setStartX(respCoord[0]);
                                tail2.setStartY(respCoord[1]);
                                tail2.setEndX(respCoord[2]);
                                tail2.setEndY(respCoord[3]);
                                linesP2.add(tail2);
                            }
                        } else {
                            str = response.split(";");
                            respCoord[0] = Double.parseDouble(str[0].split(":")[1]);
                            respCoord[1] = Double.parseDouble(str[1].split(":")[1]);
                            respCoord[2] = Double.parseDouble(str[2].split(":")[1]);
                            respCoord[3] = Double.parseDouble(str[3].split(":")[1]);

                        }
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    };

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public synchronized ArrayList<Line> getData1() {
        System.out.println("Array Of 1 player " + linesP1);
        return linesP1;
    }

    public synchronized ArrayList<Line> getData2() {
        System.out.println("Array of 2 player "+ linesP2);
        return linesP2;
    }

    public void sendData(Line line, int number) {
        out.println(line.getStartX() + "," + line.getStartY() + "," + line.getEndX() + "," + line.getEndY() + "," + number);
    }


    public synchronized double[] getData() {
        return respCoord;
    }
}
