/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.univaq.ingweb.internshiptutor.data.dao;

import it.univaq.ingweb.framework.data.DAO;
import it.univaq.ingweb.framework.data.DataException;
import it.univaq.ingweb.framework.data.DataLayer;
import it.univaq.ingweb.internshiptutor.data.model.Utente;
import it.univaq.ingweb.internshiptutor.data.proxy.UtenteProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Stefano Florio
 */
public class UtenteDAO_MySQL extends DAO implements UtenteDAO {
    
    private PreparedStatement sUtenteById, sUtenteByLogin, sUtenteByUser;
    private PreparedStatement iUtente, dUtente, uUtente;
    private PreparedStatement sCheckUtenteExist;

    public UtenteDAO_MySQL(DataLayer d) {
        super(d);
    }    
    
    @Override
    public void init() throws DataException {
        try {
            super.init();

            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sUtenteById = connection.prepareStatement("SELECT * FROM utente WHERE id=?");
            sUtenteByLogin = connection.prepareStatement("SELECT * FROM utente WHERE username=? AND pw=?");
            sUtenteByUser = connection.prepareStatement("SELECT * FROM utente WHERE username=?");
            sCheckUtenteExist = connection.prepareStatement("SELECT * FROM utente WHERE (username=?) or (email=?)");
            iUtente = connection.prepareStatement("INSERT INTO utente (email, username, pw, tipologia)"
                    + "values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            dUtente = connection.prepareStatement("DELETE FROM utente WHERE id=?");
            uUtente = connection.prepareStatement("UPDATE utente SET email=?, username=?, pw=?, tipologia=? WHERE id=?");
        } catch (SQLException ex) {
            throw new DataException("Error initializing internship tutor data layer", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        //anche chiudere i PreparedStamenent è una buona pratica...
        //also closing PreparedStamenents is a good practice...
        try {
            sUtenteById.close();
            sUtenteByLogin.close();
            iUtente.close();
            dUtente.close();
            sUtenteByLogin.close();
            sUtenteByUser.close();
            sCheckUtenteExist.close();
          
        } catch (SQLException ex) {
            throw new DataException("Error closing statements", ex);
        }
        super.destroy();
    }
    
    @Override
    public UtenteProxy createUtente() {
        return new UtenteProxy(getDataLayer());
    }

    @Override
    public UtenteProxy createUtente(ResultSet rs) throws DataException {
        try {
            UtenteProxy ut = createUtente();
            ut.setId(rs.getInt("id"));
            ut.setEmail(rs.getString("email"));
            ut.setUsername(rs.getString("username"));
            ut.setPw(rs.getString("pw"));
            ut.setTipologia(rs.getString("tipologia"));
            return ut;
        } catch (SQLException ex) {
            throw new DataException("Unable to create Utente object form ResultSet", ex);
        }
    }

    @Override
    public boolean checkUtenteExist(String user, String email) throws DataException {
        try {
            sCheckUtenteExist.setString(1, user);
            sCheckUtenteExist.setString(2, email);
            try (ResultSet rs = sCheckUtenteExist.executeQuery()) {
                if (rs.next()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Utente by ID", ex);
        }
    }

    @Override
    public Utente getUtente(int id) throws DataException {
        try {
            sUtenteById.setInt(1, id);
            try (ResultSet rs = sUtenteById.executeQuery()) {
                if (rs.next()) {
                    //notare come utilizziamo il costrutture
                    //"helper" della classe UtenteImpl
                    //per creare rapidamente un'istanza a
                    //partire dal record corrente
                    return createUtente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Utente by ID", ex);
        }
        return null;
    }
    
    @Override
    public Utente getUtente(String username, String password) throws DataException {
        try {
            sUtenteByLogin.setString(1, username);
            sUtenteByLogin.setString(2, password);
            try (ResultSet rs = sUtenteByLogin.executeQuery()) {
                if (rs.next()) {
                    //notare come utilizziamo il costrutture
                    //"helper" della classe UtenteImpl
                    //per creare rapidamente un'istanza a
                    //partire dal record corrente
                    return createUtente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Utente by username and password", ex);
        }
        return null;
    }

    @Override
    public Utente getUtenteByUser(String username) throws DataException {
        try {
            sUtenteByUser.setString(1, username);
            try (ResultSet rs = sUtenteByUser.executeQuery()) {
                if (rs.next()) {
                    //notare come utilizziamo il costrutture
                    //"helper" della classe UtenteImpl
                    //per creare rapidamente un'istanza a
                    //partire dal record corrente
                    return createUtente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Utente by username and password", ex);
        }
        return null;
    }

    @Override
    public int insertUtente(Utente ut) throws DataException {
        int id = 0;
        try {
            iUtente.setString(1, ut.getEmail());
            iUtente.setString(2, ut.getUsername());
            iUtente.setString(3, ut.getPw());
            iUtente.setString(4, ut.getTipologia());
            if (iUtente.executeUpdate() == 1) {
                    //per leggere la chiave generata dal database
                    //per il record appena inserito, usiamo il metodo
                    //getGeneratedKeys sullo statement.
                    //to read the generated record key from the database
                    //we use the getGeneratedKeys method on the same statement
                    try (ResultSet keys = iUtente.getGeneratedKeys()) {
                        //il valore restituito è un ResultSet con un record
                        //per ciascuna chiave generata (uno solo nel nostro caso)
                        //the returned value is a ResultSet with a distinct record for
                        //each generated key (only one in our case)
                        if (keys.next()) {
                            //i campi del record sono le componenti della chiave
                            //(nel nostro caso, un solo intero)
                            //the record fields are the key componenets
                            //(a single integer in our case)
                            id = keys.getInt(1);
                            //aggiornaimo la chiave in caso di inserimento
                            //after an insert, uopdate the object key
                        }
                    }
                    ut.setId(id);
                    return 1;
                } else { return 0; }
        } catch (SQLException ex) {
            throw new DataException("Unable to insert new utente", ex);
        }
    }

    @Override
    public int deleteUtente(int id) throws DataException {
        try {
            dUtente.setInt(1, id);
            return dUtente.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete utente", ex);
        }
    } 
    
    @Override
    public int updateUtente(Utente ut) throws DataException {
        try {
            uUtente.setString(1, ut.getEmail());
            uUtente.setString(2, ut.getUsername());
            uUtente.setString(3, ut.getPw());
            uUtente.setString(4, ut.getTipologia());
            uUtente.setInt(5, ut.getId());
            return uUtente.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to update utente", ex);
        }
    }
}
