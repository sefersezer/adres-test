package com.paritus.tool;

import java.net.HttpURLConnection;
import java.net.URL;

public class Connect {
  public static void checkConnectionStatus(String address,int statusCode) throws Exception {
    URL url = new URL(address);
    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
  try {
    connection.connect();
    int httpStatusCode = connection.getResponseCode(); // 200, 404 etc.
    if (httpStatusCode != statusCode) {
      System.out
          .println(String
              .format("%s kodu icin %s adresine baglanilamadi. 60sn bekleniliyor.",
                  statusCode, address));
      Thread.sleep(60000);
      if (httpStatusCode != statusCode) {
        System.out.println(String.format("baglanildi %s", address));
        return;
      } else {
        throw new Exception(
            "60sn sonra bile sunucuya baglanilamadi");
      }
    } else {
      System.out.println(String.format("baglanildi %s", address));
      return;
    }
  } catch (Exception e) {
    throw new Exception(e.getMessage());
  }
}
}
