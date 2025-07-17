package com.mapconductor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.icons.FlagIcon
import android.os.Bundle

/**
 * This example uses publicly available business addresses (e.g., Starbucks) and geocodes them
 * using the U.S. Census Bureau Geocoding API.
 * No personally identifiable information (PII) is used or inferred.
 */
val StarbucksHI_list =
    listOf(
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.647441446388,
                    longitude = -158.062544988096,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Pupukea (North Shore)")
                    putString("address", "59-720 Kamehameha Highway, Haleiwa, HI 96712")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
            icon =
                DefaultIcon(
                    label = "店",
                    labelTextColor = Color.Black,
                    labelTextSize = 13.sp,
                    fillColor = Color.Red,
                    strokeColor = Color.White,
                    strokeWidth = 1.dp,
                )
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.33310051533,
                    longitude = -157.922371535818,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Honolulu Airport (HNL) – Main")
                    putString("address", "300 Rogers Blvd, Honolulu, HI 96820")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
            icon = FlagIcon(
                fillColor = Color.Red,
                strokeColor = Color.White,
                strokeWidth = 0.5.dp,
            )
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.378981027427,
                    longitude = -157.930536387573,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Aiea Shopping Center")
                    putString("address", "99-115 Aiea Heights Drive #125, Aiea, HI 96701")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
//            icon =
//                MarkerIcon.Companion.StarInCircle(
//                    fillColor = 0xFFFFD800.toInt(),
//                    strokeColor = Color.WHITE,
//                    strokeWidth = 2f,
//                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.38441101519,
                    longitude = -157.944839558127,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Pearlridge Center")
                    putString("address", "98-125 Kaonohi Street, Aiea, HI 96701")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
//            icon =
//                MarkerIcon.Companion.Triangle(
//                    outsideColor = 0xFF008000.toInt(),
//                    strokeWidth = 2f,
//                    triangleHeight = 24f,
//                    triangleWidth = 24f,
//                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.363785189939,
                    longitude = -157.928412704343,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Stadium Marketplace")
                    putString("address", "4561 Salt Lake Boulevard, Aiea, HI 96818")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.386340299119,
                    longitude = -157.941897795274,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Pearlridge Mall")
                    putString("address", "98-1005 Moanalua Road, Aiea, HI 96701")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 19.69971686484,
                    longitude = -155.067322812851,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Waiakea Center (Hilo)")
                    putString("address", "315-325 Makaala Street, Hilo, HI 96720")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 19.695097953188,
                    longitude = -155.06690203818,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Prince Kuhio Plaza (Hilo)")
                    putString("address", "111 East Puainako Street, Hilo, HI 96720")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 19.719877684807,
                    longitude = -155.082770375139,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Downtown Hilo (Kilauea Ave)")
                    putString("address", "438 Kilauea Ave, Hilo, HI 96720")
                    putBoolean("instore", true)
                    putBoolean("drive_through", true)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.33593,
                    longitude = -157.91581,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Airport Trade Center")
                    putString("address", "Airport Trade Center, 550 Paiea St, Honolulu, HI 96819")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.307358712377,
                    longitude = -157.865194116049,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Aloha Tower")
                    putString("address", "1 Aloha Tower Drive, Honolulu, HI 96813")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.30846253,
                    longitude = -157.8614898,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Bishop (Downtown)")
                    putString("address", "1000 Bishop Street #104, Honolulu, HI 96813")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.307604966533,
                    longitude = -157.860743724617,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Pickup – King & Alakea")
                    putString("address", "220 South King Street, Honolulu, HI 96813")
                    putBoolean("instore", false)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.285300825278,
                    longitude = -157.83841421971,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Discovery Bay Center")
                    putString("address", "1778 Ala Moana Boulevard, Honolulu, HI 96815")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.334058693598,
                    longitude = -158.023228524098,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Ewa Beach – Laulani Village")
                    putString("address", "91-1401 Fort Weaver Road, Ewa Beach, HI 96706")
                    putBoolean("instore", true)
                    putBoolean("drive_through", true)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.280578442859,
                    longitude = -157.828071689214,
                ),
            extra =
                Bundle().apply {
                    putString("name", "DFS (Duty Free) Waikiki")
                    putString("address", "330 Royal Hawaiian Avenue, Honolulu, HI 96815")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.308557010703,
                    longitude = -157.862582769768,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Financial Plaza (Downtown)")
                    putString("address", "130 Merchant Street #111, Honolulu, HI 96813")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.282048,
                    longitude = -157.713041,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Hawaii Kai Town Center")
                    putString("address", "6700 Kalanianaole Highway, Honolulu, HI 96825")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.291792650634,
                    longitude = -157.849735879475,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Hokua (Ala Moana)")
                    putString("address", "1288 Ala Moana Blvd, Honolulu, HI 96814")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.335246981366,
                    longitude = -157.868748238078,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kamehameha Shopping Center")
                    putString("address", "1620 North School Street, Honolulu, HI 96817")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.27852422,
                    longitude = -157.7875773,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kahala Mall")
                    putString("address", "4211 Waialae Avenue, Honolulu, HI 96816")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.294505444307,
                    longitude = -157.841946089363,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Keeaumoku (WalMart)")
                    putString("address", "678 Keeaumoku Street #106, Honolulu, HI 96814")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.279056707748,
                    longitude = -157.813890137018,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kapahulu Avenue")
                    putString("address", "625 Kapahulu Avenue, Honolulu, HI 96815")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.276148191143,
                    longitude = -157.704922547261,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Koko Marina Center")
                    putString("address", "7192 Kalanianaole Highway, Honolulu, HI 96825")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.30985278855,
                    longitude = -157.810260198584,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Manoa Valley")
                    putString("address", "2902 East Manoa Road, Honolulu, HI 96822")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.289750395336,
                    longitude = -157.843910788044,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Macy’s Ala Moana Center")
                    putString("address", "1450 Ala Moana Boulevard, Honolulu, HI 96814")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.341260775481,
                    longitude = -157.929507250967,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Moanalua Shopping Center")
                    putString("address", "930 Valkenburgh Street, Honolulu, HI 96818")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.278908517307,
                    longitude = -157.832413265507,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Outrigger Reef (Waikiki)")
                    putString("address", "2169 Kalia Road #102, Honolulu, HI 96815")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.279011840151,
                    longitude = -157.825557564916,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Ohana Waikiki West")
                    putString("address", "2330 Kuhio Avenue, Honolulu, HI 96815")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.30278387167,
                    longitude = -157.879450611886,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Sand Island")
                    putString("address", "120 Sand Island Access Road #4, Honolulu, HI 96819")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.289750395336,
                    longitude = -157.843910788044,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Sears Ala Moana Center")
                    putString("address", "1450 Ala Moana Blvd, Honolulu, HI 96814")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.2786604527,
                    longitude = -157.828371626919,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Waikiki Shopping Plaza")
                    putString("address", "2270 Kalakaua Avenue #1800, Honolulu, HI 96815")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.280533970958,
                    longitude = -157.82749796628,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Waikiki Trade Center (Reserve Bar)")
                    putString("address", "2255 Kuhio Avenue #S-1, Honolulu, HI 96815")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", true)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.293774306726,
                    longitude = -157.85297798269,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Ward Entertainment Center")
                    putString("address", "310 Kamakee Street #6, Honolulu, HI 96814")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.406095,
                    longitude = -157.800761,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Windward City Shopping Center")
                    putString("address", "45-480 Kaneohe Bay Drive, Kaneohe, HI 96744")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.279515520356,
                    longitude = -157.829265712704,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Waikiki Walk")
                    putString("address", "2222 Kalakaua Avenue, Honolulu, HI 96815")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.293340364607,
                    longitude = -157.85256477721,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Ward Gateway")
                    putString("address", "1142 Auahi Street, Honolulu, HI 96814")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.888659282451,
                    longitude = -156.477197459052,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Queen Kaahumanu Center")
                    putString("address", "275 West Kaahumanu Avenue #1200, Kahului, HI 96732")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.881960703032,
                    longitude = -156.45511618549,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Maui Marketplace")
                    putString("address", "270 Dairy Road, Kahului, HI 96732")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.393471214679,
                    longitude = -157.740438744365,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kailua Village")
                    putString("address", "539 Kailua Road, Kailua, HI 96734")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 19.65018280057,
                    longitude = -155.987752998108,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kona Coast Shopping Center")
                    putString("address", "74-5588 Palani Road, Kailua-Kona, HI 96740")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.020593379111,
                    longitude = -155.668585540658,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Parker Ranch Center")
                    putString("address", "67-1185 Mamalahoa Highway #D108, Kamuela, HI 96743")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.344027896932,
                    longitude = -158.11830127628,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Halekuai Center")
                    putString("address", "563 Farrington Highway #101, Kapolei, HI 96707")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.328579072139,
                    longitude = -158.086506230214,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kapolei Parkway & Kamokila")
                    putString("address", "338 Kamokila Boulevard #108, Kapolei, HI 96797")
                    putBoolean("instore", true)
                    putBoolean("drive_through", true)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.734513943855,
                    longitude = -156.452970465534,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kukui Mall")
                    putString("address", "1819 South Kihei Road, Kihei, HI 96738")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.750703062197,
                    longitude = -156.451408824978,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Piilani Village Shopping Center")
                    putString("address", "247 Piikea Avenue #106, Kihei, HI 96753")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 19.94001271876,
                    longitude = -155.856842731652,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Mauna Lani (Kohala Coast)")
                    putString("address", "68-1330 Mauna Lani Drive #H-101B, Kohala Coast, HI 96743")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.886244,
                    longitude = -156.684697,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Lahaina Cannery Mall")
                    putString("address", "1221 Honoapiilani Highway, Lahaina, HI 96761")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.877708110758,
                    longitude = -156.679031878844,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Lahaina (Front Street)")
                    putString("address", "845 Wainee Street, Lahaina, HI 96761")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.969553724378,
                    longitude = -159.388283368972,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kukui Grove Center")
                    putString("address", "3-2600 Kaumualii Highway #A8, Lihue, HI 96766")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.889156401906,
                    longitude = -156.449318101378,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kahului Airport (OGG)")
                    putString("address", "1 Keolani Place, Kahului, HI 96732")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.458431746129,
                    longitude = -158.015862355331,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Mililani Shopping Center")
                    putString("address", "95-221 Kipapa Drive, Mililani, HI 96789")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.453888574294,
                    longitude = -158.007690940987,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Mililani Town Center")
                    putString("address", "95-1249 Meheula Parkway, Mililani, HI 96789")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 20.838230357792,
                    longitude = -156.342698446307,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Pukalani Foodland Center")
                    putString("address", "55 Pukalani Street, Pukalani, HI 96768")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.378675,
                    longitude = -157.728499,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Enchanted Lake Center (Kailua)")
                    putString("address", "1020 Keolu Drive, Kailua, HI 96734")
                    putBoolean("instore", true)
                    putBoolean("drive_through", true)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.389003512015,
                    longitude = -158.033431400538,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kunia Shopping Center")
                    putString("address", "94-673 Kupuohi Street, Waipahu, HI 96797")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.401138284746,
                    longitude = -158.010288643364,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Waikele Center")
                    putString("address", "94-799 Lumiaina Street, Waipahu, HI 96797")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.881002766882,
                    longitude = -159.457726341723,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Poipu Shopping Village")
                    putString("address", "2360 Kiahuna Plantation Drive #E70, Koloa, HI 96756")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.966834230955,
                    longitude = -159.381526209527,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Safeway Lihue")
                    putString("address", "4454 Nuhou Street, Lihue, HI 96766")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.970935289068,
                    longitude = -159.375643172372,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Target Lihue (Kauai)")
                    putString("address", "4303 Nawiliwili Road, Lihue, HI 96766")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 22.061786387888,
                    longitude = -159.320539848567,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Kauai Village SC (Kapaa)")
                    putString("address", "4-831 Kuhio Highway #208, Kapaa, HI 96746")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.32874860701,
                    longitude = -158.091318912219,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Target Kapolei")
                    putString("address", "4450 Kapolei Parkway, Kapolei, HI 96707")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.340541119127,
                    longitude = -158.124703887408,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Ko Olina Station")
                    putString("address", "92-1047 Olani Street, Kapolei, HI 96707")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.328151560875,
                    longitude = -158.021804173199,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Laulani Village (Ewa Beach)")
                    putString("address", "91-1105 Keaunui Drive #500, Ewa Beach, HI 96706")
                    putBoolean("instore", true)
                    putBoolean("drive_through", true)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.34213,
                    longitude = -157.95157,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Hickam AFB (Base Access)")
                    putString("address", "Bldg B-1250, Hickam AFB, Honolulu, HI 96853")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.349070820935,
                    longitude = -157.932730132699,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Pearl Harbor NEX")
                    putString("address", "4725 Bougainville Drive, Honolulu, HI 96818")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.356011085979,
                    longitude = -157.893896231076,
                ),
            extra =
                Bundle().apply {
                    putString("name", "Tripler Army Medical Center")
                    putString("address", "1 Jarrett White Road, Honolulu, HI 96859")
                    putBoolean("instore", true)
                    putBoolean("drive_through", false)
                    putBoolean("only_reserved", false)
                },
        ),
    )
