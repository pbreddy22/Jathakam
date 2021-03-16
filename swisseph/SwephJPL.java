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
/*
 | $Header: /home/dieter/sweph/RCS/swejpl.c,v 1.76 2008/08/26 13:55:36 dieter Exp $
 |
 | Subroutines for reading JPL ephemerides.
 | derived from testeph.f as contained in DE403 distribution July 1995.
 | works with DE200, DE102, DE403, DE404, DE405, DE406.
 | (attention, DE102 has 1950 reference frame and also DE4* has slightly
 | different reference frame from DE200. With DE4*, use routine
 | IERS_FK5().)

  Authors: Dieter Koch and Alois Treindl, Astrodienst Zürich

************************************************************/
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


class SwephJPL implements java.io.Serializable {
  static final int J_MERCURY =  0;
  static final int J_VENUS   =  1;
  static final int J_EARTH   =  2;
  static final int J_MARS    =  3;
  static final int J_JUPITER =  4;
  static final int J_SATURN  =  5;
  static final int J_URANUS  =  6;
  static final int J_NEPTUNE =  7;
  static final int J_PLUTO   =  8;
  static final int J_MOON    =  9;
  static final int J_SUN     = 10;
  static final int J_SBARY   = 11;
  static final int J_EMB     = 12;
  static final int J_NUT     = 13;
  static final int J_LIB     = 14;

  JplSave jplSave=new JplSave();

  AbstractSwissEph swissEph=null;
  SwissData swissData=null;
  SwissLib  swissLib=null;


  SwephJPL(AbstractSwissEph sw, SwissData swed, SwissLib sl) 
  {
    this.swissEph=sw;
    this.swissData=swed;
    this.swissLib=sl;
    if (this.swissEph==null) { this.swissEph=new SwissEph(); }
    if (this.swissData==null) { this.swissData=new SwissData(); }
    if (this.swissLib==null) { this.swissLib=new SwissLib(); }
  }


  /*
   * This subroutine opens the file jplfname, with a phony record length,
   * reads the first record, and uses the info to compute ksize,
   * the number of single precision words in a record.
   * RETURN: ksize (record size of ephemeris data)
   * jplfptr is opened on return.
   * note 26-aug-2008: now record size is computed by fsizer(), not
   * set to a fixed value depending as in previous releases. The caller of
   * fsizer() will verify by data comparison whether it computed correctly.
   */
  private int fsizer(StringBuffer serr) throws SwissephException {
    /* Local variables */
    int ncon;
    double emrat;
    int numde;
    double au, ss[]=new double[3];
    int i, kmx, khi, nd;
    int ksize, lpt[] = new int[3];
    String ttl="";  // JAVA: Not used???
    try {
      // throws SwissephException, if null or maybe for other reasons:
      jplSave.jplfptr = swissEph.swi_fopen(SwephData.SEI_FILE_PLANET, jplSave.jplfname, jplSave.jplfpath, serr);
      /* ttl = ephemeris title, e.g.
       * "JPL Planetary Ephemeris DE404/LE404
       *  Start Epoch: JED=   625296.5-3001 DEC 21 00:00:00
       *  Final Epoch: JED=  2817168.5 3001 JAN 17 00:00:00c */
//    fread((void *) &ttl[0], 1, 252, js->jplfptr);
      for(int m=0; m<252; m++) {
        ttl+=(char)jplSave.jplfptr.readByte();
      }
    /* cnam = names of constants */
//    fread((void *) js->ch_cnam, 1, 6*400, js->jplfptr);
      for(int m=0; m<6*400; m++) {
        ttl+=(char)jplSave.jplfptr.readByte();
      }
      /* ss[0] = start epoch of ephemeris
       * ss[1] = end epoch
       * ss[2] = segment size in days */
//    fread((void *) &ss[0], sizeof(double), 3, js->jplfptr);
      for(int m=0; m<3; m++) {
        ss[m]=jplSave.jplfptr.readDouble();
      }
      /* reorder ? */
      if (ss[2] < 1 || ss[2] > 200) {
        jplSave.jplfptr.setBigendian(false);
        jplSave.jplfptr.seek(jplSave.jplfptr.getFilePointer()-3*8);
        for(int m=0; m<3; m++) {
          ss[m]=jplSave.jplfptr.readDouble();
        }
      } else {
        jplSave.jplfptr.setBigendian(true);
      }
      for (i = 0; i < 3; i++)
        jplSave.eh_ss[i] = ss[i];
//    if (js.do_reorder)
//      reorder((char *) &js->eh_ss[0], sizeof(double), 3);
  /* plausibility test of these constants. Start and end date must be
   * between -20000 and +20000, segment size >= 1 and <= 200 */
      if (jplSave.eh_ss[0] < -5583942 || jplSave.eh_ss[1] > 9025909 || jplSave.eh_ss[2] < 1 || jplSave.eh_ss[2] > 200) {
        throw new SwissephException(1./0., SwissephException.OUT_OF_TIME_RANGE,
            SwephData.NOT_AVAILABLE,
            "alleged ephemeris file (" + jplSave.jplfname + ") has invalid format.");
      }
      /* ncon = number of constants */
//    fread((void *) &ncon, sizeof(long), 1, js->jplfptr);
      ncon=jplSave.jplfptr.readInt();
//    if (js->do_reorder)
//      reorder((char *) &ncon, sizeof(long), 1);
      /* au = astronomical unit */
//    fread((void *) &au, sizeof(double), 1, js->jplfptr);
      au=jplSave.jplfptr.readDouble();
//    if (js->do_reorder)
//      reorder((char *) &au, sizeof(double), 1);
      /* emrat = earth moon mass ratio */
//    fread((void *) &emrat, sizeof(double), 1, js->jplfptr);
      emrat=jplSave.jplfptr.readDouble();
//    if (js->do_reorder)
//      reorder((char *) &emrat, sizeof(double), 1);
      /* ipt[i+0]: coefficients of planet i start at buf[ipt[i+0]-1]
       * ipt[i+1]: number of coefficients (interpolation order - 1)
       * ipt[i+2]: number of intervals in segment */
//    fread((void *) &js->eh_ipt[0], sizeof(long), 36, js->jplfptr);
      for(int m=0; m<36; m++) {
        jplSave.eh_ipt[m]=jplSave.jplfptr.readInt();
      }
//    if (js->do_reorder)
//      reorder((char *) &js->eh_ipt[0], sizeof(long), 36);
      /* numde = number of jpl ephemeris "404" with de404 */
//    fread((void *) &numde, sizeof(long), 1, js->jplfptr);
        numde=jplSave.jplfptr.readInt();
//    if (js->do_reorder)
//      reorder((char *) &numde, sizeof(long), 1);
      /* read librations */
      lpt[0] = jplSave.jplfptr.readInt();
      lpt[1] = jplSave.jplfptr.readInt();
      lpt[2] = jplSave.jplfptr.readInt();
//      if (js->do_reorder)
//        reorder((char *) &lpt[0], sizeof(long), 3);
      /* fill librations into eh_ipt[36]..[38] */
      for (i = 0; i < 3; ++i)
        jplSave.eh_ipt[i + 36] = lpt[i];
      jplSave.jplfptr.seek(0);
      /*  find the number of ephemeris coefficients from the pointers */
      /* re-activated this code on 26-aug-2008 */
      kmx = 0;
      khi = 0;
      for (i = 0; i < 13; i++) {
        if (jplSave.eh_ipt[i * 3] > kmx) {
          kmx = jplSave.eh_ipt[i * 3];
          khi = i + 1;
        }
      }
      if (khi == 12) {
        nd = 2;
      } else {
        nd = 3;
      }
      ksize = (int)((jplSave.eh_ipt[khi * 3 - 3] + nd * jplSave.eh_ipt[khi * 3 - 2] * jplSave.eh_ipt[khi * 3 - 1] - 1L) * 2L);
      /*
       * de102 files give wrong ksize, because they contain 424 empty bytes
       * per record. Fixed by hand!
       */
      if (ksize == 1546) {
        ksize = 1652;
      }
    } catch (java.io.IOException ioe) {
      throw new SwissephException(1./0., SwissephException.FILE_READ_ERROR,
          SweConst.ERR, ioe.getMessage());
    } catch (java.nio.BufferUnderflowException ioe) {
      throw new SwissephException(1./0., SwissephException.FILE_READ_ERROR,
          SweConst.ERR, ioe.getMessage());
    }
    if (ksize < 1000 || ksize > 5000) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("JPL ephemeris file does not provide valid ksize (").append(ksize).append(")");/**/
      }
      throw new SwissephException(1./0., SwissephException.DAMAGED_FILE_ERROR,
          SwephData.NOT_AVAILABLE, serr);
    }
    return ksize;
  }

  /*
   *     This subroutine reads the jpl planetary ephemeris
   *     and gives the position and velocity of the point 'ntarg'
   *     with respect to 'ncent'.
   *     calling sequence parameters:
   *       et = d.p. julian ephemeris date at which interpolation
   *            is wanted.
   *       ** note the entry dpleph for a doubly-dimensioned time **
   *          the reason for this option is discussed in the
   *          subroutine state
   *     ntarg = integer number of 'target' point.
   *     ncent = integer number of center point.
   *            the numbering convention for 'ntarg' and 'ncent' is:
   *                0 = mercury           7 = neptune
   *                1 = venus             8 = pluto
   *                2 = earth             9 = moon
   *                3 = mars             10 = sun
   *                4 = jupiter          11 = solar-system barycenter
   *                5 = saturn           12 = earth-moon barycenter
   *                6 = uranus           13 = nutations (longitude and obliq)
   *                                     14 = librations, if on eph file
   *             (if nutations are wanted, set ntarg = 13. for librations,
   *              set ntarg = 14. set ncent=0.)
   *      rrd = output 6-word d.p. array containing position and velocity
   *            of point 'ntarg' relative to 'ncent'. the units are au and
   *            au/day. for librations the units are radians and radians
   *            per day. in the case of nutations the first four words of
   *            rrd will be set to nutations and rates, having units of
   *            radians and radians/day.
   *            The option is available to have the units in km and km/sec.
   *            For this, set do_km=TRUE (default FALSE).
   */
  int swi_pleph(double et, int ntarg, int ncent, double[] rrd,
                StringBuffer serr) throws SwissephException {
    int i, retc;
    int list[]=new int[12];
    double[] pv = jplSave.pv;
    double[] pvsun = jplSave.pvsun;
    for (i = 0; i < 6; ++i)
      rrd[i] = 0.0;
    if (ntarg == ncent) {
      return 0;
    }
    for (i = 0; i < 12; ++i)
      list[i] = 0;
    /*     check for nutation call */
    if (ntarg == J_NUT) {
      if (jplSave.eh_ipt[34] > 0) {
        list[10] = 2;
        return(state(et, list, false, pv, pvsun, rrd, serr));
      } else {
        if (serr != null) {
          serr.setLength(0);
          serr.append("No nutations on the JPL ephemeris file;");
        }
        throw new SwissephException(et, SwissephException.UNDEFINED_ERROR,
            SwephData.NOT_AVAILABLE, serr);
      }
    }
    if (ntarg == J_LIB) {
      if (jplSave.eh_ipt[37] > 0) {
        list[11] = 2;
        // throws SwissephException by itself:
        retc = state(et, list, false, pv, pvsun, rrd, serr);
        for (i = 0; i < 6; ++i) {
          rrd[i] = pv[i + 60];
        }
        return 0;
      } else {
        if (serr != null) {
          serr.setLength(0);
          serr.append("No librations on the ephemeris file;");
        }
        throw new SwissephException(et, SwissephException.DAMAGED_FILE_ERROR,
            SwephData.NOT_AVAILABLE, serr);
      }
    }
    /* set up proper entries in 'list' array for state call */
    if (ntarg < J_SUN) {
      list[ntarg] = 2;
    }
    if (ntarg == J_MOON)  /* Mooon needs Earth */ {
      list[J_EARTH] = 2;
    }
    if (ntarg == J_EARTH)         /* Earth needs Moon */ {
      list[J_MOON] = 2;
    }
    if (ntarg == J_EMB)   /* EMB needs Earth */ {
      list[J_EARTH] = 2;
    }
    if (ncent < J_SUN) {
      list[ncent] = 2;
    }
    if (ncent == J_MOON)  /* Mooon needs Earth */ {
      list[J_EARTH] = 2;
    }
    if (ncent == J_EARTH)         /* Earth needs Moon */ {
      list[J_MOON] = 2;
    }
    if (ncent == J_EMB)   /* EMB needs Earth */ {
      list[J_EARTH] = 2;
    }
    // throws SwissephException by itself:
    retc = state(et, list, true, pv, pvsun, rrd, serr);
    if (ntarg == J_SUN || ncent == J_SUN) {
      for (i = 0; i < 6; ++i)
        pv[i + 6*J_SUN] = pvsun[i];
    }
    if (ntarg == J_SBARY || ncent == J_SBARY) {
      for (i = 0; i < 6; ++i) {
        pv[i + 6*J_SBARY] = 0.;
      }
    }
    if (ntarg == J_EMB || ncent == J_EMB) {
      for (i = 0; i < 6; ++i)
        pv[i + 6*J_EMB] = pv[i + 6*J_EARTH];
    }
    if ((ntarg==J_EARTH && ncent==J_MOON) || (ntarg == J_MOON && ncent==J_EARTH)){
      for (i = 0; i < 6; ++i)
        pv[i + 6*J_EARTH] = 0.;

    } else {
      if (list[J_EARTH] == 2) {
        for (i = 0; i < 6; ++i)
          pv[i + 6*J_EARTH] -= pv[i + 6*J_MOON] / (jplSave.eh_emrat + 1.);
      }
      if (list[J_MOON] == 2) {
        for (i = 0; i < 6; ++i) {
          pv[i + 6*J_MOON] += pv[i + 6*J_EARTH];
        }
      }
    }
    for (i = 0; i < 6; ++i)
      rrd[i] = pv[i + ntarg * 6] - pv[i + ncent * 6];
    return SweConst.OK;
  }

  /*
   *  This subroutine differentiates and interpolates a
   *  set of chebyshev coefficients to give pos, vel, acc, and jerk
   *  calling sequence parameters:
   *    input:
   *     buf   1st location of array of d.p. chebyshev coefficients of position
   *        t   is dp fractional time in interval covered by
   *            coefficients at which interpolation is wanted, 0 <= t <= 1
   *     intv   is dp length of whole interval in input time units.
   *      ncf   number of coefficients per component
   *      ncm   number of components per set of coefficients
   *       na   number of sets of coefficients in full array
   *            (i.e., number of sub-intervals in full interval)
   *       ifl   int flag: =1 for positions only
   *                      =2 for pos and vel
   *                      =3 for pos, vel, and acc
   *                      =4 for pos, vel, acc, and jerk
   *    output:
   *      pv   d.p. interpolated quantities requested.
   *           assumed dimension is pv(ncm,fl).
   */
  /* Initialized data */
  int np_interp, nv_interp;
  int nac_interp;
  int njk_interp;
  double twot = 0.;
  private int interp(double[] buf, int bufOffs, double t, double intv,
                     int ncfin, int ncmin, int nain, int ifl, double[] pv,
                     int pvOffs) {
    /* Initialized data */
    double[] pc = jplSave.pc;
    double[] vc = jplSave.vc;
    double[] ac = jplSave.ac;
    double[] jc = jplSave.jc;
    int ncf = (int) ncfin;
    int ncm = (int) ncmin;
    int na = (int) nain;
    /* Local variables */
    double temp;
    int i, j, ni;
    double tc;
    double dt1, bma;
    double bma2, bma3;
    /*
     | get correct sub-interval number for this set of coefficients and then
     | get normalized chebyshev time within that subinterval.
     */
    if (t >= 0) {
      dt1 = SMath.floor(t);
    } else {
      dt1 = -SMath.floor(-t);
    }
    temp = na * t;
    ni = (int) (temp - dt1);
    /* tc is the normalized chebyshev time (-1 <= tc <= 1) */
    tc = ((temp % 1.0) + dt1) * 2. - 1.;
    /*
     *  check to see whether chebyshev time has changed,
     *  and compute new polynomial values if it has.
     *  (the element pc(2) is the value of t1(tc) and hence
     *  contains the value of tc on the previous call.)
     */
    if (tc != pc[1]) {
      np_interp = 2;
      nv_interp = 3;
      nac_interp = 4;
      njk_interp = 5;
      pc[1] = tc;
      twot = tc + tc;
    }
    /*
     *  be sure that at least 'ncf' polynomials have been evaluated
     *  and are stored in the array 'pc'.
     */
    if (np_interp < ncf) {
      for (i = np_interp; i < ncf; ++i)
        pc[i] = twot * pc[i - 1] - pc[i - 2];
      np_interp = ncf;
    }
    /*  interpolate to get position for each component */
    for (i = 0; i < ncm; ++i) {
      pv[pvOffs + i] = 0.;
      for (j = ncf-1; j >= 0; --j)
        pv[pvOffs + i] += pc[j] * buf[bufOffs + j + (i + ni * ncm) * ncf];
    }
    if (ifl <= 1) {
      return 0;
    }
    /*
     *       if velocity interpolation is wanted, be sure enough
     *       derivative polynomials have been generated and stored.
     */
    bma = (na + na) / intv;
    vc[2] = twot + twot;
    if (nv_interp < ncf) {
      for (i = nv_interp; i < ncf; ++i)
        vc[i] = twot * vc[i - 1] + pc[i - 1] + pc[i - 1] - vc[i - 2];
      nv_interp = ncf;
    }
    /*       interpolate to get velocity for each component */
    for (i = 0; i < ncm; ++i) {
      pv[pvOffs + i + ncm] = 0.;
      for (j = ncf-1; j >= 1; --j)
        pv[pvOffs + i + ncm] += vc[j] * buf[bufOffs + j + (i + ni * ncm) * ncf];
      pv[pvOffs + i + ncm] *= bma;
    }
    if (ifl == 2) {
      return 0;
    }
    /*       check acceleration polynomial values, and */
    /*       re-do if necessary */
    bma2 = bma * bma;
    ac[3] = pc[1] * 24.;
    if (nac_interp < ncf) {
      nac_interp = ncf;
      for (i = nac_interp; i < ncf; ++i)
        ac[i] = twot * ac[i - 1] + vc[i - 1] * 4. - ac[i - 2];
    }
    /*       get acceleration for each component */
    for (i = 0; i < ncm; ++i) {
      pv[pvOffs + i + ncm * 2] = 0.;
      for (j = ncf-1; j >= 2; --j)
        pv[pvOffs + i + ncm * 2] += ac[j] * buf[bufOffs + j + (i + ni * ncm) * ncf];
      pv[pvOffs + i + ncm * 2] *= bma2;
    }
    if (ifl == 3) {
      return 0;
    }
    /*       check jerk polynomial values, and */
    /*       re-do if necessary */
    bma3 = bma * bma2;
    jc[4] = pc[1] * 192.;
    if (njk_interp < ncf) {
      njk_interp = ncf;
      for (i = njk_interp; i < ncf; ++i)
        jc[i] = twot * jc[i - 1] + ac[i - 1] * 6. - jc[i - 2];
    }
    /*       get jerk for each component */
    for (i = 0; i < ncm; ++i) {
      pv[pvOffs + i + ncm * 3] = 0.;
      for (j = ncf-1; j >= 3; --j)
        pv[pvOffs + i + ncm * 3] += jc[j] * buf[bufOffs + j + (i + ni * ncm) * ncf];
      pv[pvOffs + i + ncm * 3] *= bma3;
    }
    return 0;
  }

  /*
   | ********** state ********************
   | this subroutine reads and interpolates the jpl planetary ephemeris file
   |  calling sequence parameters:
   |  input:
   |     et    dp julian ephemeris epoch at which interpolation is wanted.
   |     list  12-word integer array specifying what interpolation
   |           is wanted for each of the bodies on the file.
   |                      list(i)=0, no interpolation for body i
   |                             =1, position only
   |                             =2, position and velocity
   |            the designation of the astronomical bodies by i is:
   |                      i = 0: mercury
   |                        = 1: venus
   |                        = 2: earth-moon barycenter, NOT earth!
   |                        = 3: mars
   |                        = 4: jupiter
   |                        = 5: saturn
   |                        = 6: uranus
   |                        = 7: neptune
   |                        = 8: pluto
   |                        = 9: geocentric moon
   |                        =10: nutations in longitude and obliquity
   |                        =11: lunar librations (if on file)
   |            If called with list = NULL, only the header records are read and
   |            stored in the global areas.
   |  do_bary   short, if true, barycentric, if false, heliocentric.
   |              only the 9 planets 0..8 are affected by it.
   |  output:
   |       pv   dp 6 x 11 array that will contain requested interpolated
   |            quantities.  the body specified by list(i) will have its
   |            state in the array starting at pv(1,i).  (on any given
   |            call, only those words in 'pv' which are affected by the
   |            first 10 'list' entries (and by list(11) if librations are
   |            on the file) are set.  the rest of the 'pv' array
   |            is untouched.)  the order of components starting in
   |            pv is: x,y,z,dx,dy,dz.
   |            all output vectors are referenced to the earth mean
   |            equator and equinox of epoch. the moon state is always
   |            geocentric; the other nine states are either heliocentric
   |            or solar-system barycentric, depending on the setting of
   |            common flags (see below).
   |            lunar librations, if on file, are put into pv(k,10) if
   |            list(11) is 1 or 2.
   |    pvsun   dp 6-word array containing the barycentric position and
   |            velocity of the sun.
   |      nut   dp 4-word array that will contain nutations and rates,
   |            depending on the setting of list(10).  the order of
   |            quantities in nut is:
   |                     d psi  (nutation in longitude)
   |                     d epsilon (nutation in obliquity)
   |                     d psi dot
   |                     d epsilon dot
   |  globals used:
   |    do_km   logical flag defining physical units of the output states.
   |            TRUE = return km and km/sec, FALSE = return au and au/day
   |            default value = FALSE  (km determines time unit
   |            for nutations and librations.  angle unit is always radians.)
   */
  int irecsz_state;
  int nrl_state, lpt_state[]=new int[3], ncoeffs_state;
  private int state(double et, int[] list, boolean do_bary, double[] pv,
                    double[] pvsun, double[] nut, StringBuffer serr)
      throws SwissephException {
    int i, j, k;
    long flen;
    int nseg, nb;
    double[] buf = jplSave.buf;
    double aufac=0., s, t=0., intv=0., ts[] = new double[4];
    int nrecl, ksize;
    int nr;
    double et_mn, et_fr;
    int[] ipt = jplSave.eh_ipt;
    String ch_ttl="";
    boolean ferr=false;
    try {
      if (jplSave.jplfptr == null ||
          (jplSave.jplfptr.fp == null && jplSave.jplfptr.sk == null)) {
        // fsizer() throws SwissephException
        ksize = fsizer(serr); /* the number of single precision words in a record */
        nrecl = 4;
// (ksize == NOT_AVAILABLE) has thrown SwissephException already...
//      if (ksize == NOT_AVAILABLE)
//          return NOT_AVAILABLE;
        irecsz_state = nrecl * ksize;     /* record size in bytes */
        ncoeffs_state = ksize / 2;        /* # of coefficients, doubles */
        /* ttl = ephemeris title, e.g.
         * "JPL Planetary Ephemeris DE404/LE404
         *  Start Epoch: JED=   625296.5-3001 DEC 21 00:00:00
         *  Final Epoch: JED=  2817168.5 3001 JAN 17 00:00:00c */
//      fread((void *) ch_ttl, 1, 252, js->jplfptr);
        for(int m=0;m<252;m++) {
          ch_ttl+=(char)jplSave.jplfptr.readByte();
        }
        /* cnam = names of constants */
//      fread((void *) js.ch_cnam, 1, 2400, js.jplfptr);
        for(int m=0;m<2400;m++) {
          jplSave.ch_cnam+=(char)jplSave.jplfptr.readByte();
        }
        /* ss[0] = start epoch of ephemeris
         * ss[1] = end epoch
         * ss[2] = segment size in days */
//      fread((void *) &js.eh_ss[0], sizeof(double), 3, js.jplfptr);
        for(int m=0;m<3;m++) {
          jplSave.eh_ss[m]=jplSave.jplfptr.readDouble();
        }
//      if (js.do_reorder)
//        reorder((char *) &js.eh_ss[0], sizeof(double), 3);
        /* ncon = number of constants */
//      fread((void *) &js.eh_ncon, sizeof(long), 1, js.jplfptr);
        jplSave.eh_ncon=jplSave.jplfptr.readInt();
//      if (js.do_reorder)
//        reorder((char *) &js.eh_ncon, sizeof(long), 1);
        /* au = astronomical unit */
//      fread((void *) &js.eh_au, sizeof(double), 1, js.jplfptr);
        jplSave.eh_au=jplSave.jplfptr.readDouble();
//      if (js.do_reorder)
//        reorder((char *) &js.eh_au, sizeof(double), 1);
        /* emrat = earth moon mass ratio */
//      fread((void *) &js.eh_emrat, sizeof(double), 1, js.jplfptr);
        jplSave.eh_emrat=jplSave.jplfptr.readDouble();
//      if (js.do_reorder)
//        reorder((char *) &js.eh_emrat, sizeof(double), 1);
        /* ipt[i+0]: coefficients of planet i start at buf[ipt[i+0]-1]
         * ipt[i+1]: number of coefficients (interpolation order - 1)
         * ipt[i+2]: number of intervals in segment */
//      fread((void *) &ipt[0], sizeof(long), 36, js.jplfptr);
        for(int m=0;m<36;m++) {
          ipt[m]=jplSave.jplfptr.readInt();
        }
//      if (js.do_reorder)
//        reorder((char *) &ipt[0], sizeof(long), 36);
        /* numde = number of jpl ephemeris "404" with de404 */
//      fread((void *) &js.eh_denum, sizeof(long), 1, js.jplfptr);
        jplSave.eh_denum=jplSave.jplfptr.readInt();
//      if (js.do_reorder)
//        reorder((char *) &js.eh_denum, sizeof(long), 1);
//      fread((void *) &lpt[0], sizeof(long), 3, js.jplfptr);
        for(int m=0;m<3;m++) {
          lpt_state[m]=jplSave.jplfptr.readInt();
        }
//      if (js.do_reorder)
//        reorder((char *) &lpt[0], sizeof(long), 3);
        /* cval[]:  other constants in next record */
//      fseek(js.jplfptr, 1L * irecsz, 0);
        jplSave.jplfptr.seek(1L * irecsz_state);
//      fread((void *) &js.eh_cval[0], sizeof(double), 400, js.jplfptr);
        for(int m=0;m<400;m++) {
          jplSave.eh_cval[m]=jplSave.jplfptr.readDouble();
        }
//      if (js.do_reorder)
//        reorder((char *) &js.eh_cval[0], sizeof(double), 400);
        /* new 26-aug-2008: verify correct block size */
        for (i = 0; i < 3; ++i)
          ipt[i + 36] = lpt_state[i];
        nrl_state = 0;
        /* is file length correct? */
        /* file length */
//      flen = ftell(js.jplfptr);
        flen=jplSave.jplfptr.length();
        /* # of segments in file */
        nseg = (int) ((jplSave.eh_ss[1] - jplSave.eh_ss[0]) / jplSave.eh_ss[2]);
        /* sum of all cheby coeffs of all planets and segments */
        for(i = 0, nb = 0; i < 13; i++) {
          k = 3;
          if (i == 11) {
            k = 2;
          }
          nb += (ipt[i*3+1] * ipt[i*3+2]) * k * nseg;
        }
        /* add start and end epochs of segments */
        nb += 2 * nseg;
        /* doubles to bytes */
        nb *= 8;
        /* add size of header and constants section */
        nb += 2 * ksize * nrecl;
        if (flen != nb
          /* some of our files are one record too long */
          && flen - nb != ksize * nrecl) {
          if (serr != null) {
            serr.setLength(0);
            serr.append("JPL ephemeris file is mutilated; length = "+flen+" instead of "+nb+".");
            if (serr.length() + jplSave.jplfname.length() < SwissData.AS_MAXCH - 1) {
              serr.setLength(0); // Nanu???
              serr.append("JPL ephemeris file "+jplSave.jplfname+" is mutilated; length = "+flen+" instead of "+nb+".");
            }
          }
          throw new SwissephException(et, SwissephException.FILE_READ_ERROR,
              SweConst.ERR, serr);
        }
        /* check if start and end dates in segments are the same as in
         * file header */
//      fseek(js->jplfptr, 2L * irecsz, 0);
        jplSave.jplfptr.seek(2L * irecsz_state);
//      fread((void *) &ts[0], sizeof(double), 2, js->jplfptr);
        ts[0]=jplSave.jplfptr.readDouble();
        ts[1]=jplSave.jplfptr.readDouble();
//      if (js->do_reorder)
//        reorder((char *) &ts[0], sizeof(double), 2);
//      fseek(js->jplfptr, (2 + nseg) * irecsz, 0);
        jplSave.jplfptr.seek((nseg + 2 - 1) * irecsz_state);
//      fread((void *) &ts[2], sizeof(double), 2, js->jplfptr);
        ts[2]=0; //js.jplfptr.readDouble();
        ts[3]=0; //js.jplfptr.readDouble();
//      if (js->do_reorder)
//        reorder((char *) &ts[2], sizeof(double), 2);
        if (ts[0] != jplSave.eh_ss[0] || ts[3] != jplSave.eh_ss[1]) {
          if (serr != null) {
            serr.setLength(0);
            serr.append("JPL ephemeris file is corrupt; start/end date check failed.");
          }
          throw new SwissephException(et, SwissephException.DAMAGED_FILE_ERROR,
              SwephData.NOT_AVAILABLE, serr);
        }
      }
      if (list == null) {
        return 0;
      }
      s = et - .5;
      et_mn = SMath.floor(s);
      et_fr = s - et_mn;    /* fraction of days since previous midnight */
      et_mn += .5;  /* midnight before epoch */
      /*       error return for epoch out of range */
      if (et < jplSave.eh_ss[0] || et > jplSave.eh_ss[1]) {
        if (serr != null) {
          serr.setLength(0);
          serr.append("jd "+et+" outside JPL eph. range "+jplSave.eh_ss[0]+" .. "+jplSave.eh_ss[1]+";");
        }
        throw new SwissephException(et, SwissephException.OUT_OF_TIME_RANGE,
            SwephData.BEYOND_EPH_LIMITS, serr);
      }
      /*       calculate record # and relative time in interval */
      nr = (int) ((et_mn - jplSave.eh_ss[0]) / jplSave.eh_ss[2]) + 2;
      if (et_mn == jplSave.eh_ss[1]) {
        --nr;       /* end point of ephemeris, use last record */
      }
      t = (et_mn - ((nr - 2) * jplSave.eh_ss[2] + jplSave.eh_ss[0]) + et_fr) / jplSave.eh_ss[2];
      /* read correct record if not in core */
      if (nr != nrl_state) {
        nrl_state = nr;
        jplSave.jplfptr.seek(nr * irecsz_state);
        for (k = 1; k <= ncoeffs_state; ++k) {
//        if ( fread((void *) &buf[k - 1], sizeof(double), 1, js.jplfptr) != 1) {

          buf[k - 1]=jplSave.jplfptr.readDouble();
//        }
//        if (js.do_reorder)
//          reorder((char *) &buf[k-1], sizeof(double), 1);
        }
      }
      if (jplSave.do_km) {
        intv = jplSave.eh_ss[2] * 86400.;
        aufac = 1.;
      } else {
        intv = jplSave.eh_ss[2];
        aufac = 1. / jplSave.eh_au;
      }
      /*   interpolate ssbary sun */
    } catch (java.io.EOFException ie) {
      ferr=true;
    } catch (java.io.IOException ie) {
      ferr=true;
    } catch (java.nio.BufferUnderflowException ie) {
      ferr=true;
    }
    if (ferr) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("Read error in JPL eph. at "+et+"\n");
      }
      throw new SwissephException(et, SwissephException.FILE_READ_ERROR,
          SwephData.NOT_AVAILABLE, serr);
    }
    interp(buf, (int) ipt[30] - 1, t, intv, ipt[31], 3, ipt[32], 2, pvsun, 0);
    for (i = 0; i < 6; ++i) {
      pvsun[i] *= aufac;
    }
    /*   check and interpolate whichever bodies are requested */
    for (i = 0; i < 10; ++i) {
      if (list[i] > 0) {
        interp(buf, (int) ipt[i * 3] - 1, t, intv, ipt[i * 3 + 1], 3,
               ipt[i * 3 + 2], list[i], pv, i * 6);
        for (j = 0; j < 6; ++j) {
          if (i < 9 && ! do_bary) {
            pv[j + i * 6] = pv[j + i * 6] * aufac - pvsun[j];
          } else {
            pv[j + i * 6] *= aufac;
          }
        }
      }
    }
    /*       do nutations if requested (and if on file) */
    if (list[10] > 0 && ipt[34] > 0) {
      interp(buf, (int) ipt[33] - 1, t, intv, ipt[34], 2, ipt[35],
               list[10], nut, 0);
    }
    /*       get librations if requested (and if on file) */
    if (list[11] > 0 && ipt[37] > 0) {
      interp(buf, (int) ipt[36] - 1, t, intv, ipt[37], 3, ipt[38], list[1],
              pv, 60);
    }
    return SweConst.OK;
  }

  /*
   *  this entry obtains the constants from the ephemeris file
   *  call state to initialize the ephemeris and read in the constants
   */
  private int read_const_jpl(double[] ss,  StringBuffer serr) throws SwissephException {
    int i;
    // throws SwissephException if !SweConst.OK:
    state(0.0, null, false, null, null, null, serr);

    for (i = 0; i < 3; i++)
      ss[i] = jplSave.eh_ss[i];
    return SweConst.OK;
  }

//  void reorder(char *x, int size, int number) {
//    int i, j;
//    char s[8];
//    char *sp1 = x;
//    char *sp2 = &s[0];
//    for (i = 0; i < number; i++) {
//      for (j = 0; j < size; j++)
//        *(sp2 + j) = *(sp1 + size - j - 1);
//      for (j = 0; j < size; j++)
//        *(sp1 + j) = *(sp2 + j);
//      sp1 += size;
//    }
//  }

  void swi_close_jpl_file() {
    if (jplSave != null) {
      try {
        if (jplSave.jplfptr != null) {
          jplSave.jplfptr.close();
        }
      } catch (java.io.IOException e) {
      }
      if (jplSave.jplfname != null) {
        jplSave.jplfname = null;
      }
      if (jplSave.jplfpath != null) {
        jplSave.jplfpath = null;
      }
      jplSave = null;
    }
  }

  int swi_open_jpl_file(double[] ss, String fname, String fpath,
                                StringBuffer serr) throws SwissephException {
    int retc = SweConst.OK;
    /* if open, return */
    if (jplSave != null && jplSave.jplfptr != null) {
      return SweConst.OK;
    }
    jplSave=new JplSave();
/*
    if ((js = (struct jpl_save *) CALLOC(1, sizeof(struct jpl_save))) == null
      || (js.jplfname = MALLOC(strlen(fname)+1)) == null
      || (js.jplfpath = MALLOC(strlen(fpath)+1)) == null
      ) {
      if (serr != null)
        strcpy(serr, "error in malloc() with JPL ephemeris.");
      return SweConst.ERR;
    }
*/
    jplSave.jplfname=fname;
    jplSave.jplfpath=fpath;
    try {
      retc = read_const_jpl(ss, serr);
    } catch (SwissephException se) {
      swi_close_jpl_file();
      throw se;
    }

    /* intializations for function interpol() */
    jplSave.pc[0] = 1;
    jplSave.pc[1] = 2;
    jplSave.vc[1] = 1;
    jplSave.ac[2] = 4;
    jplSave.jc[3] = 24;

    return retc;
  }

  int swi_get_jpl_denum() {
    return jplSave.eh_denum;
  }

  double[] getJPLRange(String fname) throws SwissephException {
    double start=0./0., end=0./0.;
    FilePtr fp = null;
    try {
      fp = swissEph.swi_fopen(SwephData.SEI_FILE_PLANET, fname, swissData.ephepath, null);
      fp.seek(252+6*400);
      start = fp.readDouble();
      end = fp.readDouble();
    } catch (SwissephException e) {
      throw e;
    } catch (Exception e) {
    }
    try { fp.close(); } catch (Exception e) { }
    return new double[]{start, end};
  }
}


class JplSave
		implements java.io.Serializable
		{
  String jplfname=null;
  String jplfpath=null;
  FilePtr jplfptr=null;
//  boolean do_reorder;
  double eh_cval[]=new double[400];
  double eh_ss[]=new double[3], eh_au, eh_emrat;
  int eh_denum, eh_ncon, eh_ipt[]=new int[39];
  String ch_cnam="";
  double pv[]=new double[78];
  double pvsun[]=new double[6];
  double buf[]=new double[1500];
  double pc[]=new double[18], vc[]=new double[18],
         ac[]=new double[18], jc[]=new double[18];
  boolean do_km;
}
