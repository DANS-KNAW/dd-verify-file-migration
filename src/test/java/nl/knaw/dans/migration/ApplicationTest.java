package nl.knaw.dans.migration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ApplicationTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        // https://stackoverflow.com/questions/1119385/junit-test-for-system-out-println
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }


    @Test
    public void no_args_shows_help() throws Exception {
        DdVerifyMigrationApplication.main(new String[] {});
        assertThat(outContent.toString()).contains("usage: java -jar project.jar [-h] [-v] {server,check,load-from-dataverse} ...");
    }
    @Test
    public void help_for_load_from_dataverse() throws Exception {
        DdVerifyMigrationApplication.main(new String[] {"load-from-dataverse", "-h"});
        assertThat(outContent.toString()).contains("usage: java -jar project.jar load-from-dataverse [--mode {BOTH,FILES,DATASETS}] [-h] [-d DOI | --csv CSV | --UUIDs UUIDS] [file]");
        assertThat(outContent.toString()).contains("file                   application configuration file");
    }
    @Test
    public void help_for_check() throws Exception {
        DdVerifyMigrationApplication.main(new String[] {"check", "-h"});
        assertThat(outContent.toString()).contains("usage: java -jar project.jar check [-h] [file]");
        assertThat(outContent.toString()).contains("file                   application configuration file");
    }
    @Test
    public void help_for_server() throws Exception {
        DdVerifyMigrationApplication.main(new String[] {"server", "-h"});
        assertThat(outContent.toString()).contains("usage: java -jar project.jar server [-h] [file]");
        assertThat(outContent.toString()).contains("file                   application configuration file");
    }
}
