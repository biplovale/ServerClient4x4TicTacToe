package Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static String hostName = "localhost";
    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket toServerSocket;
    private static TicTacToe game;

    public static void main(String[] args){
        try {
            //connect to localhost
            System.out.println("CLIENT is attempting connection......");
            toServerSocket = new Socket("localhost", 9877);
            System.out.println("CONNECTION HAS BEEN MADE.");

            inputStream = new DataInputStream(toServerSocket.getInputStream());
            outputStream = new DataOutputStream(toServerSocket.getOutputStream());
            out = new PrintWriter(outputStream, true);
            in = new BufferedReader(new InputStreamReader(inputStream));

            game = new TicTacToe();

            game.playGame(out, in);
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private static class TicTacToe{
        private char[][] board;

        public TicTacToe(){
            board = new char[4][4];

            for(int row = 0; row < board.length; row++){
                for(int col = 0; col < board[0].length; col++){
                    board[row][col] = ' ';
                }
            }
        }

        public boolean isMoveLegal(int row, int col){
            return  ((row <= 3 && row >= 0) && (col <= 3 && col >= 0)) && board[row][col] == ' ';
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

        public void playGame(PrintWriter writer, BufferedReader reader){
            Scanner input = new Scanner(System.in);
            String response = "";
            boolean turn = false;
            boolean gameover = false;

            //keep looping until game is over
            while(!gameover){
                //user's turn
                if(turn){
                    int row, col;
                    do{
                        System.out.println("Enter your move (Format: rowInt columnInt):");
                        String move = input.nextLine();
                        String[] moveArray = move.split(" ");
                        row = Integer.parseInt(moveArray[0]);
                        col = Integer.parseInt(moveArray[1]);
                    }
                    while(!isMoveLegal(row, col));

                    board[row][col] = 'O';

                    //send move to server
                    writer.println("MOVE " + row + " " + col);

                    System.out.println("----  Your Turn  ----");
                }
                //Server's turn
                else{
                    try {
                        //read move from server
                        response = reader.readLine();

                        //server's move
                        if(response != "CLIENT"){
                            String[] args = response.split("\\s+");

                            //determining moves
                            int row = Integer.parseInt(args[1]);
                            int col = Integer.parseInt(args[2]);
                            if(args.length > 3){

                                if(!args[3].equals("WIN") && row != -1){
                                    board[row][col] = 'X';
                                }

                                if(args[3].equals("WIN")){
                                    System.out.println("\n\nCongratulations!!! You WON the game!");
                                }
                                else if(args[3].equals("TIE")){
                                    System.out.println("\nThe game was a TIE!");
                                }
                                else if(args[3].equals("LOSS")){
                                    System.out.println("\nSORRY! You LOST the game!");
                                }
                                else{ }

                                gameover = true;
                            }
                            //normal moves
                            else{
                                board[row][col] = 'X';
                            }
                        }
                        //user's move
                        else{
                            System.out.println("YOUR MOVE FIRST");
                        }
                    }
                    catch(IOException ex){
                        ex.printStackTrace();
                    }

                    System.out.println("----  Opponent's Turn  ----");
                }

                //print the game board
                printBoard();

                turn = !turn;
            }
        }
    }
}
