package com.paritus;

import com.paritus.addresstest.TestSingleAddresses;
import com.paritus.reversegeo.TestReverseGeo;

public class App {
  public static void main(String[] args) throws Exception {
    String sourceLocation = args[0];

    TestSingleAddresses.main(new String[] { sourceLocation });

    TestReverseGeo revGeo = new TestReverseGeo(sourceLocation);
    revGeo.startRevGeoFromCsv();

  }

}
