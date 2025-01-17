/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package it.univaq.ingweb.internshiptutor.data.dao;

import it.univaq.ingweb.framework.data.DAO;
import it.univaq.ingweb.framework.data.DataException;
import it.univaq.ingweb.framework.data.DataLayer;
import it.univaq.ingweb.internshiptutor.data.model.Candidatura;
import it.univaq.ingweb.internshiptutor.data.model.OffertaTirocinio;
import it.univaq.ingweb.internshiptutor.data.model.Studente;
import it.univaq.ingweb.internshiptutor.data.proxy.CandidaturaProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefano Florio
 */
public class CandidaturaDAO_MySQL extends DAO implements CandidaturaDAO {
    
    private PreparedStatement sCandidatura;
    private PreparedStatement sCandidatureByStudente, sCandidatureByTirocinio, sCandidatureByTirocinioAndStato;
    private PreparedStatement iCandidatura, uCandidaturaStato, uCandidaturaDate, uCandidaturaDoc, uCandidatura;
    
    public CandidaturaDAO_MySQL(DataLayer d) {
        super(d);
    }
    
    @Override
    public void init() throws DataException {
        try {
            sCandidatura = connection.prepareStatement("SELECT * FROM candidatura WHERE id_studente=? AND id_offerta_tirocinio=?");
            sCandidatureByStudente = connection.prepareStatement("SELECT * FROM candidatura WHERE id_studente=?");
            sCandidatureByTirocinioAndStato = connection.prepareStatement("SELECT * FROM candidatura WHERE id_offerta_tirocinio=? AND stato_candidatura=?");
            sCandidatureByTirocinio = connection.prepareStatement("SELECT * FROM candidatura WHERE id_offerta_tirocinio=?");
            iCandidatura = connection.prepareStatement("INSERT INTO candidatura (id_studente, id_offerta_tirocinio, id_tutore_uni, "
                    + "cfu, diploma, laurea, dottorato_ricerca, specializzazione) VALUES (?,?,?,?,?,?,?,?)");
            uCandidaturaStato = connection.prepareStatement("UPDATE candidatura SET stato_candidatura=? WHERE id_studente=? AND id_offerta_tirocinio=?");
            uCandidatura = connection.prepareStatement("UPDATE candidatura SET cfu=?, diploma=?, laurea=?, dottorato_ricerca=?, specializzazione=?,"
                    + "src_documento_candidatura=?, data_inizio=?, data_fine=?, stato_candidatura=? WHERE id_offerta_tirocinio=? AND id_studente=?");
            uCandidaturaDoc = connection.prepareStatement("UPDATE candidatura SET src_documento_candidatura=? WHERE id_studente=? AND id_offerta_tirocinio=?");
        } catch (SQLException ex) {
            throw new DataException("Error initializing internship tutor datalayer", ex);
        }
        super.init();
    }
    
    @Override
    public void destroy() throws DataException {
        try {
            sCandidatura.close();
            sCandidatureByStudente.close();
            sCandidatureByTirocinio.close();
            sCandidatureByTirocinioAndStato.close();
            iCandidatura.close();
            uCandidaturaStato.close();
            uCandidaturaDoc.close();
        } catch (SQLException ex) {
            throw new DataException("Error closing statements", ex);
        }
        super.destroy();
    }
    
    @Override
    public CandidaturaProxy createCandidatura() {
        return new CandidaturaProxy(getDataLayer());
    }
    
    @Override
    public CandidaturaProxy createCandidatura(ResultSet rs) throws DataException {
        CandidaturaProxy c = createCandidatura();
        try {
            c.setId_studente(rs.getInt("id_studente"));
            c.setId_offerta_tirocinio(rs.getInt("id_offerta_tirocinio"));
            c.setId_tutore_uni(rs.getInt("id_tutore_uni"));
            c.setCfu(rs.getInt("cfu"));
            c.setStatoCandidatura(rs.getInt("stato_candidatura"));
            c.setSrcDocCandidatura(rs.getString("src_documento_candidatura"));
            if (rs.getDate("data_inizio") != null)
                c.setInizioTirocinio(rs.getDate("data_inizio").toLocalDate());
            if (rs.getDate("data_fine") != null)
                c.setFineTirocinio(rs.getDate("data_fine").toLocalDate());
            c.setTms(rs.getDate("tms"));
            c.setDiploma(rs.getString("diploma"));
            c.setLaurea(rs.getString("laurea"));
            c.setDottoratoRicerca(rs.getString("dottorato_ricerca"));
            c.setSpecializzazione(rs.getString("specializzazione"));
        } catch (SQLException ex) {
            throw new DataException("Unable to create candidatura from resulset", ex);
        }
        return c;
    }
    
    @Override
    public Candidatura getCandidatura(int id_studente, int id_offerta_tirocinio) throws DataException {
        try {
            sCandidatura.setInt(1, id_studente);
            sCandidatura.setInt(2, id_offerta_tirocinio);
            try (ResultSet rs = sCandidatura.executeQuery()) {
                if (rs.next()) {
                    return createCandidatura(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load candidatura by studente and tirocinio", ex);
        }
        return null;
    }
    
    @Override
    public List<Candidatura> getCandidature(Studente st) throws DataException {
        List<Candidatura> result = new ArrayList();
        try {
            sCandidatureByStudente.setInt(1, st.getUtente().getId());
            try (ResultSet rs = sCandidatureByStudente.executeQuery()) {
                while (rs.next()) {
                    result.add(getCandidatura(rs.getInt("id_studente"), rs.getInt("id_offerta_tirocinio")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load candidature by studente", ex);
        }
        return result;
    }
    
    @Override
    public List<Candidatura> getCandidature(int id_ot, int stato) throws DataException {
        List<Candidatura> result = new ArrayList();
        try {
            sCandidatureByTirocinioAndStato.setInt(1, id_ot);
            sCandidatureByTirocinioAndStato.setInt(2, stato);
            try (ResultSet rs = sCandidatureByTirocinioAndStato.executeQuery()) {
                while (rs.next()) {
                    result.add(getCandidatura(rs.getInt("id_studente"), rs.getInt("id_offerta_tirocinio")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load candidature by tirocinio", ex);
        }
        return result;
    }
    
    @Override
    public List<Candidatura> getCandidature(OffertaTirocinio ot) throws DataException {
        List<Candidatura> result = new ArrayList();
        try {
            sCandidatureByTirocinio.setInt(1, ot.getId());
            try (ResultSet rs = sCandidatureByTirocinio.executeQuery()) {
                while (rs.next()) {
                    result.add(getCandidatura(rs.getInt("id_studente"), rs.getInt("id_offerta_tirocinio")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load candidature by tirocinio", ex);
        }
        return result;
    }
    
    @Override
    public int insertCandidatura(Candidatura c) throws DataException {
        try {
            if (c.getStudente() != null)
                iCandidatura.setInt(1, c.getStudente().getUtente().getId());
            else
                iCandidatura.setNull(1, java.sql.Types.INTEGER);
            if(c.getOffertaTirocinio() != null)
                iCandidatura.setInt(2, c.getOffertaTirocinio().getId());
            else
                iCandidatura.setNull(2, java.sql.Types.INTEGER);
            if(c.getTutoreUni() != null)
                iCandidatura.setInt(3, c.getTutoreUni().getId());
            else
                iCandidatura.setNull(3, java.sql.Types.INTEGER);
            iCandidatura.setInt(4, c.getCfu());
            iCandidatura.setString(5, c.getDiploma());
            iCandidatura.setString(6, c.getLaurea());
            iCandidatura.setString(7, c.getDottoratoRicerca());
            iCandidatura.setString(8, c.getSpecializzazione());
            return iCandidatura.executeUpdate();
        } catch(SQLException ex) {
            throw new DataException("Unable to insert new candidatura", ex);
        }
    }
    
    @Override
    public int updateCandidaturaStato(int stato, int id_st, int id_ot) throws DataException {
        try {
            uCandidaturaStato.setInt(1, stato);
            uCandidaturaStato.setInt(2, id_st);
            uCandidaturaStato.setInt(3, id_ot);
            return uCandidaturaStato.executeUpdate();
        } catch(SQLException ex) {
            throw new DataException("Unable to update candidatura stato", ex);
        }
    }
    
    @Override
    public int updateCandidatura(Candidatura c) throws DataException {
        try {
            uCandidatura.setInt(1, c.getCfu());
            uCandidatura.setString(2, c.getDiploma());
            uCandidatura.setString(3, c.getLaurea());
            uCandidatura.setString(4, c.getDottoratoRicerca());
            uCandidatura.setString(5, c.getSpecializzazione());
            uCandidatura.setString(6, c.getSrcDocCandidatura());
            if (c.getInizioTirocinio() != null) {
                uCandidatura.setDate(7, java.sql.Date.valueOf(c.getInizioTirocinio().plusDays(1)));
            } else {
                uCandidatura.setNull(7, java.sql.Types.DATE);
            }
            if (c.getFineTirocinio() != null) {
                uCandidatura.setDate(8, java.sql.Date.valueOf(c.getFineTirocinio().plusDays(1)));
            } else {
                uCandidatura.setNull(8, java.sql.Types.DATE);
            }
            uCandidatura.setInt(9, c.getStatoCandidatura());
            uCandidatura.setInt(10, c.getOffertaTirocinio().getId());
            uCandidatura.setInt(11, c.getStudente().getUtente().getId());
            return uCandidatura.executeUpdate();
        } catch(SQLException ex) {
            throw new DataException("Unable to update candidatura", ex);
        }
    }
    
}
