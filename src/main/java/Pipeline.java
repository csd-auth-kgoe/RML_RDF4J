import com.ontotext.graphdb.security.Role;

public class Pipeline {

    public static void main(String[] args) throws Exception {
        String rmlFile = "mappings.ttl";
        String rmlOutputFile = "events.ttl";
        String enrichedFile = "events-enriched.ttl";
        String repositoryName = "EnrichedEvents";

        RMLMapping m = new RMLMapping();
        m.start(rmlFile, rmlOutputFile);

        EnrichEvents e = new EnrichEvents();
        e.start(rmlOutputFile, enrichedFile);

        Repository r = new Repository();
        r.start(repositoryName, enrichedFile);
    }

}
