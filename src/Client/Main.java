package Client;

import GameClient.Client;
import java.io.IOException;

/**
 *
 * @author Bruno Santos
 */
public class Main {

    public static void main(String[] args) throws IllegalStateException, IOException {
//        if(args.length != 2)
//        {
//            System.out.println("Wrong syntax! serviceIP servicePort");
//            return;
//        }

        Client cl = new Client("localhost", "6001");
        cl.start();

    }

}
