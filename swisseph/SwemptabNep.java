
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
/* SWISSEPH
   $Header: /home/dieter/sweph/RCS/swemplan.c,v 1.74 2008/06/16 10:07:20 dieter Exp $
   Moshier planet routines

   modified for SWISSEPH by Dieter Koch

**************************************************************/
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


class SwemptabNep
		implements java.io.Serializable
		{
  /*
  First date in file = 625296.50
  Number of records = 16731.0
  Days per record = 131.0
        Julian Years      Lon    Lat    Rad
   -3000.0 to  -2499.7:   0.44   0.30   0.50
   -2499.7 to  -1999.7:   0.39   0.20   0.39
   -1999.7 to  -1499.7:   0.31   0.15   0.31
   -1499.7 to   -999.8:   0.32   0.19   0.36
    -999.8 to   -499.8:   0.29   0.15   0.29
    -499.8 to      0.2:   0.31   0.14   0.27
       0.2 to    500.2:   0.28   0.14   0.27
     500.2 to   1000.1:   0.34   0.15   0.39
    1000.1 to   1500.1:   0.31   0.16   0.31
    1500.1 to   2000.1:   0.33   0.16   0.29
    2000.1 to   2500.0:   0.38   0.21   0.36
    2500.0 to   3000.0:   0.43   0.25   0.46
    3000.0 to   3000.4:  0.122  0.071  0.260
  */
  static final double neptabl[] = {
       -1376.86480,         730.38970,    78655362.50948,     1095691.38676,

        -196.19023,        2086.77782,

        -122.04650,        -276.81592,

         184.56164,        -148.08924,

           3.39142,         -14.75027,

          -9.22741,           0.87688,

          -0.13903,          -0.44707,

          -0.17668,          -0.36299,

          -0.12682,          -0.26636,

          -0.51426,          -0.24667,

          -0.04965,          -0.03177,

           0.05050,          -0.00249,

          -0.80362,          -0.07363,          -0.15436,          -0.07180,

           2.45034,          -3.50145,           0.86698,           0.09777,

           7.72386,           7.16565,           2.10273,           8.86682,

           2.44705,          77.90155,

           0.28323,         -11.87157,         -13.64083,         252.70556,

          -4.94214,          -6.17988,        -305.60504,          51.23962,
       -2759.81719,        2476.20912,

          12.65762,          13.31543,

           0.36996,          -0.19077,           0.67363,           0.36737,

           0.02312,           0.02216,

           0.09953,           0.04777,

          -0.00572,          -0.02772,

          -0.02478,          -0.21920,

          -0.15289,          -1.50784,

          -0.17822,           0.34638,          -0.70473,          -8.61559,

          -2.65756,           1.25632,

          -0.31453,          -1.40348,          -4.02571,          -1.50467,
         -69.62308,           3.21315,

           0.69973,           0.08832,

          -0.00551,          -0.04964,

          -0.02264,          -0.34881,

           0.00762,          -1.85072,

           0.01407,          -0.30457,

          -0.09851,          -0.02372,

          -0.07729,          -0.11602,          -0.75995,          -0.71884,

          -0.08585,          -0.30406,           0.45818,           0.14921,

          -0.01033,          -0.11776,

           0.00640,          -0.57717,

          -0.01014,          -0.01357,          -0.00544,          -0.02168,

           0.40468,           0.28195,           0.00668,           0.14448,

           0.01245,          -0.08956,

          -0.26283,           0.01864,          -0.00641,          18.55347,

           0.01460,           0.08284,

          -0.04785,           0.11360,

          -0.33861,           0.01327,          -0.06392,          -0.18758,

           0.05449,          -0.05583,

          -0.00435,          -0.09869,

          -0.00286,          -0.04613,

          -0.00395,          -0.14564,

          -0.01385,          -0.01762,

           0.21160,          -0.61631,          -0.52100,          -0.04583,

           0.32812,           0.32138,

           0.04749,          -0.05724,

           0.11239,           0.13216,

          -0.01203,           0.40084,          -0.05207,          34.07903,

          -0.21457,          -0.34938,          -0.04594,           0.11198,

          -0.30662,          -0.20776,          -0.01076,          -0.10959,

           0.10891,          -0.10304,

          -0.28141,           0.25061,          -0.20293,           0.79930,

  };
  static final double neptabb[] = {
        -391.05987,        -243.95958,         -23.83558,          58.13857,

           5.04859,          -3.93183,

         -14.21914,           7.14247,

         -12.09415,          -9.70132,

           1.04307,           0.47323,

          -0.07504,           0.70575,

          -0.05239,           0.00482,

          -0.02916,           0.00877,

          -0.00528,          -0.00286,

           0.00028,          -0.00228,

          -0.00056,          -0.00149,

           0.00049,           0.00047,

          -0.18765,          -0.59571,           0.03742,          -0.14653,

           2.30535,           0.65092,           0.42216,           0.24521,

          -2.86932,           2.37808,          -0.58456,           0.27446,

          -1.12264,          -2.04413,

         -11.71318,          -1.41554,         -23.30671,         -24.70499,

           8.82738,          85.64657,         -90.02223,          22.42604,
       -4749.41359,       -4244.46820,

          25.20811,         -18.51469,

          -1.19892,          -0.61067,           0.67734,          -1.08912,

          -0.01607,           0.00626,

          -0.00008,           0.00126,

          -0.00330,          -0.00078,

          -0.01503,           0.00758,

          -0.13208,          -0.00218,

          -0.04522,           0.20297,          -0.94708,          -0.77897,

          -2.74075,          -3.01122,

          -1.03394,           0.00886,           1.55485,          -4.68416,
          -0.13244,         -57.26983,

           0.05589,          -0.55396,

          -0.00130,           0.00526,

          -0.01028,           0.02086,

           0.01334,           0.00699,

           0.08565,           0.02020,

           0.01001,          -0.08402,

           0.08558,          -0.04488,           0.57268,          -0.59574,

           0.00807,           0.00492,           0.21993,          -0.18949,

          -0.00396,           0.00735,

           0.00487,           0.00230,

           0.00699,          -0.00473,           0.01406,          -0.00139,

           0.00738,           0.00099,           0.00161,           0.00019,

          -0.00067,          -0.00047,

           0.00572,          -0.00486,          -0.00842,           0.00322,

           0.00018,          -0.00109,

          -0.00272,           0.00112,

          -0.00041,           0.00763,           0.00211,           0.00118,

          -0.46842,          -0.17877,

           0.00209,          -0.00179,

           0.00090,          -0.00075,

           0.00618,           0.00610,

           0.00015,           0.00032,

          -0.00123,           0.00026,           0.00332,           0.00135,

           0.39130,          -0.34727,

           0.00015,          -0.00027,

          -0.00026,          -0.00052,

           0.00162,           0.00913,          -0.00697,           0.00308,

          -0.00333,          -0.00258,          -0.00117,           0.00035,

           0.00766,           0.00194,           0.00135,           0.00067,

          -0.41171,           0.24241,

           0.00106,           0.00025,           0.00013,          -0.00019,

  };
  static final double neptabr[] = {
        -767.68936,        -460.59576,         -52.41861,        -273.85897,

          59.52489,           1.85006,

         -39.64750,          23.63348,

         -34.60399,         -23.41681,

           2.74937,           1.55389,

           0.20343,           2.15502,

          -0.12846,           0.07199,

          -0.07555,           0.05582,

          -0.04354,           0.01546,

          -0.03931,           0.07623,

          -0.00491,           0.00661,

           0.00322,           0.01540,

          -0.06741,          -0.35343,           0.00469,          -0.08073,

           1.94975,           0.66376,           0.06137,           0.31426,

          -2.93841,           4.27732,          -4.00342,           1.11157,

         -36.87785,           1.24960,

           4.69573,           2.15164,        -114.24899,          -6.69320,

          12.99919,          -9.47795,         -21.82350,        -156.88624,
       -1237.19769,       -1379.88864,

           6.54369,          -6.20873,

          -0.14163,          -0.32700,           0.17937,          -0.34864,

           0.01393,          -0.01286,

           0.02876,          -0.05767,

           0.02210,          -0.00128,

           0.16495,          -0.01242,

           1.15915,          -0.10365,

          -0.33224,          -0.10045,           6.83719,          -0.27499,

          -0.31284,          -0.94332,

           1.63704,          -0.33318,           1.48134,          -1.32257,
           0.96498,          -8.31047,

          -0.00402,          -0.09441,

           0.04292,          -0.00444,

           0.30325,          -0.02012,

           1.67999,           0.00353,

           0.00467,           0.03556,

           0.01393,          -0.01229,

           0.01188,          -0.01390,           0.04615,          -0.03509,

           0.32423,          -0.12491,           0.13682,           0.15131,

           0.11221,          -0.01201,

           0.57239,           0.00093,

           0.02068,          -0.01162,           0.00647,          -0.00325,

           0.27010,          -0.42993,           0.14314,          -0.01353,

          -0.08757,          -0.00699,

           0.00199,           0.31873,          18.80329,           0.01681,

           0.08009,          -0.00998,

          -0.14421,          -0.15912,

           0.37208,           0.49744,           0.35144,           0.06582,

          -0.11501,          -0.14037,

           0.10352,          -0.00768,

           0.04826,          -0.00423,

           0.19850,           0.00310,

          -0.01780,           0.01350,

          -0.61106,          -0.20525,          -0.04388,           0.52143,

           0.19300,          -0.21446,

          -0.05749,          -0.04776,

           0.12877,          -0.10908,

           0.39821,           0.00627,          34.03956,           0.04392,

          -0.34455,           0.22015,           0.11743,           0.04638,

           0.20723,          -0.30447,           0.10976,          -0.01008,

          -0.20778,          -0.21822,

           0.24939,           0.27976,           0.79790,           0.20200,

  };

  static byte nepargs[] = {
  (byte)0,  (byte)3,
  (byte)2,  (byte)1,  (byte)7, (byte)-2,  (byte)8,  (byte)0,
  (byte)3,  (byte)3,  (byte)5, (byte)-8,  (byte)6,  (byte)3,  (byte)8,  (byte)0,
  (byte)2,  (byte)2,  (byte)7, (byte)-4,  (byte)8,  (byte)0,
  (byte)3,  (byte)1,  (byte)5, (byte)-3,  (byte)6,  (byte)3,  (byte)8,  (byte)0,
  (byte)2,  (byte)3,  (byte)7, (byte)-6,  (byte)8,  (byte)0,
  (byte)2,  (byte)4,  (byte)7, (byte)-8,  (byte)8,  (byte)0,
  (byte)3,  (byte)1,  (byte)6, (byte)-6,  (byte)7,  (byte)6,  (byte)8,  (byte)0,
  (byte)3,  (byte)1,  (byte)6, (byte)-5,  (byte)7,  (byte)4,  (byte)8,  (byte)0,
  (byte)3,  (byte)1,  (byte)6, (byte)-4,  (byte)7,  (byte)2,  (byte)8,  (byte)0,
  (byte)2,  (byte)1,  (byte)6, (byte)-3,  (byte)7,  (byte)0,
  (byte)3,  (byte)1,  (byte)6, (byte)-2,  (byte)7, (byte)-1,  (byte)8,  (byte)0,
  (byte)2,  (byte)5,  (byte)7, (byte)-9,  (byte)8,  (byte)1,
  (byte)2,  (byte)4,  (byte)7, (byte)-7,  (byte)8,  (byte)1,
  (byte)2,  (byte)3,  (byte)7, (byte)-5,  (byte)8,  (byte)1,
  (byte)2,  (byte)2,  (byte)7, (byte)-3,  (byte)8,  (byte)0,
  (byte)2,  (byte)1,  (byte)7, (byte)-1,  (byte)8,  (byte)1,
  (byte)1,  (byte)1,  (byte)8,  (byte)2,
  (byte)2,  (byte)1,  (byte)7, (byte)-3,  (byte)8,  (byte)0,
  (byte)2,  (byte)2,  (byte)7, (byte)-5,  (byte)8,  (byte)1,
  (byte)3,  (byte)1,  (byte)6, (byte)-6,  (byte)7,  (byte)5,  (byte)8,  (byte)0,
  (byte)3,  (byte)1,  (byte)6, (byte)-5,  (byte)7,  (byte)3,  (byte)8,  (byte)0,
  (byte)2,  (byte)5,  (byte)7, (byte)-8,  (byte)8,  (byte)0,
  (byte)2,  (byte)4,  (byte)7, (byte)-6,  (byte)8,  (byte)0,
  (byte)2,  (byte)3,  (byte)7, (byte)-4,  (byte)8,  (byte)0,
  (byte)2,  (byte)2,  (byte)7, (byte)-2,  (byte)8,  (byte)1,
  (byte)1,  (byte)1,  (byte)7,  (byte)0,
  (byte)1,  (byte)2,  (byte)8,  (byte)2,
  (byte)2,  (byte)1,  (byte)7, (byte)-4,  (byte)8,  (byte)0,
  (byte)2,  (byte)5,  (byte)7, (byte)-7,  (byte)8,  (byte)0,
  (byte)2,  (byte)4,  (byte)7, (byte)-5,  (byte)8,  (byte)0,
  (byte)2,  (byte)3,  (byte)7, (byte)-3,  (byte)8,  (byte)0,
  (byte)2,  (byte)2,  (byte)7, (byte)-1,  (byte)8,  (byte)0,
  (byte)2,  (byte)1,  (byte)7,  (byte)1,  (byte)8,  (byte)0,
  (byte)1,  (byte)3,  (byte)8,  (byte)1,
  (byte)2,  (byte)1,  (byte)6, (byte)-2,  (byte)8,  (byte)1,
  (byte)2,  (byte)5,  (byte)7, (byte)-6,  (byte)8,  (byte)0,
  (byte)2,  (byte)4,  (byte)7, (byte)-4,  (byte)8,  (byte)0,
  (byte)1,  (byte)4,  (byte)8,  (byte)1,
  (byte)3,  (byte)2,  (byte)5, (byte)-4,  (byte)6, (byte)-1,  (byte)8,  (byte)1,
  (byte)3,  (byte)1,  (byte)6,  (byte)1,  (byte)7, (byte)-3,  (byte)8,  (byte)0,
  (byte)2,  (byte)1,  (byte)6, (byte)-1,  (byte)8,  (byte)1,
  (byte)3,  (byte)1,  (byte)6, (byte)-1,  (byte)7,  (byte)1,  (byte)8,  (byte)0,
  (byte)3,  (byte)2,  (byte)5, (byte)-6,  (byte)6,  (byte)1,  (byte)8,  (byte)0,
  (byte)2,  (byte)5,  (byte)7, (byte)-5,  (byte)8,  (byte)1,
  (byte)1,  (byte)1,  (byte)6,  (byte)0,
  (byte)2,  (byte)6,  (byte)7, (byte)-6,  (byte)8,  (byte)0,
  (byte)2,  (byte)7,  (byte)7, (byte)-7,  (byte)8,  (byte)0,
  (byte)2,  (byte)2,  (byte)6, (byte)-2,  (byte)8,  (byte)0,
  (byte)3,  (byte)2,  (byte)5, (byte)-3,  (byte)6, (byte)-1,  (byte)8,  (byte)0,
  (byte)2,  (byte)2,  (byte)6, (byte)-1,  (byte)8,  (byte)1,
  (byte)2,  (byte)1,  (byte)5, (byte)-2,  (byte)8,  (byte)0,
  (byte)3,  (byte)3,  (byte)5, (byte)-5,  (byte)6, (byte)-1,  (byte)8,  (byte)0,
  (byte)2,  (byte)4,  (byte)7,  (byte)5,  (byte)8,  (byte)0,
  (byte)2,  (byte)1,  (byte)5, (byte)-1,  (byte)8,  (byte)1,
  (byte)3,  (byte)1,  (byte)5, (byte)-1,  (byte)7,  (byte)1,  (byte)8,  (byte)1,
  (byte)3,  (byte)1,  (byte)5, (byte)-5,  (byte)6,  (byte)1,  (byte)8,  (byte)1,
  (byte)1,  (byte)1,  (byte)5,  (byte)0,
  (byte)2,  (byte)2,  (byte)5, (byte)-1,  (byte)8,  (byte)1,
 (byte)-1
  };
  /* Total terms = 59, small = 58 */
  static Plantbl nep404 = new Plantbl(
                               new short[]{0,  0,  0,  0,  3,  8,  7,  9,  0},
                               (short)3,
                               nepargs,
                               neptabl,
                               neptabb,
                               neptabr,
                               3.0110386869399999e+01
  );
}
