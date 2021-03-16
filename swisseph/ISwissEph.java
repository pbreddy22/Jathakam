/**
 * @author smallufo
 * Created on 2010/7/22 
 */
package swisseph;

public interface ISwissEph
{

  /**
   * This is the main calculation routine for all planets, asteroids, lunar
   * nodes and apogees.
   * See <A HREF="SwissEph.html#swe_calc(double, int, int, double[], java.lang.StringBuffer)">swe_calc(...)</A> for more information.
   * @param tjd_ut The Julian Day number in UT (Universal Time).
   * @param ipl The body to be calculated. See
   * <A HREF="SweConst.html">SweConst</A> for a list of bodies
   * @param iflag A flag that contains detailed specification on how the body
   * is to be computed. See <A HREF="SweConst.html">SweConst</A>
   * for a list of valid flags (SEFLG_*).
   * @param xx A double[6] in which the result is returned. See above for more
   * details.
   * @param serr A StringBuffer containing a warning or error message, if
   * something fails.
   * @return iflag or SweConst.ERR (-1); iflag MAY have changed from input
   * parameter!
   * @see SwissEph#swe_calc(double, int, int, double[], java.lang.StringBuffer)
   */
  public int swe_calc_ut(double tjd_ut, int ipl, int iflag, double xx[], StringBuffer serr);

  /**
   * This is the main calculation routine for all planets, asteroids, lunar
   * nodes and apogees. It is equal to swe_calc_ut() with the exception that
   * the time has to be given in ET (Ephemeris Time or Dynamical Time). You
   * would get ET by adding deltaT to the UT, e.g.,
   * <CODE>tjd_et&nbsp;+&nbsp;SweDate.getDeltaT(tjd_et)</CODE>.<P>
   * <P>The parameter xx is used as an output parameter containing the
   * following info:
   * <P><CODE><BLOCKQUOTE>xx[0]:&nbsp;&nbsp;&nbsp;longitude<BR>
   * xx[1]:&nbsp;&nbsp;&nbsp;latitude<BR>
   * xx[2]:&nbsp;&nbsp;&nbsp;distance in AU<BR>
   * xx[3]:&nbsp;&nbsp;&nbsp;speed in longitude (degree / day)<BR>
   * xx[4]:&nbsp;&nbsp;&nbsp;speed in latitude (degree / day)<BR>
   * xx[5]:&nbsp;&nbsp;&nbsp;speed in distance (AU / day)<BR>
   * </BLOCKQUOTE></CODE><P>
   * The speed infos will be calculated only, if the appropriate SEFLG_*
   * switch is set.
   * @param tjd The Julian Day number in ET (UT + deltaT).
   * @param ipl The body to be calculated. See
   * <A HREF="SweConst.html">SweConst</A> for a list of bodies
   * @param iflag A flag that contains detailed specification on how the body
   * is to be computed. See <A HREF="SweConst.html">SweConst</A>
   * for a list of valid flags (SEFLG_*).
   * @param xx A double[6] in which the result is returned. See above for more
   * details.
   * @param serr A StringBuffer containing a warning or error message, if
   * something fails.
   * @return iflag or SweConst.ERR (-1); iflag MAY have changed from input
   * parameter!
   * @see #swe_calc_ut(double, int, int, double[], java.lang.StringBuffer)
   * @see #swe_fixstar_ut(java.lang.StringBuffer, double, int, double[], java.lang.StringBuffer)
   * @see #swe_fixstar(java.lang.StringBuffer, double, int, double[], java.lang.StringBuffer)
   */
  public int swe_calc(double tjd, int ipl, int iflag, double xx[], StringBuffer serr);
  
  //This is the new recommended interface for planetary calculations.
  // It should be rewritten to be used for fixstars as well.
  public int calc(double jdET, int ipl, int iflag, double xx[]) throws SwissephException;

}