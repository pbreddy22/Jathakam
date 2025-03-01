
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


class SwemptabMer
		implements java.io.Serializable
		{
  /*
  First date in file = 1228000.50
  Number of records = 397276.0
  Days per record = 4.0
        Julian Years      Lon    Lat    Rad
   -1349.9 to  -1000.0:   0.42   0.18   0.16
   -1000.0 to   -500.0:   0.37   0.19   0.13
    -500.0 to      0.0:   0.35   0.17   0.12
       0.0 to    500.0:   0.34   0.15   0.10
     500.0 to   1000.0:   0.28   0.14   0.09
    1000.0 to   1500.0:   0.34   0.14   0.09
    1500.0 to   2000.0:   0.35   0.13   0.09
    2000.0 to   2500.0:   0.38   0.12   0.12
    2500.0 to   3000.0:   0.42   0.13   0.16
    3000.0 to   3000.8:  0.252  0.087  0.115
  */
  static final double mertabl[] = {
          35.85255,        -163.26379, 53810162857.56026,      908082.18475,

           0.05214,          -0.07712,

           1.07258,           0.04008,           0.49259,           0.00230,
           0.02324,           0.05869,

           0.24516,           0.22898,          -0.06037,           0.13023,

           0.00331,          -0.03576,

           0.06464,           0.00089,

           0.03103,           0.05078,

          -0.01133,           0.01520,

           0.14654,           0.07538,           0.25112,          -0.24473,
          -0.17928,          -0.53366,

          -0.06367,           0.20458,          -0.42985,           0.14848,
          -0.35317,          -0.61364,

           0.00325,          -0.08617,          -0.23180,           0.08576,
           0.22995,           0.43569,

           1.92114,           2.89319,          -5.55637,           4.70329,
          -4.91411,          -5.45521,

           0.02607,           0.04468,

          -0.05439,           0.13476,          -0.07329,          -0.00985,

          -0.00278,           0.05377,

           0.07474,          -0.09658,           0.29818,           0.20422,
          -0.29074,           0.44962,

          -0.15411,          -0.04287,           0.29907,          -1.02948,
           3.62183,           0.84869,

          -0.08157,           0.02754,

          -0.03610,          -0.12909,           0.09195,          -0.04424,

          -0.08845,           0.09347,

          -0.27140,           0.08185,

           0.24783,           0.19543,          -0.25154,           0.41371,

          -0.00046,           0.01524,

           0.04127,           0.06663,

           0.43023,           0.11790,

           0.04427,           0.05329,

           0.00411,          -0.71074,

          -0.07111,          -0.09824,

           0.01264,          -0.02075,

          -0.00068,          -0.01678,

           0.01186,           0.00181,

           0.00302,          -0.21963,

          -0.06412,          -0.10155,          -0.36856,           0.20240,
           0.32282,           0.65133,

          -0.07178,          -0.01876,           0.13399,          -0.39522,
           1.28413,           0.33790,

           0.05040,          -0.01679,

          -0.00794,           0.01117,

           0.02630,           0.00575,

          -0.07113,          -0.11414,           0.16422,          -0.23060,
           0.35198,           0.05409,

           1.11486,          -0.35833,           0.87313,           1.66304,
          -1.28434,           0.72067,

           0.01400,           0.00971,

           0.21044,          -0.87385,           3.20820,           0.67957,

          -0.01716,           0.00111,

          -0.13776,          -0.02650,

          -0.06778,           0.00908,           0.00616,          -0.04520,

          -0.31625,          -0.61913,

           0.36184,           0.09373,

           0.00984,          -0.03292,

           0.01944,           0.00530,

           0.00243,          -0.00123,

           0.01589,           0.02223,

          -0.02992,          -0.01086,

        4356.04809,       -5859.86328,        2918.27323,       -4796.67315,
         510.24783,       -1220.02233,         127.48927,         250.10654,
        3250.43013,        -904.27614,       -5667.40042,      -22634.00922,
      -82471.79425,       18615.92342,

           0.01941,           0.00372,

           0.01830,          -0.00652,

          -0.02548,          -0.01157,

           0.00635,           0.02343,

          -0.00980,           0.00961,

           0.12137,           0.10068,           0.16676,          -0.07257,

          -0.07267,          -0.13761,           0.25305,          -0.28112,

          -0.07974,           0.07866,

          -0.41726,           0.49991,          -1.55187,          -1.14150,
           1.54754,          -2.35141,

          -0.00862,           0.00808,

           0.00218,          -0.03726,

           0.06914,          -0.08986,

          -0.00501,           2.09577,

          -0.01409,          -0.01842,

           0.04138,           0.05961,

          -0.12276,          -0.04929,

          -0.03963,          -0.06080,

          -0.27697,          -0.09329,

          -0.01011,           0.00295,

          -0.01374,           0.01328,

          -0.00171,           0.25815,

           0.01446,           0.00782,

           0.17909,          -0.04683,

           0.03765,          -0.04990,

           0.00036,           0.00528,

           0.05508,          -0.01369,

          -0.11751,          -0.10624,          -0.14448,           0.10522,

          -0.00884,           0.43006,

           0.01162,           0.01659,

          -0.00076,           0.10143,

           0.55779,           0.05510,           0.12350,          -0.34025,

           0.01320,           0.92985,
          -0.00026,          -0.03426,

           0.01305,           0.00041,

           0.13187,          -0.11903,

           0.00058,           0.09877,

         -33.10230,         -41.96782,        -268.28908,         174.29259,
         731.20089,        1508.07639,        5223.99114,       -3008.08849,
       -3909.34957,       -9646.69156,

           0.02988,           0.03182,

           0.07149,           0.04513,

          -0.02356,          -0.01641,

          -0.03188,          -0.03711,           0.15084,          -0.22436,
           0.61987,           0.25706,

           0.02425,           0.01200,

          -0.05543,          -0.14435,          -0.53398,           0.10997,

           0.00465,          -0.01893,

           0.01260,          -0.01314,

           0.00650,          -0.05499,

          -0.06804,           0.01608,

           0.02134,           0.04160,

           0.00636,           0.01293,

          -0.03470,          -0.02697,

          -0.11323,           0.02409,

          -0.02618,           0.00827,

           0.01879,           0.16838,           0.08978,           0.01934,

          -0.23564,           0.05565,

           0.03686,           0.02644,

          -0.02471,           0.00558,

        -140.22669,        -120.40692,        -501.88143,         434.05868,
        1044.54998,        1162.72084,        1527.78437,        -882.37371,

          -0.00768,           0.02213,

          -0.04090,           0.16718,

          -0.05923,          -0.12595,

           0.01154,          -0.00025,

          -0.00776,          -0.01653,

          -0.01213,          -0.02773,

           0.00344,           0.02180,

          -0.02558,          -0.05682,

          -0.00490,           0.01050,

          38.75496,         -78.17502,        -189.90700,        -136.33371,
        -249.94062,         319.76423,         205.73478,         272.64549,

          -0.01132,          -0.01071,          -0.04607,          -0.00390,

           0.02903,          -0.02070,

           0.01326,          -0.00901,

          35.38435,           7.45358,          31.08987,         -70.52685,
         -92.13879,         -51.58876,         -51.80016,          48.98102,

          -0.00124,          -0.01159,

           0.47335,          13.71886,          23.71637,           5.55804,
          10.06850,         -25.65292,         -11.85300,         -10.20802,

          -4.72861,           1.27151,          -0.47322,           7.46754,
           6.99528,           1.79089,           2.05336,          -2.90866,

          -1.97528,           0.72236,          -0.25084,           1.90269,
           0.72127,           0.41354,

          -0.30286,          -0.53125,          -0.50883,          -0.01200,
          -0.08301,           0.18083,

          -0.04286,          -0.10963,          -0.04544,          -0.01645,

          -0.00013,          -0.00986,

  };
  static final double mertabb[] = {
          68.33369,         422.77623,       -2057.26405,       -2522.29068,

          -0.00030,          -0.00009,

           0.02400,          -0.06471,           0.02074,          -0.00904,
           0.00044,           0.00261,

          -0.00174,          -0.00088,          -0.00027,           0.00003,

           0.00005,          -0.00004,

          -0.00036,           0.00200,

           0.01432,           0.01199,

           0.00006,          -0.00004,

           0.00236,           0.00803,           0.01235,           0.00406,
          -0.03253,           0.00179,

          -0.00243,           0.00132,          -0.00352,           0.00011,
          -0.00146,          -0.01154,

           0.00824,          -0.01195,          -0.01829,          -0.00465,
           0.12540,           0.09997,

           0.00400,           0.00288,          -0.02848,           0.01094,
          -0.02273,          -0.07051,

           0.01305,           0.01078,

          -0.00119,           0.00136,          -0.00107,          -0.00066,

           0.00097,          -0.00315,

           0.00120,           0.00430,          -0.00710,          -0.00157,
           0.06052,          -0.04777,

           0.00192,          -0.00229,          -0.02077,           0.00647,
           0.06907,           0.07644,

          -0.00717,           0.00451,

           0.00052,          -0.00262,           0.00345,           0.00039,

          -0.00674,           0.00346,

          -0.02880,           0.00807,

           0.00054,           0.00206,          -0.01745,           0.00517,

          -0.00044,           0.00049,

           0.01749,           0.01230,

           0.01703,           0.01563,

           0.00934,           0.02372,

           0.01610,          -0.01136,

           0.00186,          -0.00503,

           0.00082,          -0.00673,

           0.00170,          -0.00539,

           0.00042,           0.00037,

           0.00415,          -0.00430,

           0.00258,          -0.00914,          -0.01761,          -0.00251,
           0.15909,           0.13276,

           0.02436,          -0.00791,           0.00491,           0.03890,
          -0.02982,           0.05645,

          -0.00003,           0.00427,

          -0.00363,           0.00221,

           0.00077,           0.00130,

           0.00131,          -0.00071,           0.00796,           0.00453,
           0.01186,           0.01631,

           0.12949,          -0.02546,           0.03613,           0.32854,
          -0.43001,           0.01417,

           0.00034,           0.00095,

          -0.03268,           0.04034,           0.11407,           0.15049,

          -0.00079,          -0.00052,

          -0.04009,           0.00988,

          -0.00259,          -0.00085,           0.00221,          -0.00133,

           0.00003,          -0.01733,

           0.01055,           0.01976,

           0.00222,           0.00085,

           0.00089,           0.00087,

           0.00014,           0.00001,

           0.00145,           0.00802,

           0.00122,           0.00068,

         947.79367,       -1654.39690,         542.00864,       -1281.09901,
          90.02068,        -318.36115,         -87.67090,          92.91960,
         376.98232,        -419.10705,        5094.60412,        2476.97098,
      -18160.57888,       16010.48165,

           0.00621,          -0.00128,

           0.00186,          -0.00153,

          -0.00790,           0.00011,

          -0.00032,           0.00165,

          -0.00277,           0.00539,

           0.00552,           0.00682,           0.01086,          -0.00978,

          -0.02292,          -0.01300,           0.02940,          -0.04427,

          -0.02051,           0.04860,

          -0.05020,           0.29089,          -0.50763,          -0.04900,
           0.11177,          -0.41357,

          -0.00222,           0.00504,

          -0.00006,          -0.00459,

          -0.00175,          -0.02691,

           0.05921,           0.18938,

          -0.00181,          -0.00154,

           0.00322,           0.00586,

          -0.01098,          -0.00520,

          -0.00861,          -0.01342,

          -0.02694,          -0.00706,

          -0.00103,           0.00012,

          -0.00284,           0.00797,

           0.00743,           0.02523,

           0.00872,           0.00096,

           0.03155,          -0.01644,

           0.00414,          -0.00583,

           0.00029,           0.00066,

           0.00935,          -0.00619,

          -0.02498,          -0.01600,          -0.03545,           0.07623,

           0.01649,           0.06498,

           0.00148,           0.00209,

           0.00621,           0.02014,

           0.17407,          -0.05022,          -0.03485,          -0.17012,

           0.06164,           0.20059,

          -0.00804,          -0.01475,

           0.00296,          -0.00068,

           0.01880,          -0.03797,

           0.00608,           0.02270,

           5.89651,          -6.62562,         -37.41057,         -10.51542,
         -47.22373,          95.76862,         494.45951,          -5.37252,
       -3991.04809,       -2886.97750,

           0.01232,           0.00487,

           0.03163,           0.00561,

          -0.01847,          -0.00207,

          -0.10138,           0.01430,          -0.04269,          -0.22338,
           0.24955,          -0.02066,

           0.01119,          -0.00186,

           0.03416,           0.01805,          -0.12498,           0.10385,

          -0.00210,          -0.01011,

           0.00346,          -0.00682,

          -0.00683,          -0.02227,

          -0.01649,           0.01259,

           0.01392,           0.01174,

           0.00440,           0.00351,

          -0.02871,          -0.00375,

          -0.03170,           0.02246,

          -0.00833,           0.00596,
           0.04081,           0.06666,           0.05400,          -0.02387,

          -0.07852,           0.05781,

           0.01881,           0.00324,

          -0.00868,           0.00606,

          -6.52157,         -19.74446,         -72.46009,          43.12366,
         321.78233,         215.45201,         452.61804,       -1025.05619,

           0.00119,           0.01169,

           0.02239,           0.09003,

          -0.05329,          -0.03974,

           0.00688,          -0.00421,

          -0.00676,          -0.00515,

          -0.01171,          -0.00952,

           0.01337,           0.01270,

          -0.02791,          -0.02184,

           0.00058,           0.00679,

           8.42102,         -11.87757,         -49.07247,         -25.34584,
         -43.54829,         161.26509,         261.70993,          56.25777,

           0.00568,           0.00871,          -0.02656,           0.01582,

           0.00875,          -0.02114,

           0.00464,          -0.01075,

           9.08966,           1.37810,           3.44548,         -27.44651,
         -59.62749,          -0.73611,          -0.77613,          65.72607,

          -0.00664,          -0.00723,

           1.04214,           4.78920,          11.67397,          -1.84524,
          -4.16685,         -19.14211,         -16.14483,           3.02496,

          -1.98140,           1.16261,           1.81526,           4.21224,
           5.59020,          -2.55741,          -1.54151,          -3.85817,

          -1.08723,           1.23372,           1.12378,           1.51554,
           0.88937,          -0.57631,

          -0.50549,          -0.25617,          -0.37618,           0.42163,
           0.18902,           0.19575,

          -0.15402,          -0.04062,          -0.04017,           0.05717,

          -0.01665,          -0.00199,

  };
  static final double mertabr[] = {
          -8.30490,         -11.68232,          86.54880,        4361.05018,

           0.00002,          -0.00001,

          -0.01102,           0.00410,           0.00007,          -0.00276,
           0.00117,           0.00082,

           0.00049,           0.00007,           0.00003,          -0.00001,

           0.00012,           0.00005,

          -0.00186,          -0.00534,

          -0.03301,           0.01808,

           0.00008,           0.00005,

          -0.00394,           0.00202,           0.02362,          -0.00359,
           0.00638,          -0.06767,

           0.00422,          -0.00493,           0.00660,           0.00513,
          -0.00417,           0.00708,

           0.05849,          -0.00213,          -0.07647,          -0.16162,
          -0.30551,           0.13856,

          -0.02789,           0.01811,          -0.04155,          -0.06229,
           0.05729,          -0.03694,

          -0.03087,           0.01610,

          -0.00297,          -0.00167,           0.00041,          -0.00157,

          -0.00115,           0.00058,

           0.00796,           0.00436,          -0.01393,           0.02921,
          -0.05902,          -0.02363,

           0.00459,          -0.01512,           0.10038,           0.02964,
          -0.08369,           0.34570,

          -0.00749,          -0.02653,

           0.01361,          -0.00326,           0.00406,           0.00952,

          -0.00594,          -0.00829,

          -0.02763,          -0.09933,

          -0.04143,           0.05152,          -0.08436,          -0.05294,

          -0.00329,          -0.00016,

          -0.04340,           0.02566,

          -0.03027,           0.10904,

           0.03665,          -0.03070,

           0.23525,           0.00182,

           0.03092,          -0.02212,

           0.01255,           0.00777,

          -0.01025,           0.00042,

          -0.00065,           0.00440,

           0.08688,           0.00136,

           0.05700,          -0.03616,          -0.11272,          -0.20838,
          -0.37048,           0.18314,

           0.00717,          -0.02911,           0.15848,           0.05266,
          -0.13451,           0.51639,

           0.00688,           0.02029,

           0.00596,           0.00423,

          -0.00253,           0.01196,

           0.05264,          -0.03301,           0.10669,           0.07558,
          -0.02461,           0.16282,

          -0.18481,          -0.57118,           0.85303,          -0.44876,
           0.37090,           0.65915,

          -0.00458,           0.00660,

           0.41186,           0.09829,          -0.31999,           1.51149,

          -0.00052,          -0.00809,

           0.01384,          -0.07114,

          -0.00435,          -0.03237,           0.02162,           0.00294,

           0.29742,          -0.15430,

          -0.04508,           0.17436,

           0.01577,           0.00485,

          -0.00258,           0.00946,

           0.00061,           0.00119,

           0.01095,          -0.00788,

           0.00530,          -0.01478,

        2885.06380,        2152.76256,        2361.91098,        1442.28586,
         602.45147,         251.18991,        -121.68155,          71.20167,
         404.94753,        1607.37580,       11211.04090,       -2905.37340,
       -9066.27933,      -40747.62807,

          -0.00189,           0.00957,

           0.00332,           0.00907,

           0.00574,          -0.01255,

          -0.01134,           0.00291,

          -0.00666,          -0.00615,

          -0.04947,           0.06182,           0.03965,           0.08091,

           0.06846,          -0.03612,           0.13966,           0.12543,

          -0.05494,          -0.05043,

          -0.24454,          -0.20507,           0.56201,          -0.75997,
           1.15728,           0.76203,
          -0.00559,          -0.00536,

           0.01872,           0.00104,

           0.03044,           0.02504,

          -1.07241,          -0.00288,

           0.00950,          -0.00760,

          -0.03211,           0.02261,

           0.02678,          -0.06868,

           0.03008,          -0.02062,

           0.04997,          -0.15164,

          -0.00176,          -0.00580,

          -0.00730,          -0.00676,

          -0.13906,          -0.00089,

          -0.00362,           0.00817,

           0.02021,           0.07719,

           0.02788,           0.02061,

          -0.00274,           0.00016,

           0.00566,           0.02293,

           0.04691,          -0.05005,          -0.05095,          -0.06225,

          -0.19770,          -0.00456,

          -0.00848,           0.00595,

          -0.04506,          -0.00172,

          -0.01960,           0.22971,           0.14459,           0.04362,

          -0.40199,           0.00386,

           0.01442,          -0.00088,

          -0.00020,           0.00544,

           0.04768,           0.05222,

          -0.04069,          -0.00003,

          15.71084,         -12.28846,         -66.23443,        -109.83758,
        -586.31996,         311.09606,        1070.75040,        2094.34080,
        3839.04103,       -1797.34193,

          -0.01216,           0.01244,

          -0.01666,           0.02627,

           0.00687,          -0.01291,

           0.00939,          -0.01905,           0.09401,           0.05027,
          -0.09398,           0.23942,

          -0.00379,           0.00834,

           0.05632,          -0.01907,          -0.04654,          -0.21243,

           0.00255,           0.00179,

           0.00540,           0.00497,

           0.01427,           0.00243,

          -0.00697,          -0.02792,

          -0.01524,           0.00810,

          -0.00461,           0.00238,

           0.00899,          -0.01515,

          -0.01011,          -0.04390,

          -0.00447,          -0.00992,

          -0.06110,           0.00975,          -0.00261,           0.03415,

          -0.02336,          -0.08776,

          -0.00883,           0.01346,

          -0.00229,          -0.00895,

          42.18049,         -48.21316,        -148.61588,        -171.57236,
        -414.27195,         343.09118,         394.59044,         511.79914,

          -0.00911,          -0.00220,

          -0.06315,          -0.00988,

           0.04357,          -0.02389,

           0.00004,           0.00232,

           0.00581,          -0.00317,

           0.00948,          -0.00497,

          -0.00734,           0.00300,

           0.01883,          -0.01055,

          -0.00365,          -0.00126,

          24.18074,          12.28004,          43.18187,         -58.69806,
        -102.40566,         -79.48349,         -74.81060,          89.71332,

           0.00241,          -0.00135,          -0.00136,          -0.01617,

           0.00818,           0.00873,

           0.00368,           0.00383,

          -2.25893,          10.18542,          20.73104,           9.07389,
          13.73458,         -29.10491,         -20.62071,         -10.63404,

           0.00382,          -0.00143,

          -3.77385,           0.12725,          -1.30842,           6.75795,
           7.94463,           1.79092,           1.24458,          -4.73211,

          -0.36978,          -1.25710,          -2.06373,           0.06194,
          -0.00509,           2.08851,           1.07491,           0.04112,

          -0.28582,          -0.51413,          -0.53312,           0.11936,
           0.04447,           0.23945,

           0.12450,          -0.11821,          -0.06100,          -0.12924,
          -0.05193,           0.02219,

           0.01977,          -0.02933,          -0.00771,          -0.01077,

           0.00109,          -0.00273,

  };

  static final byte[] merargs={
    (byte)0,  (byte)3,
    (byte)3,  (byte)1,  (byte)1,(byte)-10,  (byte)3, (byte)11,  (byte)4,(byte)0,
    (byte)2,  (byte)2,  (byte)5, (byte)-5,  (byte)6,  (byte)2,
    (byte)3,  (byte)5,  (byte)1,(byte)-14,  (byte)2,  (byte)2,  (byte)3,(byte)1,
    (byte)3,  (byte)1,  (byte)1, (byte)-5,  (byte)2,  (byte)4,  (byte)3,(byte)0,
    (byte)1,  (byte)1,  (byte)6,  (byte)0,
    (byte)1,  (byte)2,  (byte)6,  (byte)0,
    (byte)3,  (byte)2,  (byte)1, (byte)-7,  (byte)2,  (byte)3,  (byte)3,(byte)0,
    (byte)1,  (byte)1,  (byte)5,  (byte)2,
    (byte)2,  (byte)1,  (byte)1, (byte)-4,  (byte)3,  (byte)2,
    (byte)1,  (byte)2,  (byte)5,  (byte)2,
    (byte)2,  (byte)2,  (byte)1, (byte)-5,  (byte)2,  (byte)2,
    (byte)1,  (byte)3,  (byte)5,  (byte)0,
    (byte)2,  (byte)4,  (byte)1,(byte)-10,  (byte)2,  (byte)1,
    (byte)2,  (byte)3,  (byte)1, (byte)-8,  (byte)2,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-3,  (byte)2,  (byte)2,
    (byte)2,  (byte)1,  (byte)1, (byte)-2,  (byte)2,  (byte)2,
    (byte)1,  (byte)1,  (byte)3,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-7,  (byte)2,  (byte)1,
    (byte)2,  (byte)1,  (byte)1, (byte)-3,  (byte)3,  (byte)0,
    (byte)1,  (byte)1,  (byte)2,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-4,  (byte)2,  (byte)1,
    (byte)2,  (byte)4,  (byte)1, (byte)-9,  (byte)2,  (byte)0,
    (byte)1,  (byte)2,  (byte)3,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-2,  (byte)3,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-4,  (byte)2,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-1,  (byte)2,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-6,  (byte)2,  (byte)0,
    (byte)1,  (byte)3,  (byte)3,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-7,  (byte)2,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-2,  (byte)4,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-1,  (byte)3,  (byte)0,
    (byte)1,  (byte)2,  (byte)2,  (byte)2,
    (byte)2,  (byte)2,  (byte)1, (byte)-3,  (byte)2,  (byte)2,
    (byte)2,  (byte)4,  (byte)1, (byte)-8,  (byte)2,  (byte)0,
    (byte)2,  (byte)3,  (byte)1,(byte)-10,  (byte)2,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-4,  (byte)5,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-3,  (byte)5,  (byte)2,
    (byte)2,  (byte)1,  (byte)1, (byte)-5,  (byte)2,  (byte)2,
    (byte)2,  (byte)1,  (byte)1, (byte)-5,  (byte)6,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-2,  (byte)5,  (byte)1,
    (byte)3,  (byte)1,  (byte)1, (byte)-4,  (byte)5,  (byte)5,  (byte)6,(byte)0,
    (byte)1,  (byte)4,  (byte)3,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-3,  (byte)6,  (byte)1,
    (byte)2,  (byte)1,  (byte)1, (byte)-1,  (byte)5,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-2,  (byte)6,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-1,  (byte)6,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-2,  (byte)7,  (byte)0,
    (byte)2,  (byte)1,  (byte)1, (byte)-1,  (byte)7,  (byte)0,
    (byte)3,  (byte)4,  (byte)1,(byte)-14,  (byte)2,  (byte)2,  (byte)3,(byte)0,
    (byte)3,  (byte)1,  (byte)1,  (byte)2,  (byte)5, (byte)-5,  (byte)6,(byte)0,
    (byte)1,  (byte)1,  (byte)1,  (byte)6,
    (byte)3,  (byte)2,  (byte)1,(byte)-10,  (byte)3, (byte)11,  (byte)4,(byte)0,
    (byte)3,  (byte)1,  (byte)1, (byte)-2,  (byte)5,  (byte)5,  (byte)6,(byte)0,
    (byte)3,  (byte)6,  (byte)1,(byte)-14,  (byte)2,  (byte)2,  (byte)3,(byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)1,  (byte)6,  (byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)2,  (byte)6,  (byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)1,  (byte)5,  (byte)1,
    (byte)2,  (byte)2,  (byte)1, (byte)-4,  (byte)3,  (byte)1,
    (byte)2,  (byte)1,  (byte)1,  (byte)2,  (byte)5,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-5,  (byte)2,  (byte)2,
    (byte)2,  (byte)1,  (byte)1,  (byte)3,  (byte)5,  (byte)0,
    (byte)2,  (byte)5,  (byte)1,(byte)-10,  (byte)2,  (byte)0,
    (byte)1,  (byte)3,  (byte)2,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-2,  (byte)2,  (byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)1,  (byte)3,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-7,  (byte)2,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-3,  (byte)3,  (byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)1,  (byte)2,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-4,  (byte)2,  (byte)0,
    (byte)2,  (byte)5,  (byte)1, (byte)-9,  (byte)2,  (byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)2,  (byte)3,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-2,  (byte)3,  (byte)0,
    (byte)1,  (byte)4,  (byte)2,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-1,  (byte)2,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-6,  (byte)2,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-2,  (byte)4,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-1,  (byte)3,  (byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)2,  (byte)2,  (byte)1,
    (byte)2,  (byte)3,  (byte)1, (byte)-3,  (byte)2,  (byte)0,
    (byte)2,  (byte)5,  (byte)1, (byte)-8,  (byte)2,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-3,  (byte)5,  (byte)0,
    (byte)1,  (byte)5,  (byte)2,  (byte)1,
    (byte)2,  (byte)2,  (byte)1, (byte)-2,  (byte)5,  (byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)4,  (byte)3,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-3,  (byte)6,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-1,  (byte)5,  (byte)0,
    (byte)2,  (byte)2,  (byte)1, (byte)-2,  (byte)6,  (byte)0,
    (byte)1,  (byte)2,  (byte)1,  (byte)4,
    (byte)2,  (byte)2,  (byte)1,  (byte)1,  (byte)5,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-4,  (byte)3,  (byte)0,
    (byte)2,  (byte)2,  (byte)1,  (byte)2,  (byte)5,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-5,  (byte)2,  (byte)2,
    (byte)2,  (byte)1,  (byte)1,  (byte)3,  (byte)2,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-2,  (byte)2,  (byte)1,
    (byte)2,  (byte)3,  (byte)1, (byte)-3,  (byte)3,  (byte)0,
    (byte)2,  (byte)2,  (byte)1,  (byte)1,  (byte)2,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-4,  (byte)2,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-2,  (byte)3,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-1,  (byte)2,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-1,  (byte)3,  (byte)0,
    (byte)2,  (byte)2,  (byte)1,  (byte)2,  (byte)2,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-3,  (byte)2,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-3,  (byte)5,  (byte)0,
    (byte)2,  (byte)1,  (byte)1,  (byte)5,  (byte)2,  (byte)1,
    (byte)2,  (byte)3,  (byte)1, (byte)-2,  (byte)5,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-1,  (byte)5,  (byte)0,
    (byte)2,  (byte)3,  (byte)1, (byte)-2,  (byte)6,  (byte)0,
    (byte)1,  (byte)3,  (byte)1,  (byte)3,
    (byte)2,  (byte)4,  (byte)1, (byte)-4,  (byte)3,  (byte)0,
    (byte)2,  (byte)5,  (byte)1, (byte)-5,  (byte)2,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-2,  (byte)2,  (byte)0,
    (byte)2,  (byte)5,  (byte)1, (byte)-4,  (byte)2,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-2,  (byte)3,  (byte)0,
    (byte)2,  (byte)5,  (byte)1, (byte)-3,  (byte)2,  (byte)0,
    (byte)2,  (byte)2,  (byte)1,  (byte)5,  (byte)2,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-2,  (byte)5,  (byte)0,
    (byte)2,  (byte)4,  (byte)1, (byte)-1,  (byte)5,  (byte)0,
    (byte)1,  (byte)4,  (byte)1,  (byte)3,
    (byte)2,  (byte)6,  (byte)1, (byte)-5,  (byte)2,  (byte)1,
    (byte)2,  (byte)5,  (byte)1, (byte)-2,  (byte)2,  (byte)0,
    (byte)2,  (byte)5,  (byte)1, (byte)-2,  (byte)5,  (byte)0,
    (byte)1,  (byte)5,  (byte)1,  (byte)3,
    (byte)2,  (byte)7,  (byte)1, (byte)-5,  (byte)2,  (byte)0,
    (byte)1,  (byte)6,  (byte)1,  (byte)3,
    (byte)1,  (byte)7,  (byte)1,  (byte)3,
    (byte)1,  (byte)8,  (byte)1,  (byte)2,
    (byte)1,  (byte)9,  (byte)1,  (byte)2,
    (byte)1, (byte)10,  (byte)1,  (byte)1,
    (byte)1, (byte)11,  (byte)1,  (byte)0,
   (byte)-1
  };
  /* Total terms = 130, small = 128 */
  static Plantbl mer404=new Plantbl(
                            new short[]{11, 14, 10, 11,  4,  5,  2,  0,  0},
                            (short)6,merargs,mertabl,mertabb,mertabr,
                            3.8709830979999998e-01);
}
