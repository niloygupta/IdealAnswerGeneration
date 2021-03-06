package edu.cmu.lti.oaqa.bioasq.idealanswer;


import com.google.common.collect.Lists;
import gov.nih.nlm.uts.webservice.finder.Psf;
import gov.nih.nlm.uts.webservice.finder.UiLabel;
import gov.nih.nlm.uts.webservice.finder.UtsWsFinderController;
import gov.nih.nlm.uts.webservice.finder.UtsWsFinderControllerImplService;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;
import gov.nih.nlm.uts.webservice.security.UtsWsSecurityController;
import gov.nih.nlm.uts.webservice.security.UtsWsSecurityControllerImplService;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Wrapper class for UMLS metathesaurus service.
 *
 * @author junjiah
 */
public class UMLSService {

  private static final String UMLS_RELEASE = "2014AB";

  private static final String SERVICE_NAME = "http://umlsks.nlm.nih.gov";

  private static final UtsWsFinderController utsFinderService = (new UtsWsFinderControllerImplService())
          .getUtsWsFinderControllerImplPort();

  private static final UtsWsSecurityController securityService = (new UtsWsSecurityControllerImplService())
          .getUtsWsSecurityControllerImplPort();

  private static String ticketGrantingTicket;

  private static UMLSService umlsService = null;

  /**
   * Singleton service instance.
   */
  private UMLSService() {
    try {
      String pw;
      try {
        pw = new String(Base64.decodeBase64("MTE3OTFzdWNrcyE="), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        pw = "";
      }
      ticketGrantingTicket = securityService.getProxyGrantTicket("yangzi", "Oaqa12#$");
    } catch (UtsFault_Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Return the singleton service instance.
   *
   * @return
   */
  public static UMLSService getInstance() {
    if (umlsService == null)
      umlsService = new UMLSService();
    return umlsService;
  }

  /**
   * Retrieve the synonyms from UMLS metathesaurus service.
   *
   * @param searchWord Strings to be searched on UMLS
   * @return Unique synonyms on UMLS for the searched word
   */
  public List<String> getSynonyms(String searchWord) {
    try {
      String singleUseTicket = securityService.getProxyTicket(ticketGrantingTicket, SERVICE_NAME);
      Psf myPsf = new Psf();
      myPsf.setPageLn(50);
      List<UiLabel> uiLabels = utsFinderService.findAtoms(singleUseTicket, UMLS_RELEASE, "atom",
              searchWord, "words", myPsf);
      return uiLabels
              .stream()
              .map(u -> u.getLabel().toLowerCase())
              .distinct()
              .collect(toList());
    } catch (Exception e) {
      return Lists.newArrayList();
    }
  }
}
