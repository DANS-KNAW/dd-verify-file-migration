package nl.knaw.dans.filemigration.db;

import io.dropwizard.hibernate.AbstractDAO;
import nl.knaw.dans.filemigration.api.EasyFile;
import org.hibernate.SessionFactory;

import java.util.List;

public class EasyFileDAO extends AbstractDAO<EasyFile> {

  public EasyFileDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public List<EasyFile> findByDatasetId(String id) {
    return list(namedTypedQuery(EasyFile.FIND_BY_DATASET_ID).setParameter(EasyFile.DATASET_ID, id));
  }
}
