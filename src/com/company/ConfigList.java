package com.company;

class ConfigList{
    
    private String userName;
    private String serverURL;
    private String outDirName;
    private String linksFileName;

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }
    public void setOutDirName(String outDirName) { this.outDirName = outDirName; }
    public void setLinksFileName(String linksFileName) {
        this.linksFileName = linksFileName;
    }

    public String getUserName() { return userName; }
    public String getServerURL() { return serverURL; }
    public String getOutDirName() { return outDirName; }
    public String getLinksFileName() { return linksFileName; }
}
