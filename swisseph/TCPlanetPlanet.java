package swisseph;


/**
* This class implements a TransitCalculator for two planets
* in relative positions or speeds to each other.<p>
* You would create a TransitCalculator from this class and
* use the SwissEph.getTransit() methods to actually calculate
* a transit, e.g.:<p>
* <pre>
* SwissEph sw = new SwissEph(...);
* ...
* int flags = SweConst.SEFLG_SWIEPH
*             SweConst.SEFLG_TRANSIT_LATITUDE |
*             SweConst.SEFLG_TRANSIT_SPEED;
* boolean backwards = false;
* 
* TransitCalculator tc = new TCPlanetPlanet(
*                                  sw,
*                                  SweConst.SE_MERCURY,
*                                  SweConst.SE_VENUS,
*                                  flags,
*                                  0.48);
* ...
* double nextTransitUT = sw.getTransitUT(tc, jdUT, backwards);
* </pre>
* This would calculate the (UT-) date, when Mercury and Venus will
* have a very different latitudinal speed (and Mercury the higher
* speed of both...).
*/
public class TCPlanetPlanet extends TransitCalculator
		implements java.io.Serializable
		{


  private int pl1, pl2;
  private int idx = 0; // The index into the xx[] array in swe_calc() to use:
  private int tflags = 0; // The transit flags
  private int flags = 0;  // The calculation flags for swe_calc()
  private boolean calcYoga = false;
  private double maxSpeed1;
  private double minSpeed1;
  private double maxSpeed2;
  private double minSpeed2;
  private double minSpeed, maxSpeed;
  // The y = f(x) value to reach, speaking mathematically...
  private double offset = 0.;
  private double extPrecision = 1.;



double minVal = 0., maxVal = 0.;  // Thinking about it...


  /**
  * Creates a new TransitCalculator for relative transits of two different
  * planets to each other with the option for transits over longitudes,
  * latitudes, distance or the speed in any of these directions in the
  * geocentric or topocentric coordinate system, and in tropical or sidereal
  * zodiac system, both with the sum and difference of both planets positions
  * and speeds.<p>
  * @param sw A SwissEph object, if you have one available. Can be null.
  * @param pl1 The first planet. Valid planets are SweConst.SE_SUN up to
  * SweConst.SE_INTP_PERG with the exception of  SweConst.SE_EARTH.
  * @param pl2 The second planet that will be transited by the first planet.
  * @param flags The calculation type flags (SweConst.SEFLG_TRANSIT_LONGITUDE,
  * SweConst.SEFLG_TRANSIT_LATITUDE or SweConst.SEFLG_TRANSIT_DISTANCE in
  * conjunction with SweConst.SEFLG_TRANSIT_SPEED for transits over a speed
  * value and SweConst.SEFLG_YOGA_TRANSIT to calculate for the SUM of the
  * two positions or speeds instead of the difference). Also flags modifying
  * the basic planet calculations, these are SweConst.SEFLG_TOPOCTR,
  * SweConst.SEFLG_HELCTR and SweConst.SEFLG_SIDEREAL, plus the (optional)
  * ephemeris flags SweConst.SEFLG_MOSEPH, SweConst.SEFLG_SWIEPH
  * or SweConst.SEFLG_JPLEPH.
  * @param offset This is an offset to the exact conjunction transit point.
  * E.g., when the offset is 180 for longitude calculations, you will get the
  * dates, when the two planets are opposite to each other. Note: The offset
  * is related to the FIRST planet, so an offset value of 30 degree will find
  * the transit points, when the FIRST planet will be 30 degrees behind the
  * the position of the second planet.
  * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
  * @see swisseph.SweConst#SEFLG_TRANSIT_LONGITUDE
  * @see swisseph.SweConst#SEFLG_TRANSIT_LATITUDE
  * @see swisseph.SweConst#SEFLG_TRANSIT_DISTANCE
  * @see swisseph.SweConst#SEFLG_TRANSIT_SPEED
  * @see swisseph.SweConst#SEFLG_YOGA_TRANSIT
  * @see swisseph.SweConst#SEFLG_TOPOCTR
  * @see swisseph.SweConst#SEFLG_HELCTR
  * @see swisseph.SweConst#SEFLG_SIDEREAL
  * @see swisseph.SweConst#SEFLG_MOSEPH
  * @see swisseph.SweConst#SEFLG_SWIEPH
  * @see swisseph.SweConst#SEFLG_JPLEPH
  */
  public TCPlanetPlanet(SwissEph sw, int pl1, int pl2, int flags, double offset) {
    this.sw = sw;
    if (this.sw == null) {
      this.sw = new SwissEph();
    }

    // Check parameter: //////////////////////////////////////////////////////
    // List of all valid flags:
    this.tflags = flags;
    int vFlags = SweConst.SEFLG_EPHMASK |
                 SweConst.SEFLG_TOPOCTR |
                 SweConst.SEFLG_HELCTR |
                 SweConst.SEFLG_NOABERR |
                 SweConst.SEFLG_NOGDEFL |
                 SweConst.SEFLG_SIDEREAL |
                 SweConst.SEFLG_YOGA_TRANSIT |
                 SweConst.SEFLG_TRANSIT_LONGITUDE |
                 SweConst.SEFLG_TRANSIT_LATITUDE |
                 SweConst.SEFLG_TRANSIT_DISTANCE |
                 SweConst.SEFLG_TRANSIT_SPEED;
    // NOABERR and NOGDEFL is allowed for HELCTR, as they get set
    // anyway.
    if ((flags & SweConst.SEFLG_HELCTR) != 0) {
      vFlags |= SweConst.SEFLG_NOABERR | SweConst.SEFLG_NOGDEFL;
    }
    if ((flags&~vFlags) != 0) {
      throw new IllegalArgumentException("Invalid flag(s): "+(flags&~vFlags));
    }

    // Allow only one of SEFLG_TRANSIT_LONGITUDE, SEFLG_TRANSIT_LATITUDE, SEFLG_TRANSIT_DISTANCE:
    int type = flags&(SweConst.SEFLG_TRANSIT_LONGITUDE |
                      SweConst.SEFLG_TRANSIT_LATITUDE |
                      SweConst.SEFLG_TRANSIT_DISTANCE);
    if (type != SweConst.SEFLG_TRANSIT_LONGITUDE &&
        type != SweConst.SEFLG_TRANSIT_LATITUDE &&
        type != SweConst.SEFLG_TRANSIT_DISTANCE) {
      throw new IllegalArgumentException("Invalid flag combination '" + flags +
        "': specify at least exactly one of SEFLG_TRANSIT_LONGITUDE (" +
        SweConst.SEFLG_TRANSIT_LONGITUDE + "), SEFLG_TRANSIT_LATITUDE (" +
        SweConst.SEFLG_TRANSIT_LATITUDE + "), SEFLG_TRANSIT_DISTANCE (" +
        SweConst.SEFLG_TRANSIT_DISTANCE + ").");
    }
    if (pl1<SweConst.SE_SUN ||
        pl1>SweConst.SE_INTP_PERG ||
        pl1==SweConst.SE_EARTH) {
      throw new IllegalArgumentException(
          "Unsupported planet number " + pl1 + " ("+
              sw.swe_get_planet_name(pl1) + ")");
    }
    if (pl2<SweConst.SE_SUN ||
        pl2>SweConst.SE_INTP_PERG ||
        pl2==SweConst.SE_EARTH) {
      throw new IllegalArgumentException(
          "Unsupported planet number " + pl1 + " ("+
              sw.swe_get_planet_name(pl1) + ")");
    }
    if ((flags & SweConst.SEFLG_HELCTR) != 0 &&
        (pl1 == SweConst.SE_MEAN_APOG ||
         pl1 == SweConst.SE_OSCU_APOG ||
         pl1 == SweConst.SE_MEAN_NODE ||
         pl1 == SweConst.SE_TRUE_NODE)) {
      throw new IllegalArgumentException(
          "Unsupported planet number " + pl1 + " (" +
              sw.swe_get_planet_name(pl1) + ") for heliocentric " +
              "calculations");
    }
    if ((flags & SweConst.SEFLG_HELCTR) != 0 &&
        (pl2 == SweConst.SE_MEAN_APOG ||
         pl2 == SweConst.SE_OSCU_APOG ||
         pl2 == SweConst.SE_MEAN_NODE ||
         pl2 == SweConst.SE_TRUE_NODE)) {
      throw new IllegalArgumentException(
          "Unsupported planet number " + pl2 + " (" +
              sw.swe_get_planet_name(pl2) + ") for heliocentric " +
              "calculations");
    }
    if (pl1 == pl2) {
      throw new IllegalArgumentException(
                   "Transiting and referred planet have to be different!");
    }

    this.pl1 = pl1;
    this.pl2 = pl2;

    calcYoga = ((flags & SweConst.SEFLG_YOGA_TRANSIT) != 0);


    // The index into the xx[] array in swe_calc() to use:
    if ((flags&SweConst.SEFLG_TRANSIT_LATITUDE) != 0) { // Calculate latitudinal transits
      idx = 1;
    } else if ((flags&SweConst.SEFLG_TRANSIT_DISTANCE) != 0) { // Calculate distance transits
      idx = 2;
    }
    if ((flags&SweConst.SEFLG_TRANSIT_SPEED) != 0) { // Calculate speed transits
      idx += 3;
      flags |= SweConst.SEFLG_SPEED;
    }

    // Eliminate SEFLG_TRANSIT_* flags for use in swe_calc():
    flags &= ~(SweConst.SEFLG_TRANSIT_LONGITUDE |
               SweConst.SEFLG_TRANSIT_LATITUDE |
               SweConst.SEFLG_TRANSIT_DISTANCE |
               SweConst.SEFLG_YOGA_TRANSIT |
               SweConst.SEFLG_TRANSIT_SPEED);
    this.flags = flags;

    // Calculate basic parameters: ///////////////////////////////////////////
    rollover = (idx == 0);

    this.offset = checkOffset(offset);


    maxSpeed1=getSpeed(false,pl1);
    minSpeed1=getSpeed(true,pl1);
    maxSpeed2=getSpeed(false,pl2);
    minSpeed2=getSpeed(true,pl2);
    if (Double.isInfinite(maxSpeed1) || Double.isInfinite(minSpeed1)) {
      throw new IllegalArgumentException(
          ((flags&SweConst.SEFLG_TOPOCTR)!=0?"Topo":((flags&SweConst.SEFLG_HELCTR)!=0?"Helio":"Geo")) +
          "centric transit calculations with planet number " + pl1 + " ("+
          sw.swe_get_planet_name(pl1) + ") not possible.");
    }
    if (Double.isInfinite(maxSpeed2) || Double.isInfinite(minSpeed2)) {
      throw new IllegalArgumentException(
          ((flags&SweConst.SEFLG_TOPOCTR)!=0?"Topo":((flags&SweConst.SEFLG_HELCTR)!=0?"Helio":"Geo")) +
          "centric transit calculations with planet number " + pl2 + " ("+
          sw.swe_get_planet_name(pl2) + ") not possible.");
    }

    if (calcYoga) {
      minSpeed = minSpeed1+minSpeed2;
      maxSpeed = maxSpeed1+maxSpeed2;
    } else {
      if (rollover) {
        minSpeed = (maxSpeed1>maxSpeed2)?minSpeed1-maxSpeed2:minSpeed2-maxSpeed1;
        maxSpeed = (maxSpeed1>maxSpeed2)?maxSpeed1-minSpeed2:maxSpeed2-minSpeed1;
      } else {
        minSpeed = SMath.max(minSpeed1-maxSpeed2, minSpeed2-maxSpeed1);
        maxSpeed = SMath.max(maxSpeed1-minSpeed2, maxSpeed2-minSpeed1);
      }
    }

  }

  /**
  * @return Returns true, if one position value is identical to another
  * position value. E.g., 360 degree is identical to 0 degree in
  * circle angles.
  * @see #rolloverVal
  */
  public boolean getRollover() {
    return rollover;
  }
  /**
  * This sets the transit degree or other transit value for the difference
  * or sum of the positions or speeds of both planets. It will be used on
  * the next call to getTransit().
  * @param value The offset value.
  * @see #getOffset()
  */
  public void setOffset(double value) {
    offset = checkOffset(value);
  }
  /**
  * This returns the transit degree or other transit value of the relative
  * position or speed of the two planets.
  * @return The current offset value.
  * @see #setOffset(double)
  */
  public double getOffset() {
    return offset;
  }
  /**
  * This returns all the &quot;object identifiers s&quot; used in this
  * TransitCalculator. It may be the planet number or planet numbers,
  * when calculating planets.
  * @return An array of identifiers identifying the calculated objects.
  */
  public Object[] getObjectIdentifiers() {
    return new Object[]{"" + pl1, "" + pl2};
  }
  /**
  * Set the factor, when to stop the calculation.
  * The getTransit*() methods will iterate calculations until the maximum
  * precision in the planetary calculation routines has been passed. With
  * this method, you specify a factor to the calculation precision used.
  * E.g., 100 means, stop the calculation, when the difference between the
  * calculated value and requested value has crossed a value of 100 times
  * less than the precision available in the calculation routines. 0.01 on
  * the other hand would stop the calculation, BEFORE the maximum available
  * precision had been reached. The default for the precision factor is 1.<p>
  * @param pfac The factor for the precision as explained above.
  * <b>Note:</b> A value greater than one will NOT really increase precision,
  * it will just <i>appear</i> to do so. It can be handy to show more equal
  * values when approaching the transit point from different sides (forward
  * or backward) or from different starting points.
  * @see #getPrecisionFactor()
  */
  public void setPrecisionFactor(double pfac) {
    extPrecision = pfac;
  }
  /**
  * Returns the factor used to control stopping of the calculation
  * iterations.
  * @return The precision factor as set by the setPrecisionFactor()
  * method.
  * @see #setPrecisionFactor(double)
  */
  public double getPrecisionFactor() {
    return extPrecision;
  }




  ///////////////////////////////////////////////////////////////////////////////
  protected double getMaxSpeed() {
    return maxSpeed;
  }
  protected double getMinSpeed() {
    return minSpeed;
  }

  protected double calc(double jdET) {
    StringBuffer serr = new StringBuffer();
    double[] xx1 = new double[6];
    double[] xx2 = new double[6];

    int ret = sw.swe_calc(jdET, pl1, flags, xx1, serr);
    if (ret<0) {
      throw new SwissephException(jdET, SwissephException.UNDEFINED_ERROR,
          "Calculation failed with return code " + ret + ":\n" +
          serr.toString());
    }

    ret = sw.swe_calc(jdET, pl2, flags, xx2, serr);
    if (ret<0) {
      throw new SwissephException(jdET, SwissephException.UNDEFINED_ERROR,
          "Calculation failed with return code " + ret + ":\n" +
          serr.toString());
    }

    return xx1[idx] + (calcYoga?xx2[idx]:-xx2[idx]);
  }



  protected double getTimePrecision(double degPrec) {
    // Recalculate degPrec to mean the minimum  time, in which the planet can
    // possibly move that degree:
    double amin = SMath.min(SMath.abs(minSpeed1),SMath.abs(minSpeed2));
    double amax = SMath.min(SMath.abs(maxSpeed1),SMath.abs(maxSpeed2));

    double maxVal = SMath.max(SMath.abs(amin),SMath.abs(amax));
    if (maxVal != 0.) {
      return degPrec / maxVal;
    }
    return 1E-9;
  }

  protected double getDegreePrecision(double jd) {
    // Calculate the planet's minimum movement regarding the maximum available
    // precision.
    //
    // For all calculations, we assume the following minimum exactnesses
    // based on the discussions on http://www.astro.com/swisseph, even though
    // these values are nothing more than very crude estimations which should
    // leave us on the save side always, even more, when seeing that we always
    // consider the maximum possible speed / acceleration of a planet in the
    // transit calculations and not the real speed.
    //
    // Take degPrec to be the minimum exact degree in longitude
    double degPrec = 0.005;
    if (idx>2) { // Speed
      // "The speed precision is now better than 0.002" for all planets"
      degPrec = 0.002;
    } else { // Degrees
      // years 1980 to 2099:              0.005"
      // years before 1980:               0.08"   (from sun to jupiter)
      // years 1900 to 1980:              0.08"   (from saturn to neptune) (added: nodes)
      // years before 1900:               1"      (from saturn to neptune) (added: nodes)
      // years after 2099:                same as before 1900
      //
      if (pl1>=SweConst.SE_SUN && pl1<=SweConst.SE_JUPITER) {
        if (jd<1980 || jd>2099) {
          degPrec = 0.08;
        }
      } else {
        if (jd>=1900 && jd<1980) {
          degPrec = 0.08;
        } else if (jd<1900 || jd>2099) { // Unclear about true nodes...
          degPrec = 1;
        }
      }
      if (pl2>=SweConst.SE_SUN && pl2<=SweConst.SE_JUPITER) {
        if (jd<1980 || jd>2099) {
          degPrec = SMath.max(0.08,degPrec);
        }
      } else {
        if (jd>=1900 && jd<1980) {
          degPrec = SMath.max(0.08,degPrec);
        } else if (jd<1900 || jd>2099) { // Unclear about true nodes...
          degPrec = SMath.max(1,degPrec);
        }
      }
    }
    degPrec/=3600.;
    degPrec*=0.5; // We take the precision to be BETTER THAN ... as it is stated somewhere

    // We recalculate these degrees to the minimum time difference that CAN
    // possibly give us data differing more than the above given precision.
    switch (idx) {
      case 0: // Longitude
      case 1: // Latitude
      case 3: // Speed in longitude
      case 4: // Speed in latitude
        break;
      case 2: // Distance
      case 5: // Speed in distance
        // We need to recalculate the precision in degrees to a distance value.
        // For this we need the maximum distance to the centre of calculation,
        // which is the barycentre for the main planets.
        degPrec *= SMath.max(sw.ext.maxBaryDist[pl1],
                            sw.ext.maxBaryDist[pl2]);
    }

    return degPrec;

    // Barycentre:
    //            0.981683040      1.017099581  (Barycenter of the earth!)
    // Sun:       0.982747149 AU   1.017261973 AU
    // Moon:      0.980136691 AU   1.019846623 AU
    // Mercury:   0.307590579 AU   0.466604085 AU
    // Venus:     0.717960758 AU   0.728698831 AU
    // Mars:      1.382830768 AU   0.728698831 AU
    // Jupiter:   5.448547595 AU   4.955912195 AU
    // Saturn:   10.117683425 AU   8.968685733 AU
    // Uranus:   18.327870391 AU  19.893326756 AU
    // Neptune:  29.935653168 AU  30.326750627 AU
    // Pluto:    29.830132096 AU  41.499626899 AU
    // MeanNode:  0.002569555 AU   0.002569555 AU
    // TrueNode:  0.002361814 AU   0.002774851 AU

    //
    // Minimum and maximum (barycentric) distances:
    // Sun:       0.000095 AU      0.01034 AU
    // Moon:      0.972939 AU      1.02625 AU
    // Mercury:   0.298782 AU      0.47569 AU
    // Venus:     0.709190 AU      0.73723 AU
    // Mars:      1.370003 AU      1.67685 AU
    // Jupiter:   4.912031 AU      5.47705 AU
    // Saturn:    8.948669 AU     10.13792 AU
    // Uranus:   18.257511 AU     20.12033 AU
    // Neptune:  29.780622 AU     30.36938 AU
    // Pluto:    29.636944 AU     49.43648 AU
    // MeanNode:  -        AU      -       AU ?
    // TrueNode:  -        AU      -       AU ?


    // Maximum and minimum (geocentric) distances:
    // Sun:        1.016688129 AU   0.983320477 AU
    // Moon:       0.002710279 AU   0.002439921 AU
    // Mercury:    0.549188094 AU   1.448731236 AU

    // Saturn:     7.84 / 7.85 AU  11.25/11.26  AU
    // Uranus:    21.147/21.148 AU              AU

  }


  //////////////////////////////////////////////////////////////////////////////

  private double checkOffset(double val) {
    // Similar rollover considerations for the latitude will be necessary, if
    // swe_calc() would return latitudinal values beyond -90 and +90 degrees.

    if (rollover) {        // Longitude from 0 to 360 degrees:
      while (val < 0.) { val += 360.; }
      val %= 360.;
      minVal = 0.;
      maxVal = 360.;
    } else if (idx == 1) { // Latitude from -90 to +90 degrees:
      while (val < -90.) { val += 180.; }
      while (val >  90.) { val -= 180.; }
      minVal = -90.;
      maxVal = +90.;
    }
    return val;
  }


  private double getSpeed(boolean min, int planet) {
    boolean lat = ((tflags&SweConst.SEFLG_TRANSIT_LATITUDE) != 0);
    boolean dist = ((tflags&SweConst.SEFLG_TRANSIT_DISTANCE) != 0);
    boolean speed = ((tflags&SweConst.SEFLG_TRANSIT_SPEED) != 0);
    boolean topo = ((tflags&SweConst.SEFLG_TOPOCTR) != 0);
    boolean helio = ((tflags&SweConst.SEFLG_HELCTR) != 0);

    // Some topocentric speeds are very different to the geocentric
    // speeds, so we use other values than for geocentric calculations:
    if (topo) {
      if (!sw.swissData.geopos_is_set) {
        throw new IllegalArgumentException("Geographic position is not set for "+
                                           "requested topocentric calculations.");
      }
      if (sw.swissData.topd.geoalt>50000.) {
        throw new IllegalArgumentException("Topocentric transit calculations "+
                                           "are restricted to a maximum "+
                                           "altitude of 50km so far.");
      } else if (sw.swissData.topd.geoalt<-12000000) {
        throw new IllegalArgumentException("Topocentric transit calculations "+
                                           "are restricted to a minimum "+
                                           "altitude of -12000km so far.");
      }
      if (speed) {
        if (lat) {
          return (min?SwephData.minTopoLatAccel[planet]:SwephData.maxTopoLatAccel[planet]);
        } else if (dist) {
          return (min?SwephData.minTopoDistAccel[planet]:SwephData.maxTopoDistAccel[planet]);
        } else {
          return (min?SwephData.minTopoLonAccel[planet]:SwephData.maxTopoLonAccel[planet]);
        }
      } else {
        if (lat) {
          return (min?SwephData.minTopoLatSpeed[planet]:SwephData.maxTopoLatSpeed[planet]);
        } else if (dist) {
          return (min?SwephData.minTopoDistSpeed[planet]:SwephData.maxTopoDistSpeed[planet]);
        } else {
          return (min?SwephData.minTopoLonSpeed[planet]:SwephData.maxTopoLonSpeed[planet]);
        }
      }
    }

    // Heliocentric speeds are very different to the geocentric speeds, so
    // we use other values than for geocentric calculations:
    if (helio) {
      if (speed) {
        if (lat) {
          return (min?SwephData.minHelioLatAccel[planet]:SwephData.maxHelioLatAccel[planet]);
        } else if (dist) {
          return (min?SwephData.minHelioDistAccel[planet]:SwephData.maxHelioDistAccel[planet]);
        } else {
          return (min?SwephData.minHelioLonAccel[planet]:SwephData.maxHelioLonAccel[planet]);
        }
      } else {
        if (lat) {
          return (min?SwephData.minHelioLatSpeed[planet]:SwephData.maxHelioLatSpeed[planet]);
        } else if (dist) {
          return (min?SwephData.minHelioDistSpeed[planet]:SwephData.maxHelioDistSpeed[planet]);
        } else {
          return (min?SwephData.minHelioLonSpeed[planet]:SwephData.maxHelioLonSpeed[planet]);
        }
      }
    }

    // Geocentric:
    if (speed) {
      if (lat) {
        return (min?SwephData.minLatAccel[planet]:SwephData.maxLatAccel[planet]);
      } else if (dist) {
        return (min?SwephData.minDistAccel[planet]:SwephData.maxDistAccel[planet]);
      } else {
        return (min?SwephData.minLonAccel[planet]:SwephData.maxLonAccel[planet]);
      }
    } else {
      if (lat) {
        return (min?SwephData.minLatSpeed[planet]:SwephData.maxLatSpeed[planet]);
      } else if (dist) {
        return (min?SwephData.minDistSpeed[planet]:SwephData.maxDistSpeed[planet]);
      } else {
        return (min?SwephData.minLonSpeed[planet]:SwephData.maxLonSpeed[planet]);
      }
    }
  }

  public String toString() {
    return "[Planets:" + pl1 + "/" + pl2 + "];Offset:" + getOffset();
  }
}
