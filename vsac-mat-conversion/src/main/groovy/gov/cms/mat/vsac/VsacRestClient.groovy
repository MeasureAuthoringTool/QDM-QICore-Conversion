package gov.cms.mat.vsac

import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.GetMethod
import org.vsac.VSACGroovyClient
import org.vsac.VSACResponseResult

class VsacRestClient extends VSACGroovyClient {
    VsacRestClient(String proxyServer, int proxyPort, String vsacServerURL, String vsacServiceURL, String vsacReteriveServiceURL, String profileServiceURL, String versionServiceURL, String drcVsacUrl) {
        super(proxyServer, proxyPort, vsacServerURL, vsacServiceURL, vsacReteriveServiceURL, profileServiceURL, versionServiceURL, drcVsacUrl)
    }

    VSACResponseResult getVsacDataForConversion(String oid, String serviceTicket, String profile) {
        VSACResponseResult vsacResponseResult = new VSACResponseResult()
        if (serviceTicket == null) {
            return null
        }
        GetMethod method = new GetMethod(retieriveMultiOIDSService)

        method.setQueryString(([new NameValuePair("id", oid), new NameValuePair("profile", profile)
                                , new NameValuePair("ticket", serviceTicket)
                              /*  new NameValuePair("includeDraft", "yes") */
        ].toArray(new NameValuePair[3])))

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

    /**
     * Method to remove UTF8BOM characters from retrieve xml from VSAC. private in vsac lib
     * */
    private String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

}