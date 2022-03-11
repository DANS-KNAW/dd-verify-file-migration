package nl.knaw.dans.migration.core;

import nl.knaw.dans.migration.core.tables.ExpectedDataset;

public class DatasetRights {
    AccessCategory accessCategory;
    FileRights defaultFileRights;

    public AccessCategory getAccessCategory() {
        return accessCategory;
    }

    public void setAccessCategory(AccessCategory accessCategory) {
        this.accessCategory = accessCategory;
    }

    public FileRights getDefaultFileRights() {
        return defaultFileRights;
    }

    public void setDefaultFileRights(FileRights defaultFileRights) {
        this.defaultFileRights = defaultFileRights;
    }

    public ExpectedDataset expectedDataset(String doi, String depositor) {
        ExpectedDataset expectedDataset = new ExpectedDataset();
        expectedDataset.setDoi(doi);
        expectedDataset.setDepositor(depositor);
        expectedDataset.setAccessCategory(accessCategory);
        expectedDataset.setEmbargoDate(defaultFileRights);
        return expectedDataset;
    }
}
