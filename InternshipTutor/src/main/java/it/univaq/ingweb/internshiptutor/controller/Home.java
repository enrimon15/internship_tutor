/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.univaq.ingweb.internshiptutor.controller;

import it.univaq.ingweb.framework.data.DataException;
import it.univaq.ingweb.framework.result.FailureResult;
import it.univaq.ingweb.framework.security.SecurityLayer;
import it.univaq.ingweb.framework.result.TemplateManagerException;
import it.univaq.ingweb.framework.result.TemplateResult;
import it.univaq.ingweb.internshiptutor.data.dao.InternshipTutorDataLayer;
import it.univaq.ingweb.internshiptutor.data.model.Azienda;
import it.univaq.ingweb.internshiptutor.data.model.Candidatura;
import it.univaq.ingweb.internshiptutor.data.model.OffertaTirocinio;
import it.univaq.ingweb.internshiptutor.data.model.Studente;
import it.univaq.ingweb.internshiptutor.data.model.Utente;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Stefano Florio
 */
public class Home extends InternshipTutorBaseController {
    
    private void action_error(HttpServletRequest request, HttpServletResponse response) {
        if (request.getAttribute("exception") != null) {
            (new FailureResult(getServletContext())).activate((Exception) request.getAttribute("exception"), request, response);
        } else {
            (new FailureResult(getServletContext())).activate((String) request.getAttribute("message"), request, response);
        }
    }
    
    private void action_anonymous(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, TemplateManagerException {
        
        request.setAttribute("page_title", "Home anonimo");
        TemplateResult res = new TemplateResult(getServletContext());
        res.activate("home_anonimo.ftl.html", request, response);    
    }
    
    private void action_admin(HttpServletRequest request, HttpServletResponse response, int id_utente) throws IOException {
        try {
            // dati dell'admin
            Utente ut = ((InternshipTutorDataLayer)request.getAttribute("datalayer")).getUtenteDAO().getUtente(id_utente);
            request.setAttribute("nome_utente", ut.getUsername());
            
            // lista aziende registrate, ovvero in attesa di convenzione
            List<Azienda> az_registrate = ((InternshipTutorDataLayer)request.getAttribute("datalayer")).getAziendaDAO().getAziendeByStato(0);
            request.setAttribute("az_registrate", az_registrate);
            
            // lista azinde convenzionate
            List<Azienda> az_convenzionate = ((InternshipTutorDataLayer)request.getAttribute("datalayer")).getAziendaDAO().getAziendeByStato(1);
            request.setAttribute("az_convenzionate", az_convenzionate);
            
            
            TemplateResult res = new TemplateResult(getServletContext());
            request.setAttribute("page_title", "Dashboard admin");
            res.activate("home_admin.ftl.html", request, response);
        } catch (TemplateManagerException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DataException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    private void action_azienda(HttpServletRequest request, HttpServletResponse response, int id_utente)
            throws IOException {
        try {
            // dati dell'azienda
            Azienda az = ((InternshipTutorDataLayer)request.getAttribute("datalayer")).getAziendaDAO().getAzienda(id_utente);
            request.setAttribute("nome_utente", az.getRagioneSociale());
            request.setAttribute("azienda", az);
            
            // lista delle offerte di tirocinio dell'azienda (sia attive che oscurate)
            List<OffertaTirocinio> tirocini_attivi = ((InternshipTutorDataLayer)request.getAttribute("datalayer")).getOffertaTirocinioDAO().getOfferteTirocinio(az, true);
            request.setAttribute("ot_attive", tirocini_attivi);
            
             // lista delle offerte di tirocinio dell'azienda (sia attive che oscurate)
            List<OffertaTirocinio> tirocini_disattivi = ((InternshipTutorDataLayer)request.getAttribute("datalayer")).getOffertaTirocinioDAO().getOfferteTirocinio(az, false);
            request.setAttribute("ot_disattive", tirocini_disattivi);
            
            TemplateResult res = new TemplateResult(getServletContext());
            request.setAttribute("page_title", "Home azienda");
        
            res.activate("home_azienda.ftl.html", request, response);
        } catch (TemplateManagerException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DataException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void action_studente(HttpServletRequest request, HttpServletResponse response, int id_utente) 
            throws IOException {
        try { 
            // dati dello studente
            Studente st = ((InternshipTutorDataLayer)request.getAttribute("datalayer")).getStudenteDAO().getStudente(id_utente);
            request.setAttribute("nome_utente", st.getNome() + " " + st.getCognome());
            
            // lista delle candidature dello studente
            List<Candidatura> candidature = ((InternshipTutorDataLayer)request.getAttribute("datalayer")).getCandidaturaDAO().getCandidature(st);
            request.setAttribute("candidature", candidature);
            
            TemplateResult res = new TemplateResult(getServletContext());
            request.setAttribute("page_title", "Home studente");
        
            res.activate("home_studente.ftl.html", request, response);
        } catch (TemplateManagerException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DataException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("activeHome", "active");

            HttpSession s = SecurityLayer.checkSession(request);
            if (s == null) {
                action_anonymous(request, response);
            } else {
                int id_utente = (int)s.getAttribute("id_utente");
                request.setAttribute("nome_utente", (String)s.getAttribute("username"));
                switch ((String) s.getAttribute("tipologia")) {
                    case "ad":
                        action_admin(request, response, id_utente);
                        break;
                    case "st":
                        action_studente(request, response, id_utente);
                        break;
                    case "az":
                        action_azienda(request, response, id_utente);
                }
            }
        } catch (IOException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);
        } catch (TemplateManagerException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);
        }
    }

}
