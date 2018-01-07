package GameClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import logic.GameModel;
import ui.gui.PopupView;
import ui.gui.ThreeInRowView;

public final class GameClientConnector implements Runnable
{

    Game game;
    //ThreeInRowView gui;

    ServerSocket clientServer;
    Socket socket;
    int servicePort;

    ObjectInputStream in;
    ObjectOutputStream out;

    String player = null;
    private boolean stop = false;
    private boolean lock = true;

    public GameClientConnector(String servicePort) throws IOException
    {
        this.servicePort = Integer.parseInt(servicePort);
        clientServer = new ServerSocket(this.servicePort);
        System.out.println("ServerSocket started @Port " + clientServer.getLocalPort() + " ");
    }

    public void startStreams()
    {
        try
        {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

        } catch (IOException ex)
        {
            System.out.println("GameClient: Error creating streams.");

        }
    }

    public void objectUpdate(Object obj) throws InterruptedException, IOException
    {
        if (obj instanceof String)
        {
            if (obj.equals("Player1") || obj.equals("Player2"))
            {
                if (obj.equals("Player1"))
                {
                    player = "A";
                } else
                {
                    player = "B";
                }
                System.out.println("GameClient: String to play recieved! I'm player " + player);
                updateCentralServer("Ok");
                System.out.println("GameClient: String OK sent!");

            } else
            {
                if (obj.equals("GAMEOVER"))
                {
                    System.out.println("GameClient: GAMEOVER arrived...");
                    shutdown();
                } else
                {
                    System.out.println("GameClient: An unexpected string arrived..." + obj + "");
                }
            }
        } else
        {
            if (obj instanceof GameModel)
            {
                if (game == null)
                {
                    game = new Game(player);
                    game.updateGame((GameModel) obj);
                    System.out.println("GameClient: New Game created...");
                } else
                {
                    game.updateGame((GameModel) obj);
                    System.out.println("GameClient: Game updated with object " + obj);
                }
            } else
            {
                System.out.print("GameClient: I don't really know what this is... " + obj.toString() + " ");
            }
        }

    }

    public void shutdown()
    {
        try
        {
            updateCentralServer("CLOSING");
            out.close();
            in.close();
            if (socket != null)
            {
                socket.close();
            }
            if (clientServer != null)
            {
                clientServer.close();
            }
            stop = true;
            Thread.sleep(2000);
            game.closeGui();
            Thread.sleep(2000);
            game.closePop();

            Thread.currentThread().interrupt();

        } catch (IOException ex)
        {
            System.out.print("GameClient: Shutdown error " + ex + "");
        } catch (InterruptedException ex)
        {
            System.out.print("GameClient: interrupted Shutdown error " + ex + "");
        }
    }

    public void updateCentralServer(Object obj)
    {
        try
        {
            out.writeObject(obj);
            out.flush();
           // System.err.println("GameClient: updateCentralServer sent " + obj);

        } catch (IOException ex)
        {
            System.err.println("GameClient: updateGame IOException: " + ex + "");
        }
    }

//    //here!
//    public void update()
//    {
//
//        if (!game.getGame().getCurrentPlayerName().equals(player))
//        {
//            System.err.println("FIRST IF");
//            updateCentralServer(game.getGame());
//            gui.enableGrid(false);
//        } else
//        {
//            System.err.println("FISRT ELSE");
//            gui.enableGrid(true);
//        }
//        if (game.getGame().hasWon(game.getGame().getCurrentPlayer()))
//        {
//            System.err.println("WON IF");
//            updateCentralServer(game.getGame());
//        }
//
//    }
    @Override
    public void run()
    {
        try
        {
            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    socket = clientServer.accept();
                    System.out.println("GameClient: GameServer accepted");
                    startStreams();

                } catch (IOException ex)
                {
                    System.out.println("GameClient: Error starting socket." + ex + " ");
                }
                while (!stop)
                {
                    Object obj = in.readObject();
                    objectUpdate(obj);
                    System.out.println("GameClient: objectUpdate(obj)");

                    //and here!
                    if (game != null)
                    {

                        while (game.getGame().getCurrentPlayerName().equals(player) && !game.getGame().isOver())
                        {

                            //do nothing
                            //sleep
                            System.err.print('.');
                        }

                        if (!lock || player.equals("A"))
                        {
                            updateCentralServer(game.getGame());
                            System.out.println("After do nothing... updta central");
                        } else
                        {
                            if (player.equals("B")) //carefull here - FUTURE!
                            {
                                lock = false;
                                System.err.println("GameClientConnector: Player 2 unlocked");
                            }
                        }
                        System.out.println("After do nothing...");

                    }
                    //
                }
            }
        } catch (IOException e)
        {
            System.err.println("GameClient: run() IOException: " + e + "");
        } catch (ClassNotFoundException ex)
        {
            System.err.println("GameClient: ClassNotFoundException: " + ex + "");
        } catch (InterruptedException ex)
        {
            System.err.println("GameClient: InterruptedException: " + ex + "");
        }
    }
}
