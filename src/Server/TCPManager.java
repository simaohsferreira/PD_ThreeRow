package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import static java.sql.DriverManager.println;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bruno Santos
 */
public class TCPManager implements Runnable//, ClientHandlerCallback
{

    //TCP
    private final String serverName;
    private final ServerSocket serverTCPSocket;

    DBhandler uh;
    private Thread ClientHandlerThread;
    private ClientHandler CHandler;

    public TCPManager(String serverName) throws IOException {
        this.serverName = serverName;

        serverTCPSocket = new ServerSocket(6001);
        println("Starting DBhandler . . . ");
        uh = new DBhandler("localhost:3306");
        StartClientHandler();
        System.out.println("Port " + serverTCPSocket.getLocalPort() + " ");

    }

    public void StartClientHandler() throws IOException {

        println("Starting ClientHandler . . . ");

        CHandler = new ClientHandler();
        ClientHandlerThread = new Thread(CHandler);
        ClientHandlerThread.setDaemon(true);
        ClientHandlerThread.start();
    }

    public String getPairs() {
        String listPairs = "";

        return listPairs;
    }

    public void killPlayers() throws SQLException {
        uh.killPlayers();
    }

    @Override
    public void run() {
        //registar instância do TCPManager para threads mais "exteriores" poderem aceder a alguns dados
        // GlobalReferences.registerReference(GlobalReferences.ReferenceType.TCP_MANAGER, this);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final Socket nextClient;
                nextClient = serverTCPSocket.accept();
                System.out.println("TCPManager: new client accepted");

                Thread Login = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String arr[];
                            int control = 0;
                            String temp = null;
                            Socket Client = nextClient;
                            final ObjectInputStream in;
                            final ObjectOutputStream out;
                            out = new ObjectOutputStream(Client.getOutputStream());
                            in = new ObjectInputStream(Client.getInputStream());

                            do {

                                temp = (String) in.readObject();

                                arr = temp.split("[\\W]");

                                if (arr[0].equalsIgnoreCase("Register")) {
                                    if (uh.register(arr[1], arr[2])) {
                                        out.writeObject(arr[1] +" sucefully registered.");
                                        control = 1;
                                    } else {
                                        out.writeObject("Impossible to register.");
                                    }
                                    out.flush();
                                } else {
                                    if (arr[0].equalsIgnoreCase("Login")) {
                                        String ip =nextClient.getInetAddress().toString().substring(1);
                                        if (uh.login(arr[1], arr[2], ip, nextClient.getPort())) {
                                            out.writeObject(arr[1]+" sucefully logged in.");
                                            control = 1;
                                        } else {
                                            out.writeObject("Faulty login");
                                        }
                                        out.flush();
                                    } else {
                                        System.out.println("TCPManager: Rejected client, faulty login data");
                                    }
                                }

                            } while (control == 0);

                            if (control == 1) {
                                CHandler.addNewClient(arr[1], nextClient, in, out);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(TCPManager.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(TCPManager.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException ex) {
                            Logger.getLogger(TCPManager.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (CustomException ex) {
                            Logger.getLogger(TCPManager.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                });

                Login.setDaemon(true);
                Login.start();
            } catch (IOException ex) {
                Logger.getLogger(TCPManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
