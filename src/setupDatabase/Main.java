package setupDatabase;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws IllegalStateException, IOException, InterruptedException, SQLException {

        new TableCreation().generateTableClient();
        new TableCreation().generateTablePairs();
//        new TableCreation().generateTableGames();
    }
}
