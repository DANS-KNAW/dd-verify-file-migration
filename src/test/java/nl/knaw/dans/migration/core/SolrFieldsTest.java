package nl.knaw.dans.migration.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolrFieldsTest {
    @Test
    public void canParseFreeDate() throws IOException {
        String result = new SolrFields("2012-06-29,\"OPEN_ACCESS,accept\",onderzoeksdataNIOD,PUBLISHED,,\"1983-1984\"").date;
        assertEquals("1983-1984", result);
    }

    @Test
    public void canParseFormattedDate() throws IOException {
        String result = new SolrFields("2012-06-29,\"OPEN_ACCESS,accept\",onderzoeksdataNIOD,PUBLISHED,\"1983-01-01T22:00:00Z\",\"1984\"").date;
        assertEquals("1983", result);
    }

    @Test
    public void canParseNoDateAtAll() throws IOException {
        String result = new SolrFields("2012-06-29,\"OPEN_ACCESS,accept\",onderzoeksdataNIOD,PUBLISHED,,").date;
        assertEquals("", result);
    }
}
