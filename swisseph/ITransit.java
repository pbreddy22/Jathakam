/**
 * @author smallufo
 * Created on 2010/7/22 
 */
package swisseph;

/** Transit Calculation */
public interface ITransit
{
  /**
   * Searches for the next or previous transit of a planet over a specified
   * longitude, latitude, distance or speed value with geocentric or topocentric
   * positions in a tropical or sidereal zodiac. Dates are interpreted as ET
   * (=UT&nbsp;+&nbsp;deltaT).<p>
   *
   * @param tc The TransitCalculator that should be used here.
   * @param jdET The date (and time) in ET, from where to start searching.
   * @param backwards If backward search should be performed.
   * @param jdLimit This is the date, when the search for transits should be
   * stopped, even if no transit point had been found up to then.
   * @return return A double containing the julian day number for the next /
   * previous transit as ET.
   * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
   * @see swisseph.TCPlanetPlanet#TCPlanetPlanet(SwissEph, int, int, int, double)
   */
   public double getTransitET(TransitCalculator tc, double jdET, boolean backwards, double jdLimit)
          throws IllegalArgumentException, SwissephException;
   
   /**
    * Searches for the next or previous transit of a planet over a specified
    * longitude, latitude, distance or speed value with geocentric or topocentric
    * positions in a tropical or sidereal zodiac. Dates are interpreted as ET
    * (=UT&nbsp;+&nbsp;deltaT).<p>
    *
    * @param tc The TransitCalculator that should be used here.
    * @param jdET The date (and time) in ET, from where to start searching.
    * @param backwards If backward search should be performed.
    * @return return A double containing the julian day number for the next /
    * previous transit as ET.
    * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
    * @see swisseph.TCPlanetPlanet#TCPlanetPlanet(SwissEph, int, int, int, double)
    */
    public double getTransitET(TransitCalculator tc, double jdET, boolean backwards)
           throws IllegalArgumentException, SwissephException;
    
    /**
     * Searches for the next or previous transit of a planet over a specified
     * longitude, latitude, distance or speed value with geocentric or topocentric
     * positions in a tropical or sidereal zodiac. Dates are interpreted as UT
     * (=ET&nbsp;-&nbsp;deltaT).<p>
     *
     * @param tc The TransitCalculator that should be used here.
     * @param jdUT The date (and time) in UT, from where to start searching.
     * @param backwards If backward search should be performed.
     * @param jdLimit This is the date, when the search for transits should be
     * stopped, even if no transit point had been found up to then. It is
     * interpreted as UT time as well.
     * @return return A double containing the julian day number for the next /
     * previous transit as UT.
     * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
     * @see swisseph.TCPlanetPlanet#TCPlanetPlanet(SwissEph, int, int, int, double)
     */
     public double getTransitUT(
             TransitCalculator tc,
             double jdUT,
             boolean backwards,
             double jdLimit)
            throws IllegalArgumentException, SwissephException;
     
     /**
      * Searches for the next or previous transit of a planet over a specified
      * longitude, latitude, distance or speed value with geocentric or topocentric
      * positions in a tropical or sidereal zodiac. Dates are interpreted as UT
      * (=ET&nbsp;-&nbsp;deltaT).<p>
      *
      * @param tc The TransitCalculator that should be used here.
      * @param jdUT The date (and time) in UT, from where to start searching.
      * @param backwards If backward search should be performed.
      * @return return A double containing the julian day number for the next /
      * previous transit as UT.
      * @see swisseph.TCPlanet#TCPlanet(SwissEph, int, int, double)
      * @see swisseph.TCPlanetPlanet#TCPlanetPlanet(SwissEph, int, int, int, double)
      */
      public double getTransitUT(
              TransitCalculator tc,
              double jdUT,
              boolean backwards)
             throws IllegalArgumentException, SwissephException;

}
