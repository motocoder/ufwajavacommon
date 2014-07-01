package llc.ufwa.data.dao;

import java.io.InputStream;

import llc.ufwa.data.dao.exception.DAOException;

public interface BucketDAO {
    
    Object get(String guid) throws DAOException;
    InputStream getStream(String guid) throws DAOException;
    void save(String guid, Object value) throws DAOException;
    void save(String guid, InputStream value) throws DAOException;
    void createGuid(String guid) throws DAOException;
    void delete(String guid) throws DAOException;
    void clear() throws DAOException;
    boolean exists(String gUID) throws DAOException;

}
