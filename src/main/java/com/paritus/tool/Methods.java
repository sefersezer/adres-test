package com.paritus.tool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.paritus.dto.AddressParseResult2;
import com.paritus.dto.ParsedToken;

/***
 * parser ve verifier için kullanılan ortak veya tekil methodları içeren
 * unstructured class
 * 
 * @author sefer
 */
public class Methods {

  private static final String UTF_8 = "UTF-8";
  private static final String EQUAL = "=";
  private static final String STARS = " ** ";
  private static final String SUFFIX = ".SUFFIX=";
  private static final String PREFIX = ".PREFIX=";
  private static final String BAR = "|";
  private static final String DOT = ".";

  /**
   * verilen metni encode eder
   */
  public static String encodeText(String val) throws UnsupportedEncodingException {
    if (val != null) {
      val = URLEncoder.encode(val, UTF_8);
    }
    return val;
  }

  /**
   * Reflection içinde method isimlerini
   * çağırmak için kullanılır.
   * Verilen metnin ilk harfini büyütür.
   * Ornek : sefer => Sefer
   */
  public static String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

  /**
   * @param strParts
   *          = {A=a, B=b, C=c, D=d}
   * @param parameterKeyword
   *          = C
   * @return c
   */
  public static String getValueInParameter(String[] strParts, String parameterKeyword) {
    for (int i = 0; i < strParts.length; i++) {
      if (strParts[i].startsWith(parameterKeyword)) {
        String[] kv = strParts[i].split(EQUAL);
        if (kv.length == 2) {
          return kv[1];
        }
      }
    }
    return null;
  }

  /**
   * @return addressParseResult'tan gelen
   * tüm tokenları prefix + text + suffix ile birlikte döner.
   */
  public static String createArtificialAddressResult(AddressParseResult2 addressResult) {
    String returnText = addressResult.getNormalizedAddress();
    ParsedToken[] parsedTokens = addressResult.getParsedAddress().getTokens();

    String parameters = STARS;
    for (int i = 0; i < parsedTokens.length; i++) {
      parameters += getTokenDetails(parsedTokens[i]).trim();
    }

    returnText += " " + parameters;
    return returnText;
  }

  /**
   *  @return HOUSENUMBER.PREFIX=NO|HOUSENUMBER=12|HOUSENUMBER.SUFFIX=A|
   */
  private static String getTokenDetails(ParsedToken parsedToken) {
    String returnText = "";

    // prefix
    if (parsedToken.getPrefix() != null) {
      returnText += parsedToken.getTokenType() + PREFIX;
      returnText += (parsedToken.getPrefix() == null ? "" : parsedToken.getPrefix() + BAR);
    }

    // text
    returnText = parsedToken.getTokenType() + EQUAL + parsedToken.getText() + BAR;

    // suffix
    if (parsedToken.getSuffix() != null) {
      returnText += parsedToken.getTokenType() + SUFFIX;
      returnText += (parsedToken.getSuffix() == null ? "" : parsedToken.getSuffix() + BAR);
    }

    return returnText;
  }

  /**
   * @return Verilen listedeki QUARTER'ları döner.
   * Paritustan gelen cevapta ayni tokendan birden fazla olabilir.
   * Quarter = x,
   * Quarter =y diye gelebilir.
   */
  public static List<ParsedToken> getToken(ParsedToken[] parsedTokenList, String tokenKeyword) {
    List<ParsedToken> returnList = new ArrayList<ParsedToken>();
    if (tokenKeyword.contains(DOT)) {
      tokenKeyword = tokenKeyword.substring(0, tokenKeyword.indexOf(DOT));
    }

    for (int i = 0; i < parsedTokenList.length; i++) {
      if (tokenKeyword.equals(parsedTokenList[i].getTokenType())) {
        returnList.add(parsedTokenList[i]);
      }
    }
    return returnList;
  }
}
