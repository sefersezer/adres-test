package com.paritus.addresstest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import com.paritus.Keys;
import com.paritus.client.ParitusClient;
import com.paritus.client.ParitusClientImpl;
import com.paritus.dto.AddressParseResult2;
import com.paritus.dto.ParsedToken;
import com.paritus.tool.Connect;
import com.paritus.tool.Methods;

public class TestSingleAddresses {

  private static final String UTF8 = "UTF8";

  private static final String EQUAL = "=";

  private static final String DOTSPLITTER = "\\.";
  private static final String DOT = ".";

  private static final String STARS = " ** ";
  private static final String COMMA = ", ";

  private static final String BARSPLIT = "\\|";
  private static final String NULL = "NULL";

  private static final String DOTPREFIX = DOT + "PREFIX";
  private static final String DOTSUFFIX = DOT + "SUFFIX";

  private static final String ERROR = " ERROR = ";
  private static final String TAB = "\t";
  private static final String CLEAN = "Clean";

  private static final String UNDEFINED_FOUND = "UNDEFINED FOUND";
  private static final String UNDEFINED = "UNDEFINED";
  private static String APIKEY;
  private static String SERVER;

  // D:/maven-projects/adresTest/testfiles
  public static void main(String[] args) throws Exception {
    try {
      int sumRow = 0;
      if (args[0] != null) {
        Keys.setFileLocation(String.valueOf(args[0]));
        APIKEY = Keys.getInstance().getAdminApiKey();
        SERVER = Keys.getInstance().getServer();
        Connect.checkConnectionStatus(SERVER, 200);
        String cleanParseaddressInputFile = Keys.getInstance().getCleanParseaddressInputFile();
        String cleanParseaddressOutputFile = Keys.getInstance().getCleanParseaddressOutputFile();
        String dirtyParseaddressInputFile = Keys.getInstance().getDirtyParseaddressInputFile();
        String dirtyParseaddressOutputFile = Keys.getInstance().getDirtyParseaddressOutputFile();
        // String checkUndefined = Keys.getInstance().checkUndefined();
        String cleanParsetokenInputFile = Keys.getInstance().getCleanParsetokenInputFile();
        String cleanParsetokenOutputFile = Keys.getInstance().getCleanParsetokenOutputFile();
        String dirtyParsetokenInputFile = Keys.getInstance().getDirtyParsetokenInputFile();
        String dirtyParsetokenOutputFile = Keys.getInstance().getDirtyParsetokenOutputFile();

        // checkUndefined.contains("Parsetoken") ? true : false;
        boolean checkThat = false;

        System.out.println("\nProcessing Clean ParseToken File");
        sumRow += parseToken(cleanParsetokenInputFile, cleanParsetokenOutputFile, true, true, checkThat, SERVER,
            APIKEY);

        System.out.println("\nProcessing Dirty ParseToken File");
        sumRow += parseToken(dirtyParsetokenInputFile, dirtyParsetokenOutputFile, false, true, checkThat, SERVER,
            APIKEY);
        
        System.out.println("\nEOF ParseToken Tests\tTotal Unexpected Count: " + sumRow);
        
        System.out.println("\nProcessing Clean ParseAddress File");
        sumRow += ParseAddress.parseAddress(cleanParseaddressInputFile, cleanParseaddressOutputFile, true, checkThat, SERVER,
            APIKEY);

        System.out.println("\nProcessing Dirty ParseAddress File");
        sumRow += ParseAddress.parseAddress(dirtyParseaddressInputFile, dirtyParseaddressOutputFile, false, checkThat, SERVER,
            APIKEY);

        System.out.println("\nEOF ParseAddress Tests\tTotal Unexpected Count: " + sumRow);

      }
    } catch (Exception e) {
      System.out.println(ERROR + e.getMessage());
    }
  }

  private static int parseToken(String testFile, String outputFile, boolean cleanTests, boolean parseOnly,
      boolean checkUndefined, String server, String apiKey) throws Exception {
    int sumRowCount = 0;
    String returnAddress = "";
    FileInputStream fs = null;
    String line = null;
    try {
      fs = new FileInputStream(testFile);

      BufferedReader ds = new BufferedReader(new InputStreamReader(fs, UTF8));
      BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), UTF8));
      line = ds.readLine();
      int lineNumber = 0;

      AddressParseResult2 addressResult = new AddressParseResult2();
      ParsedToken[] parsedTokenList = null;
      ParitusClient clientUser = new ParitusClientImpl(server);
      clientUser.login(apiKey);

      for (; line != null; line = ds.readLine()) {
        lineNumber++;
        String[] strParts = line.split(BARSPLIT);
        String rawInputAddress = strParts[0].trim();
        String tmpAddressText;
        int length = strParts.length;
        String description = strParts[length - 1];
        if (description.contains(EQUAL)) {
          description = "";
        } else {
          length = length - 1;
        }

        try {
          tmpAddressText = Methods.encodeText(rawInputAddress);
          tmpAddressText = String.format("address=%s&capitalizeoutput=true&parseonly=" + String.valueOf(parseOnly),
              tmpAddressText);
          addressResult = clientUser.verifyAddress(tmpAddressText);
          parsedTokenList = addressResult.getParsedAddress().getTokens();
          returnAddress = Methods.createArtificialAddressResult(addressResult);
        } catch (Exception e) {

          String msg = lineNumber + TAB + ERROR + rawInputAddress + TAB + e.getMessage();
          System.out.println(msg);
          bwr.write(msg);
          bwr.newLine();
          throw new RuntimeException();
        }

        boolean problemFound = false;

        if (checkUndefined) {
          boolean undefinedTokenFounded = (Methods.getToken(parsedTokenList, UNDEFINED) != null && cleanTests
              && Methods.getValueInParameter(strParts, UNDEFINED) == null ? true : false);
          if (undefinedTokenFounded && parseOnly) {
            String msg = lineNumber + TAB + UNDEFINED_FOUND + STARS + description + STARS + rawInputAddress + STARS
                + returnAddress;
            System.out.println(msg);
            bwr.write(msg);
            bwr.newLine();
          }

        }

        for (int j = 1; j < length; j++) {
          // first one address last one explanation
          // parameter-0; "TOKEN" (=) parameter-1; "TOKENVALUE"
          String[] expectedParameterKV = strParts[j].split(EQUAL);

          String tokenKeyword = expectedParameterKV[0].trim();
          String prefixOrSuffixKeyword = null;
          String tokenValue = expectedParameterKV[1].trim();
          String actual = "";

          if (tokenKeyword.contains(DOT)) {
            String[] tmp = tokenKeyword.split(DOTSPLITTER);
            // tokenKeyword = tmp[0];
            // example unexpected token: "HOUSENUMBER."
            prefixOrSuffixKeyword = (tmp[1] != null ? DOT + tmp[1] : NULL);
          }

          List<ParsedToken> parsedToken = Methods.getToken(parsedTokenList, tokenKeyword);

          int tokenSize = parsedToken.size();
          for (int ptSize = 0; ptSize < tokenSize; ptSize++) {
            if (prefixOrSuffixKeyword != null) {
              if (prefixOrSuffixKeyword.equals(DOTPREFIX)) {
                actual += parsedToken.get(ptSize).getPrefix() + COMMA;
              }
              if (prefixOrSuffixKeyword.equals(DOTSUFFIX)) {
                actual += parsedToken.get(ptSize).getSuffix() + COMMA;
              }
            } else {
              actual += parsedToken.get(ptSize).getText() + COMMA;
            }
          }
          if (actual == "") {
            actual = NULL;
          } else {
            actual = actual.substring(0, actual.length() - 2);
          }

          if (expectedParameterKV.length != 2) {
            String msg = lineNumber + TAB + ERROR + rawInputAddress + TAB;
            System.out.println(String.format("hatali adres %s", msg));
          }

          if (!tokenValue.equalsIgnoreCase(actual.trim())) {
            problemFound = true;
            if (cleanTests) { // temiz dosyasinda uyuzmazlik var ise
              // (actual.length() == 0 ? "NULL" : actual)
              String msg = lineNumber + TAB + strParts[j].trim() + " " + tokenKeyword + EQUAL + actual + STARS
                  + description + STARS + rawInputAddress + STARS + returnAddress;

              System.out.println(msg);
              bwr.write(msg);
              bwr.newLine();
              sumRowCount++;
              break;
            }
          }
        }

        if (!problemFound && !cleanTests) { // DIRTY dosyasinda hata yok ise yaz
          String msg = lineNumber + TAB + CLEAN + STARS + description + STARS + rawInputAddress + STARS + returnAddress;
          System.out.println(msg);
          bwr.write(msg);
          bwr.newLine();
          sumRowCount++;
        }
      }

      fs.close();
      bwr.close();
      return sumRowCount;

    } catch (Exception e) {
      System.out.println(line);
      System.out.println(e.getMessage());
      throw new RuntimeException();
    }
  }

}