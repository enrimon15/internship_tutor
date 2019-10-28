/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.univaq.web_engineering.internship_tutor.data.dao;


import it.univaq.web_engineering.framework.data.DAO;
import it.univaq.web_engineering.framework.data.DataException;
import it.univaq.web_engineering.framework.data.DataLayer;
import it.univaq.web_engineering.internship_tutor.data.model.TutoreTirocinio;
import it.univaq.web_engineering.internship_tutor.data.proxy.TutoreTirocinioProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Stefano Florio
 */
public class TutoreTirocinioDAO_MySQL extends DAO implements TutoreTirocinioDAO {

    
    private PreparedStatement sTutoreTirocinioById;
    private PreparedStatement iTutoreTirocinio;

    public TutoreTirocinioDAO_MySQL(DataLayer d) {
        super(d);
    }

    @Override
    public void init() throws DataException {
        try {
            super.init();

            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sTutoreTirocinioById = connection.prepareStatement("SELECT * FROM tutore_tirocinio WHERE ID=?");
            iTutoreTirocinio = connection.prepareStatement("INSERT INTO tutore_tirocinio (nome, cognome, email, telefono)"
                    + "values (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException ex) {
            throw new DataException("Error initializing internship tutor data layer", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        //anche chiudere i PreparedStamenent è una buona pratica...
        //also closing PreparedStamenents is a good practice...
        try {
            sTutoreTirocinioById.close();
            iTutoreTirocinio.close();
          
        } catch (SQLException ex) {
            //
        }
        super.destroy();
    }
    
    @Override
    public TutoreTirocinioProxy createTutoreTirocinio() {
        return new TutoreTirocinioProxy(getDataLayer());
    }

    @Override
    public TutoreTirocinioProxy createTutoreTirocinio(ResultSet rs) throws DataException {
        try {
            TutoreTirocinioProxy tt = createTutoreTirocinio();
            tt.setId(rs.getInt("id"));
            tt.setNome(rs.getString("nome"));
            tt.setCognome(rs.getString("cognome"));
            tt.setEmail(rs.getString("email"));
            tt.setTelefono(rs.getString("telefono"));
            return tt;
        } catch (SQLException ex) {
            throw new DataException("Unable to create TutoreTirocinio object form ResultSet", ex);
        }
    }

    @Override
    public TutoreTirocinio getTutoreTirocinio(int id) throws DataException {
        try {
            sTutoreTirocinioById.setInt(1, id);
            try (ResultSet rs = sTutoreTirocinioById.executeQuery()) {
                if (rs.next()) {
                    //notare come utilizziamo il costrutture
                    //"helper" della classe AuthorImpl
                    //per creare rapidamente un'istanza a
                    //partire dal record corrente
                    //note how we use here the helper constructor
                    //of the AuthorImpl class to quickly
                    //create an instance from the current record
                    return createTutoreTirocinio(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load TutoreTirocinio by ID", ex);
        }
        return null;
    }
    
    @Override
    public void insertTutoreTirocinio(TutoreTirocinio tt) throws DataException {
        try {
            iTutoreTirocinio.setString(1, tt.getNome());
            iTutoreTirocinio.setString(2, tt.getCognome());
            iTutoreTirocinio.setString(3, tt.getEmail());
            iTutoreTirocinio.setString(4, tt.getTelefono());
            if (iTutoreTirocinio.executeUpdate() == 1) {
                    //per leggere la chiave generata dal database
                    //per il record appena inserito, usiamo il metodo
                    //getGeneratedKeys sullo statement.
                    //to read the generated record key from the database
                    //we use the getGeneratedKeys method on the same statement
                    try (ResultSet keys = iTutoreTirocinio.getGeneratedKeys()) {
                        //il valore restituito è un ResultSet con un record
                        //per ciascuna chiave generata (uno solo nel nostro caso)
                        //the returned value is a ResultSet with a distinct record for
                        //each generated key (only one in our case)
                        if (keys.next()) {
                            //i campi del record sono le componenti della chiave
                            //(nel nostro caso, un solo intero)
                            //the record fields are the key componenets
                            //(a single integer in our case)
                            tt.setId(keys.getInt(1));
                            //aggiornaimo la chiave in caso di inserimento
                            //after an insert, uopdate the object key
                        }
                    }
                }
        } catch (SQLException ex) {
            throw new DataException("Unable to insert new tutore tirocinio", ex);
        }
    }
    
}

