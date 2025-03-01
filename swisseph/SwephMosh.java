/*
   This is a port of the Swiss Ephemeris Free Edition, Version 1.76.00
   of Astrodienst AG, Switzerland from the original C Code to Java. For
   copyright see the original copyright notices below and additional
   copyright notes in the file named LICENSE, or - if this file is not
   available - the copyright notes at http://www.astro.ch/swisseph/ and
   following. 

   For any questions or comments regarding this port to Java, you should
   ONLY contact me and not Astrodienst, as the Astrodienst AG is not involved
   in this port in any way.

   Thomas Mack, mack@ifis.cs.tu-bs.de, 23rd of April 2001

*/
/* Copyright (C) 1997 - 2008 Astrodienst AG, Switzerland.  All rights reserved.

  License conditions
  ------------------

  This file is part of Swiss Ephemeris.

  Swiss Ephemeris is distributed with NO WARRANTY OF ANY KIND.  No author
  or distributor accepts any responsibility for the consequences of using it,
  or for whether it serves any particular purpose or works at all, unless he
  or she says so in writing.

  Swiss Ephemeris is made available by its authors under a dual licensing
  system. The software developer, who uses any part of Swiss Ephemeris
  in his or her software, must choose between one of the two license models,
  which are
  a) GNU public license version 2 or later
  b) Swiss Ephemeris Professional License

  The choice must be made before the software developer distributes software
  containing parts of Swiss Ephemeris to others, and before any public
  service using the developed software is activated.

  If the developer choses the GNU GPL software license, he or she must fulfill
  the conditions of that license, which includes the obligation to place his
  or her whole software project under the GNU GPL or a compatible license.
  See http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

  If the developer choses the Swiss Ephemeris Professional license,
  he must follow the instructions as found in http://www.astro.com/swisseph/
  and purchase the Swiss Ephemeris Professional Edition from Astrodienst
  and sign the corresponding license contract.

  The License grants you the right to use, copy, modify and redistribute
  Swiss Ephemeris, but only under certain conditions described in the License.
  Among other things, the License requires that the copyright notices and
  this notice be preserved on all copies.

  Authors of the Swiss Ephemeris: Dieter Koch and Alois Treindl

  The authors of Swiss Ephemeris have no control or influence over any of
  the derived works, i.e. over software or services created by other
  programmers which use Swiss Ephemeris functions.

  The names of the authors or of the copyright holder (Astrodienst) must not
  be used for promoting any software, product or service which uses or contains
  the Swiss Ephemeris. This copyright notice is the ONLY place where the
  names of the authors can legally appear, except in cases where they have
  given special permission in writing.

  The trademarks 'Swiss Ephemeris' and 'Swiss Ephemeris inside' may be used
  for promoting such software, products or services.
*/
package swisseph;


class SwephMosh implements java.io.Serializable {

  SwissLib swissLib=null;
  AbstractSwissEph swissEph=null;
  SwissData swissData=null;
  Swemmoon swemMoon=null;
  SweDate sweDate=null;


  private static final double TIMESCALE=3652500.0;
  private static final int FICT_GEO=1;
  private static final int pnoint2msh[] = {2, 2, 0, 1, 3, 4, 5, 6, 7, 8};

  /* From Simon et al (1994)  */
  private static final double freqs[] = {
  /* Arc sec per 10000 Julian years.  */
    53810162868.8982,
    21066413643.3548,
    12959774228.3429,
    6890507749.3988,
    1092566037.7991,
    439960985.5372,
    154248119.3933,
    78655032.0744,
    52272245.1795
  };

  private static final double phases[] = {
  /* Arc sec.  */
    252.25090552 * 3600.,
    181.97980085 * 3600.,
    100.46645683 * 3600.,
    355.43299958 * 3600.,
    34.35151874 * 3600.,
    50.07744430 * 3600.,
    314.05500511 * 3600.,
    304.34866548 * 3600.,
    860492.1546,
  };

  double ss[][]=new double[9][24];
  double cc[][]=new double[9][24];



  SwephMosh(SwissLib sl, AbstractSwissEph swissEph, SwissData swed) {
    this.swissLib    = sl;
    this.swissEph    = swissEph;
    this.swissData  = swed;
    this.swemMoon    = new Swemmoon();
    if (this.swissLib   ==null) { this.swissLib   =new SwissLib(); }
    if (this.swissEph   ==null) { this.swissEph   =new SwissEph(); }
    if (this.swissData ==null) { this.swissData =new SwissData(); }
  }



  private int swi_moshplan2 (double J, int iplm, double[] pobj) {
    int i, j, k, m, k1, ip, np, nt;
    byte p[]; int pOff=0;
    double pl[], pb[], pr[]; int plOff=0, pbOff=0, prOff=0;
    double su, cu, sv, cv, T;
    double t, sl, sb, sr;
    Plantbl plan = planets[iplm];

    T = (J - SwephData.J2000) / TIMESCALE;
    /* Calculate sin( i*MM ), etc. for needed multiple angles.  */
    for (i = 0; i < 9; i++) {
      if ((j = plan.max_harmonic[i]) > 0) {
        sr = (swemMoon.mods3600 (freqs[i] * T) + phases[i]) * SwephData.STR;
        sscc (i, sr, j);
      }
    }

    /* Point to start of table of arguments. */
    p = plan.arg_tbl;
    /* Point to tabulated cosine and sine amplitudes.  */
    pl = plan.lon_tbl;
    pb = plan.lat_tbl;
    pr = plan.rad_tbl;
    sl = 0.0;
    sb = 0.0;
    sr = 0.0;

    for (;;)
      {
        /* argument of sine and cosine */
        /* Number of periodic arguments. */
        np = p[pOff++];
        if (np < 0) {
          break;
        }
        if (np == 0) {                       /* It is a polynomial term.  */
            nt = p[pOff++];
            /* Longitude polynomial. */
            cu = pl[plOff++];
            for (ip = 0; ip < nt; ip++)
              {
                cu = cu * T + pl[plOff++];
              }
            sl +=  swemMoon.mods3600 (cu);
            /* Latitude polynomial. */
            cu = pb[pbOff++];
            for (ip = 0; ip < nt; ip++)
              {
                cu = cu * T + pb[pbOff++];
              }
            sb += cu;
            /* Radius polynomial. */
            cu = pr[prOff++];
            for (ip = 0; ip < nt; ip++)
              {
                cu = cu * T + pr[prOff++];
              }
            sr += cu;
            continue;
          }
        k1 = 0;
        cv = 0.0;
        sv = 0.0;
        for (ip = 0; ip < np; ip++)
          {
            /* What harmonic.  */
            j = p[pOff++];
            /* Which planet.  */
            m = p[pOff++] - 1;
            if (j!=0) {
                k = j;
                if (j < 0) {
                  k = -k;
                }
                k -= 1;
                su = ss[m][k];    /* sin(k*angle) */
                if (j < 0) {
                  su = -su;
                }
                cu = cc[m][k];
                if (k1 == 0) {               /* set first angle */
                    sv = su;
                    cv = cu;
                    k1 = 1;
                  }
                else
                  {               /* combine angles */
                    t = su * cv + cu * sv;
                    cv = cu * cv - su * sv;
                    sv = t;
                  }
              }
          }
        /* Highest power of T.  */
        nt = p[pOff++];
        /* Longitude. */
        cu = pl[plOff++];
        su = pl[plOff++];
        for (ip = 0; ip < nt; ip++)
          {
            cu = cu * T + pl[plOff++];
            su = su * T + pl[plOff++];
          }
        sl += cu * cv + su * sv;
        /* Latitiude. */
        cu = pb[pbOff++];
        su = pb[pbOff++];
        for (ip = 0; ip < nt; ip++)
          {
            cu = cu * T + pb[pbOff++];
            su = su * T + pb[pbOff++];
          }
        sb += cu * cv + su * sv;
        /* Radius. */
        cu = pr[prOff++];
        su = pr[prOff++];
        for (ip = 0; ip < nt; ip++)
          {
            cu = cu * T + pr[prOff++];
            su = su * T + pr[prOff++];
          }
        sr += cu * cv + su * sv;
      }
    pobj[0] = SwephData.STR * sl;
    pobj[1] = SwephData.STR * sb;
    pobj[2] = SwephData.STR * plan.distance * sr + plan.distance;
    return SweConst.OK;
  }

  /* Moshier ephemeris.
   * computes heliocentric cartesian equatorial coordinates of
   * equinox 2000
   * for earth and a planet
   * tjd          julian day
   * ipli         internal SWEPH planet number
   * xp           array of 6 doubles for planet's position and speed
   * xe                                  earth's
   * serr         error string
   */
  int swi_moshplan(double tjd, int ipli, boolean do_save, double[] xpret,
                   double[] xeret, StringBuffer serr) {
    int i;
    boolean do_earth = false;
    double dx[]=new double[3], x2[]=new double[3],
           xxe[]=new double[6], xxp[]=new double[6];
    double xp[], xe[];
    double dt;
    String s;
    int iplm = pnoint2msh[ipli];
    PlanData pdp = swissData.pldat[ipli];
    PlanData pedp = swissData.pldat[SwephData.SEI_EARTH];
    double seps2000 = swissData.oec2000.seps;
    double ceps2000 = swissData.oec2000.ceps;
    if (do_save) {
      xp = pdp.x;
      xe = pedp.x;
    } else {
      xp = xxp;
      xe = xxe;
    }
    if (do_save || ipli == SwephData.SEI_EARTH || xeret != null) {
      do_earth = true;
    }
    /* tjd beyond ephemeris limits, give some margin for spped at edge */
    if (tjd < SwephData.MOSHPLEPH_START - 0.3 ||
        tjd > SwephData.MOSHPLEPH_END + 0.3) {
      if (serr != null) {
        serr.setLength(0);
        s="jd "+tjd+" outside Moshier planet range "+
          SwephData.MOSHPLEPH_START+" .. "+
          SwephData.MOSHPLEPH_END+" ";
        if (serr.length() + s.length() < SwissData.AS_MAXCH) {
          serr.append(s);
        }
      }
      return(SweConst.ERR);
    }
    /* earth, for geocentric position */
    if (do_earth) {
      if (tjd == pedp.teval && pedp.iephe == SweConst.SEFLG_MOSEPH) {
        xe = pedp.x;
      } else {
        /* emb */
        swi_moshplan2(tjd, pnoint2msh[SwephData.SEI_EMB], xe); /* emb hel. ecl. 2000 polar */
        swissLib.swi_polcart(xe, xe);                        /* to cartesian */
        swissLib.swi_coortrf2(xe, xe, -seps2000, ceps2000);/* and equator 2000 */
        embofs_mosh(tjd, xe);               /* emb -> earth */
        if (do_save) {
          pedp.teval = tjd;
          pedp.xflgs = -1;
          pedp.iephe = SweConst.SEFLG_MOSEPH;
        }
        /* one more position for speed. */
        swi_moshplan2(tjd - SwephData.PLAN_SPEED_INTV, pnoint2msh[SwephData.SEI_EMB], x2);
        swissLib.swi_polcart(x2, x2);
        swissLib.swi_coortrf2(x2, x2, -seps2000, ceps2000);
        embofs_mosh(tjd - SwephData.PLAN_SPEED_INTV, x2);/**/
        for (i = 0; i <= 2; i++)
          dx[i] = (xe[i] - x2[i]) / SwephData.PLAN_SPEED_INTV;
        /* store speed */
        for (i = 0; i <= 2; i++) {
          xe[i+3] = dx[i];
        }
      }
      if (xeret != null) {
        for (i = 0; i <= 5; i++) {
          xeret[i] = xe[i];
        }
      }
    }
    /* earth is the planet wanted */
    if (ipli == SwephData.SEI_EARTH) {
      xp = xe;
    } else {
      /* other planet */
      /* if planet has already been computed, return */
      if (tjd == pdp.teval && pdp.iephe == SweConst.SEFLG_MOSEPH) {
        xp = pdp.x;
      } else {
        swi_moshplan2(tjd, iplm, xp);
        swissLib.swi_polcart(xp, xp);
        swissLib.swi_coortrf2(xp, xp, -seps2000, ceps2000);
        if (do_save) {
          pdp.teval = tjd;/**/
          pdp.xflgs = -1;
          pdp.iephe = SweConst.SEFLG_MOSEPH;
        }
        /* one more position for speed.
         * the following dt gives good speed for light-time correction
         */
        dt = SwephData.PLAN_SPEED_INTV;
        swi_moshplan2(tjd - dt, iplm, x2);
        swissLib.swi_polcart(x2, x2);
        swissLib.swi_coortrf2(x2, x2, -seps2000, ceps2000);
        for (i = 0; i <= 2; i++)
          dx[i] = (xp[i] - x2[i]) / dt;
        /* store speed */
        for (i = 0; i <= 2; i++) {
          xp[i+3] = dx[i];
        }
      }
      if (xpret != null) {
        for (i = 0; i <= 5; i++) {
          xpret[i] = xp[i];
        }
      }
    }
    return(SweConst.OK);
  }


  /* Prepare lookup table of sin and cos ( i*Lj )
   * for required multiple angles
   */
  private void sscc (int k, double arg, int n) {
    double cu, su, cv, sv, s;
    int i;

    su = SMath.sin (arg);
    cu = SMath.cos (arg);
    ss[k][0] = su;                /* sin(L) */
    cc[k][0] = cu;                /* cos(L) */
    sv = 2.0 * su * cu;
    cv = cu * cu - su * su;
    ss[k][1] = sv;                /* sin(2L) */
    cc[k][1] = cv;
    for (i = 2; i < n; i++)
      {
        s = su * cv + cu * sv;
        cv = cu * cv - su * sv;
        sv = s;
        ss[k][i] = sv;            /* sin( i+1 L ) */
        cc[k][i] = cv;
      }
  }


  /* Adjust position from Earth-Moon barycenter to Earth
   *
   * J = Julian day number
   * xemb = rectangular equatorial coordinates of Earth
   */
  private void embofs_mosh(double tjd, double xemb[]) {
    double T, M, a, L, B, p;
    double smp, cmp, s2mp, c2mp, s2d, c2d, sf, cf;
    double s2f, sx, cx, xyz[]=new double[6];
    double seps = swissData.oec.seps;
    double ceps = swissData.oec.ceps;
    int i;
    /* Short series for position of the Moon
     */
    T = (tjd-SwephData.J1900)/36525.0;
    /* Mean anomaly of moon (MP) */
    a = swissLib.swe_degnorm(((1.44e-5*T + 0.009192)*T + 477198.8491)*T + 296.104608);
    a *= SwissData.DEGTORAD;
    smp = SMath.sin(a);
    cmp = SMath.cos(a);
    s2mp = 2.0*smp*cmp;           /* sin(2MP) */
    c2mp = cmp*cmp - smp*smp;     /* cos(2MP) */
    /* Mean elongation of moon (D) */
    a = swissLib.swe_degnorm(((1.9e-6*T - 0.001436)*T + 445267.1142)*T + 350.737486);
    a  = 2.0 * SwissData.DEGTORAD * a;
    s2d = SMath.sin(a);
    c2d = SMath.cos(a);
    /* Mean distance of moon from its ascending node (F) */
    a = swissLib.swe_degnorm((( -3.e-7*T - 0.003211)*T + 483202.0251)*T + 11.250889);
    a  *= SwissData.DEGTORAD;
    sf = SMath.sin(a);
    cf = SMath.cos(a);
    s2f = 2.0*sf*cf;      /* sin(2F) */
    sx = s2d*cmp - c2d*smp;       /* sin(2D - MP) */
    cx = c2d*cmp + s2d*smp;       /* cos(2D - MP) */
    /* Mean longitude of moon (LP) */
    L = ((1.9e-6*T - 0.001133)*T + 481267.8831)*T + 270.434164;
    /* Mean anomaly of sun (M) */
    M = swissLib.swe_degnorm((( -3.3e-6*T - 1.50e-4)*T + 35999.0498)*T + 358.475833);
    /* Ecliptic longitude of the moon */
    L =   L
          + 6.288750*smp
          + 1.274018*sx
          + 0.658309*s2d
          + 0.213616*s2mp
          - 0.185596*SMath.sin( SwissData.DEGTORAD * M )
          - 0.114336*s2f;
    /* Ecliptic latitude of the moon */
    a = smp*cf;
    sx = cmp*sf;
    B =     5.128189*sf
          + 0.280606*(a+sx)               /* sin(MP+F) */
          + 0.277693*(a-sx)               /* sin(MP-F) */
          + 0.173238*(s2d*cf - c2d*sf);   /* sin(2D-F) */
    B *= SwissData.DEGTORAD;
    /* Parallax of the moon */
    p =    0.950724
          +0.051818*cmp
          +0.009531*cx
          +0.007843*c2d
          +0.002824*c2mp;
    p *= SwissData.DEGTORAD;
    /* Elongation of Moon from Sun
     */
    L = swissLib.swe_degnorm(L);
    L *= SwissData.DEGTORAD;
    /* Distance in au */
    a = 4.263523e-5/SMath.sin(p);
    /* Convert to rectangular ecliptic coordinates */
    xyz[0] = L;
    xyz[1] = B;
    xyz[2] = a;
    swissLib.swi_polcart(xyz, xyz);
    /* Convert to equatorial */
    swissLib.swi_coortrf2(xyz, xyz, -seps, ceps);
    /* Precess to equinox of J2000.0 */
    swissLib.swi_precess(xyz, tjd, SwephData.J_TO_J2000);/**/
    /* now emb -> earth */
    for (i = 0; i <= 2; i++)
      xemb[i] -= xyz[i] / (SwephData.EARTH_MOON_MRAT + 1.0);
  }

  /* orbital elements of planets that are computed from osculating elements
   *   epoch
   *   equinox
   *   mean anomaly,
   *   semi axis,
   *   eccentricity,
   *   argument of perihelion,
   *   ascending node
   *   inclination
   */
                                  /* use James Neely's revised elements
                                   *      of Uranian planets*/
  static final String plan_fict_nam[] =
    {"Cupido", "Hades", "Zeus", "Kronos",
     "Apollon", "Admetos", "Vulkanus", "Poseidon",
     "Isis-Transpluto", "Nibiru", "Harrington",
     "Leverrier", "Adams",
     "Lowell", "Pickering",};

  String swi_get_fict_name(int ipl, String snam) {
    if (snam==null) { snam=""; }
    StringBuffer sbnam=new StringBuffer(snam);
    if (read_elements_file(ipl, 0, null, null,
         null, null, null, null, null, null,
         sbnam, null, null) == SweConst.ERR) {
      return "name not found";
    }
    return sbnam.toString();
  }

  private static final double plan_oscu_elem[][]=new double[][] {
    {SwephData.J1900, SwephData.J1900, 163.7409, 40.99837, 0.00460, 171.4333, 129.8325, 1.0833},/* Cupido Neely */
    {SwephData.J1900, SwephData.J1900,  27.6496, 50.66744, 0.00245, 148.1796, 161.3339, 1.0500},/* Hades Neely */
    {SwephData.J1900, SwephData.J1900, 165.1232, 59.21436, 0.00120, 299.0440,   0.0000, 0.0000},/* Zeus Neely */
    {SwephData.J1900, SwephData.J1900, 169.0193, 64.81960, 0.00305, 208.8801,   0.0000, 0.0000},/* Kronos Neely */
    {SwephData.J1900, SwephData.J1900, 138.0533, 70.29949, 0.00000,   0.0000,   0.0000, 0.0000},/* Apollon Neely */
    {SwephData.J1900, SwephData.J1900, 351.3350, 73.62765, 0.00000,   0.0000,   0.0000, 0.0000},/* Admetos Neely */
    {SwephData.J1900, SwephData.J1900,  55.8983, 77.25568, 0.00000,   0.0000,   0.0000, 0.0000},/* Vulcanus Neely */
    {SwephData.J1900, SwephData.J1900, 165.5163, 83.66907, 0.00000,   0.0000,   0.0000, 0.0000},/* Poseidon Neely */
    /* Isis-Transpluto; elements from "Die Sterne" 3/1952, p. 70ff.
     * Strubell does not give an equinox. 1945 is taken to best reproduce
     * ASTRON ephemeris. (This is a strange choice, though.)
     * The epoch is 1772.76. The year is understood to have 366 days.
     * The fraction is counted from 1 Jan. 1772 */
    {2368547.66, 2431456.5, 0.0, 77.775, 0.3, 0.7, 0, 0},
    /* Nibiru, elements from Christian Woeltge, Hannover */
    {1856113.380954, 1856113.380954, 0.0, 234.8921, 0.981092, 103.966, -44.567, 158.708},
    /* Harrington, elements from Astronomical Journal 96(4), Oct. 1988 */
    {2374696.5, SwephData.J2000, 0.0, 101.2, 0.411, 208.5, 275.4, 32.4},
    /* Leverrier's Neptune,
          according to W.G. Hoyt, "Planets X and Pluto", Tucson 1980, p. 63 */
    {2395662.5, 2395662.5, 34.05, 36.15, 0.10761, 284.75, 0, 0},
    /* Adam's Neptune */
    {2395662.5, 2395662.5, 24.28, 37.25, 0.12062, 299.11, 0, 0},
    /* Lowell's Pluto */
    {2425977.5, 2425977.5, 281, 43.0, 0.202, 204.9, 0, 0},
    /* Pickering's Pluto */
    {2425977.5, 2425977.5, 48.95, 55.1, 0.31, 280.1, 100, 15}, /**/
  };

  /* computes a planet from osculating elements *
   * tjd          julian day
   * ipl          body number
   * ipli         body number in planetary data structure
   * iflag        flags
   */
  int swi_osc_el_plan(double tjd, double xp[], int ipl, int ipli,
                      double[] xearth, double[] xsun, StringBuffer serr) {
    double pqr[]=new double[9], x[]=new double[6];
    double eps, K, fac, rho, cose, sine;
    double alpha, beta, zeta, sigma, M2, Msgn, M_180_or_0;
    double tjd0[] = new double[1]; /* double used as output parameter */
    double tequ[] = new double[1]; /* double used as output parameter */
    double mano[] = new double[1]; /* double used as output parameter */
    double sema[] = new double[1]; /* double used as output parameter */
    double ecce[] = new double[1]; /* double used as output parameter */
    double parg[] = new double[1]; /* double used as output parameter */
    double node[] = new double[1]; /* double used as output parameter */
    double incl[] = new double[1]; /* double used as output parameter */
    double dmot;
    double cosnode, sinnode, cosincl, sinincl, cosparg, sinparg;
    double M, E;
    PlanData pedp = swissData.pldat[SwephData.SEI_EARTH];
    PlanData pdp = swissData.pldat[ipli];
    int[] fict_ifl = new int[] {0};	// Output parameter
    int i;
    /* orbital elements, either from file or, if file not found,
     * from above built-in set
     */
    if (read_elements_file(ipl, tjd, tjd0, tequ,
         mano, sema, ecce, parg, node, incl,
         null, fict_ifl, serr) == SweConst.ERR) {
      return SweConst.ERR;
    }
    dmot = 0.9856076686 * SwissData.DEGTORAD / sema[0] / SMath.sqrt(sema[0]);
                                                            /* daily motion */
    if ((fict_ifl[0] & FICT_GEO) != 0) {
      dmot /= SMath.sqrt(SwephData.SUN_EARTH_MRAT);
    }
    cosnode = SMath.cos(node[0]);
    sinnode = SMath.sin(node[0]);
    cosincl = SMath.cos(incl[0]);
    sinincl = SMath.sin(incl[0]);
    cosparg = SMath.cos(parg[0]);
    sinparg = SMath.sin(parg[0]);
    /* Gaussian vector */
    pqr[0] = cosparg * cosnode - sinparg * cosincl * sinnode;
    pqr[1] = -sinparg * cosnode - cosparg * cosincl * sinnode;
    pqr[2] = sinincl * sinnode;
    pqr[3] = cosparg * sinnode + sinparg * cosincl * cosnode;
    pqr[4] = -sinparg * sinnode + cosparg * cosincl * cosnode;
    pqr[5] = -sinincl * cosnode;
    pqr[6] = sinparg * sinincl;
    pqr[7] = cosparg * sinincl;
    pqr[8] = cosincl;
    /* Kepler problem */
    E = M = swissLib.swi_mod2PI(mano[0] + (tjd - tjd0[0]) * dmot); /* mean anomaly of date*/
    /* better E for very high eccentricity and small M */
    if (ecce[0] > 0.975) {
      M2 = M * SwissData.RADTODEG;
      if (M2 > 150 && M2 < 210) {
        M2 -= 180;
        M_180_or_0 = 180;
      } else
        M_180_or_0 = 0;
      if (M2 > 330) {
        M2 -= 360;
      }
      if (M2 < 0) {
        M2 = -M2;
        Msgn = -1;
      } else {
        Msgn = 1;
      }
      if (M2 < 30) {
        M2 *= SwissData.DEGTORAD;
        alpha = (1 - ecce[0]) / (4 * ecce[0] + 0.5);
        beta = M2 / (8 * ecce[0] + 1);
        zeta = SMath.pow(beta + SMath.sqrt(beta * beta + alpha * alpha), 1/3);
        sigma = zeta - alpha / 2;
        sigma = sigma - 0.078 * sigma * sigma * sigma * sigma * sigma / (1 + ecce[0])
  ;
        E = Msgn * (M2 + ecce[0] * (3 * sigma - 4 * sigma * sigma * sigma))
                          + M_180_or_0;
      }
    }
    E = swissLib.swi_kepler(E, M, ecce[0]);
    /* position and speed, referred to orbital plane */
    if ((fict_ifl[0] & FICT_GEO) != 0) {
      K = SwephData.KGAUSS_GEO / SMath.sqrt(sema[0]); 
    } else {
      K = SwephData.KGAUSS / SMath.sqrt(sema[0]);
    }
    cose = SMath.cos(E);
    sine = SMath.sin(E);
    fac = SMath.sqrt((1 - ecce[0]) * (1 + ecce[0]));
    rho = 1 - ecce[0] * cose;
    x[0] = sema[0] * (cose - ecce[0]);
    x[1] = sema[0] * fac * sine;
    x[3] = -K * sine / rho;
    x[4] = K * fac * cose / rho;
    /* transformation to ecliptic */
    xp[0] = pqr[0] * x[0] + pqr[1] * x[1];
    xp[1] = pqr[3] * x[0] + pqr[4] * x[1];
    xp[2] = pqr[6] * x[0] + pqr[7] * x[1];
    xp[3] = pqr[0] * x[3] + pqr[1] * x[4];
    xp[4] = pqr[3] * x[3] + pqr[4] * x[4];
    xp[5] = pqr[6] * x[3] + pqr[7] * x[4];
    /* transformation to equator */
    eps = swissLib.swi_epsiln(tequ[0]);
    swissLib.swi_coortrf(xp, xp, -eps);
    swissLib.swi_coortrf(xp, 3, xp, 3, -eps);
    /* precess to J2000 */
    if (tequ[0] != SwephData.J2000) {
      swissLib.swi_precess(xp, tequ[0], SwephData.J_TO_J2000);
      swissLib.swi_precess(xp, 3, tequ[0], SwephData.J_TO_J2000);
    }
    /* to solar system barycentre */
    if ((fict_ifl[0] & FICT_GEO) != 0) {
      for (i = 0; i <= 5; i++) {
        xp[i] += xearth[i];
      }
    } else {
      for (i = 0; i <= 5; i++) {    
        xp[i] += xsun[i];
      }
    }
    if (pdp.x == xp) {
      pdp.teval = tjd;   /* for precession! */
      pdp.iephe = pedp.iephe;
    }
    return SweConst.OK;
  }

  /* note: input parameter tjd is required for T terms in elements */
  private int read_elements_file(int ipl, double tjd,
                                 double tjd0[] /* double used as output parameter */,
                                 double tequ[] /* double used as output parameter */,
                                 double mano[] /* double used as output parameter */,
                                 double sema[] /* double used as output parameter */,
                                 double ecce[] /* double used as output parameter */,
                                 double parg[] /* double used as output parameter */,
                                 double node[] /* double used as output parameter */,
                                 double incl[] /* double used as output parameter */,
                                 StringBuffer pname,
                                 int fict_ifl[], /* integer used as output parameter */
                                 StringBuffer serr) {
    int i, iline, iplan, retc, ncpos;
    FilePtr fp = null;
    String s, sp;
    int spIdx=0;
    String cpos[]=new String[20], serri="";
    boolean elem_found = false;
    double tt = 0;
    /* -1, because file information is not saved, file is always closed */
    try {
      fp = swissEph.swi_fopen(-1, SweConst.SE_FICTFILE, swissData.ephepath, serr);
    } catch (SwissephException se) {
      /* file does not exist, use built-in bodies */
      if (ipl >= SweConst.SE_NFICT_ELEM) {
        if (serr != null) {
          serr.append("error no elements for fictitious body no ").append(ipl);
        }
        return SweConst.ERR;
      }
      if (tjd0 != null) {
        tjd0[0] = plan_oscu_elem[ipl][0];                   /* epoch */
      }
      if (tequ != null) {
        tequ[0] = plan_oscu_elem[ipl][1];                   /* equinox */
      }
      if (mano != null) {
        mano[0] = plan_oscu_elem[ipl][2] * SwissData.DEGTORAD; /* mean anomaly */
      }
      if (sema != null) {
        sema[0] = plan_oscu_elem[ipl][3];                   /* semi-axis */
      }
      if (ecce != null) {
        ecce[0] = plan_oscu_elem[ipl][4];                   /* eccentricity */
      }
      if (parg != null) {
        parg[0] = plan_oscu_elem[ipl][5] * SwissData.DEGTORAD; /* arg. of peri. */
      }
      if (node != null) {
        node[0] = plan_oscu_elem[ipl][6] * SwissData.DEGTORAD;  /* asc. node */
      }
      if (incl != null) {
        incl[0] = plan_oscu_elem[ipl][7] * SwissData.DEGTORAD; /* inclination*/
      }
      if (pname != null) {
        pname.setLength(0);
        pname.append(plan_fict_nam[ipl]);
      }
      return SweConst.OK;
    }
    /*
     * find elements in file
     */
    iline = 0;
    iplan = -1;
    try {
//    while (fgets(s, AS_MAXCH, fp) != null)
      while ((s=fp.readLine()) != null) {
        s=s.trim();
//        iline++;
//        spIdx = 0;
//        while(s.charAt(spIdx) == ' ' || s.charAt(spIdx) == '\t')
//          spIdx++;
//        s=s.substring(spIdx);
        sp = s;
        spIdx=0;
        char ch=s.charAt(spIdx);
        if (ch == '#' || ch=='\r' || ch=='\n' || ch=='\0') {
          continue;
        }
//    if ((sp = strchr(s, '#')) != NULL)
//      *sp = '\0';
        sp = null;
        if ((spIdx = s.indexOf('#')) >= 0) {
          s = s.substring(0,s.indexOf('#'));
          sp = "";
        }
        ncpos = swissLib.swi_cutstr(s, ",", cpos, 20);
        serri="error in file "+SweConst.SE_FICTFILE+", line "+
              iline+":";
        if (ncpos < 9) {
          if (serr != null) {
            serr.setLength(0);
            serr.append(serri).append(" nine elements required");
          }
          return SweConst.ERR;
        }
        iplan++;
        if (iplan != ipl) {
          continue;
        }
        elem_found = true;
        /* epoch of elements */
        if (tjd0 != null) {
          sp = cpos[0];
//          for (i = 0; i < 5; i++)
//       sp[i] = tolower(sp[i]);
          sp=sp.length()<=5?sp.toLowerCase():
                               sp.substring(0,5).toLowerCase()+sp.substring(5);
          if (sp.startsWith("j2000")) {
            tjd0[0] = SwephData.J2000;
          } else if (sp.startsWith("b1950")) {
            tjd0[0] = SwephData.B1950;
          } else if (sp.startsWith("j1900")) {
            tjd0[0] = SwephData.J1900;
          } else if (sp.charAt(0) == 'j' || sp.charAt(0) == 'b') {
            if (serr != null) {
              serr.setLength(0);
              serr.append(serri).append(" invalid epoch");
            }
//          goto return_err;
            fp.close(); return SweConst.ERR;
          } else
            tjd0[0] = SwissLib.atof(sp);
          tt = tjd - tjd0[0];
        }
        /* equinox */
        if (tequ != null) {
          sp = cpos[1];
          spIdx=0;
          while(sp.charAt(spIdx) == ' ' || sp.charAt(spIdx) == '\t')
            spIdx++;
//          for (i = 0; i < 5; i++)
//       sp[i] = tolower(sp[i]);
          sp=sp.substring(spIdx);
          sp=sp.length()<5?sp.toLowerCase():
                               sp.substring(0,5).toLowerCase()+sp.substring(5);
          if (sp.startsWith("j2000")) {
            tequ[0] = SwephData.J2000;
          } else if (sp.startsWith("b1950")) {
            tequ[0] = SwephData.B1950;
          } else if (sp.startsWith("j1900")) {
            tequ[0] = SwephData.J1900;
          } else if (sp.startsWith("jdate")) {
            tequ[0] = tjd;
          } else if (sp.charAt(0) == 'j' || sp.charAt(0) == 'b') {
            if (serr != null) {
              serr.setLength(0);
              serr.append(serri).append(" invalid equinox");
            }
//          goto return_err;
            fp.close(); return SweConst.ERR;
          } else {
//        *tequ = atof(sp);
            tequ[0] = SwissLib.atof(sp);
          }
        }
        /* mean anomaly t0 */
        if (mano != null) {
          retc = check_t_terms(tt, cpos[2], mano);
          mano[0] = swissLib.swe_degnorm(mano[0]);
          if (retc == SweConst.ERR) {
            if (serr != null) {
              serr.append(serri).append(" mean anomaly value invalid");
            }
//          goto return_err;
            fp.close(); return SweConst.ERR;
          }
          /* if mean anomaly has t terms (which happens with fictitious
           * planet Vulcan), we set
           * epoch = tjd, so that no motion will be added anymore
           * equinox = tjd */
          if (retc == 1) {
            tjd0[0] = tjd;
          }
          mano[0] *= SwissData.DEGTORAD;
        }
        /* semi-axis */
        if (sema != null) {
          retc = check_t_terms(tt, cpos[3], sema);
          if (sema[0] <= 0 || retc == SweConst.ERR) {
            if (serr != null) {
              serr.append(serri).append(" semi-axis value invalid");
            }
//          goto return_err;
            fp.close(); return SweConst.ERR;
          }
        }
        /* eccentricity */
        if (ecce != null) {
          retc = check_t_terms(tt, cpos[4], ecce);
          if (ecce[0] >= 1 || ecce[0] < 0 || retc == SweConst.ERR) {
            if (serr != null) {
              serr.setLength(0);
              serr.append(serri).append(" eccentricity invalid (no parabolic or hyperbolic or bits allowed)");
            }
//          goto return_err;
            fp.close(); return SweConst.ERR;
          }
        }
        /* perihelion argument */
        if (parg != null) {
          retc = check_t_terms(tt, cpos[5], parg);
          parg[0] = swissLib.swe_degnorm(parg[0]);
          if (retc == SweConst.ERR) {
            if (serr != null) {
              serr.setLength(0);
              serr.append(serri).append(" perihelion argument value invalid");
            }
//          goto return_err;
            fp.close(); return SweConst.ERR;
          }
          parg[0] *= SwissData.DEGTORAD;
        }
        /* node */
        if (node != null) {
          retc = check_t_terms(tt, cpos[6], node);
          node[0] = swissLib.swe_degnorm(node[0]);
          if (retc == SweConst.ERR) {
            if (serr != null) {
              serr.setLength(0);
              serr.append(serri).append(" node value invalid");
            }
//          goto return_err;
            fp.close(); return SweConst.ERR;
          }
          node[0] *= SwissData.DEGTORAD;
        }
        /* inclination */
        if (incl != null) {
          retc = check_t_terms(tt, cpos[7], incl);
          incl[0] = swissLib.swe_degnorm(incl[0]);
          if (retc == SweConst.ERR) {
            if (serr != null) {
              serr.setLength(0);
              serr.append(serri).append(" inclination value invalid");
            }
//          goto return_err;
            fp.close(); return SweConst.ERR;
          }
          incl[0] *= SwissData.DEGTORAD;
        }
        /* planet name */
        if (pname != null) {
          sp = cpos[8];
          spIdx=0;
          while(sp.charAt(spIdx) == ' ' || sp.charAt(spIdx) == '\t')
            spIdx++;
          sp=sp.substring(spIdx);
//      swi_right_trim(sp);
          sp=sp.trim();
          pname.setLength(0); pname.append(sp);
        }
        /* geocentric */
        if (fict_ifl != null && ncpos > 9) {
//          for (sp = cpos[9]; *sp != '\0'; sp++)
//            *sp = tolower(*sp);
          sp = sp.substring(0,SMath.min(sp.length(),spIdx+9)) +
               sp.substring(SMath.min(sp.length(),spIdx+9)).toLowerCase();
//          if (strstr(cpos[9], "geo") != NULL)
//            fict_ifl[0] |= FICT_GEO;
          if (cpos[9].indexOf("geo") >= 0) {
            fict_ifl[0] |= FICT_GEO;
          }
        }
        break;
      }
      if (!elem_found) {
        if (serr != null) {
          serr.append(serri).append(" elements for planet ").append(ipl).append(" not found");
        }
//      goto return_err;
        fp.close(); return SweConst.ERR;
      }
      fp.close();
      return SweConst.OK;
    } catch (java.io.IOException e) {
      if (fp!=null) { try { fp.close(); } catch (java.io.IOException ie) { } }
    } catch (java.nio.BufferUnderflowException e) {
      if (fp!=null) { try { fp.close(); } catch (java.io.IOException ie) { } }
    }
    return SweConst.ERR;
  }

  private int check_t_terms(double t, String sinp, double doutp[] /* double used as output parameter */) {
    int i, isgn = 1, z;
    int retc = 0;
    int spidx;
    double tt[]=new double[5], fac;
    tt[0] = t / 36525;
    tt[1] = tt[0];
    tt[2] = tt[1] * tt[1];
    tt[3] = tt[2] * tt[1];
    tt[4] = tt[3] * tt[1];
//    if (strpbrk(sinp, "+-") != null)
    if (sinp.indexOf('+') + sinp.indexOf('-') > -2) {
      retc = 1; /* with additional terms */
    }
    spidx=0;
    doutp[0] = 0;
    fac = 1;
    z = 0;
    while (true) {
      while(spidx<sinp.length() &&
            (sinp.charAt(spidx)==' ' || sinp.charAt(spidx)=='\t')) {
        spidx++;
      }
      if (spidx==sinp.length() ||
          sinp.charAt(spidx)=='+' || sinp.charAt(spidx)=='-') {
        if (z > 0) {
          doutp[0] += fac;
        }
        isgn = 1;
        if (spidx!=sinp.length() && sinp.charAt(spidx) == '-') {
          isgn = -1;
        }
        fac = 1 * isgn;
        if (spidx==sinp.length()) {
          return retc;
        }
        spidx++;
      } else {
        while(spidx<sinp.length() &&
              (sinp.charAt(spidx)=='*' || sinp.charAt(spidx)==' '
              || sinp.charAt(spidx)=='\t')) {
          spidx++;
        }
        if (spidx<sinp.length() &&
            (sinp.charAt(spidx)=='t' || sinp.charAt(spidx)=='T')) {
                /* a T */
          spidx++;
          if (spidx<sinp.length() &&
              (sinp.charAt(spidx)=='+' || sinp.charAt(spidx)=='-')) {
            fac *= tt[0];
          } else if ((i = SwissLib.atoi(sinp.substring(SMath.min(sinp.length(),spidx)))) <= 4 && i >= 0) {
            fac *= tt[i];
          }
        } else {
          /* a number */
          double db=SwissLib.atof(sinp.substring(spidx));
          if (db!=0 || sinp.charAt(spidx)=='0') {
            fac *= db;
          }
        }
        while (spidx<sinp.length() &&
               (Character.isDigit(sinp.charAt(spidx)) ||
                sinp.charAt(spidx)=='.'))
          spidx++;
      }
      z++;
    }
  }

  private Plantbl planets[] = {
    SwemptabMer.mer404,
    SwemptabVen.ven404,
    SwemptabEar.ear404,
    SwemptabMar.mar404,
    SwemptabJup.jup404,
    SwemptabSat.sat404,
    SwemptabUra.ura404,
    SwemptabNep.nep404,
    SwemptabPlu.plu404,
  };
}
