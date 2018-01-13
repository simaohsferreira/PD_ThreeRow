package rmi.monitor;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import rmi.commons.RemoteServiceInterface;
import rmi.commons.ServerMonitorListener;

public class RMIMonitor extends UnicastRemoteObject implements ServerMonitorListener, Runnable {

    private String addr;

    RemoteServiceInterface rmiService;

    public RMIMonitor(String string) throws RemoteException {
        this.addr = string;

    }

    @Override
    public void run() {
        try {
            String registration = "rmi://" + addr + "/PD";

            Remote remoteService = Naming.lookup(registration);
            rmiService = (RemoteServiceInterface) remoteService;
            rmiService.addObserver(this);

        } catch (NotBoundException e) {
            System.out.println("Não existe servico disponivel! ");
        } catch (RemoteException e) {
            System.out.println("Erro no RMI: " + e);
        } catch (Exception e) {
            System.out.println("Erro: " + e);
        }
    }

    @Override
    public void printPairs() throws RemoteException {

        try {
            List<String> pairs = new ArrayList<>();
            pairs.add("Pairs on server:");

            if (rmiService.getPairs() != null) {
                pairs.addAll(rmiService.getPairs());
            }

            for (String str : pairs) {
                System.err.println("Monitor: " + str);
            }
        } catch (Exception e) {
            System.out.println("RmiMonitor: printPairs() " + e);
        }
    }

    @Override
    public void printUsers() throws RemoteException {
        try {
            List<String> users = new ArrayList<>();
            users.add("Users on server:");

            if (rmiService.getUsers() != null) {
                users.addAll(rmiService.getUsers());
            }

            for (String str : users) {
                System.err.println("Monitor: " + str);
            }
        } catch (Exception e) {
            System.out.println("RmiMonitor: printUsers() " + e);
        }
    }

    @Override
    public void printHistory() throws RemoteException {
        try {
            List<String> historic = new ArrayList<>();
            historic.add("Old Games:");

            if (rmiService.getOldGames() != null) {
                historic.addAll(rmiService.getUsers());
            }

            for (String str : historic) {
                System.err.println("Monitor: " + str);
            }
        } catch (Exception e) {
            System.out.println("RmiMonitor: printHistory " + e);
        }
    }

    public void terminate() throws RemoteException {
        rmiService.removeObserver(this);
    }

}
