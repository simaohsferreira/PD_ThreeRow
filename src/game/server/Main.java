package game.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main implements Runnable {

    public static void main(String[] args) throws IllegalStateException, ClassNotFoundException {

        System.out.println("Game server initianting!");
        System.err.println("Heartbeat starting");

        GameCommUDP heartbeat = new GameCommUDP("UDPclient", 6999);
        heartbeat.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            System.err.println("Heartbeat starting");
        }
        System.out.println("hearbeat: " + heartbeat.port());

        String ipAndPort = "localhost:3306";
        GameLauncher server;
        GameDBHandler database = new GameDBHandler(ipAndPort);
        List<Pair> pairs = new ArrayList<>();
        // while (true) {
        try {
            pairs = database.getPairs();
        } catch (SQLException ex) {
            System.err.println("Game Server SQLException - DB " + ex);

        }

        for (Pair p : pairs) {
            System.out.println("leu um par");
            server = new GameLauncher(ipAndPort, p);
            server.start();
        }

    }
    //}

    @Override
    public void run() {

    }

}
