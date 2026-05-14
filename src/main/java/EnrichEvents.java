import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class EnrichEvents {

    public static void main(String[] args) throws Exception {
        new EnrichEvents().start("events.ttl","events-enriched.ttl");
    }

    public void start(String rmlOutputFile, String enrichedFile) throws Exception {
        ValueFactory vf = SimpleValueFactory.getInstance();

        Model model = Rio.parse(
                new FileInputStream(rmlOutputFile),
                "http://example.org/event/",
                RDFFormat.TURTLE
        );

        String EX = "http://example.org/event/";
        String SCHEMA = "https://schema.org/";

        IRI price = vf.createIRI(SCHEMA, "price");
        IRI startDate = vf.createIRI(SCHEMA, "startDate");
        IRI endDate = vf.createIRI(SCHEMA, "endDate");

        IRI priceCategory = vf.createIRI(EX, "priceCategory");
        IRI durationHours = vf.createIRI(EX, "durationHours");

        IRI freeEvent = vf.createIRI(EX, "FreeEvent");
        IRI lowCostEvent = vf.createIRI(EX, "LowCostEvent");
        IRI expensiveEvent = vf.createIRI(EX, "ExpensiveEvent");

        // Copy subjects first, because we will modify the model inside the loop
        Set<Resource> events = new HashSet<>(model.subjects());

        for (Resource event : events) {

            // --- Extract price ---
            Literal priceLiteral = null;
            for (Statement st : model.filter(event, price, null)) {
                if (st.getObject() instanceof Literal) {
                    priceLiteral = (Literal) st.getObject();
                    break;
                }
            }

            // --- Extract start date ---
            Literal startLiteral = null;
            for (Statement st : model.filter(event, startDate, null)) {
                if (st.getObject() instanceof Literal) {
                    startLiteral = (Literal) st.getObject();
                    break;
                }
            }

            // --- Extract end date ---
            Literal endLiteral = null;
            for (Statement st : model.filter(event, endDate, null)) {
                if (st.getObject() instanceof Literal) {
                    endLiteral = (Literal) st.getObject();
                    break;
                }
            }

            // --- Add price category ---
            if (priceLiteral != null) {
                double value = priceLiteral.doubleValue();
                if (value == 0) {
                    model.add(event, priceCategory, freeEvent);
                } else if (value < 20) {
                    model.add(event, priceCategory, lowCostEvent);
                } else {
                    model.add(event, priceCategory, expensiveEvent);
                }
            }

            // --- Add duration in hours ---
            if (startLiteral != null && endLiteral != null) {
                LocalDateTime start = LocalDateTime.parse(startLiteral.getLabel());
                LocalDateTime end = LocalDateTime.parse(endLiteral.getLabel());
                long hours = Duration.between(start, end).toHours();
                model.add(
                        event,
                        durationHours,
                        vf.createLiteral(hours)
                );
            }
        }

        Rio.write(
                model,
                new FileOutputStream(enrichedFile),
                RDFFormat.TURTLE
        );



        



    }
}