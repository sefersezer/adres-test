package com.paritus.reversegeo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import com.ao.etl.CsvFileReader;
import com.ao.etl.CsvFileWriter;
import com.ao.etl.ExcelFileReader;
import com.ao.etl.FileRow;
import com.ao.etl.FileRowConverter;
import com.ao.etl.ObjectArrayConverter;
import com.paritus.Keys;
import com.paritus.dto.ParsedAddress2;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class TestReverseGeo {
  private static String APIKEY;
  private static String SERVER;

  private static Client client = new Client();

  Double defaultRadius;
  Double streetRadius;

  public TestReverseGeo(String sourceLocation) {
    Keys.setFileLocation(sourceLocation);
  }

  public void startRevGeoFromCsv() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      NoSuchMethodException, SecurityException {
    startRevGeoFromCsv(Keys.getInstance().getReversegeocodingSource(), Keys.getInstance().getReversegeocodingTarget(),
        getDouble(Keys.getInstance().getDefaultRadius()),
        getDouble(Keys.getInstance().getStreetRadius()));
  }

  public void startRevGeoFromCsv(String csvInput, String excelOutput, Double defaultRadius, Double streetRadius)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
      SecurityException {
    CsvFileReader<String[]> reader = getInstallmentReader(csvInput);
    CsvFileWriter<String[]> writer = getAddressResultWriter(excelOutput);

    for (String[] inputRow = reader.nextAs(); inputRow != null; inputRow = reader.nextAs()) {
      ParsedAddress2[] parsedAddressObject = callReverseGeocoding(getDouble(inputRow[4]), getDouble(inputRow[5]),
          defaultRadius, streetRadius);
      if (parsedAddressObject != null && parsedAddressObject.length != 0) {
        ParsedAddress2 pa = parsedAddressObject[0];
        String[] addressResultRow = new String[15];
        addressResultRow[0] = inputRow[0];
        addressResultRow[1] = inputRow[1];
        addressResultRow[2] = inputRow[2];
        addressResultRow[3] = inputRow[3];
        addressResultRow[4] = inputRow[4];
        addressResultRow[5] = inputRow[5];
        addressResultRow[6] = inputRow[6];
        addressResultRow[7] = getStringValue(pa.getOriginal());
        addressResultRow[8] = getStringValue(pa.getCity());
        addressResultRow[9] = getStringValue(pa.getTown());
        addressResultRow[10] = getStringValue(pa.getQuarter());
        addressResultRow[11] = getStringValue(pa.getMainStreet());
        addressResultRow[12] = getStringValue(pa.getStreet());
        addressResultRow[13] = getStringValue(pa.getLongitude());
        addressResultRow[14] = getStringValue(pa.getLatitude());

        writer.write(addressResultRow);
      } else {
        writer.write(new String[] { inputRow[0], inputRow[1], inputRow[2], "not found" });
      }
    }

    reader.close();
    writer.close();
  }

  private CsvFileWriter<String[]> getAddressResultWriter(String path) {
    CsvFileWriter<String[]> writer = new CsvFileWriter<String[]>(path);
    writer.accept(new ObjectArrayConverter<String[]>() {
      public Object[] convert(String[] arg0) {
        return arg0;
      }
    });
    return writer;
  }

  /**
   * // id // adres // doğrulama tipi // doğrulama skoru // enlem // boylam //
   * koordinat tipi
   * 
   * @param path
   * @return
   */
  private CsvFileReader<String[]> getInstallmentReader(String path) {
    CsvFileReader<String[]> reader = new CsvFileReader<String[]>(path, "UTF-8", '"', ';', "\n");
    reader.setSkipFirstRow(true);
    reader.accept(new FileRowConverter<String[]>() {
      public String[] convert(FileRow fileRow) {
        String[] row = new String[7];
        row[0] = fileRow.get(0); // id
        row[1] = fileRow.get(1); // adres
        row[2] = fileRow.get(2); // doğrulama tipi
        row[3] = fileRow.get(3); // doğrulama skoru
        row[4] = fileRow.get(4); // enlem
        row[5] = fileRow.get(5); // boylam
        row[6] = fileRow.get(6); // koordinat tipi
        return row;
      }
    });
    return reader;
  }

  private String getStringValue(String original) {
    return original == null ? "" : original;
  }

  private String getStringValue(Double original) {
    return original.equals(null) ? "" : String.valueOf(original);
  }

  @Deprecated
  public void startRevGeoFromXls() throws IOException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
    ExcelFileReader reader = new ExcelFileReader(Keys.getInstance().getReversegeocodingSource());
    reader.next(); // skip first row
    int i = 1;
    for (FileRow row = reader.next(); row != null; row = reader.next()) {
      CoordinateRow cr = setCoordinateRow(row);

      String header = "Row: " + (++i) + ", Jira: " + cr.getJira() + ", " + cr.getComment();
      System.out.println(header);

      long time = System.currentTimeMillis();
      ParsedAddress2[] pa2 = callReverseGeocoding(cr.getLatitude(), cr.getLongitude(), cr.getDefaultRadius(),
          cr.getStreetRadius());
      time = System.currentTimeMillis() - time;

      verifyParsedAddress(cr, pa2);
      printResult(pa2, time);

    }
    reader.close();
  }

  @Deprecated
  public void startRevGeoFromLatLon(String latlon) {
    startRevGeoFromLatLon(latlon, 10.0, 10.0);
  }

  @Deprecated
  public void startRevGeoFromLatLon(String latlon, Double defaultRadius) {
    startRevGeoFromLatLon(latlon, 10.0, null);
  }

  @Deprecated
  public void startRevGeoFromLatLon(String latlon, Double defaultRadius, Double streetRadius) {
    latlon = latlon.trim().replace("  ", " ").replace("\t", " ");
    String[] coordinateString = latlon.split(" ");
    Double[] coordinate = new Double[] { getDouble(coordinateString[0]), getDouble(coordinateString[1]) };
    long time = System.currentTimeMillis();
    ParsedAddress2[] pa2 = callReverseGeocoding(coordinate[0], coordinate[1], defaultRadius, streetRadius);
    time = System.currentTimeMillis() - time;
    printResult(pa2, time);
  }

  private void verifyParsedAddress(CoordinateRow cr, ParsedAddress2[] pa2) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    if (!cr.getTarget().equals("") && pa2 != null) {
      ParsedAddress2 parsedAddress = pa2[0];
      String expectedValue = String.valueOf(parsedAddress.getClass()
          .getDeclaredMethod(getCapitalizeMethodName(cr.getTarget()), null).invoke(parsedAddress, new Object[] {}));
      if (expectedValue == null) {
        System.out.println("(-) Not Found Parameter: " + cr.getTarget());
      } else {
        expectedValue = expectedValue.trim();
        if (!expectedValue.equals(cr.getExpected())) {
          System.out.println("(-) Not Equal Value: " + expectedValue);
        } else {
          System.out.println("(+) Return Value: " + expectedValue);
        }
      }
    }

  }

  private static String getCapitalizeMethodName(final String line) {
    return "get" + Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

  private CoordinateRow setCoordinateRow(FileRow row) {
    return new CoordinateRow(getDouble(row.get(0)), getDouble(row.get(1)), getDouble(row.get(2)), getDouble(row.get(3)),
        row.get(4).trim(), row.get(5).trim(), row.get(6).trim(), row.get(7).trim());
  }

  private Double getDouble(String string) {
    String val = string.trim();
    return val.length() != 0 ? Double.valueOf(val) : null;
  }

  private ParsedAddress2[] callReverseGeocoding(Double lat, Double lon, Double defaultRadius, Double streetRadius) {
    APIKEY = Keys.getInstance().getAdminApiKey();
    SERVER = Keys.getInstance().getServer();

    StringBuffer params = new StringBuffer();
    params.append(setParam("streetradiusinm", streetRadius));
    params.append(setParam("radiusinm", defaultRadius));
    params.append(setParam("latitude", lat));
    params.append(setParam("longitude", lon));

    String apiAddress = String.format("%s/%s?apikey=%s%s", SERVER, "services/rest/reversegeocoding.xml", APIKEY,
        params.toString());
    WebResource webResource = client.resource(UriBuilder.fromUri(apiAddress).buildFromEncoded());
    return webResource.acceptLanguage(new Locale("tr-TR")).accept(MediaType.APPLICATION_XML)
        .get(ParsedAddress2[].class);
  }

  private static void printResult(ParsedAddress2[] pa2, long time) {
    for (ParsedAddress2 pa : pa2) {
      System.out.println(time + " ms" + " | " + pa.getOriginal() + " | " + pa.getLatitude() + " " + pa.getLongitude());
    }
    System.out.println();
  }

  private static String setParam(String param, Double val) {
    return val != null ? ("&" + param + "=" + val.toString()) : "";
  }

}
