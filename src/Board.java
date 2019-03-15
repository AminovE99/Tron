public class Board{
	//Attributes
	private int[][] board;
	private Car player1, player2;
	public static final int ARENA_SIZE = 50;
	
	//Constructor
	public Board() {
		board = new int[ARENA_SIZE][ARENA_SIZE];
		player1 = new Car(1);
		player2 = new Car(2);
		reset();
	}
	
	//Get & Set methods
	public int[][] getArray() {return board;}
	public Car getPlayer(int player) {
		if(player == 1)
			return player1;
		else
			return player2;
	}
	public int getBoard() {return board.length;}
	
	//Reset method
	public void reset() {
		for(int i = 0; i<board.length; i++) {
			for(int j = 0; j<board[i].length; j++) {
				board[i][j] = 0;
			}
		}
	}
	//Update method
	public void update() {
		for(int i = 0; i<board.length; i++) {
			for(int j = 0; j<board[i].length; j++) {
				if (player1.getCol() == j && player1.getRow() == i) {
					board[i][j] = 1;
				}
			}
		}
	}
}