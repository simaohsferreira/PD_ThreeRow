/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi.client;

import java.rmi.RemoteException;

/**
 *
 * @author simao
 */
public class main {

    public static void main(String[] args) throws RemoteException {
        RMIClient client = new RMIClient("localhost");
        client.run();
    }

}
