import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import sockets.SocketClient;

public class TronDemo extends Application {
    //Creating the crash sound

    //progress bar
    Rectangle[] bar1 = new Rectangle[3];
    Group barPlayer1 = new Group();
    Rectangle[] bar2 = new Rectangle[3];
    Group barPlayer2 = new Group();
    SocketClient socketClient;


    //Builds the GUI
    @Override
    public void start(Stage stage) throws Exception {
        // Implements runnable
        // Подключась,  жду соперника.
        // Если соперник готов - игра начинается
        socketClient = new SocketClient();
        socketClient.startConnection("127.0.0.1", 6666);

        StackPane stack = new StackPane();

        GridPane board = new GridPane();
        Pane[][] grid = new Pane[25][25];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = new Pane();
                grid[i][j].setPrefSize(20, 20);
                grid[i][j].setStyle("-fx-background-color: black;");
                board.add(grid[i][j], i, j);
            }
        }

        stack.setStyle("-fx-background-color: #67C8FF;" + "-fx-border-color: grey;");
        stack.setPadding(new Insets(2));
        stack.setMinSize(board.getMinWidth(), board.getMinHeight());

        //styles progress bars (it has to come before GamePane initialization or else it crashes)
        int xOffset = 10;
        for (int i = 0; i < 3; i++) {

            Rectangle r1 = new Rectangle(xOffset, 15, 20, 5);
            r1.setFill(Color.web("#262626"));
            bar1[i] = r1;
            barPlayer1.getChildren().add(r1);

            Rectangle r2 = new Rectangle(xOffset, 15, 20, 5);
            r2.setFill(Color.web("#262626"));
            bar2[i] = r2;
            barPlayer2.getChildren().add(r2);

            xOffset += 22;
        }


        GamePane game = new GamePane();
        game.setMinSize(500, 500);

        stack.getChildren().addAll(board, game);

        BorderPane head = new BorderPane();
        InputStream is = new FileInputStream("src/TronLogo.jpg");
        Image logo = new Image(is, 200, 100, true, true);
        ImageView iv = new ImageView(logo);

        //adds text and progress bars to vboxes (left and right of Head)
        VBox player1info = new VBox(5);

        VBox player2info = new VBox(5);

        head.setStyle("-fx-background-color: rgba(0,0,0,1);");
        head.setLeft(player1info);
        head.setCenter(iv);
        head.setRight(player2info);
        head.setPadding(new Insets(20));

        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: blue;");
        pane.setTop(head);
        pane.setCenter(stack);

        Scene scene = new Scene(pane);
        scene.setOnKeyPressed(game::processKeyPress);

        stage.setTitle("Tron");
        stage.setScene(scene);
        stage.show();
    }


    public class GamePane extends Pane {
        //Attributes
        public ArrayList<Line> linesP1 = new ArrayList<Line>();
        public ArrayList<Line> linesP2 = new ArrayList<Line>();

        public boolean crash1 = false;
        public boolean crash2 = false;

        public int player1wins = 0;
        private int player2wins = 0;
        private String winner = "";

        private Board game;
        private Circle p1, p2;
        private double p1x, p1y, p2x, p2y;
        private Line tailP1, tailP2;

        private Timeline animation;
        private boolean isPaused = true;
        private boolean winReset = false;

        Label press = new Label("Press Space Bar to Start");

        Label winnerText = new Label(winner);

        //Constructor
        public GamePane() {


            winnerText.setFont(new Font("Arial", 20));

            game = new Board();

            press.setStyle("-fx-background-color: black;" + "-fx-border-color: cyan;" + "-fx-text-fill: white;");
            press.layoutXProperty().bind(this.widthProperty().subtract(press.widthProperty()).divide(2));
            press.layoutYProperty().bind(this.heightProperty().subtract(press.heightProperty()).divide(2));
            press.setPadding(new Insets(5));

            winnerText.setStyle("-fx-background-color: black;" + "-fx-border-color: white;" + "-fx-text-fill: white;");
            winnerText.layoutXProperty().bind(this.widthProperty().subtract(winnerText.widthProperty()).divide(2));
            winnerText.layoutYProperty().bind(this.heightProperty().subtract(winnerText.heightProperty()).divide(2));
            winnerText.setVisible(false);

            reset();

            animation = new Timeline(
                    new KeyFrame(Duration.millis(3), e -> movePlayer()));
            animation.setCycleCount(Timeline.INDEFINITE);
            animation.pause();
        }

        //Resumes game
        public void play() {
            animation.play();
            isPaused = false;
        }

        //Pauses game
        public void pause() {
            animation.pause();
            isPaused = true;
        }

        //The movement for the two players
        protected void movePlayer() {
            if (game.getPlayer(1).getDir().equals("l")) {
                p1x -= 1;
            } else if (game.getPlayer(1).getDir().equals("u")) {
                p1y -= 1;
            } else if (game.getPlayer(1).getDir().equals("r")) {
                p1x += 1;
            } else if (game.getPlayer(1).getDir().equals("d")) {
                p1y += 1;
            }

            if (game.getPlayer(2).getDir().equals("l")) {
                p2x -= 1;
            } else if (game.getPlayer(2).getDir().equals("u")) {
                p2y -= 1;
            } else if (game.getPlayer(2).getDir().equals("r")) {
                p2x += 1;
            } else if (game.getPlayer(2).getDir().equals("d")) {
                p2y += 1;
            }
            socketClient.sendData2(p1x, p1y, p2x, p2y);
            double[] coords = socketClient.getData();

            p1x = coords[0];
            p1y = coords[1];
            p2x = coords[2];
            p2y = coords[3];

            p1.setCenterX(p1x);
            p1.setCenterY(p1y);
            p2.setCenterX(p2x);
            p2.setCenterY(p2y);

            tailP1.setEndX(p1x);
            tailP1.setEndY(p1y);
            tailP2.setEndX(p2x);
            tailP2.setEndY(p2y);



            Point point1 = new Point(p1x, p1y);
            Point point2 = new Point(p2x, p2y);

            if (point1.getX() == point2.getX() && point1.getY() == point2.getY()) {
                tie();
            }
            //Checks for players crashing into each other or the borders of the arena
            crash1 = checkCrash1(point1);
            if (crash1) {
                crash(1);
            }
            crash2 = checkCrash1(point2);
            if (crash2) {
                crash(2);
            }

            crash2 = check(linesP2, tailP2);
            crash1 = check(linesP1, tailP1);

            if (crash1) {
                crash(1);
            }
            if (crash2) {
                crash(2);
            }
            crash1 = check2(linesP2, tailP1);
            crash2 = check2(linesP1, tailP2);
            if (crash1) {
                crash(1);
            }
            if (crash2) {
                crash(2);
            }
        }

        public void processKeyPress(KeyEvent e) {
            if (e.getCode() == KeyCode.SPACE && isPaused && !winReset) {
                play();
                press.setVisible(false);
                winnerText.setVisible(false);
            } else if (e.getCode() == KeyCode.SPACE && winReset) {
                pause();
                winnerText.setVisible(false);
                press.setVisible(true);

                winReset = false;

                player1wins = 0;
                player2wins = 0;

                for (int i = 0; i < 3; i++) {
                    bar1[i].setFill(Color.web("#262626"));
                    bar2[i].setFill(Color.web("#262626"));
                }
            } else if (e.getCode() == KeyCode.SPACE && !isPaused) {
                pause();
            } else if (e.getCode() == KeyCode.UP && !game.getPlayer(1).getDir().equals("d")) {
                game.getPlayer(1).setDir("u");
                socketClient.sendData(tailP1, 1);
                linesP1.add(tailP1);
                linesP1 = socketClient.getData1();
                tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                getChildren().add(tailP1);
            } else if (e.getCode() == KeyCode.LEFT && !game.getPlayer(1).getDir().equals("r")) {
                game.getPlayer(1).setDir("l");
                socketClient.sendData(tailP1, 1);
                linesP1.add(tailP1);
                linesP1 = socketClient.getData1();
                tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                getChildren().add(tailP1);
            } else if (e.getCode() == KeyCode.DOWN && !game.getPlayer(1).getDir().equals("u")) {
                game.getPlayer(1).setDir("d");
                socketClient.sendData(tailP1, 1);
                linesP1.add(tailP1);
                linesP1 = socketClient.getData1();
                tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                getChildren().add(tailP1);
            } else if (e.getCode() == KeyCode.RIGHT && !game.getPlayer(1).getDir().equals("l")) {
                game.getPlayer(1).setDir("r");
                socketClient.sendData(tailP1, 1);
                linesP1.add(tailP1);
                linesP1 = socketClient.getData1();
                tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                getChildren().add(tailP1);
            } else if (e.getCode() == KeyCode.W && !game.getPlayer(2).getDir().equals("d")) {
                game.getPlayer(2).setDir("u");
                socketClient.sendData(tailP2, 2);
                linesP2.add(tailP2);
                linesP2 = socketClient.getData2();
                tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                getChildren().add(tailP2);
            } else if (e.getCode() == KeyCode.A && !game.getPlayer(2).getDir().equals("r")) {
                game.getPlayer(2).setDir("l");
                socketClient.sendData(tailP2, 2);
                linesP2.add(tailP2);
                linesP2 = socketClient.getData2();
                tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                getChildren().add(tailP2);
            } else if (e.getCode() == KeyCode.S && !game.getPlayer(2).getDir().equals("u")) {
                game.getPlayer(2).setDir("d");
                socketClient.sendData(tailP2, 2);
                linesP2.add(tailP2);
                linesP2 = socketClient.getData2();
                tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                getChildren().add(tailP2);
            } else if (e.getCode() == KeyCode.D && !game.getPlayer(2).getDir().equals("l")) {
                game.getPlayer(2).setDir("r");
                linesP2.add(tailP2);
                tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                socketClient.sendData(tailP2, 2);
                getChildren().add(tailP2);
                linesP2 = socketClient.getData2();
            }
        }

        public Line tail(double sx, double sy, double ex, double ey, int player) {
            Line l = new Line(sx, sy, ex, ey);
            l.setStrokeWidth(4);
            l.setStrokeLineCap(StrokeLineCap.ROUND);
            if (player == 1)
                l.setStroke(Color.CYAN);
            else
                l.setStroke(Color.HOTPINK);

            return l;
        }

        //Tests for crashing
        public void crash(int i) {
            isPaused = true;
            animation.pause();
            if (i == 1) {
                winnerText.setText("PLAYER 2 WINS");
                bar2[player2wins].setFill(Color.HOTPINK);
                player2wins++;
                win();
            } else if (i == 2) {
                winnerText.setText("PLAYER 1 WINS");
                bar1[player1wins].setFill(Color.CYAN);
                player1wins++;
                win();
            }
        }

        //Determines if it is a tie or not
        public void tie() {
            winnerText.setText("TIE");
            animation.pause();
            isPaused = true;
            winnerText.setVisible(true);
            reset();
        }

        public void win() {
            p1x = 101.0;
            p1y = 260.0;
            p2x = 400.0;
            p2y = 260.0;
            if (player1wins == 3) {
                winReset = true;
                winnerText.setText("CONGRATULATIONS PLAYER 1! YOU WON!");
            } else if (player2wins == 3) {
                winReset = true;
                winnerText.setText("CONGRATULATIONS PLAYER 2! YOU WON!");
            }

            winnerText.setVisible(true);
            reset();
        }

        public void reset() {
            getChildren().clear();
            game.getPlayer(1).setDir("r");
            game.getPlayer(2).setDir("l");

            p1x = 100;
            p1y = 520 / 2;
            p2x = 400;
            p2y = 520 / 2;

            tailP1 = new Line(p1x, p1y, p1x, p1y);
            tailP2 = new Line(p2x, p2y, p2x, p2y);

            tailP1.setStrokeWidth(4);
            tailP1.setStroke(Color.CYAN);
            tailP1.setStrokeLineCap(StrokeLineCap.ROUND);
            tailP2.setStrokeWidth(4);
            tailP2.setStroke(Color.HOTPINK);
            tailP2.setStrokeLineCap(StrokeLineCap.ROUND);

            p1 = new Circle(p1x, p1y, 2);
            p2 = new Circle(p2x, p2y, 2);
            p1.setFill(Color.CYAN);
            p2.setFill(Color.HOTPINK);

            crash1 = false;
            crash2 = false;
            linesP1.clear();
            linesP2.clear();

            getChildren().addAll(p1, p2, tailP1, tailP2, press, winnerText);
        }

        //Crashes into self
        public boolean check(ArrayList<Line> list, Line l) {
            boolean crash = false;
            for (int i = 0; i < list.size() - 2; i++) {
                if (list.get(i).getBoundsInLocal().intersects(l.getBoundsInLocal())) {
                    crash = true;
                } else if (l.getBoundsInLocal().intersects(list.get(i).getBoundsInLocal())) {
                    crash = true;
                }
            }
            return crash;
        }

        //Crashes into other line
        public boolean check2(ArrayList<Line> list, Line l) {
            boolean crash = false;
            for (int i = 0; i < list.size(); i++) {
                if (l.getBoundsInLocal().intersects(list.get(i).getBoundsInLocal())) {
                    crash = true;
                } else if (list.get(i).getBoundsInLocal().intersects(l.getBoundsInLocal())) {
                    crash = true;
                }
            }
            return crash;
        }

        //Checks for out of bounds
        public boolean checkCrash1(Point p) {
            boolean crash = false;
            if (p.getX() < 0 || p.getX() > 500 || p.getY() < 0 || p.getY() > 500) {
                crash = true;
            }
            return crash;
        }
    }

    //Starts game
    public static void main(String[] args) {
        Application.launch(args);
    }
}