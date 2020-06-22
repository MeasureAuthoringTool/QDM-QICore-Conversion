package gov.cms.mat.vsac

import groovy.json.JsonSlurper
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpConnectionManager
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.params.HttpClientParams
import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import org.apache.commons.httpclient.util.EncodingUtil

import java.util.logging.Logger

/**
 * @author MAT Team
 *
 */
class MyVSACGroovyClient {
    static final Logger LOG = Logger.getLogger(MyVSACGroovyClient.class.getName())
    String server
    String service
    String retieriveMultiOIDSService
    String profileService
    String retieriveVersionListForOidService
    String drcUrl
    HttpClient client
    static final String UTF8_BOM = "\uFEFF";
    static final int TIMEOUT_PERIOD = 5 * 60 * 1000
    static final int REQUEST_TIMEDOUT = 3
    static final int REQUEST_FAILED = 4

    /**
     * Constructor
     * */
    MyVSACGroovyClient(String proxyServer, int proxyPort, String vsacServerURL, String vsacServiceURL, String vsacReteriveServiceURL, String profileServiceURL, String versionServiceURL, String drcVsacUrl) {
        HttpConnectionManager manager = new MultiThreadedHttpConnectionManager()
        manager.setParams(new HttpConnectionManagerParams())
        HttpClientParams params = new HttpClientParams()
        params.setSoTimeout(TIMEOUT_PERIOD)
        HttpClient httpClient = new HttpClient(params, manager)
        if (proxyServer)
            httpClient.getHostConfiguration().setProxy(proxyServer, proxyPort)
        client = httpClient
        server = vsacServerURL
        service = vsacServiceURL
        retieriveMultiOIDSService = vsacReteriveServiceURL
        profileService = profileServiceURL
        retieriveVersionListForOidService = versionServiceURL
        drcUrl = drcVsacUrl
        LOG.info(drcUrl)
    }
    /**
     * Eight hour Ticket granting service call.
     * @param username
     * @param password
     * @return 8 Hours Ticket Granting Ticket in String.
     */
    String getTicketGrantingTicket(String username, String password) {
        def eightHourTicket = null
        PostMethod post = new PostMethod(server)
        post.setRequestBody([new NameValuePair("username", username), new NameValuePair("password", password)].toArray(new NameValuePair[2]))
        LOG.info "VSAC URL inside getTicketGrantingTicket method : " + post.getURI()
        try {
            client.executeMethod(post)
            switch (post.getStatusCode()) {
                case 200:
                    eightHourTicket = post.getResponseBodyAsString()
                    LOG.info("Eight Hours Ticket from VSAC === " + eightHourTicket)
                    break
                default:
                    LOG.warning("Invalid response code from VSAC server!")
                    break
            }
        } catch (final IOException e) {
            LOG.warning(e.getMessage())
        } finally {
            post.releaseConnection()
        }
        return eightHourTicket
    }
    /**
     * Five min Ticket granting service call.
     * @param ticketGrantingTicket
     * @return Five Min Service Ticket in String.
     */
    String getServiceTicket(String ticketGrantingTicket) {
        if (!ticketGrantingTicket)
            return null
        def eightMinTicketURL = server + "/" + ticketGrantingTicket
        PostMethod post = new PostMethod(eightMinTicketURL)
        post.setRequestBody([new NameValuePair("service", service)].toArray(new NameValuePair[1]))
        LOG.info "VSAC URL inside getServiceTicket method : " + post.getURI()
        def srviceTicketFiveMin = null
        try {
            client.executeMethod(post)
            switch (post.getStatusCode()) {
                case 200:
                    srviceTicketFiveMin = post.getResponseBodyAsString()
                    break
                default:
                    LOG.warning("Invalid response code (" + post.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (final IOException e) {
            LOG.warning(e.getMessage())
        } finally {
            post.releaseConnection()
        }
        return srviceTicketFiveMin
    }

    void notNull(Object object, String message) {
        if (object == null)
            throw new IllegalArgumentException(message)
    }

    /**
     * Retrieve All profile List.
     * @param serviceTicket
     * @return VSACResponseResult
     */
    MyVSACResponseResult getProfileList(String serviceTicket) {
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        if (serviceTicket == null) {
            return null
        }
        GetMethod method = new GetMethod(profileService);
        method.setQueryString([new NameValuePair("ticket", serviceTicket)].toArray(new NameValuePair[1]))
        LOG.info "VSAC URL inside getProfileList method : " + method.getURI()
        def responseString = null
        try {
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(),
                            "UTF-8");
                    BufferedReader r = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    boolean firstLine = true;
                    for (String s = ""; (s = r.readLine()) != null;) {
                        if (firstLine) {
                            s = removeUTF8BOM(s);
                            firstLine = false;
                        }
                        stringBuilder.append(s);
                    }
                    responseString = stringBuilder.toString()
                    LOG.info(responseString)
                    vsacResponseResult.setXmlPayLoad(responseString)
                    break
                default:
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (Exception e) {
            LOG.warning("EXCEPTION IN VSAC JAR: getProfileList..")
            if (e instanceof java.net.SocketTimeoutException) {
                vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
            } else {
                vsacResponseResult.setFailReason(REQUEST_FAILED);
            }
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }

    /**
     * Multiple Value Set Retrieval based on oid.
     * @param oid
     * @param serviceTicket
     * @return VSACResponseResult
     */
    MyVSACResponseResult getMultipleValueSetsResponseByOID(String oid, String serviceTicket, String profile) {
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        if (serviceTicket == null) {
            return null
        }
        GetMethod method = new GetMethod(retieriveMultiOIDSService)
        method.setQueryString(([new NameValuePair("id", oid), new NameValuePair("profile", profile)
                                , new NameValuePair("ticket", serviceTicket), new NameValuePair("includeDraft", "yes")].toArray(new NameValuePair[4])))
        LOG.info "VSAC URL inside getMultipleValueSetsResponseByOID method : " + method.getURI()
        def responseString = null
        try {
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(),
                            "UTF-8");
                    BufferedReader r = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    boolean firstLine = true;
                    for (String s = ""; (s = r.readLine()) != null;) {
                        if (firstLine) {
                            s = removeUTF8BOM(s);
                            firstLine = false;
                        }
                        stringBuilder.append(s);
                    }
                    responseString = stringBuilder.toString()
                    LOG.info(responseString)
                    vsacResponseResult.setXmlPayLoad(responseString)
                    break
                default:
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (Exception e) {
            LOG.warning("EXCEPTION IN VSAC JAR: getTicketGrantingTicket..")
            if (e instanceof java.net.SocketTimeoutException) {
                vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
            } else {
                vsacResponseResult.setFailReason(REQUEST_FAILED);
            }
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }
    /*
     * Method to reterive Direct Reference Codes
     * from VSAC with default resultSet which is standard.
     *
     * */

    MyVSACResponseResult getDirectReferenceCode(String codeURLString, String serviceTicket) {
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        if (serviceTicket == null) {
            return null
        }
        try {
            codeURLString = codeURLString.replaceAll(" ", "%20")
            GetMethod method = new GetMethod(drcUrl + codeURLString)
            method.setQueryString([new NameValuePair("ticket", serviceTicket), new NameValuePair("resultFormat", "xml"),
                                   new NameValuePair("resultSet", "standard")].toArray(new NameValuePair[3]))

            LOG.info "VSAC URL inside getDirectReferenceCode method : " + method.getURI()
            def responseString = null
            try {
                client.executeMethod(method)
                switch (method.getStatusCode()) {
                    case 200:
                        InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(),
                                "UTF-8");
                        BufferedReader r = new BufferedReader(inputStreamReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        boolean firstLine = true;
                        for (String s = ""; (s = r.readLine()) != null;) {
                            if (firstLine) {
                                s = removeUTF8BOM(s);
                                firstLine = false;
                            }
                            stringBuilder.append(s);
                        }
                        responseString = stringBuilder.toString()
                        LOG.info(responseString)
                        vsacResponseResult.setXmlPayLoad(responseString)
                        break
                    default:

                        def (BufferedReader r, StringBuilder stringBuilder) = getShit(method)
                        responseString = getMoreShit(r, stringBuilder)

                        System.out.println(responseString)

                        LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")

                        break
                }
            } catch (Exception e) {
                LOG.warning("EXCEPTION IN VSAC JAR: getDirectReferenceCode..")
                if (e instanceof java.net.SocketTimeoutException) {

                } else {

                }
            } finally {
                method.releaseConnection()
            }
        } catch (Exception e) {
            LOG.warning("Illegal Argument..")
        }
        return vsacResponseResult
    }

    /**
     * Multiple Value Set Retrieval based on oid and version.
     * @param oid
     * @param version
     * @param serviceTicket
     * @return VSACResponseResult
     */
    MyVSACResponseResult getMultipleValueSetsResponseByOIDAndVersion(String oid, String version, String serviceTicket) {
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        if (serviceTicket == null) {
            return null
        }
        GetMethod method = new GetMethod(retieriveMultiOIDSService)
        method.setQueryString(([new NameValuePair("id", oid), new NameValuePair("version", version),
                                new NameValuePair("ticket", serviceTicket)
        ].toArray(new NameValuePair[3])))
        LOG.info "VSAC URL inside getMultipleValueSetsResponseByOIDAndVersion method : " + method.getURI()
        def responseString = null
        try {
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    def (BufferedReader r, StringBuilder stringBuilder) = getShit(method)
                    responseString = getMoreShit(r, stringBuilder)
                    LOG.info(responseString)
                    vsacResponseResult.setXmlPayLoad(responseString)
                    break
                default:
                    def (BufferedReader r, StringBuilder stringBuilder) = getShit(method)
                    responseString = getMoreShit(r, stringBuilder)
                    LOG.info(responseString)
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (Exception e) {
            LOG.warning("EXCEPTION IN VSAC JAR: getTicketGrantingTicket..")
            if (e instanceof java.net.SocketTimeoutException) {
                vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
            } else {
                vsacResponseResult.setFailReason(REQUEST_FAILED);
            }
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }

    private String getMoreShit(BufferedReader r, StringBuilder stringBuilder) {
        def responseString
        boolean firstLine = true;
        for (String s = ""; (s = r.readLine()) != null;) {
            if (firstLine) {
                s = removeUTF8BOM(s);
                firstLine = false;
            }
            stringBuilder.append(s);
        }
        responseString = stringBuilder.toString()
        responseString
    }

    private List getShit(GetMethod method) {
        InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(),
                "UTF-8");
        BufferedReader r = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        [r, stringBuilder]
    }
    /**
     * Multiple Value Set Retrieval based on oid and effective date.
     * @param oid
     * @param effectiveDate
     * @param serviceTicket
     * @return VSACResponseResult
     */
    MyVSACResponseResult getMultipleValueSetsResponseByOIDAndEffectiveDate(String oid, String effectiveDate, String serviceTicket) {
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        if (serviceTicket == null) {
            return null
        }
        GetMethod method = new GetMethod(retieriveMultiOIDSService)
        method.setQueryString(([new NameValuePair("id", oid), new NameValuePair("effectiveDate", effectiveDate),
                                new NameValuePair("ticket", serviceTicket),
                                new NameValuePair("ReleaseType", "VSAC"), new NameValuePair("IncludeDraft", "yes")
        ].toArray(new NameValuePair[5])))
        LOG.info "VSAC URL inside getMultipleValueSetsResponseByOIDAndEffectiveDate method : " + method.getURI()
        def responseString = null
        try {
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(),
                            "UTF-8");
                    BufferedReader r = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    boolean firstLine = true;
                    for (String s = ""; (s = r.readLine()) != null;) {
                        if (firstLine) {
                            s = removeUTF8BOM(s);
                            firstLine = false;
                        }
                        stringBuilder.append(s);
                    }
                    responseString = stringBuilder.toString()
                    LOG.info(responseString)
                    vsacResponseResult.setXmlPayLoad(responseString)
                    break
                default:
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (Exception e) {
            LOG.warning("EXCEPTION IN VSAC JAR: getTicketGrantingTicket..")
            if (e instanceof java.net.SocketTimeoutException) {
                vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
            } else {
                vsacResponseResult.setFailReason(REQUEST_FAILED);
            }
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }

    /**
     * Multiple Value Set Retrieval based on oid and Profile Name.
     * @param oid
     * @param effectiveDate
     * @param serviceTicket
     * @return VSACResponseResult
     */
    MyVSACResponseResult getMultipleValueSetsResponseByOIDAndProfile(String oid, String profile, String serviceTicket) {
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        if (serviceTicket == null) {
            return null
        }
        GetMethod method = new GetMethod(retieriveMultiOIDSService)
        def queryString = EncodingUtil.formUrlEncode(([new NameValuePair("id", oid), new NameValuePair("profile", profile),
                                                       new NameValuePair("ticket", serviceTicket), new NameValuePair("includeDraft", "yes")].toArray(new NameValuePair[4])), "UTF-8");
        method.setQueryString(queryString)
        LOG.info "VSAC URL inside getMultipleValueSetsResponseByOIDAndEffectiveDate method : " + queryString
        def responseString = null
        try {
            URLEncoder.encode(method.getQueryString(), "UTF-8")
            LOG.info "VSAC URL inside getMultipleValueSetsResponseByOIDAndEffectiveDate method Encoding done : " + method.getURI()
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(),
                            "UTF-8");
                    BufferedReader r = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    boolean firstLine = true;
                    for (String s = ""; (s = r.readLine()) != null;) {
                        if (firstLine) {
                            s = removeUTF8BOM(s);
                            firstLine = false;
                        }
                        stringBuilder.append(s);
                    }
                    responseString = stringBuilder.toString()
                    LOG.info(responseString)
                    vsacResponseResult.setXmlPayLoad(responseString)
                    break
                default:
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (Exception e) {
            LOG.warning("EXCEPTION IN VSAC JAR: getTicketGrantingTicket..")
            if (e instanceof java.net.SocketTimeoutException) {
                vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
            } else {
                vsacResponseResult.setFailReason(REQUEST_FAILED);
            }
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }

    /**
     * Method to remove UTF8BOM characters from retrieve xml from VSAC.
     * */
    private String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * Retrieve All Programs List.
     * @return VSACResponseResult
     */
    MyVSACResponseResult getAllPrograms() {
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        GetMethod method = new GetMethod("https://vsac.nlm.nih.gov/vsac/programs");
        LOG.info "VSAC URL inside getProgramsList method : " + method.getURI()
        try {
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    def responseString = method.getResponseBodyAsString();
                    def slurper = new JsonSlurper();
                    def jsonResult = slurper.parseText(responseString);
                    List<Map> programs = (List) jsonResult.get("Program");
                    List<String> names = new ArrayList<String>();
                    for (item in programs) {
                        names.add((String) item.get("name"));
                    }
                    vsacResponseResult.setPgmRels(names);
                    break
                default:
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (Exception e) {
            LOG.warning("EXCEPTION IN VSAC JAR: getAllPrograms..")
            if (e instanceof java.net.SocketTimeoutException) {
                vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
            } else {
                vsacResponseResult.setFailReason(REQUEST_FAILED);
            }
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }

    /**
     * Retrieve list of Releases for a given Program.
     * @param serviceTicket
     * @return VSACResponseResult
     */
    MyVSACResponseResult getReleasesOfProgram(String program) {
        String releaseURL = "https://vsac.nlm.nih.gov/vsac/program/" + program;
        releaseURL = releaseURL.replaceAll(" ", "%20");
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        GetMethod method = new GetMethod(releaseURL);
        LOG.info "VSAC URL inside getProgramsList method : " + method.getURI()
        try {
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    def responseString = method.getResponseBodyAsString();
                    def slurper = new JsonSlurper();
                    def jsonResult = slurper.parseText(responseString);
                    List<Map> releases = (List) jsonResult.get("release");
                    List<String> names = new ArrayList<String>();
                    for (item in releases) {
                        names.add((String) item.get("name"));
                    }
                    vsacResponseResult.setPgmRels(names);
                    break
                default:
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (Exception e) {
            LOG.warning("EXCEPTION IN VSAC JAR: getReleasesOfProgram..")
            if (e instanceof java.net.SocketTimeoutException) {
                vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
            } else {
                vsacResponseResult.setFailReason(REQUEST_FAILED);
            }
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }

    MyVSACResponseResult getMultipleValueSetsResponseByOIDAndRelease(String oid, String release, String serviceTicket) {
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        if (serviceTicket == null) {
            return null
        }
        GetMethod method = new GetMethod(retieriveMultiOIDSService)
        method.setQueryString(([new NameValuePair("id", oid), new NameValuePair("release", release),
                                new NameValuePair("ticket", serviceTicket)].toArray(new NameValuePair[3])))
        LOG.info "VSAC URL inside getMultipleValueSetsResponseByOIDAndRelease method : " + method.getURI()
        def responseString = null
        try {
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8");
                    BufferedReader r = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    boolean firstLine = true;
                    for (String s = ""; (s = r.readLine()) != null;) {
                        if (firstLine) {
                            s = removeUTF8BOM(s);
                            firstLine = false;
                        }
                        stringBuilder.append(s);
                    }
                    responseString = stringBuilder.toString()
                    LOG.info(responseString)
                    vsacResponseResult.setXmlPayLoad(responseString)
                    break
                default:
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (Exception e) {
            LOG.warning("EXCEPTION IN VSAC JAR: getTicketGrantingTicket..")
            if (e instanceof java.net.SocketTimeoutException) {
                vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
            } else {
                vsacResponseResult.setFailReason(REQUEST_FAILED);
            }
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }

    MyVSACResponseResult getLatestProfileOfProgram(String programName) {
        String releaseURL = drcUrl + "/program/" + programName + "/latest profile";
        releaseURL = releaseURL.replaceAll(" ", "%20");
        MyVSACResponseResult vsacResponseResult = new MyVSACResponseResult()
        GetMethod method = new GetMethod(releaseURL);
        LOG.info "VSAC URL inside getProgramsList method : " + method.getURI()
        try {
            client.executeMethod(method)
            switch (method.getStatusCode()) {
                case 200:
                    def responseString = method.getResponseBodyAsString();
                    def slurper = new JsonSlurper();
                    def jsonResult = slurper.parseText(responseString);
                    String name = (String) jsonResult.get("name");
                    vsacResponseResult.setXmlPayLoad(name)
                    break
                default:
                    LOG.warning("Invalid response code (" + method.getStatusCode() + ") from VSAC server!")
                    break
            }
        } catch (SocketTimeoutException e) {
            LOG.warning("Timeout Exception: getLatestProfileOfProgram.." + e.getMessage())
            vsacResponseResult.setFailReason(REQUEST_TIMEDOUT);
        } catch (Exception e) {
            LOG.warning("Exception: getLatestProfileOfProgram.." + e.getMessage())
            vsacResponseResult.setFailReason(REQUEST_FAILED);
        } finally {
            method.releaseConnection()
        }
        return vsacResponseResult
    }

}