/**
 * @author smallufo
 * Created on 2010/7/22 
 */
package swisseph;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Logger;

public abstract class AbstractSwissEph implements Serializable , ISwissEph, IAyanamsa 
{
  /* The routine called by the user.
   * It checks whether a position for the same planet, the same t, and the
   * same flag bits has already been computed.
   * If yes, this position is returned. Otherwise it is computed.
   * -> If the SEFLG_SPEED flag has been specified, the speed will be returned
   * at offset 3 of position array x[]. Its precision is probably better
   * than 0.002"/day.
   * -> If the SEFLG_SPEED3 flag has been specified, the speed will be computed
   * from three positions. This speed is less accurate than SEFLG_SPEED,
   * i.e. better than 0.1"/day. And it is much slower. It is used for
   * program tests only.
   * -> If no speed flag has been specified, no speed will be returned.
   */
  private int swe_calc_epheflag_sv = 0;
  private int httpBufSize=300;
  
  protected SwissData swissData;
  SwephMosh  swephMosh;
  SwephJPL   swephJpl;
  SwissLib   swissLib;
  Swemmoon   swemMoon;
  
  private static Logger logger = Logger.getLogger(AbstractSwissEph.class.getSimpleName());
  
  
  public AbstractSwissEph()
  {
    if (swissData == null) 
      swissData = new SwissData();
    
    swissLib       = new SwissLib(this.swissData);
    swemMoon       = new Swemmoon(this.swissData, this.swissLib);
    swephMosh      = new SwephMosh(this.swissLib, this, this.swissData);
    swephJpl       = new SwephJPL(this, this.swissData, this.swissLib);
  }
  
  @Override
  public int swe_calc_ut(double tjd_ut, int ipl, int iflag, double[] xx, StringBuffer serr)
  {
    return swe_calc(tjd_ut + SweDate.getDeltaT(tjd_ut), ipl, iflag, xx, serr);
  }
  
  @Override
  public int swe_calc(double tjd, int ipl, int iflag, double xx[], StringBuffer serr) 
  {
    // It has been rewritten to be wrapper to the old interface without
    // exception handling like it was in C. The old routine can now be
    // found in the method _calc().
    int ret = 0;
    try
    {
      ret = _calc(tjd, ipl, iflag, xx, serr);
    }
    catch (SwissephException se)
    {
      ret = SweConst.ERR; // se.getRC();
      serr.setLength(0);
      serr.append(se.getMessage());
    }
    return ret;
  }

  
  @Override
  public int calc(double jdET, int ipl, int iflag, double xx[]) throws SwissephException 
  {
    return _calc(jdET, ipl, iflag, xx, new StringBuffer());
  }
  
  protected int _calc(double tjd, int ipl, int iflag, double xx[], StringBuffer serr) throws SwissephException
  {
    int i, j;
    int iflgcoor;
    int iflgsave = iflag;
    int epheflag;
    SavePositions sd;
    double x[] = new double[6], xs[];
    double x0[] = new double[24], x2[] = new double[24];
    double dt;

    /*
     * function calls for Pluto with asteroid number 134340 are treated as calls
     * for Pluto as main body SE_PLUTO. Reason: Our numerical integrator takes
     * into account Pluto perturbation and therefore crashes with body 134340
     * Pluto.
     */
    if (ipl == SweConst.SE_AST_OFFSET + 134340)
    {
      ipl = SweConst.SE_PLUTO;
    }
    /*
     * if ephemeris flag != ephemeris flag of last call, we clear the save area,
     * to prevent swecalc() using previously computed data for current
     * calculation. except with ipl = SE_ECL_NUT which is not dependent on
     * ephemeris, and except if change is from ephemeris = 0 to ephemeris =
     * SEFLG_DEFAULTEPH or vice-versa.
     */
    epheflag = iflag & SweConst.SEFLG_EPHMASK;
    if ((epheflag & SweConst.SEFLG_DEFAULTEPH) != 0)
    {
      epheflag = 0;
    }
    if (swe_calc_epheflag_sv != epheflag && ipl != SweConst.SE_ECL_NUT)
    {
      close();
      swe_calc_epheflag_sv = epheflag;
    }
    /* high precision speed prevails fast speed */
    if ((iflag & SweConst.SEFLG_SPEED3) != 0 && (iflag & SweConst.SEFLG_SPEED) != 0)
    {
      iflag = iflag & ~SweConst.SEFLG_SPEED3;
    }
    /* cartesian flag excludes radians flag */
    if (((iflag & SweConst.SEFLG_XYZ) != 0) && ((iflag & SweConst.SEFLG_RADIANS) != 0))
    {
      iflag = iflag & ~SweConst.SEFLG_RADIANS;
    }
    /*
     * if (iflag & SweConst.SEFLG_ICRS) iflag |= SweConst.SEFLG_J2000;
     */
    /* pointer to save area */
    if (ipl < SweConst.SE_NPLANETS && ipl >= SweConst.SE_SUN)
    {
      sd = swissData.savedat[ipl];
    }
    else
    {
      /* other bodies, e.g. asteroids called with ipl = SE_AST_OFFSET + MPC# */
      sd = swissData.savedat[SweConst.SE_NPLANETS];
    }
    /*
     * if position is available in save area, it is returned. this is the case,
     * if tjd = tsave and iflag = iflgsave. coordinate flags can be neglected,
     * because save area provides all coordinate types. if ipl >
     * SE_AST(EROID)_OFFSET, ipl must be checked, because all asteroids called
     * by MPC number share the same save area.
     */
    iflgcoor = SweConst.SEFLG_EQUATORIAL | SweConst.SEFLG_XYZ | SweConst.SEFLG_RADIANS;

    try
    { // SwissephExceptions from swecalc
      if (sd.tsave != tjd || tjd == 0 || ipl != sd.ipl || ((sd.iflgsave & ~iflgcoor) != (iflag & ~iflgcoor)))
      {
        /*
         * otherwise, new position must be computed
         */
        if ((iflag & SweConst.SEFLG_SPEED3) == 0)
        {
          /*
           * with high precision speed from one call of swecalc() (FAST speed)
           */
          sd.tsave = tjd;
          sd.ipl = ipl;
          // throws SwissephException:
          if ((sd.iflgsave = swecalc(tjd, ipl, iflag, sd.xsaves, serr)) == SweConst.ERR)
          {
            return swe_calc_error(xx);
          }
        }
        else
        {
          /*
           * with speed from three calls of swecalc(), slower and less accurate.
           * (SLOW speed, for test only)
           */
          sd.tsave = tjd;
          sd.ipl = ipl;
          switch (ipl)
          {
            case SweConst.SE_MOON:
              dt = SwephData.MOON_SPEED_INTV;
              break;
            case SweConst.SE_OSCU_APOG:
            case SweConst.SE_TRUE_NODE:
              /*
               * this is the optimum dt with Moshier ephemeris, but not with JPL
               * ephemeris or SWISSEPH. To avoid completely false speed in case
               * that JPL is wanted but the program returns Moshier, we use
               * Moshier optimum. For precise speed, use JPL and FAST speed
               * computation,
               */
              dt = SwephData.NODE_CALC_INTV_MOSH;
              break;
            default:
              dt = SwephData.PLAN_SPEED_INTV;
              break;
          }
          sd.iflgsave = swecalc(tjd - dt, ipl, iflag, x0, serr);
          if (sd.iflgsave == SweConst.ERR)
          {
            return swe_calc_error(xx);
          }
          sd.iflgsave = swecalc(tjd + dt, ipl, iflag, x2, serr);
          if (sd.iflgsave == SweConst.ERR)
          {
            return swe_calc_error(xx);
          }
          sd.iflgsave = swecalc(tjd, ipl, iflag, sd.xsaves, serr);
          if (sd.iflgsave == SweConst.ERR)
          {
            return swe_calc_error(xx);
          }
          denormalize_positions(x0, sd.xsaves, x2);
          calc_speed(x0, sd.xsaves, x2, dt);
        }
      }
    }
    catch (SwissephException se)
    {
      sd.iflgsave = SweConst.ERR;
      swe_calc_error(xx);
      throw se;
    }
    // end_swe_calc:
    int xsOffset = 0;
    xs = sd.xsaves;
    if ((iflag & SweConst.SEFLG_EQUATORIAL) != 0)
    {
      xsOffset = 12; /* equatorial coordinates */
      // } else {
      // xsOffset=0; /* ecliptic coordinates */
    }
    if ((iflag & SweConst.SEFLG_XYZ) != 0)
    {
      xsOffset += 6; /* cartesian coordinates */
    }
    if (ipl == SweConst.SE_ECL_NUT)
    {
      i = 4;
    }
    else
    {
      i = 3;
    }
    for (j = 0; j < i; j++)
    {
      x[j] = xs[j + xsOffset];
    }
    for (j = i; j < 6; j++)
    {
      x[j] = 0;
    }
    if ((iflag & (SweConst.SEFLG_SPEED3 | SweConst.SEFLG_SPEED)) != 0)
    {
      for (j = 3; j < 6; j++)
      {
        x[j] = xs[j + xsOffset];
      }
    }
    if ((iflag & SweConst.SEFLG_RADIANS) != 0)
    {
      if (ipl == SweConst.SE_ECL_NUT)
      {
        for (j = 0; j < 4; j++)
          x[j] *= SwissData.DEGTORAD;
      }
      else
      {
        for (j = 0; j < 2; j++)
          x[j] *= SwissData.DEGTORAD;
        if ((iflag & (SweConst.SEFLG_SPEED3 | SweConst.SEFLG_SPEED)) != 0)
        {
          for (j = 3; j < 5; j++)
            x[j] *= SwissData.DEGTORAD;
        }
      }
    }
    for (i = 0; i <= 5; i++)
    {
      xx[i] = x[i];
    }
    iflag = sd.iflgsave;
    /* if no ephemeris has been specified, do not return chosen ephemeris */
    if ((iflgsave & SweConst.SEFLG_EPHMASK) == 0)
    {
      iflag = iflag & ~SweConst.SEFLG_DEFAULTEPH;
    }
    return iflag;
  } //_calc()
  
  
  /* closes all open files, frees space of planetary data,
   * deletes memory of all computed positions
   */
  /**
  * This method closes all open data files and sets planetary data objects
  * to null for a hint to the garbage collector that those objects can be
  * freed.
  */
  public void close()
  {
    int i;
    /* close SWISSEPH files */
    for (i = 0; i < SwephData.SEI_NEPHFILES; i++)
    {
      swissData.fidat[i].clearData();
    }
    /* free planets data space */
    for (i = 0; i < SwephData.SEI_NPLANETS; i++)
    {
      swissData.pldat[i].clearData();
    }
    for (i = 0; i <= SweConst.SE_NPLANETS; i++)
    {/* "<=" is correct! see decl. */
      swissData.savedat[i].clearData();
    }
    /* clear node data space */
    for (i = 0; i < SwephData.SEI_NNODE_ETC; i++)
    {
      swissData.nddat[i].clearData();
    }
    swissData.oec.clearData();
    swissData.oec2000.clearData();
    swissData.nut.clearData();
    swissData.nut2000.clearData();
    swissData.nutv.clearData();
    /* close JPL file */
    swephJpl.swi_close_jpl_file();
    swissData.jpl_file_is_open = false;
    /* close fixed stars */
    if (swissData.fixfp != null)
    {
      try
      {
        swissData.fixfp.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      swissData.fixfp = null;
    }
    closePlatform();
  }
  
  /**
   * close platform-dependent resources , leaving for platform implementations to implement
   */
  protected void closePlatform()
  {
  }
  
  private int swe_calc_error(double[] xx) 
  {
    for (int i = 0; i < xx.length; i++) {
      xx[i] = 0;
    }
    return SweConst.ERR;
  }
  
  private int swecalc(double tjd, int ipl, int iflag, double[] x, StringBuffer serr) throws SwissephException
  {
    int i;
    int ipli, ipli_ast, ifno;
    int retc;
    int epheflag = SweConst.SEFLG_DEFAULTEPH;
    PlanData pdp;
    PlanData pedp = swissData.pldat[SwephData.SEI_EARTH];
    PlanData psdp = swissData.pldat[SwephData.SEI_SUNBARY];
    PlanData ndp;
    double xp[], xp2[];
    double ss[] = new double[3];
    String serr2 = "";

    if (serr != null)
    {
      serr.setLength(0);
    }
    /******************************************
     * iflag plausible? *
     ******************************************/
    iflag = plaus_iflag(iflag);
    /******************************************
     * which ephemeris is wanted, which is used? Three ephemerides are possible:
     * MOSEPH, SWIEPH, JPLEPH. JPLEPH is best, SWIEPH is nearly as good, MOSEPH
     * is least precise. The availability of the various ephemerides depends on
     * the installed ephemeris files in the users ephemeris directory. This can
     * change at any time. Swisseph should try to fulfil the wish of the user
     * for a specific ephemeris, but use a less precise one if the desired
     * ephemeris is not available for the given date and body. If internal
     * ephemeris errors are detected (data error, file length error) an error is
     * returned. If the time range is bad but another ephemeris can deliver this
     * range, the other ephemeris is used. If no ephemeris is specified,
     * DEFAULTEPH is assumed as desired. DEFAULTEPH is defined at compile time,
     * usually as SWIEPH. The caller learns from the return flag which ephemeris
     * was used. ephe_flag is extracted from iflag, but can change later if the
     * desired ephe is not available.
     ******************************************/
    if ((iflag & SweConst.SEFLG_MOSEPH) != 0)
    {
      epheflag = SweConst.SEFLG_MOSEPH;
    }
    if ((iflag & SweConst.SEFLG_SWIEPH) != 0)
    {
      epheflag = SweConst.SEFLG_SWIEPH;
    }
    if ((iflag & SweConst.SEFLG_JPLEPH) != 0)
    {
      epheflag = SweConst.SEFLG_JPLEPH;
    }
    /* no barycentric calculations with Moshier ephemeris */
    if (((iflag & SweConst.SEFLG_BARYCTR) != 0) && ((iflag & SweConst.SEFLG_MOSEPH) != 0))
    {
      if (serr != null)
      {
        serr.append("barycentric Moshier positions are not supported.");
      }
      throw new SwissephException(tjd, SwissephException.INVALID_PARAMETER_COMBINATION, SweConst.ERR, serr);
    }
    if (epheflag != SweConst.SEFLG_MOSEPH && !swissData.ephe_path_is_set)
    {
      swe_set_ephe_path(null);
    }
    if ((iflag & SweConst.SEFLG_SIDEREAL) != 0 && !swissData.ayana_is_set)
    {
      swe_set_sid_mode(SweConst.SE_SIDM_FAGAN_BRADLEY, 0, 0);
    }
    /******************************************
     * obliquity of ecliptic 2000 and of date *
     ******************************************/
    swi_check_ecliptic(tjd);
    /******************************************
     * nutation *
     ******************************************/
    swi_check_nutation(tjd, iflag);
    /******************************************
     * select planet and ephemeris * * ecliptic and nutation *
     ******************************************/
    if (ipl == SweConst.SE_ECL_NUT)
    {
      x[0] = swissData.oec.eps + swissData.nut.nutlo[1]; /* true ecliptic */
      x[1] = swissData.oec.eps; /* mean ecliptic */
      x[2] = swissData.nut.nutlo[0]; /* nutation in longitude */
      x[3] = swissData.nut.nutlo[1]; /* nutation in obliquity */
      /* if ((iflag & SweConst.SEFLG_RADIANS) == 0) */
      for (i = 0; i <= 3; i++)
        x[i] *= SwissData.RADTODEG;
      return (iflag);
      /******************************************
       * moon *
       ******************************************/
    }
    else if (ipl == SweConst.SE_MOON)
    {
      /* internal planet number */
      ipli = SwephData.SEI_MOON;
      pdp = swissData.pldat[ipli];
      xp = pdp.xreturn;
      switch (epheflag)
      {
        case SweConst.SEFLG_JPLEPH:
          retc = jplplan(tjd, ipli, iflag, SwephData.DO_SAVE, null, null, null, serr);
          /* read error or corrupt file */
          if (retc == SweConst.ERR)
          {
            swecalc_error(x);
            throw new SwissephException(tjd, SwissephException.DAMAGED_FILE_ERROR, SweConst.ERR, serr);
          }
          /*
           * jpl ephemeris not on disk or date beyond ephemeris range or file
           * corrupt
           */
          if (retc == SwephData.NOT_AVAILABLE)
          {
            iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
            if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
            {
              serr.append(" \ntrying Swiss Eph; ");
            }
            retc = sweph_moon(tjd, ipli, iflag, serr);
            if (retc == SweConst.ERR)
            {
              return swecalc_error(x);
            }
          }
          else if (retc == SwephData.BEYOND_EPH_LIMITS)
          {
            if (tjd > SwephData.MOSHLUEPH_START && tjd < SwephData.MOSHLUEPH_END)
            {
              iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_MOSEPH;
              if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
              {
                serr.append(" \nusing Moshier Eph; ");
              }
              // goto moshier_moon;
              retc = moshier_moon(tjd, SwephData.DO_SAVE, null, serr);
              if (retc == SweConst.ERR)
              {
                return swecalc_error(x);
              }
            }
            else
              return swecalc_error(x);
          }
          break;
        case SweConst.SEFLG_SWIEPH:
          retc = sweph_moon(tjd, ipli, iflag, serr);
          if (retc == SweConst.ERR)
          {
            return swecalc_error(x);
          }
          break;
        case SweConst.SEFLG_MOSEPH:
          // moshier_moon:
          retc = moshier_moon(tjd, SwephData.DO_SAVE, null, serr);
          if (retc == SweConst.ERR)
          {
            return swecalc_error(x);
          }
          break;
        default:
          break;
      }
      /* heliocentric, lighttime etc. */
      if ((retc = app_pos_etc_moon(iflag, serr)) != SweConst.OK)
      {
        return swecalc_error(x); // retc may be wrong with sidereal calculation
      }
      /**********************************************
       * barycentric sun * (only JPL and SWISSEPH ephemerises) *
       **********************************************/
    }
    else if (ipl == SweConst.SE_SUN && ((iflag & SweConst.SEFLG_BARYCTR) != 0))
    {
      /*
       * barycentric sun must be handled separately because of the following
       * reasons: ordinary planetary computations use the function main_planet()
       * and its subfunction jplplan(), see further below. now, these functions
       * need the swisseph internal planetary indices, where SEI_EARTH = SEI_SUN
       * = 0. therefore they don't know the difference between a barycentric sun
       * and a barycentric earth and always return barycentric earth. to avoid
       * this problem, many functions would have to be changed. as an
       * alternative, we choose a more separate handling.
       */
      ipli = SwephData.SEI_SUN; /* = SEI_EARTH ! */
      xp = pedp.xreturn;

      switch (epheflag)
      {
        case SweConst.SEFLG_JPLEPH:
          /* open ephemeris, if still closed */
          if (!swissData.jpl_file_is_open)
          {
            retc = swephJpl.swi_open_jpl_file(ss, swissData.jplfnam, swissData.ephepath, serr);
            if (retc != SweConst.OK)
            {
              retc = sweph_sbar(tjd, iflag, psdp, pedp, serr);
            }
            if (retc == SweConst.ERR)
            {
              return swecalc_error(x);
            }
            swissData.jpldenum = swephJpl.swi_get_jpl_denum();
            swissData.jpl_file_is_open = true;
          }
          retc = swephJpl.swi_pleph(tjd, SwephJPL.J_SUN, SwephJPL.J_SBARY, psdp.x, serr);
          if (retc == SweConst.ERR || retc == SwephData.BEYOND_EPH_LIMITS)
          {
            swephJpl.swi_close_jpl_file();
            swissData.jpl_file_is_open = false;
            return swecalc_error(x);
          }
          /*
           * jpl ephemeris not on disk or date beyond ephemeris range or file
           * corrupt
           */
          if (retc == SwephData.NOT_AVAILABLE)
          {
            iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
            if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
            {
              serr.append(" \ntrying Swiss Eph; ");
            }
            retc = sweph_sbar(tjd, iflag, psdp, pedp, serr);
            if (retc == SweConst.ERR)
            {
              return swecalc_error(x);
            }
          }
          psdp.teval = tjd;
          break;
        case SweConst.SEFLG_SWIEPH:
          retc = sweph_sbar(tjd, iflag, psdp, pedp, serr);
          if (retc == SweConst.ERR)
          {
            return swecalc_error(x);
          }
          break;
        default:
          return SweConst.ERR;
      }
      /* flags */
      if ((retc = app_pos_etc_sbar(iflag, serr)) != SweConst.OK)
      {
        return swecalc_error(x);
      }
      /* iflag has possibly changed */
      iflag = pedp.xflgs;
      /*
       * barycentric sun is now in save area of barycentric earth.
       * (pedp->xreturn = swed.pldat[SEI_EARTH].xreturn). in case a barycentric
       * earth computation follows for the same date, the planetary functions
       * will return the barycentric SUN unless we force a new computation of
       * pedp->xreturn. this can be done by initializing the save of iflag.
       */
      pedp.xflgs = -1;
      /******************************************
       * mercury - pluto *
       ******************************************/
    }
    else if (ipl == SweConst.SE_SUN /* main planet */
        || ipl == SweConst.SE_MERCURY || ipl == SweConst.SE_VENUS || ipl == SweConst.SE_MARS || ipl == SweConst.SE_JUPITER || ipl == SweConst.SE_SATURN || ipl == SweConst.SE_URANUS || ipl == SweConst.SE_NEPTUNE || ipl == SweConst.SE_PLUTO || ipl == SweConst.SE_EARTH)
    {
      if ((iflag & SweConst.SEFLG_HELCTR) != 0)
      {
        if (ipl == SweConst.SE_SUN)
        {
          /* heliocentric position of Sun does not exist */
          for (i = 0; i < 24; i++)
          {
            x[i] = 0;
          }
          return iflag;
        }
      }
      else if ((iflag & SweConst.SEFLG_BARYCTR) != 0)
      {
        // NOOP
      }
      else
      { /* geocentric */
        if (ipl == SweConst.SE_EARTH)
        {
          /* geocentric position of Earth does not exist */
          for (i = 0; i < 24; i++)
          {
            x[i] = 0;
          }
          return iflag;
        }
      }
      /* internal planet number */
      ipli = SwissData.pnoext2int[ipl];
      pdp = swissData.pldat[ipli];
      xp = pdp.xreturn;
      retc = main_planet(tjd, ipli, epheflag, iflag, serr);
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      /* iflag has possibly changed in main_planet() */
      iflag = pdp.xflgs;
      /**********************************************
       * mean lunar node * for comment s. moshmoon.c, swi_mean_node() *
       **********************************************/
    }
    else if (ipl == SweConst.SE_MEAN_NODE)
    {
      if (((iflag & SweConst.SEFLG_HELCTR) != 0) || ((iflag & SweConst.SEFLG_BARYCTR) != 0))
      {
        /* heliocentric/barycentric lunar node not allowed */
        for (i = 0; i < 24; i++)
        {
          x[i] = 0;
        }
        return iflag;
      }
      ndp = swissData.nddat[SwephData.SEI_MEAN_NODE];
      xp = ndp.xreturn;
      xp2 = ndp.x;
      retc = swemMoon.swi_mean_node(tjd, xp2, serr);
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      /* speed (is almost constant; variation < 0.001 arcsec) */
      retc = swemMoon
          .swi_mean_node(tjd - SwephData.MEAN_NODE_SPEED_INTV, xp2, 3, serr);
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      xp2[3] = swissLib.swe_difrad2n(xp2[0], xp2[3]) / SwephData.MEAN_NODE_SPEED_INTV;
      xp2[4] = xp2[5] = 0;
      ndp.teval = tjd;
      ndp.xflgs = -1;
      /* lighttime etc. */
      retc = app_pos_etc_mean(SwephData.SEI_MEAN_NODE, iflag, serr);
      if (retc != SweConst.OK)
      {
        return swecalc_error(x);
      }
      /*
       * to avoid infinitesimal deviations from latitude = 0 that result from
       * conversions
       */
      if ((iflag & SweConst.SEFLG_SIDEREAL) == 0 && (iflag & SweConst.SEFLG_J2000) == 0)
      {
        ndp.xreturn[1] = 0.0; /* ecl. latitude */
        ndp.xreturn[4] = 0.0; /* speed */
        ndp.xreturn[5] = 0.0; /* radial speed */
        ndp.xreturn[8] = 0.0; /* z coordinate */
        ndp.xreturn[11] = 0.0; /* speed */
      }
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      /**********************************************
       * mean lunar apogee ('dark moon', 'lilith') * for comment s. moshmoon.c,
       * swi_mean_apog() *
       **********************************************/
    }
    else if (ipl == SweConst.SE_MEAN_APOG)
    {
      if (((iflag & SweConst.SEFLG_HELCTR) != 0) || ((iflag & SweConst.SEFLG_BARYCTR) != 0))
      {
        /* heliocentric/barycentric lunar apogee not allowed */
        for (i = 0; i < 24; i++)
        {
          x[i] = 0;
        }
        return iflag;
      }
      ndp = swissData.nddat[SwephData.SEI_MEAN_APOG];
      xp = ndp.xreturn;
      xp2 = ndp.x;
      retc = swemMoon.swi_mean_apog(tjd, xp2, serr);
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      /* speed (is not constant! variation ~= several arcsec) */
      retc = swemMoon
          .swi_mean_apog(tjd - SwephData.MEAN_NODE_SPEED_INTV, xp2, 3, serr);
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      for (i = 0; i <= 1; i++)
      {
        xp2[3 + i] = swissLib.swe_difrad2n(xp2[i], xp2[3 + i]) / SwephData.MEAN_NODE_SPEED_INTV;
      }
      xp2[5] = 0;
      ndp.teval = tjd;
      ndp.xflgs = -1;
      /* lighttime etc. */
      if ((retc = app_pos_etc_mean(SwephData.SEI_MEAN_APOG, iflag, serr)) != SweConst.OK)
      {
        return swecalc_error(x);
      }
      /*
       * to avoid infinitesimal deviations from r-speed = 0 that result from
       * conversions
       */
      ndp.xreturn[5] = 0.0; /* speed */
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      /***********************************************
       * osculating lunar node ('true node') *
       ***********************************************/
    }
    else if (ipl == SweConst.SE_TRUE_NODE)
    {
      if (((iflag & SweConst.SEFLG_HELCTR) != 0) || ((iflag & SweConst.SEFLG_BARYCTR) != 0))
      {
        /* heliocentric/barycentric lunar node not allowed */
        for (i = 0; i < 24; i++)
        {
          x[i] = 0;
        }
        return iflag;
      }
      ndp = swissData.nddat[SwephData.SEI_TRUE_NODE];
      xp = ndp.xreturn;
      retc = lunar_osc_elem(tjd, SwephData.SEI_TRUE_NODE, iflag, serr);
      iflag = ndp.xflgs;
      /*
       * to avoid infinitesimal deviations from latitude = 0 that result from
       * conversions
       */
      if ((iflag & SweConst.SEFLG_SIDEREAL) == 0 && (iflag & SweConst.SEFLG_J2000) == 0)
      {
        ndp.xreturn[1] = 0.0; /* ecl. latitude */
        ndp.xreturn[4] = 0.0; /* speed */
        ndp.xreturn[8] = 0.0; /* z coordinate */
        ndp.xreturn[11] = 0.0; /* speed */
      }
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      /***********************************************
       * osculating lunar apogee *
       ***********************************************/
    }
    else if (ipl == SweConst.SE_OSCU_APOG)
    {
      if (((iflag & SweConst.SEFLG_HELCTR) != 0) || ((iflag & SweConst.SEFLG_BARYCTR) != 0))
      {
        /* heliocentric/barycentric lunar apogee not allowed */
        for (i = 0; i < 24; i++)
        {
          x[i] = 0;
        }
        return iflag;
      }
      ndp = swissData.nddat[SwephData.SEI_OSCU_APOG];
      xp = ndp.xreturn;
      retc = lunar_osc_elem(tjd, SwephData.SEI_OSCU_APOG, iflag, serr);
      iflag = ndp.xflgs;
      if (retc == SweConst.ERR)
      {
        return swecalc_error(x);
      }
      /***********************************************
       * interpolated lunar apogee *
       ***********************************************/
    }
    else if (ipl == SweConst.SE_INTP_APOG)
    {
      if ((iflag & SweConst.SEFLG_HELCTR) != 0 || (iflag & SweConst.SEFLG_BARYCTR) != 0)
      {
        /* heliocentric/barycentric lunar apogee not allowed */
        for (i = 0; i < 24; i++)
        {
          x[i] = 0;
        }
        return iflag;
      }
      ndp = swissData.nddat[SwephData.SEI_INTP_APOG];
      xp = ndp.xreturn;
      retc = intp_apsides(tjd, SwephData.SEI_INTP_APOG, iflag, serr);
      iflag = ndp.xflgs;
      if (retc == SweConst.ERR)
        return swecalc_error(x);
      /***********************************************
       * interpolated lunar perigee *
       ***********************************************/
    }
    else if (ipl == SweConst.SE_INTP_PERG)
    {
      if ((iflag & SweConst.SEFLG_HELCTR) != 0 || (iflag & SweConst.SEFLG_BARYCTR) != 0)
      {
        /* heliocentric/barycentric lunar apogee not allowed */
        for (i = 0; i < 24; i++)
        {
          x[i] = 0;
        }
        return iflag;
      }
      ndp = swissData.nddat[SwephData.SEI_INTP_PERG];
      xp = ndp.xreturn;
      retc = intp_apsides(tjd, SwephData.SEI_INTP_PERG, iflag, serr);
      iflag = ndp.xflgs;
      if (retc == SweConst.ERR)
        return swecalc_error(x);
      /***********************************************
       * minor planets *
       ***********************************************/
    }
    else if (ipl == SweConst.SE_CHIRON || ipl == SweConst.SE_PHOLUS || ipl == SweConst.SE_CERES /*
                                                                                                 * Ceres
                                                                                                 * -
                                                                                                 * Vesta
                                                                                                 */
        || ipl == SweConst.SE_PALLAS || ipl == SweConst.SE_JUNO || ipl == SweConst.SE_VESTA || ipl > SweConst.SE_AST_OFFSET)
    {
      /* internal planet number */
      if (ipl < SweConst.SE_NPLANETS)
      {
        ipli = SwissData.pnoext2int[ipl];
      }
      else if (ipl <= SweConst.SE_AST_OFFSET + SwephData.MPC_VESTA)
      {
        ipli = SwephData.SEI_CERES + ipl - SweConst.SE_AST_OFFSET - 1;
        ipl = SweConst.SE_CERES + ipl - SweConst.SE_AST_OFFSET - 1;
      }
      else
      { /* any asteroid except */
        ipli = SwephData.SEI_ANYBODY;
      }
      if (ipli == SwephData.SEI_ANYBODY)
      {
        ipli_ast = ipl;
      }
      else
      {
        ipli_ast = ipli;
      }
      pdp = swissData.pldat[ipli];
      xp = pdp.xreturn;
      if (ipli_ast > SweConst.SE_AST_OFFSET)
      {
        ifno = SwephData.SEI_FILE_ANY_AST;
      }
      else
      {
        ifno = SwephData.SEI_FILE_MAIN_AST;
      }
      if (ipli == SwephData.SEI_CHIRON && (tjd < SwephData.CHIRON_START || tjd > SwephData.CHIRON_END))
      {
        if (serr != null)
        {
          serr.setLength(0);
          serr.append("Chiron's ephemeris is restricted to JD " + SwephData.CHIRON_START + " - JD " + SwephData.CHIRON_END);
        }
        return SweConst.ERR;
      }
      if (ipli == SwephData.SEI_PHOLUS && tjd < SwephData.PHOLUS_START)
      {
        if (serr != null)
        {
          serr.setLength(0);
          serr.append("Pholus's ephemeris is restricted to the time after JD " + SwephData.PHOLUS_START);
        }
        return SweConst.ERR;
      }
      // do_asteroid:
      while (true)
      {
        /* earth and sun are also needed */
        retc = main_planet(tjd, SwephData.SEI_EARTH, epheflag, iflag, serr);
        if (retc == SweConst.ERR)
        {
          return swecalc_error(x);
        }
        /* iflag (ephemeris bit) has possibly changed in main_planet() */
        iflag = swissData.pldat[SwephData.SEI_EARTH].xflgs;
        /* asteroid */
        if (serr != null)
        {
          serr2 = serr.toString();
          serr.setLength(0);
        }
        /* asteroid */
        retc = sweph(tjd, ipli_ast, ifno, iflag, psdp.x, SwephData.DO_SAVE, null, serr);
        if (retc == SweConst.ERR || retc == SwephData.NOT_AVAILABLE)
        {
          return swecalc_error(x);
        }
        retc = app_pos_etc_plan(ipli_ast, iflag, serr);
        if (retc == SweConst.ERR)
        {
          return swecalc_error(x);
        }
        /*
         * app_pos_etc_plan() might have failed, if t(light-time) is beyond
         * ephemeris range. in this case redo with Moshier
         */
        if (retc == SwephData.NOT_AVAILABLE || retc == SwephData.BEYOND_EPH_LIMITS)
        {
          if (epheflag != SweConst.SEFLG_MOSEPH)
          {
            iflag = (iflag & ~SweConst.SEFLG_EPHMASK) | SweConst.SEFLG_MOSEPH;
            epheflag = SweConst.SEFLG_MOSEPH;
            if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
            {
              serr.append("\nusing Moshier eph.; ");
            }
            // goto do_asteroid;
            continue;
          }
          else
            return swecalc_error(x);
        }
        break;
      }
      /* add warnings from earth/sun computation */
      if (serr != null && serr.length() == 0 && serr2.length() != 0)
      {
        serr.setLength(0);
        serr2 = serr2
            .substring(0, SMath.min(serr2.length(), SwissData.AS_MAXCH - 5));
        serr.append("sun: " + serr2);
      }
      /***********************************************
       * fictitious planets * (Isis-Transpluto and Uranian planets) *
       ***********************************************/
      // JAVA: Geht nur mit Moshier Routinen???
    }
    else if (ipl >= SweConst.SE_FICT_OFFSET && ipl <= SweConst.SE_FICT_MAX)
    {
      /* internal planet number */
      ipli = SwephData.SEI_ANYBODY;
      pdp = swissData.pldat[ipli];
      xp = pdp.xreturn;
      // do_fict_plan:
      while (true)
      {
        /* the earth for geocentric position */
        retc = main_planet(tjd, SwephData.SEI_EARTH, epheflag, iflag, serr);
        /* iflag (ephemeris bit) has possibly changed in main_planet() */
        iflag = swissData.pldat[SwephData.SEI_EARTH].xflgs;
        /* planet from osculating elements */
        if (swephMosh.swi_osc_el_plan(tjd, pdp.x, ipl - SweConst.SE_FICT_OFFSET, ipli, pedp.x, psdp.x, serr) != SweConst.OK)
        {
          return swecalc_error(x);
        }
        if (retc == SweConst.ERR)
        {
          return swecalc_error(x);
        }
        retc = app_pos_etc_plan_osc(ipl, ipli, iflag, serr);
        if (retc == SweConst.ERR)
        {
          return swecalc_error(x);
        }
        /*
         * app_pos_etc_plan_osc() might have failed, if t(light-time) is beyond
         * ephemeris range. in this case redo with Moshier
         */
        if (retc == SwephData.NOT_AVAILABLE || retc == SwephData.BEYOND_EPH_LIMITS)
        {
          if (epheflag != SweConst.SEFLG_MOSEPH)
          {
            iflag = (iflag & ~SweConst.SEFLG_EPHMASK) | SweConst.SEFLG_MOSEPH;
            epheflag = SweConst.SEFLG_MOSEPH;
            if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
            {
              serr.append("\nusing Moshier eph.; ");
            }
            // goto do_fict_plan;
            continue;
          }
          else
            return swecalc_error(x);
        }
        break;
      }
      /***********************************************
       * invalid body number *
       ***********************************************/
    }
    else
    {
      if (serr != null)
      {
        serr.setLength(0);
        serr.append("illegal planet number " + ipl + ".");
      }
      return swecalc_error(x);
    }
    for (i = 0; i < 24; i++)
    {
      x[i] = xp[i];
    }
    return (iflag);
  } // swecalc()
  
  /***********************************************
   * return error                                *
   ***********************************************/
  private int swecalc_error(double x[]) 
  {
    for (int i = 0; i < 24; i++) {
      x[i] = 0.;
    }
    return SweConst.ERR;
  }
  
  private int sweph_moon(double tjd, int ipli, int iflag, StringBuffer serr)
  {
    int retc;
    retc = sweplan(tjd, ipli, SwephData.SEI_FILE_MOON, iflag, SwephData.DO_SAVE, null, null, null, null, serr);
    if (retc == SweConst.ERR)
    {
      return SweConst.ERR;
    }
    /* if sweph file not found, switch to moshier */
    if (retc == SwephData.NOT_AVAILABLE)
    {
      if (tjd > SwephData.MOSHLUEPH_START && tjd < SwephData.MOSHLUEPH_END)
      {
        iflag = (iflag & ~SweConst.SEFLG_SWIEPH) | SweConst.SEFLG_MOSEPH;
        if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
        {
          serr.append(" \nusing Moshier eph.; ");
        }
        // goto moshier_moon;
        retc = moshier_moon(tjd, SwephData.DO_SAVE, null, serr);
        if (retc == SweConst.ERR)
        {
          return SweConst.ERR;
        }
      }
      else
        return SweConst.ERR;
    }
    return SweConst.OK;
  } // sweph_moon()

  
  private int moshier_moon(double tjd, boolean do_save, double[] xpmret, StringBuffer serr)
  {
    int retc = swemMoon.swi_moshmoon(tjd, do_save, null, serr);/**/
    if (retc == SweConst.ERR)
    {
      return SweConst.ERR;
    }
    /* for hel. position, we need earth as well */
    retc = swephMosh
        .swi_moshplan(tjd, SwephData.SEI_EARTH, do_save, null, null, serr);/**/
    if (retc == SweConst.ERR)
    {
      return SweConst.ERR;
    }
    return SweConst.OK;
  } // moshier_moon()
  
  private int sweph_sbar(double tjd, int iflag, PlanData psdp, PlanData pedp, StringBuffer serr)
  {
    int retc;
    /*
     * sweplan() provides barycentric sun as a by-product in save area; it is
     * saved in swed.pldat[SEI_SUNBARY].x
     */
    retc = sweplan(tjd, SwephData.SEI_EARTH, SwephData.SEI_FILE_PLANET, iflag, SwephData.DO_SAVE, null, null, null, null, serr);
    if (retc == SweConst.ERR || retc == SwephData.NOT_AVAILABLE)
    {
      return SweConst.ERR;
    }
    psdp.teval = tjd;
    /* pedp.teval = tjd; */
    return SweConst.OK;
  } // sweph_sbar()
  
  
  /* SWISSEPH
   * this function computes
   * 1. a barycentric planet
   * plus, under certain conditions,
   * 2. the barycentric sun,
   * 3. the barycentric earth, and
   * 4. the geocentric moon,
   * in barycentric cartesian equatorial coordinates J2000.
   *
   * these are the data needed for calculation of light-time etc.
   *
   * tjd          julian date
   * ipli         SEI_ planet number
   * ifno         ephemeris file number
   * do_save      write new positions in save area
   * xp           array of 6 doubles for planet's position and velocity
   * xpe                                 earth's
   * xps                                 sun's
   * xpm                                 moon's
   * serr         error string
   *
   * xp - xpm can be NULL. if do_save is TRUE, all of them can be NULL.
   * the positions will be written into the save area (swed.pldat[ipli].x)
   */
  int sweplan(double tjd, int ipli, int ifno, int iflag, boolean do_save,
              double xpret[], double xperet[], double xpsret[],
              double xpmret[], StringBuffer serr) {
    int i, retc;
    boolean do_earth = false, do_moon = false, do_sunbary = false;
    PlanData pdp = swissData.pldat[ipli];
    PlanData pebdp = swissData.pldat[SwephData.SEI_EMB];
    PlanData psbdp = swissData.pldat[SwephData.SEI_SUNBARY];
    PlanData pmdp = swissData.pldat[SwephData.SEI_MOON];
    double xxp[]=new double[6], xxm[]=new double[6],
           xxs[]=new double[6], xxe[]=new double[6];
    double xp[], xpe[], xpm[], xps[];
    int speedf1, speedf2;
    /* xps (barycentric sun) may be necessary because some planets on sweph
     * file are heliocentric, other ones are barycentric. without xps,
     * the heliocentric ones cannot be returned barycentrically.
     */
    if (do_save || ipli == SwephData.SEI_SUNBARY
        || (pdp.iflg & SwephData.SEI_FLG_HELIO)!=0
        || xpsret != null || (iflag & SweConst.SEFLG_HELCTR)!=0) {
      do_sunbary = true;
    }
    if (do_save || ipli == SwephData.SEI_EARTH || xperet != null) {
      do_earth = true;
    }
    if (ipli == SwephData.SEI_MOON) {
        do_earth = true;
        do_sunbary = true;
    }
    if (do_save || ipli == SwephData.SEI_MOON || ipli == SwephData.SEI_EARTH ||
        xperet != null || xpmret != null) {
      do_moon = true;
    }
    if (do_save) {
      xp = pdp.x;
      xpe = pebdp.x;
      xps = psbdp.x;
      xpm = pmdp.x;
    } else {
      xp = xxp;
      xpe = xxe;
      xps = xxs;
      xpm = xxm;
    }
    speedf2 = iflag & SweConst.SEFLG_SPEED;
    /* barycentric sun */
    if (do_sunbary) {
      speedf1 = psbdp.xflgs & SweConst.SEFLG_SPEED;
      /* if planet has already been computed for this date, return
       * if speed flag has been turned on, recompute planet */
      if (tjd == psbdp.teval
          && psbdp.iephe == SweConst.SEFLG_SWIEPH
          && ((speedf2==0) || (speedf1!=0))) {
        for (i = 0; i <= 5; i++) {
          xps[i] = psbdp.x[i];
        }
      } else {
        retc = sweph(tjd, SwephData.SEI_SUNBARY, SwephData.SEI_FILE_PLANET, iflag,
                     null, do_save, xps, serr);/**/
        if (retc != SweConst.OK) {
          return(retc);
        }
      }
      if (xpsret != null) {
        for (i = 0; i <= 5; i++) {
          xpsret[i] = xps[i];
        }
      }
    }
    /* moon */
    if (do_moon) {
      speedf1 = pmdp.xflgs & SweConst.SEFLG_SPEED;
      if (tjd == pmdp.teval
          && pmdp.iephe == SweConst.SEFLG_SWIEPH
          && ((speedf2==0) || (speedf1!=0))) {
        for (i = 0; i <= 5; i++) {
          xpm[i] = pmdp.x[i];
        }
      } else {
        retc = sweph(tjd, SwephData.SEI_MOON, SwephData.SEI_FILE_MOON, iflag, null,
                     do_save, xpm, serr);
        if (retc == SweConst.ERR) {
          return(retc);
        }
        /* if moon file doesn't exist, take moshier moon */
        if (swissData.fidat[SwephData.SEI_FILE_MOON].fptr == null) {
          if (serr != null && serr.length() + 35 < SwissData.AS_MAXCH) {
            serr.append(" \nusing Moshier eph. for moon; ");
          }
          retc = sm.swi_moshmoon(tjd, do_save, xpm, serr);
          if (retc != SweConst.OK) {
            return(retc);
          }
        }
      }
      if (xpmret != null) {
        for (i = 0; i <= 5; i++) {
          xpmret[i] = xpm[i];
        }
      }
    }
    /* barycentric earth */
    if (do_earth) {
      speedf1 = pebdp.xflgs & SweConst.SEFLG_SPEED;
      if (tjd == pebdp.teval
          && pebdp.iephe == SweConst.SEFLG_SWIEPH
          && ((speedf2==0) || (speedf1!=0))) {
        for (i = 0; i <= 5; i++) {
          xpe[i] = pebdp.x[i];
        }
      } else {
        retc = sweph(tjd, SwephData.SEI_EMB, SwephData.SEI_FILE_PLANET, iflag, null,
                     do_save, xpe, serr);
        if (retc != SweConst.OK) {
          return(retc);
        }
        /* earth from emb and moon */
        embofs(xpe, 0, xpm, 0);
        /* speed is needed, if
         * 1. true position is being computed before applying light-time etc.
         *    this is the position saved in pdp->x.
         *    in this case, speed is needed for light-time correction.
         * 2. the speed flag has been specified.
         */
        if (xpe == pebdp.x || ((iflag & SweConst.SEFLG_SPEED)!=0)) {
          embofs(xpe, 3, xpm, 3);
        }
      }
      if (xperet != null) {
        for (i = 0; i <= 5; i++) {
          xperet[i] = xpe[i];
        }
      }
    }
    if (ipli == SwephData.SEI_MOON) {
      for (i = 0; i <= 5; i++) {
        xp[i] = xpm[i];
      }
    } else if (ipli == SwephData.SEI_EARTH) {
      for (i = 0; i <= 5; i++) {
        xp[i] = xpe[i];
      }
    } else if (ipli == SwephData.SEI_SUN) {
      for (i = 0; i <= 5; i++) {
        xp[i] = xps[i];
      }
    } else {
      /* planet */
      speedf1 = pdp.xflgs & SweConst.SEFLG_SPEED;
      if (tjd == pdp.teval
          && pdp.iephe == SweConst.SEFLG_SWIEPH
          && ((speedf2==0) || (speedf1!=0))) {
        for (i = 0; i <= 5; i++) {
          xp[i] = pdp.x[i];
        }
        return(SweConst.OK);
      } else {
        retc = sweph(tjd, ipli, ifno, iflag, null, do_save, xp, serr);
        if (retc != SweConst.OK) {
          return(retc);
        }
        /* if planet is heliocentric, it must be transformed to barycentric */
        if ((pdp.iflg & SwephData.SEI_FLG_HELIO)!=0) {
          /* now barycentric planet */
          for (i = 0; i <= 2; i++) {
            xp[i] += xps[i];
          }
          if (do_save || ((iflag & SweConst.SEFLG_SPEED)!=0)) {
            for (i = 3; i <= 5; i++) {
              xp[i] += xps[i];
            }
          }
        }
      }
    }
    if (xpret != null) {
      for (i = 0; i <= 5; i++) {
        xpret[i] = xp[i];
      }
    }
    return SweConst.OK;
  } // sweplan()
  
  /* computes a main planet from any ephemeris, if it
   * has not yet been computed for this date.
   * since a geocentric position requires the earth, the
   * earth's position will be computed as well. With SWISSEPH
   * files the barycentric sun will be done as well.
   * With Moshier, the moon will be done as well.
   *
   * tjd          = julian day
   * ipli         = body number
   * epheflag     = which ephemeris? JPL, SWISSEPH, Moshier?
   * iflag        = other flags
   *
   * the geocentric apparent position of ipli (or whatever has
   * been specified in iflag) will be saved in
   * &swed.pldat[ipli].xreturn[];
   *
   * the barycentric (heliocentric with Moshier) position J2000
   * will be kept in
   * &swed.pldat[ipli].x[];
   */
  int main_planet(double tjd, int ipli, int epheflag, int iflag, StringBuffer serr) throws SwissephException
  {
    int retc;
    boolean calc_swieph = false;
    boolean calc_moshier = false;
    if (epheflag == SweConst.SEFLG_JPLEPH)
    {
      retc = jplplan(tjd, ipli, iflag, SwephData.DO_SAVE, null, null, null, serr);
      /* read error or corrupt file */
      if (retc == SweConst.ERR)
      {
        return SweConst.ERR;
      }
      /* jpl ephemeris not on disk or date beyond ephemeris range */
      if (retc == SwephData.NOT_AVAILABLE)
      {
        iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
        if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
        {
          serr.append(" \ntrying Swiss Eph; ");
        }
        calc_swieph = true;
        // goto sweph_planet;
      }
      else if (retc == SwephData.BEYOND_EPH_LIMITS)
      {
        if (tjd > SwephData.MOSHPLEPH_START && tjd < SwephData.MOSHPLEPH_END)
        {
          iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_MOSEPH;
          if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
          {
            serr.append(" \nusing Moshier Eph; ");
          }
          calc_moshier = true;
          // goto moshier_planet;
        }
        else
        {
          return SweConst.ERR;
        }
      }
      if (!calc_swieph && !calc_moshier)
      {
        /* geocentric, lighttime etc. */
        if (ipli == SwephData.SEI_SUN)
        {
          retc = app_pos_etc_sun(iflag, serr)/**/;
        }
        else
        {
          retc = app_pos_etc_plan(ipli, iflag, serr);
        }
        if (retc == SweConst.ERR)
        {
          return SweConst.ERR;
        }
        /* t for light-time beyond ephemeris range */
        if (retc == SwephData.NOT_AVAILABLE)
        {
          iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_SWIEPH;
          if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
          {
            serr.append(" \ntrying Swiss Eph; ");
          }
          calc_swieph = true;
          // goto sweph_planet;
        }
        else if (retc == SwephData.BEYOND_EPH_LIMITS)
        {
          if (tjd > SwephData.MOSHPLEPH_START && tjd < SwephData.MOSHPLEPH_END)
          {
            iflag = (iflag & ~SweConst.SEFLG_JPLEPH) | SweConst.SEFLG_MOSEPH;
            if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
            {
              serr.append(" \nusing Moshier Eph; ");
            }
            calc_moshier = true;
            // goto moshier_planet;
          }
          else
          {
            return SweConst.ERR;
          }
        }
      }
    } // SweConst.SEFLG_JPLEPH
    if (epheflag == SweConst.SEFLG_SWIEPH || calc_swieph)
    {
      // sweph_planet:
      /* compute barycentric planet (+ earth, sun, moon) */
      retc = sweplan(tjd, ipli, SwephData.SEI_FILE_PLANET, iflag, SwephData.DO_SAVE, null, null, null, null, serr);
      if (retc == SweConst.ERR)
      {
        return SweConst.ERR;
      }
      /* if sweph file not found, switch to moshier */
      if (retc == SwephData.NOT_AVAILABLE)
      {
        if (tjd > SwephData.MOSHPLEPH_START && tjd < SwephData.MOSHPLEPH_END)
        {
          iflag = (iflag & ~SweConst.SEFLG_SWIEPH) | SweConst.SEFLG_MOSEPH;
          if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
          {
            serr.append(" \nusing Moshier eph.; ");
          }
          calc_moshier = true;
          // goto moshier_planet;
        }
        else
        {
          return SweConst.ERR;
        }
      }
      if (!calc_moshier)
      {
        /* geocentric, lighttime etc. */
        if (ipli == SwephData.SEI_SUN)
        {
          retc = app_pos_etc_sun(iflag, serr)/**/;
        }
        else
        {
          retc = app_pos_etc_plan(ipli, iflag, serr);
        }
        if (retc == SweConst.ERR)
        {
          return SweConst.ERR;
        }
        /* if sweph file for t(lighttime) not found, switch to moshier */
        if (retc == SwephData.NOT_AVAILABLE)
        {
          if (tjd > SwephData.MOSHPLEPH_START && tjd < SwephData.MOSHPLEPH_END)
          {
            iflag = (iflag & ~SweConst.SEFLG_SWIEPH) | SweConst.SEFLG_MOSEPH;
            if (serr != null && serr.length() + 30 < SwissData.AS_MAXCH)
            {
              serr.append(" \nusing Moshier eph.; ");
            }
            calc_moshier = true;
            // goto moshier_planet;
          }
          else
          {
            return SweConst.ERR;
          }
        }
      } // SweConst.SEFLG_SWIEPH
    } // !calc_moshier
    if (epheflag == SweConst.SEFLG_MOSEPH || calc_moshier)
    {
      // moshier_planet:
      retc = swephMosh.swi_moshplan(tjd, ipli, SwephData.DO_SAVE, null, null, serr);/**/
      if (retc == SweConst.ERR)
      {
        return SweConst.ERR;
      }
      /* geocentric, lighttime etc. */
      if (ipli == SwephData.SEI_SUN)
      {
        retc = app_pos_etc_sun(iflag, serr)/**/;
      }
      else
      {
        retc = app_pos_etc_plan(ipli, iflag, serr);
      }
      if (retc == SweConst.ERR)
      {
        return SweConst.ERR;
      }
    }
    return SweConst.OK;
  } //main_planet()
  
  //Only used with SEFLG_SPEED3
  protected void calc_speed(double[] x0, double[] x1, double[] x2, double dt) {
    int i, j, k;
    double a, b;
    for (j = 0; j <= 18; j += 6) {
      for (i = 0; i < 3; i++) {
        k = j + i;
        b = (x2[k] - x0[k]) / 2;
        a = (x2[k] + x0[k]) / 2 - x1[k];
        x1[k+3] = (2 * a + b) / dt;
      }
    }
  }
  
  //Only used with SEFLG_SPEED3
  protected void denormalize_positions(double[] x0, double[] x1, double[] x2) {
    int i;
    /* x*[0] = ecliptic longitude, x*[12] = rectascension */
    for (i = 0; i <= 12; i += 12) {
      if (x1[i] - x0[i] < -180) {
        x0[i] -= 360;
      }
      if (x1[i] - x0[i] > 180) {
        x0[i] += 360;
      }
      if (x1[i] - x2[i] < -180) {
        x2[i] -= 360;
      }
      if (x1[i] - x2[i] > 180) {
        x2[i] += 360;
      }
    }
  }
  
  protected int plaus_iflag(int iflag)
  {
    int epheflag = 0;
    /* if topocentric bit, turn helio- and barycentric bits off */
    if ((iflag & SweConst.SEFLG_TOPOCTR) != 0)
    {
      iflag = iflag & ~(SweConst.SEFLG_HELCTR | SweConst.SEFLG_BARYCTR);
    }
    /* if heliocentric bit, turn aberration and deflection off */
    if ((iflag & SweConst.SEFLG_HELCTR) != 0)
    {
      iflag |= SweConst.SEFLG_NOABERR | SweConst.SEFLG_NOGDEFL;
      /* iflag |= SEFLG_TRUEPOS; */
    }
    /* same, if barycentric bit */
    if ((iflag & SweConst.SEFLG_BARYCTR) != 0)
    {
      iflag |= SweConst.SEFLG_NOABERR | SweConst.SEFLG_NOGDEFL;
      /* iflag |= SEFLG_TRUEPOS; */
    }
    /* if no_precession bit is set, set also no_nutation bit */
    if ((iflag & SweConst.SEFLG_J2000) != 0)
    {
      iflag |= SweConst.SEFLG_NONUT;
    }
    /* if truepos is set, turn off grav. defl. and aberration */
    if ((iflag & SweConst.SEFLG_TRUEPOS) != 0)
    {
      iflag |= (SweConst.SEFLG_NOGDEFL | SweConst.SEFLG_NOABERR);
    }
    /* if sidereal bit is set, set also no_nutation bit */
    if ((iflag & SweConst.SEFLG_SIDEREAL) != 0)
    {
      iflag |= SweConst.SEFLG_NONUT;
    }
    if ((iflag & SweConst.SEFLG_MOSEPH) != 0)
    {
      epheflag = SweConst.SEFLG_MOSEPH;
    }
    if ((iflag & SweConst.SEFLG_SWIEPH) != 0)
    {
      epheflag = SweConst.SEFLG_SWIEPH;
    }
    if ((iflag & SweConst.SEFLG_JPLEPH) != 0)
    {
      epheflag = SweConst.SEFLG_JPLEPH;
    }
    if (epheflag == 0)
    {
      epheflag = SweConst.SEFLG_DEFAULTEPH;
    }
    /* delete wrong ephe bits from flag */
    iflag = (iflag & ~SweConst.SEFLG_EPHMASK) | epheflag;

    return iflag;
  } // plaus_iflag()
  
  /*
   * Alois 2.12.98: inserted error message generation for file not found
   */
  FilePtr swi_fopen(int ifno, String fname, String ephepath, StringBuffer serr) throws SwissephException {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.log("SwissEph.swi_fopen(int, String <" + fname + ">, String, StringBuffer)");
////#ifdef TRACE1
//    Trace.log("   ifno: " + ifno + "\n    fname: " + fname + "\n    ephepath: " + ephepath + "\n    serr: " + serr);
////#endif /* TRACE1 */
////#endif /* TRACE0 */
    int np, i;
    java.io.RandomAccessFile fp = null;
    String fnamp;
    String[] cpos=new String[20];
    String path, s, s1;
    // if (ifno >= 0) ...: Semantik in den try - catch Block verlagert!!!
    s1=ephepath;
    np = swissLib.swi_cutstr(s1, SwissData.PATH_SEPARATOR, cpos, 20);
    for (i = 0; i < np; i++) {
      path=cpos[i];
// Why this? We skip this differentiation:
//      if (path.equals(".")) { /* current directory */
//        path = "";
//      } else {
//        if (!path.equals("") && !path.endsWith(swed.DIR_GLUE)) {
//            path+=swed.DIR_GLUE;
//          }
//      }
      if ("".equals(path)) { path="."; }
      fnamp=path+swissData.DIR_GLUE;
      if (fnamp.length() + fname.length() < SwissData.AS_MAXCH) {
        fnamp+=fname;
      } else {
        if (serr != null) {
          serr.setLength(0);
          serr.append("error: file path and name must be shorter than "+ SwissData.AS_MAXCH+".");
        }
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
        throw new SwissephException(1./0., SwissephException.INVALID_FILE_NAME,
            SweConst.ERR, serr);
      }
      try {
        fp = new java.io.RandomAccessFile(fnamp, SwissData.BFILE_R_ACCESS);
        if (ifno >= 0) {
          swissData.fidat[ifno].fnam=fnamp;
        }
        FilePtr sfp = new FilePtr(fp,null,null,null,fnamp,-1,httpBufSize);
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
        return sfp;
      } catch (java.io.IOException ex) {
        // Maybe it is an URL...
        FilePtr f=tryFileAsURL(path+"/"+fname, ifno);
        if (f!=null) {
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
          return f;
        }
      } catch (SecurityException ex) {
        // Probably an applet, we try fnamp as an URL:
        FilePtr f=tryFileAsURL(path+"/"+fname, ifno);
        if (f!=null) {
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
          return f;
        }
      }
    }
//    s="SwissEph file '"+fname+"' not found in PATH '"+ephepath+"'";
    s="SwissEph file '"+fname+"' not found in the paths of: ";
    for (int n=0;n<cpos.length;n++) {
      if (cpos[n]!=null && !"".equals(cpos[n])) { s+="'"+cpos[n]+"', "; }
    }
    /* s may be longer then AS_MAXCH */
// Who cares...
//    s=s.substring(0,SMath.min(s.length(),SwissData.AS_MAXCH));
    if (serr != null) {
      serr.setLength(0);
      serr.append(s);
    }
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    throw new SwissephException(1./0., SwissephException.FILE_NOT_FOUND,
        SwephData.NOT_AVAILABLE, serr);
  }
  
  private FilePtr tryFileAsURL(String fnamp, int ifno) {
////#ifdef TRACE0
//    Trace.level++;
//    Trace.log("SwissEph.tryFileAsURL(String, int)");
////#ifdef TRACE1
//    Trace.log("   fnamp: " + fnamp + "\n    ifno: " + ifno);
////#endif /* TRACE1 */
////#endif /* TRACE0 */
    if (!fnamp.startsWith("http://")) {
        return null;
    }
    Socket sk=null;
    try {
      URL u=new URL(fnamp);
      sk=new Socket(u.getHost(),(u.getPort()<0?80:u.getPort()));
      String sht="HEAD "+fnamp+" HTTP/1.1\r\n"+
                 "User-Agent: "+FilePtr.useragent+"\r\n"+
                 "Host: "+u.getHost()+":"+(u.getPort()<0?80:u.getPort())+
                                                                  "\r\n\r\n";
      sk.setSoTimeout(5000);
      InputStream is=sk.getInputStream();
      BufferedOutputStream os=new BufferedOutputStream(sk.getOutputStream());
      for(int n=0; n<sht.length(); n++) {
        os.write((byte)sht.charAt(n));
      }
      os.flush();
      String sret=""+(char)is.read();
      while (is.available()>0) {
        sret+=(char)is.read();
      }
      int idx=sret.indexOf("Content-Length:");
      if (idx < 0) {
        sk.close();
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
        return null;
      }
      // We need to query ranges, otherwise it will not make much sense...
      if (sret.indexOf("Accept-Ranges: none")>=0) {
        logger.warning("Server does not accept HTTP range requests. Aborting!");
        sk.close();
////#ifdef TRACE0
//        Trace.level--;
////#endif /* TRACE0 */
        return null;
      }
      sret=sret.substring(idx+"Content-Length:".length());
      sret=sret.substring(0,sret.indexOf("\n")).trim();
// We might want to check for a minimum length?
      long len=Long.parseLong(sret);
      if (ifno >= 0) {
        swissData.fidat[ifno].fnam=fnamp;
      }
////#ifdef TRACE0
//      Trace.level--;
////#endif /* TRACE0 */
      return new FilePtr(null,sk,is,os,fnamp,len,httpBufSize);
    } catch (MalformedURLException m) {
    } catch (IOException ie) {
    } catch (NumberFormatException nf) {
      // Why this? Should not be able to happen...
    } catch (SecurityException se) {
    }
    try { sk.close(); }
    catch (IOException e) { }
    catch (NullPointerException np) { }
////#ifdef TRACE0
//    Trace.level--;
////#endif /* TRACE0 */
    return null;
  }
  
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
  @Override
  public double getAyanamsa(double tjd_et)
  {
    double x[]=new double[6], eps;
    SidData sip = swissData.sidd;
    if (!swissData.ayana_is_set) {
      setSidMode(SweConst.SE_SIDM_FAGAN_BRADLEY, 0, 0);
    }
    /* vernal point (tjd), cartesian */
    x[0] = 1;
    x[1] = x[2] = 0;
    /* to J2000 */
    if (tjd_et != SwephData.J2000) {
      swissLib.swi_precess(x, tjd_et, SwephData.J_TO_J2000);
    }
    /* to t0 */
    swissLib.swi_precess(x, sip.t0, SwephData.J2000_TO_J);
    /* to ecliptic */
    eps = swissLib.swi_epsiln(sip.t0);
    swissLib.swi_coortrf(x, x, eps);
    /* to polar */
    swissLib.swi_cartpol(x, x);
    /* subtract initial value of ayanamsa */
    x[0] = x[0] * SwissData.RADTODEG - sip.ayan_t0;
    /* get ayanamsa */
    return swissLib.swe_degnorm(-x[0]);
  }
  
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
  @Override
  public double getAyanamsaUt(double tjd_ut)
  {
    return getAyanamsa(tjd_ut + SweDate.getDeltaT(tjd_ut));
  }
  
  @Override
  public String getAyanamsaName(int isidmode) 
  {
    if (isidmode < SwissData.SE_NSIDM_PREDEF)
      return SwissData.ayanamsa_name[isidmode];
    return null;
  }

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
  @Override
  public void setSidMode(int sid_mode, double t0, double ayan_t0)
  {
    SidData sip = swissData.sidd;
    sip.sid_mode = sid_mode;
    if (sid_mode >= SweConst.SE_SIDBITS) {
      sid_mode %= SweConst.SE_SIDBITS;
    }
    /* standard equinoxes: positions always referred to ecliptic of t0 */
    if (sid_mode == SweConst.SE_SIDM_J2000
            || sid_mode == SweConst.SE_SIDM_J1900
            || sid_mode == SweConst.SE_SIDM_B1950) {
      sip.sid_mode |= SweConst.SE_SIDBIT_ECL_T0;
    }
    if (sid_mode >= SwissData.SE_NSIDM_PREDEF && sid_mode != SweConst.SE_SIDM_USER) {
      sip.sid_mode = sid_mode = SweConst.SE_SIDM_FAGAN_BRADLEY;
    }
    swissData.ayana_is_set = true;
    if (sid_mode == SweConst.SE_SIDM_USER) {
      sip.t0 = t0;
      sip.ayan_t0 = ayan_t0;
    } else {
      sip.t0 = SwephData.ayanamsa[sid_mode].t0;
      sip.ayan_t0 = SwephData.ayanamsa[sid_mode].ayan_t0;
    }
    swi_force_app_pos_etc();
  }


  protected void swi_force_app_pos_etc() 
  {
    int i;
    for (i = 0; i < SwephData.SEI_NPLANETS; i++) {
      swissData.pldat[i].xflgs = -1;
    }
    for (i = 0; i < SwephData.SEI_NNODE_ETC; i++) {
      swissData.nddat[i].xflgs = -1;
    }
    for (i = 0; i < SweConst.SE_NPLANETS; i++) {
      swissData.savedat[i].tsave = 0;
      swissData.savedat[i].iflgsave = -1;
    }
  }

  /**
   * Returns the version information of this swisseph package.
   * 
   * @return package information in the form x.yy.zz
   * @see SwissEph#swe_java_version()
   */
  public String swe_version()
  {
    return SwephData.SE_VERSION;
  }

  /**
   * Returns the version information of this swisseph package including the
   * version of this java port.
   * 
   * @return package information in the form x.yy.zz_jj
   * @see SwissEph#swe_version()
   */
  public String swe_java_version()
  {
    return SwephData.SE_JAVA_VERSION;
  }  
}
