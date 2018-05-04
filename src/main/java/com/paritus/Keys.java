package com.paritus;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Keys {

  private static Keys instance;
  private static String fileRootPath;
  private Properties prop = new Properties();

  public static Keys getInstance() {
    if (instance == null) {
      instance = new Keys();
      instance.load();
    }
    return instance;
  }

  public static void setFileLocation(String fileRootPath) {
    if (fileRootPath.charAt(fileRootPath.length() - 1) != '/') {
      fileRootPath += "/";
    }
    Keys.fileRootPath = fileRootPath;
  }

  private void load() {
    String filename = fileRootPath + "keys.properties";
    InputStream input = null;
    try {
      input = new FileInputStream(filename);
      prop.load(input);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public String getServer() {
    return prop.getProperty("server");
  }

  public String getAdminApiKey() {
    return prop.getProperty("adminapikey");
  }

  public String getAdminUser() {
    return prop.getProperty("adminuser");
  }

  public String getAdminPwd() {
    return prop.getProperty("adminpassword");
  }

  public String getCleanParsetokenInputFile() {
    return fileRootPath + prop.getProperty("cleanParsetokenInputFile");
  }

  public String getCleanParsetokenOutputFile() {
    return fileRootPath + prop.getProperty("cleanParsetokenOutputFile");
  }

  public String getCleanParseaddressInputFile() {
    return fileRootPath + prop.getProperty("cleanParseaddressInputFile");
  }

  public String getCleanParseaddressOutputFile() {
    return fileRootPath + prop.getProperty("cleanParseaddressOutputFile");
  }

  public String getDirtyParsetokenInputFile() {
    return fileRootPath + prop.getProperty("dirtyParsetokenInputFile");
  }

  public String getDirtyParsetokenOutputFile() {
    return fileRootPath + prop.getProperty("dirtyParsetokenOutputFile");
  }

  public String getDirtyParseaddressInputFile() {
    return fileRootPath + prop.getProperty("dirtyParseaddressInputFile");
  }

  public String getDirtyParseaddressOutputFile() {
    return fileRootPath + prop.getProperty("dirtyParseaddressOutputFile");
  }

  public String checkUndefined() {
    return prop.getProperty("checkUndefined");
  }

  public String getReversegeocodingSource() {
    return fileRootPath + prop.getProperty("reversegeocodingSource");
  }

  public String getReversegeocodingTarget() {
    return fileRootPath + prop.getProperty("reversegeocodingTarget");
  }

  public String getDefaultRadius() {
    return prop.getProperty("defaultRadius");
  }
  
  public String getStreetRadius() {
    return prop.getProperty("streetRadius");
  }

}
