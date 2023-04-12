package Server;
import java.io.*;
import java.net.*;
import java.util.Random;

public class ServerThread extends Thread{
    private int threadId;
    private Socket toClientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private PrintWriter out;
    private BufferedReader in;
    private Random ranGen;
    private char[][] board;
    private int row, col;

    public ServerThread(Socket cli, int id) throws IOException {
        this.threadId = id;
        this.toClientSocket = cli;
        this.ranGen = new Random();
        this.inputStream = new DataInputStream(toClientSocket.getInputStream());
        this.outputStream = new DataOutputStream(toClientSocket.getOutputStream());
        this.out = new PrintWriter(outputStream, true);
        this.in = new BufferedReader(new InputStreamReader(inputStream));

        this.board = new char[4][4];
        for(int row = 0; row < board.length; row++){
            for(int col = 0; col < board[0].length; col++){
                board[row][col] = ' ';
            }
        }

        this.row = -1;
        this.col = -1;
    }

    public void run() {
        int moveCounter = 0;
        String response = "";
        boolean turn = false;
        boolean gameover = false;

        //randomly select the first turn
        if(ranGen.nextInt() % 2 != 0){
            turn = true;
        }
        else {
            turn = false;
        }

        //tell client the first move is to user
        if(turn == true){
            out.println("CLIENT");
        }

        while(!gameover){
            //User's turn
            if(turn){
                try{
                    response = in.readLine();
                }
                catch(IOException ex){
                    System.out.println("Read error in server thread: " + ex);
                    ex.printStackTrace();
                }

                String[] data = response.split("\\s+");
                int row = Integer.parseInt(data[1]);
                int col = Integer.parseInt(data[2]);

                board[row][col] = 'O';
                System.out.println("---- Thread " + threadId + " ----");
                System.out.println("---- User's turn ----");
                printBoard();
                moveCounter++;

                if(checkwin() || moveCounter == 16){
                    gameover = true;
                    if(checkwin()){
                        out.println("MOVE -1 -1 WIN");
                    }
                    else{
                        out.println("MOVE -1 -1 TIE");
                    }
                }
            }
            //Server's turn
            else{
                int[] moveData = makeMove();
                int row = moveData[0];
                int col = moveData[1];
                board[row][col] = 'X';
                System.out.println("---- Thread " + threadId + " ----");
                System.out.println("---- Server's turn ----");
                printBoard();

                //did computer win or tie?
                if(checkwin() || moveCounter == 16){
                    gameover = true;
                    if(checkwin()){
                        out.println("MOVE " + row + " " + col + " LOSS");
                    }
                    else{
                        out.println("MOVE " + row + " " + col + " TIE");
                    }
                }
                //move does not end the game
                else{
                    out.println("MOVE " + row + " " + col);
                }
            }

            turn = !turn;
        }
    }

    public int[] makeMove(){
        int[] result = new int[2];

        while(true){
            this.row = ranGen.nextInt(4);
            this.col = ranGen.nextInt(4);

            if(board[row][col] == ' '){
                result[0] = row;
                result[1] = col;
                break;
            }
        }

        return result;
    }

    public boolean checkwin(){
        // check for a row-win
        for (int x = 0; x <= 3; x++)
            if (board[x][0] == board[x][1] && board[x][1] == board[x][2] &&
                    board[x][2] == board[x][3] && board[x][0] != ' ')
                return true;

        // check for a col-win
        for (int y = 0; y <= 3; y++)
            if(board[0][y] == board[1][y] && board[1][y] == board[2][y] &&
                    board[2][y] == board[3][y] && board[0][y] != ' ')
                return true;

        // check for a diagonal
        if ((board[0][0] == board[1][1] && board[1][1] == board[2][2] &&
                board[2][2] == board[3][3] && board[0][0] != ' ') ||
            (board[3][0] == board[2][1] && board[2][1] == board[1][2] &&
                    board[1][2] == board[0][3] && board[3][0] != ' '))
                return true;

        return false;
    }

    public void printBoard(){
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                System.out.print(" " + board[row][col] + " ");
                if (col != 3) {
                    System.out.print("|");
                }
            }
            System.out.println();
            if (row != 3) {
                System.out.println("---+---+---+---");
            }
        }
    }
}
