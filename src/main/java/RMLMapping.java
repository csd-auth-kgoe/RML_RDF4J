import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;

import java.io.*;

public class RMLMapping {

    public static void main(String[] args) {
        new RMLMapping().start("mappings.ttl", "events.ttl");
    }

    public void start(String rmlFile, String rmlOutputFile) {

        try {
            // Mapping file
            File mappingFile = new File(rmlFile);

            // Base IRI from @base in the Turtle mapping file
            String baseIRI = Utils.getBaseDirectiveTurtle(mappingFile.getAbsolutePath());

            // Load mapping file
            InputStream mappingStream = new FileInputStream(mappingFile);

            // Parse mapping into QuadStore
            QuadStore rmlStore = QuadStoreFactory.read(mappingStream);
            mappingStream.close();

            // Base path for input files, e.g. events.json
            RecordsFactory factory = new RecordsFactory(mappingFile.getParent());

            // Output store
            QuadStore outputStore = new RDF4JStore();

            // Create executor
            Executor executor = new Executor(
                    rmlStore,
                    factory,
                    outputStore,
                    baseIRI,
                    null
            );

            // Execute mapping
            QuadStore result = executor.execute(null)
                    .get(new NamedNode("rmlmapper://default.store"));

            // Print result to console
//            BufferedWriter consoleWriter = new BufferedWriter(
//                    new OutputStreamWriter(System.out)
//            );
//            result.write(consoleWriter, "turtle");
//            consoleWriter.flush();

            // Write result to file
            Writer output = new FileWriter(rmlOutputFile);
            result.write(output, "turtle");
            output.close();

            System.out.println("\nRML mapping completed. Output written to " +
                    rmlOutputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}