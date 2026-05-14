import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.FileInputStream;
import java.io.IOException;

public class Repository {


    public static void main(String[] args) {
        new Repository().start("EnrichedEvents", "events-enriched.ttl");
    }


    public void start (String repositoryName, String turtleFile) {
        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/" + repositoryName);
        RepositoryConnection connection = repository.getConnection();

        // Clear the repository before we start
        connection.clear();

        // load a simple ontology from a file
        connection.begin();
        // Adding the family ontology
        try {
            connection.add(
                    new FileInputStream(turtleFile),
                    "http://events.example.com/mapping/",
                    RDFFormat.TURTLE
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Committing the transaction persists the data
        connection.commit();
        System.out.println("Repository updated with: " + turtleFile);

        String queryString = "";
        queryString += "PREFIX ex: <http://events.example.com/mapping/> \n";
        queryString += "PREFIX schema: <https://schema.org/> \n";
        queryString += "SELECT ?event ?name \n";
        queryString += "WHERE { \n";
        queryString += "    ?event ex:priceCategory ex:LowCostEvent . \n";
        queryString += "    OPTIONAL { ?event schema:name ?name . } \n";
        queryString += "}";

        TupleQuery query = connection.prepareTupleQuery(queryString);

        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution : result) {
                System.out.println("Event = " + solution.getValue("event"));

                if (solution.hasBinding("name")) {
                    System.out.println("Name = " + solution.getValue("name"));
                }

                System.out.println();
            }
        }

        connection.close();
        repository.shutDown();
    }


}
