package com.mapconductor.example.pages.stores

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import java.io.Serializable

/**
 * This example uses publicly available business addresses (e.g., Starbucks) and geocodes them
 * using the U.S. Census Bureau Geocoding API.
 * No personally identifiable information (PII) is used or inferred.
 */
data class StoreInfo(
    val name: String,
    val address: String,
    val instore: Boolean,
    val driveThrough: Boolean,
    val onlyReserved: Boolean,
    val store: String,
) : Serializable

val StarbucksHI_list =
    listOf(
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.647441446388,
                    longitude = -158.062544988096,
                ),
            extra =
                StoreInfo(
                    name = "Pupukea (North Shore)",
                    address = "59-720 Kamehameha Highway, Haleiwa, HI 96712",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_bean",
                ),
            icon =
                DefaultIcon(
                    label = "店",
                    labelTextColor = Color.Black,
                    labelTextSize = 13.sp,
                    fillColor = Color.Red,
                    strokeColor = Color.White,
                    strokeWidth = 1.dp,
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.33310051533,
                    longitude = -157.922371535818,
                ),
            extra =
                StoreInfo(
                    name = "Honolulu Airport (HNL) – Main",
                    address = "300 Rogers Blvd, Honolulu, HI 96820",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_bean",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.378981027427,
                    longitude = -157.930536387573,
                ),
            extra =
                StoreInfo(
                    name = "Aiea Shopping Center",
                    address = "99-115 Aiea Heights Drive #125, Aiea, HI 96701",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.38441101519,
                    longitude = -157.944839558127,
                ),
            extra =
                StoreInfo(
                    name = "Pearlridge Center",
                    address = "98-125 Kaonohi Street, Aiea, HI 96701",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.363785189939,
                    longitude = -157.928412704343,
                ),
            extra =
                StoreInfo(
                    name = "Stadium Marketplace",
                    address = "4561 Salt Lake Boulevard, Aiea, HI 96818",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.386340299119,
                    longitude = -157.941897795274,
                ),
            extra =
                StoreInfo(
                    name = "Pearlridge Mall",
                    address = "98-1005 Moanalua Road, Aiea, HI 96701",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_extra",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 19.69971686484,
                    longitude = -155.067322812851,
                ),
            extra =
                StoreInfo(
                    name = "Waiakea Center (Hilo)",
                    address = "315-325 Makaala Street, Hilo, HI 96720",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 19.695097953188,
                    longitude = -155.06690203818,
                ),
            extra =
                StoreInfo(
                    name = "Prince Kuhio Plaza (Hilo)",
                    address = "111 East Puainako Street, Hilo, HI 96720",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 19.719877684807,
                    longitude = -155.082770375139,
                ),
            extra =
                StoreInfo(
                    name = "Downtown Hilo (Kilauea Ave)",
                    address = "438 Kilauea Ave, Hilo, HI 96720",
                    instore = true,
                    driveThrough = true,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.33593,
                    longitude = -157.91581,
                ),
            extra =
                StoreInfo(
                    name = "Airport Trade Center",
                    address = "Airport Trade Center, 550 Paiea St, Honolulu, HI 96819",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.307358712377,
                    longitude = -157.865194116049,
                ),
            extra =
                StoreInfo(
                    name = "Aloha Tower",
                    address = "1 Aloha Tower Drive, Honolulu, HI 96813",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_extra",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.30846253,
                    longitude = -157.8614898,
                ),
            extra =
                StoreInfo(
                    name = "Bishop (Downtown)",
                    address = "1000 Bishop Street #104, Honolulu, HI 96813",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_extra",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.307604966533,
                    longitude = -157.860743724617,
                ),
            extra =
                StoreInfo(
                    name = "Pickup – King & Alakea",
                    address = "220 South King Street, Honolulu, HI 96813",
                    instore = false,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "honolulu_coffee",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.285300825278,
                    longitude = -157.83841421971,
                ),
            extra =
                StoreInfo(
                    name = "Discovery Bay Center",
                    address = "1778 Ala Moana Boulevard, Honolulu, HI 96815",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_extra",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.334058693598,
                    longitude = -158.023228524098,
                ),
            extra =
                StoreInfo(
                    name = "Ewa Beach – Laulani Village",
                    address = "91-1401 Fort Weaver Road, Ewa Beach, HI 96706",
                    instore = true,
                    driveThrough = true,
                    onlyReserved = false,
                    store = "coffee_bean",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.280578442859,
                    longitude = -157.828071689214,
                ),
            extra =
                StoreInfo(
                    name = "DFS (Duty Free) Waikiki",
                    address = "330 Royal Hawaiian Avenue, Honolulu, HI 96815",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_extra",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.308557010703,
                    longitude = -157.862582769768,
                ),
            extra =
                StoreInfo(
                    name = "Financial Plaza (Downtown)",
                    address = "130 Merchant Street #111, Honolulu, HI 96813",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "honolulu_coffee",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.282048,
                    longitude = -157.713041,
                ),
            extra =
                StoreInfo(
                    name = "Hawaii Kai Town Center",
                    address = "6700 Kalanianaole Highway, Honolulu, HI 96825",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.291792650634,
                    longitude = -157.849735879475,
                ),
            extra =
                StoreInfo(
                    name = "Hokua (Ala Moana)",
                    address = "1288 Ala Moana Blvd, Honolulu, HI 96814",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_bean",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.335246981366,
                    longitude = -157.868748238078,
                ),
            extra =
                StoreInfo(
                    name = "Kamehameha Shopping Center",
                    address = "1620 North School Street, Honolulu, HI 96817",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.27852422,
                    longitude = -157.7875773,
                ),
            extra =
                StoreInfo(
                    name = "Kahala Mall",
                    address = "4211 Waialae Avenue, Honolulu, HI 96816",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.279056707748,
                    longitude = -157.813890137018,
                ),
            extra =
                StoreInfo(
                    name = "Kapahulu Avenue",
                    address = "625 Kapahulu Avenue, Honolulu, HI 96815",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "coffee_extra",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.276148191143,
                    longitude = -157.704922547261,
                ),
            extra =
                StoreInfo(
                    name = "Koko Marina Center",
                    address = "7192 Kalanianaole Highway, Honolulu, HI 96825",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.30985278855,
                    longitude = -157.810260198584,
                ),
            extra =
                StoreInfo(
                    name = "Manoa Valley",
                    address = "2902 East Manoa Road, Honolulu, HI 96822",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "starbucks",
                ),
        ),
        MarkerState(
            position =
                GeoPoint(
                    latitude = 21.289750395336,
                    longitude = -157.843910788044,
                ),
            extra =
                StoreInfo(
                    name = "Macy’s Ala Moana Center",
                    address = "1450 Ala Moana Boulevard, Honolulu, HI 96814",
                    instore = true,
                    driveThrough = false,
                    onlyReserved = false,
                    store = "honolulu_coffee",
                ),
        ),
    )
