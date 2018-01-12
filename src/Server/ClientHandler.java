package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bruno Santos
 */
public class ClientHandler implements Runnable {

    Map<String, Socket> PlayerSocket;
    Map<String, Thread> PlayerThread;
    Map<String, ObjectInputStream> PlayerIn;
    Map<String, ObjectOutputStream> PlayerOut;
    Map<String, String> pedido;
    int numLogs;

    DBhandler uh;

    ClientHandler() throws IOException {
        pedido = new HashMap<String, String>();
        PlayerThread = new HashMap<String, Thread>();
        PlayerSocket = new HashMap<String, Socket>();
        PlayerIn = new HashMap<String, ObjectInputStream>();
        PlayerOut = new HashMap<String, ObjectOutputStream>();
        uh = new DBhandler();

    }

    @Override
    public void run() {
        while (true) {

        }
    }

    public void AcceptGame(String msg) {
        String[] arr = msg.split("[\\W]");
        if (arr[0].equals("accept")) {
            if (!arr[1].equals("yes")) {
                uh.freePlayer(arr[2]);
                uh.freePlayer(arr[3]);
            }else
            {
                uh.createMatch(arr[2], arr[3]);
            }
        }
    }

    public void addNewClient(String username, Socket ToClient, ObjectInputStream in, ObjectOutputStream out) throws IOException {

        System.out.println("vou tentar addiconar a tread do " + username);
        PlayerSocket.put(username, ToClient);
        PlayerIn.put(username, in);
        PlayerOut.put(username, out);

        Thread thread = new Thread(new Runnable() {
            public String username;
            String tempUser;

            public void setUser(String user) {
                username = user;
            }

            @Override
            public void run() {
                Thread thread = Thread.currentThread();
                setUser(thread.getName());
                ObjectInputStream in = PlayerIn.get(username);
                while (true) {
                    try {
                        Object temp = in.readObject();

                        if (temp != null) {
                            String msg = (String) temp;

                            String[] arr = msg.split("[\\W]");

                            if (arr[0].equals("gamereq")) //pretende iniciar um jogo, nos seguintes argumentos estarão mais dados
                            {
                                PlayerOut.get(arr[1]).writeObject(("gamereq " + username));   //envia mensagem ao cliente com que se pretende iniciar o jogo
                                pedido.put(arr[1], username);
                                PlayerOut.get(arr[1]).flush();

                            } else if (arr[0].equals("accept") || tempUser != null)//caso seja a resposta a um pedido de um cliente
                            {
                                System.out.println("accept " + arr[1] + " " + username + " " + pedido.get(username));
                                AcceptGame(("accept " + arr[1] + " " + username + " " + pedido.get(username)));//chama função de avaliação da resposta

                            } else if (arr[0].equals("list"))//caso seja o pedido de lista
                            {
                                PlayerOut.get(username).writeObject(uh.getFreePlayers());
                                PlayerOut.get(username).flush();
                            }else if (arr[0].equals("msgto"))//caso seja o pedido de lista
                            {
                                PlayerOut.get(arr[1]).writeObject(temp);
                                PlayerOut.get(arr[1]).flush();
                            }else if (arr[0].equals("msgall"))//caso seja o pedido de lista
                            {
                                for(Map.Entry<String, String> entry : pedido.entrySet()) {
                                    String key = entry.getKey();
                                    PlayerOut.get(key).writeObject(temp);
                                    PlayerOut.get(key).flush();
                                }
                            } else if (arr[0].equals("logout"))
                            {//caso seja a resposta a um pedido de um cliente 
                                uh.logout(username);
                                PlayerIn.get(username).close();
                                PlayerIn.remove(username);
                                PlayerOut.get(username).close();
                                PlayerOut.remove(username);
                                PlayerSocket.get(username).close();
                                PlayerSocket.remove(username);
                                Thread.currentThread().interrupt(); 
                            }
                            else {
                                PlayerOut.get(username).writeObject("Comando Invalido"); //envia mensagem ao cliente da thread
                            }
                            PlayerOut.get(username).flush();
                        }
                    } catch (ClassNotFoundException ex) {

                    } catch (SocketException e) {
                        System.out.println(">>>SocketFechado");

                        return;
                    } catch (IOException ex) {

                    } catch (SQLException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        });

        thread.setName(username);
        PlayerThread.put(username, thread);
        thread.start();
    }

}
