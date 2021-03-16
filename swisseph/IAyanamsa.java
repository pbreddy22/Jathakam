/**
 * @author smallufo
 * Created on 2010/8/6 
 */
package swisseph;

public interface IAyanamsa
{
  /* the ayanamsa (precession in longitude)
   * according to Newcomb's definition: 360 -
   * longitude of the vernal point of t referred to the
   * ecliptic of t0.
   */
  /**
  * This calculates the ayanamsha for a given date. You should call
  * swe_set_sid_mode(...) before, where you will set the mode of ayanamsha,
  * as many different ayanamshas are used in the world today.
  * @param tjd_et The date as Julian Day in ET (Ephemeris Time or Dynamic Time)
  * @return The value of the ayanamsha
  * @see #swe_set_sid_mode(int, double, double)
  * @see #swe_get_ayanamsa_ut(double)
  */
  public double getAyanamsa(double tjd_et);
  
  /**
   * This calculates the ayanamsha for a given date. You should call
   * swe_set_sid_mode(...) before, where you will set the mode of ayanamsha,
   * as many different ayanamshas are used in the world today.
   * @param tjd_ut The date as Julian Day in UT (Universal Time)
   * @return The value of the ayanamsha
   * @see #swe_set_sid_mode(int, double, double)
   * @see #swe_get_ayanamsa(double)
   */
  public double getAyanamsaUt(double tjd_ut);
  
  
  public String getAyanamsaName(int isidmode);
  
  /**
   * This sets the ayanamsha mode for sidereal planet calculations. If you
   * don't set the ayanamsha mode, it will default to Fagan/Bradley
   * (SE_SIDM_FAGAN_BRADLEY).
   * The predefined ayanamsha modes are as follows:<P><CODE>
   * <blockquote>
   * SE_SIDM_FAGAN_BRADLEY<BR>
   * SE_SIDM_LAHIRI<BR>
   * SE_SIDM_DELUCE<BR>
   * SE_SIDM_RAMAN<BR>
   * SE_SIDM_USHASHASHI<BR>
   * SE_SIDM_KRISHNAMURTI<BR>
   * SE_SIDM_DJWHAL_KHUL<BR>
   * SE_SIDM_YUKTESHWAR<BR>
   * SE_SIDM_JN_BHASIN<BR>
   * SE_SIDM_BABYL_KUGLER1<BR>
   * SE_SIDM_BABYL_KUGLER2<BR>
   * SE_SIDM_BABYL_KUGLER3<BR>
   * SE_SIDM_BABYL_HUBER<BR>
   * SE_SIDM_BABYL_ETPSC<BR>
   * SE_SIDM_ALDEBARAN_15TAU<BR>
   * SE_SIDM_HIPPARCHOS<BR>
   * SE_SIDM_SASSANIAN<BR>
   * SE_SIDM_GALCENT_0SAG<BR>
   * SE_SIDM_J2000<BR>
   * SE_SIDM_J1900<BR>
   * SE_SIDM_B1950<BR>
   * SE_SIDM_USER
   * </blockquote>
   * </CODE><P>
   * You do not consider the parameters ayan_t0 and t0 if you use a
   * predefined ayanamsha as above. If you specify SE_SIDM_USER, you
   * have to give the value of a reference date t0, and the value of
   * of the ayanamsha at that date in ayan_t0.
   * @param sid_mode One of the above ayanamsha modes plus (optionally)
   * one of the non-standard sidereal calculation modes of
   * <CODE>SE_SIDBIT_ECL_T0</CODE> or <CODE>SE_SIDBIT_SSY_PLANE</CODE>.
   * @param t0 Reference date (Julian day), if sid_mode is SE_SIDM_USER
   * @param ayan_t0 Initial ayanamsha at t0, if sid_mode is SE_SIDM_USER. This
   * is (tropical position - sidereal position) at date t0.
   * @see SweConst#SE_SIDM_FAGAN_BRADLEY
   * @see SweConst#SE_SIDM_LAHIRI
   * @see SweConst#SE_SIDM_DELUCE
   * @see SweConst#SE_SIDM_RAMAN
   * @see SweConst#SE_SIDM_USHASHASHI
   * @see SweConst#SE_SIDM_KRISHNAMURTI
   * @see SweConst#SE_SIDM_DJWHAL_KHUL
   * @see SweConst#SE_SIDM_YUKTESHWAR
   * @see SweConst#SE_SIDM_JN_BHASIN
   * @see SweConst#SE_SIDM_BABYL_KUGLER1
   * @see SweConst#SE_SIDM_BABYL_KUGLER2
   * @see SweConst#SE_SIDM_BABYL_KUGLER3
   * @see SweConst#SE_SIDM_BABYL_HUBER
   * @see SweConst#SE_SIDM_BABYL_ETPSC
   * @see SweConst#SE_SIDM_ALDEBARAN_15TAU
   * @see SweConst#SE_SIDM_HIPPARCHOS
   * @see SweConst#SE_SIDM_SASSANIAN
   * @see SweConst#SE_SIDM_GALCENT_0SAG
   * @see SweConst#SE_SIDM_J2000
   * @see SweConst#SE_SIDM_J1900
   * @see SweConst#SE_SIDM_B1950
   * @see SweConst#SE_SIDM_USER
   * @see SweConst#SE_SIDBIT_ECL_T0
   * @see SweConst#SE_SIDBIT_SSY_PLANE
   */
  public void setSidMode(int sid_mode, double t0, double ayan_t0);
}
