package gov.cms.mat.vsac

class MyVSACResponseResult {
    String xmlPayLoad = "";
    //Added to handle programs and releases
    List<String> pgmRels;
    boolean isFailResponse = false;
    int failReason;
}
