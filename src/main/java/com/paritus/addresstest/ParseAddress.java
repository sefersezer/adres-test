package com.paritus.addresstest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.paritus.client.ParitusClient;
import com.paritus.client.ParitusClientImpl;
import com.paritus.dto.AddressParseResult2;
import com.paritus.dto.MatchScore;
import com.paritus.dto.MetaData;
import com.paritus.dto.ParsedAddress2;
import com.paritus.tool.Methods;

public class ParseAddress {
  
  private static final String BAR = "\\|";
  private static final String EQUAL = "=";
  private static final String FIRST_ID = "firstId";
  private static final String NOT_DEFINED = "NOT DEFINED";
  private static final String NEIGHBOR_TOWN = "neighborTown";
  private static final String NEIGHBOR_QUARTER = "neighborQuarter";
  private static final String EXPLANATION = "explanation";
  private static final String GET = "get";
  private static final String CLEAN = "Clean";
  private static final String ARROW = " --> ";
  private static final String PROBLEMSPLITTER = " hasProblem ";
  private static final String STARS = " ** ";
  private static final String COUNTUPPERTHAN = "countUpperThan";
  private static final char SHARP = '#';
  private static final String NULL = "NULL";
  private static final String ERROR = " ERROR = ";
  private static final String TAB = "\t";
  private static final String UTF8 = "UTF8";
  private final static String[] selfDefinedTokenKeywords = { "parseonlyAddress" };
  
  public static int parseAddress(String testFile, String outputFile, boolean cleanTests, boolean parseOnly,
      String server, String apiKey) throws Exception {
    int sumRowCount = 0;
    FileInputStream fs = null;
    String line = null;
    try {
      fs = new FileInputStream(testFile);

      BufferedReader ds = new BufferedReader(new InputStreamReader(fs, UTF8));
      BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), UTF8));
      line = ds.readLine();
      int lineNumber = 0;

      AddressParseResult2 addressResult = new AddressParseResult2();
      ParitusClient clientUser = new ParitusClientImpl(server);
      clientUser.login(apiKey);

      // all lines
      for (; line != null; line = ds.readLine()) {
        lineNumber++;
        // skip comment line
        if (line.trim().isEmpty() || line.trim().charAt(0) == SHARP) {
          continue;
        }

        String[] strParts = line.split(BAR);
        String rawInputAddress = strParts[0].trim();
        String tmpAddressText;
        int length = strParts.length;
        boolean problemFound = false;
        String description = strParts[length - 1];

        if (description.contains(EQUAL)) {
          description = "";
        } else {
          length = length - 1;
        }

        List<InputToken> inputTokens = loadInputTokens(strParts, length);

        try {
          tmpAddressText = Methods.encodeText(rawInputAddress);
          tmpAddressText = String.format("address=%s&capitalizeoutput=true&parseonly=" + String.valueOf(parseOnly),
              tmpAddressText);
          addressResult = clientUser.verifyAddress(tmpAddressText);

        } catch (Exception e) {
          String msg = lineNumber + TAB + ERROR + rawInputAddress + TAB + e.getMessage();
          System.out.println(msg);
          bwr.write(msg);
          bwr.newLine();
          throw e;
        }

        for (InputToken inputToken : inputTokens) {
          String[] tokenKeywordParts = inputToken.getTokenKeywordParts();
          String actualTokenValue = getActualTokenValue(tokenKeywordParts, addressResult).trim();
          if (!inputToken.getTokenKeyword().contains(COUNTUPPERTHAN)
              && (!inputToken.getTokenValue().equalsIgnoreCase(actualTokenValue))
              || (inputToken.getTokenKeyword().contains(COUNTUPPERTHAN)
                  && (Integer.parseInt(inputToken.getTokenValue()) >= Integer.parseInt(actualTokenValue)))) {
            problemFound = true;
            if (cleanTests) {
              // single line, clean tests, all tokens
              String msg = lineNumber + TAB + inputToken.getTokenKeyword() + ARROW + inputToken.getTokenValue()
                  + PROBLEMSPLITTER + (actualTokenValue.length() == 0 ? NULL : actualTokenValue) + STARS + description
                  + STARS + rawInputAddress + STARS + addressResult.getNormalizedAddress();
              System.out.println(msg);
              bwr.write(msg);
              bwr.newLine();
              sumRowCount++;
              break;
            }
          }
        }

        if (!problemFound && !cleanTests) {
          String msg = lineNumber + TAB + CLEAN + STARS + description + STARS + rawInputAddress + STARS
              + addressResult.getNormalizedAddress();
          System.out.println(msg);
          bwr.write(msg);
          bwr.newLine();
          sumRowCount++;
        }

      }
      fs.close();
      bwr.close();
    } catch (Exception e) {
      System.out.println(line);
      throw e;
    }
    return sumRowCount;
  }
  
  /**
   * TODO-LIST: - DIRTY DOSYASINDA streetHits.id=xyz gibi birsey verildiginde,
   * streetHits.id degeri senin if-clause yapinda tanimli olmadigi icin hata
   * vermeli.
   * 
   * - streetHits.matchScore.partsUsed =true icin 3. kirilimi yaz
   */
  private static String getActualTokenValue(String[] tokenKeywordParts, AddressParseResult2 addressResult)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
      SecurityException {
    Method[] methods = addressResult.getClass().getDeclaredMethods();
    String firstPartofTokenKeyword = GET + Methods.capitalize(tokenKeywordParts[0]);
    int len = tokenKeywordParts.length;

    for (int i = 0; i < methods.length; i++) {
      // Equals tokenKeywords
      if (firstPartofTokenKeyword.equalsIgnoreCase(methods[i].getName())) {
        if (len == 1) {
          // example : latitute
          return String.valueOf(methods[i].invoke(addressResult, new Object[] {}));
        } else if (len == 2 && !tokenKeywordParts[1].isEmpty()) {
          String secPartofTokenKeyword = tokenKeywordParts[1];

          if (secPartofTokenKeyword.equalsIgnoreCase(COUNTUPPERTHAN)) {
            // streetHits or quarterHits
            ParsedAddress2[] objectList = (ParsedAddress2[]) methods[i].invoke(addressResult, new Object[] {});
            return String.valueOf(objectList.length);
          } else if (secPartofTokenKeyword.equalsIgnoreCase(FIRST_ID)) {
            // objectList'teki elemanlari gezerek istedigin idyi de arama
            // ozelligine kavusabilirsin.
            // example : streetHits.firstId, quarterHits.firstId
            ParsedAddress2[] objectList = (ParsedAddress2[]) methods[i].invoke(addressResult, new Object[] {});
            return (objectList.equals(null) ? NULL
                : String.valueOf(objectList[0].getId().equals(null) ? NULL : objectList[0].getId()));
          } else {
            Object o = addressResult.getClass().getDeclaredMethod(firstPartofTokenKeyword, null).invoke(addressResult,
                new Object[] {});

            if (o.getClass().equals(MetaData.class)) {
              if (secPartofTokenKeyword.equalsIgnoreCase(EXPLANATION)) {
                return String.valueOf(((MetaData) o).getExplanation());
              } else if (secPartofTokenKeyword.equalsIgnoreCase(NEIGHBOR_QUARTER)) {
                return String.valueOf(((MetaData) o).isNeighborQuarter());
              } else if (secPartofTokenKeyword.equalsIgnoreCase(NEIGHBOR_TOWN)) {
                return String.valueOf(((MetaData) o).isNeighborQuarter());
              }
              return NOT_DEFINED;
            }
            // else...
            return NOT_DEFINED;
          }

        } else if (len == 3) {

          /*
           * is + (FuzzyUsed | PartsUsed | InitialsUsed | PrefixUsed) sadece ilk
           * hit icin arama yapiyorsun
           */

          Object o = addressResult.getClass().getDeclaredMethod(firstPartofTokenKeyword, null).invoke(addressResult,
              new Object[] {});

          if (o.getClass().equals(ParsedAddress2[].class)) {
            MatchScore matchScore = (((ParsedAddress2[]) o)[0]).getMatchScore();
            Method isNFunction = matchScore.getClass().getDeclaredMethod("is" + Methods.capitalize(tokenKeywordParts[2]), null);
            return String.valueOf(isNFunction.invoke(matchScore, new Object[] {}));
          }
          return NOT_DEFINED;
        }
        // else...
        return NOT_DEFINED;
      }

    }
    return NULL;
  }
  
  private static boolean checkSelfDefinedToken(String tokenKeyword) {
    for (int i = 0; i < selfDefinedTokenKeywords.length; i++) {
      if (selfDefinedTokenKeywords[i].equals(tokenKeyword)) {
        return true;
      }
    }
    return false;
  }

  // for single line
  public static List<InputToken> loadInputTokens(String[] strParts, int length) {
    List<InputToken> returnList = new ArrayList<InputToken>();
    for (int i = 1; i < length; i++) {
      String[] strPart = strParts[i].split(EQUAL);
      InputToken tmpToken = new InputToken();
      tmpToken.setTokenKeyword(strPart[0].trim());
      tmpToken.setTokenValue(strPart[1].trim());

      // self defined token =0
      if (checkSelfDefinedToken(tmpToken.getTokenKeyword())) {
        tmpToken.setGroup(0);
      } else {
        // set real token levels = 1,2,3
        tmpToken.setGroup(tmpToken.getTokenKeywordParts().length);
      }
      returnList.add(tmpToken);
    }
    return returnList;
  }
}
