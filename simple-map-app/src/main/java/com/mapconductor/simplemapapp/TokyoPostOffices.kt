package com.mapconductor.example.pages.marker.postoffice

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import java.io.Serializable

data class PostOffice(
    val position: GeoPoint,
    val name: String,
    val address: String,
) : Serializable

val TokyoPostOffices =
    listOf<PostOffice>(
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691153, 139.756878),
            name = "Palace Side Building Post Office",
            address = "Hitotsubashi 1-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.686347, 139.739268),
            name = "Chiyoda Ichiban-cho Post Office",
            address = "Ichiban-cho10-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.678782, 139.741565),
            name = "Town and Village Hall Post Office",
            address = "Nagata-cho1-11-32",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674765, 139.744324),
            name = "Diet Building Post Office",
            address = "Nagata-cho1-7-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673238, 139.740213),
            name = "Sanno Park Tower Post Office",
            address = "Nagata-cho2-11-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676487, 139.738158),
            name = "Sanno Grand Building Post Office",
            address = "Nagata-cho2-14-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674877, 139.752601),
            name = "Tokyo High Court Post Office",
            address = "Kasumigaseki1-1-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674377, 139.751934),
            name = "Kasumigaseki Post Office",
            address = "Kasumigaseki1-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671266, 139.75124),
            name = "Chiyoda Kasumigaseki Post Office",
            address = "Kasumigaseki1-3-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67546, 139.751073),
            name = "Second Kasumigaseki Post Office",
            address = "Kasumigaseki2-1-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671476, 139.746956),
            name = "Kasumigaseki Building Post Office",
            address = "Kasumigaseki3-2-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69972, 139.772399),
            name = "Akihabara UDX Post Office",
            address = "Soto-Kanda4-14-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.683765, 139.7656),
            name = "Marunouchi Center Building Post Office",
            address = "Marunouchi1-6-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.683126, 139.770294),
            name = "Tekko BuildingPost Office (Temporarily Closed)",
            address = "Marunouchi1-8-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.679849, 139.764766),
            name = "Tokyo Central Post Office",
            address = "Marunouchi2-7-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.678154, 139.761795),
            name = "Chiyoda Marunouchi Post Office",
            address = "Marunouchi3-2-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693875, 139.777127),
            name = "Chiyoda Iwamoto-cho Post Office",
            address = "Iwamoto-cho2-11-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68113, 139.733598),
            name = "Hotel New Otani Post Office",
            address = "Kioicho4-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695458, 139.752434),
            name = "Kudan Post Office",
            address = "Kudan-minami1-4-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.692124, 139.740351),
            name = "Kojimachi Post Office",
            address = "Kudan-minami4-5-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.684986, 139.741518),
            name = "Hanzomon Station Post Office",
            address = "Kojimachi2-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.683153, 139.736685),
            name = "海事BuildingPost Office",
            address = "Kojimachi4-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.684347, 139.733324),
            name = "Kojimachi本通Post Office",
            address = "Kojimachi5-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700207, 139.754794),
            name = "Kanda Misaki-cho Post Office",
            address = "Misaki-cho2-2-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68868, 139.736241),
            name = "Chiyoda Yonban-cho Post Office",
            address = "Yonban-cho4-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691236, 139.763572),
            name = "Kanda Nishiki-cho Post Office",
            address = "KandaNishiki-cho1-17-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700196, 139.762477),
            name = "Kanda Surugadai Post Office",
            address = "KandaSurugadai2-3-45",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696312, 139.764595),
            name = "新御茶ノ水Station FrontPost Office",
            address = "KandaSurugadai3-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696152, 139.762072),
            name = "Ogawa-machi Post Office",
            address = "KandaOgawa-machi3-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695208, 139.758683),
            name = "Kanda Minami-Jimbo-cho Post Office",
            address = "KandaJimbo-cho1-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69793, 139.759044),
            name = "Kanda Kita-Jimbo-cho Post Office（ (Temporarily Closed)）",
            address = "KandaJimbo-cho1-36",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69543, 139.774293),
            name = "Kanda Suda-cho Post Office",
            address = "KandaSuda-cho2-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693347, 139.769599),
            name = "Kanda Station Post Office",
            address = "KandaTa-cho2-2-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695402, 139.765766),
            name = "Kanda Awaji-cho Post Office",
            address = "KandaAwaji-cho1-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697458, 139.768905),
            name = "Kanda Post Office",
            address = "KandaAwaji-cho2-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68382, 139.753823),
            name = "宮庁Post Office",
            address = "Chiyoda1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.689455, 139.763063),
            name = "Otemachi Ichi Post Office",
            address = "Otemachi1-3-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68632, 139.764183),
            name = "Otemachi Building Post Office",
            address = "Otemachi1-6-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687857, 139.764204),
            name = "ＫＤＤＩOtemachi Building Post Office",
            address = "Otemachi1-8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.685348, 139.769988),
            name = "Nihon Building Post Office",
            address = "Otemachi2-6-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690542, 139.77196),
            name = "Kanda Imagawabashi Post Office",
            address = "Kaji-cho1-7-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693514, 139.771738),
            name = "Chiyoda Kaji-cho Post Office",
            address = "Kaji-cho2-11-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.694792, 139.782627),
            name = "Higashi-Kanda Ichi Post Office",
            address = "Higashi-Kanda1-15-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67221, 139.758878),
            name = "Imperial Hotel Post Office",
            address = "Uchisaiwai-cho1-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671099, 139.757656),
            name = "ＮＴＴHibiyaBuildingPost Office",
            address = "Uchisaiwai-cho1-1-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.679931, 139.742685),
            name = "Supreme Court Post Office",
            address = "Hayabusa-cho4-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699346, 139.749322),
            name = "KojimachiIidabashi通Post Office",
            address = "Iidabashi2-7-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699271, 139.744682),
            name = "Iidabashi Post Office",
            address = "Fujimi2-10-43",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696678, 139.742598),
            name = "Kojimachi Post OfficeHigashi京Teishin Hospital Branch",
            address = "Fujimi2-14-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.680727, 139.739999),
            name = "Zenkyoren Building Post Office（ (Temporarily Closed)）",
            address = "Hirakawa-cho2-7-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67907, 139.739102),
            name = "Prefectural Hall Post Office",
            address = "Hirakawa-cho2-6-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.675988, 139.761267),
            name = "Dai-ichi Life Building Post Office",
            address = "Yurakucho1-13-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674932, 139.764683),
            name = "Tokyo Kotsu Kaikan Post Office",
            address = "Yurakucho2-10-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.677649, 139.770115),
            name = "KyobashiNiPost Office（ (Temporarily Closed)）",
            address = "Kyobashi2-1-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.675488, 139.769822),
            name = "Kyobashi通Post Office",
            address = "Kyobashi3-6-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.672544, 139.770238),
            name = "GinzaIchiPost Office",
            address = "Ginza1-20-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673349, 139.767572),
            name = "Ginza通Post Office",
            address = "Ginza2-7-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670239, 139.769405),
            name = "GinzaSanPost Office",
            address = "Ginza3-14-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673266, 139.764989),
            name = "Ginza並木通Post Office",
            address = "Ginza3-2-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671294, 139.765406),
            name = "GinzaYonPost Office",
            address = "Ginza4-6-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66935, 139.764461),
            name = "GinzaRokuPost Office（ (Temporarily Closed)）",
            address = "Ginza6-11-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670563, 139.763388),
            name = "Ginzaみゆき通Post Office",
            address = "Ginza6-8-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667072, 139.765267),
            name = "GinzaNanaPost Office",
            address = "Ginza7-15-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.6651, 139.764045),
            name = "GinzaPost Office",
            address = "Ginza8-20-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669317, 139.758947),
            name = "GinzaNishiPost Office",
            address = "GinzaNishi8-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.661879, 139.781738),
            name = "Kyobashi月島Post Office",
            address = "月島4-1-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659707, 139.777743),
            name = "Central勝どきPost Office",
            address = "勝どき1-7-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659212, 139.773628),
            name = "Central勝どきSanPost Office",
            address = "勝どき3-13-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.677516, 139.782321),
            name = "Central新川Post Office",
            address = "新川1-9-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674669, 139.783734),
            name = "Central新川NiPost Office",
            address = "新川2-15-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673294, 139.775766),
            name = "新富Post Office",
            address = "新富1-19-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671905, 139.773877),
            name = "Central新富NiPost Office",
            address = "新富2-5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.656796, 139.78296),
            name = "晴海トリトンスクエアPost Office",
            address = "晴海1-8-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.653555, 139.779892),
            name = "晴海Post Office",
            address = "晴海4-6-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667294, 139.769211),
            name = "KyobashiPost Office",
            address = "Tsukiji4-2-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.661823, 139.767461),
            name = "CentralTsukijiPost Office",
            address = "Tsukiji5-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664656, 139.773322),
            name = "CentralTsukijiRokuPost Office",
            address = "Tsukiji6-8-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.668211, 139.785765),
            name = "リバーシティ２１Post Office",
            address = "佃2-2-6-101",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664629, 139.785599),
            name = "Central佃Post Office",
            address = "佃3-5-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695486, 139.785654),
            name = "両国Post Office",
            address = "HigashiNihon橋2-27-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691348, 139.78346),
            name = "HigashiNihon橋SanPost Office",
            address = "HigashiNihon橋3-4-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.683376, 139.777377),
            name = "Nihon橋Post Office",
            address = "Nihon橋1-18-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681702, 139.772696),
            name = "Nihon橋MinamiPost Office",
            address = "Nihon橋2-2-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.680794, 139.7772),
            name = "Nihon橋兜町Post Office（ (Temporarily Closed)）",
            address = "Nihon橋兜町12-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.678682, 139.778349),
            name = "Nihon橋茅場町Post Office",
            address = "Nihon橋茅場町2-4-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68611, 139.775315),
            name = "Nihon橋室町Post Office",
            address = "Nihon橋室町1-12-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687098, 139.772544),
            name = "Nihon橋San井BuildingPost Office",
            address = "Nihon橋室町2-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687459, 139.777238),
            name = "Nihon橋小舟町Post Office",
            address = "Nihon橋小舟町4-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691403, 139.77921),
            name = "小伝馬町Post Office",
            address = "Nihon橋小伝馬町10-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682765, 139.781265),
            name = "Nihon橋小網町Post Office",
            address = "Nihon橋小網町11-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.685737, 139.781905),
            name = "Nihon橋人形町Post Office",
            address = "Nihon橋人形町1-5-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.684599, 139.785543),
            name = "Central人形町NiPost Office",
            address = "Nihon橋人形町2-15-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690153, 139.780877),
            name = "Nihon橋大伝馬町Post Office",
            address = "Nihon橋大伝馬町12-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.679349, 139.786598),
            name = "ＩＢＭ箱崎BuildingPost Office",
            address = "Nihon橋箱崎町19-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681266, 139.786404),
            name = "Higashi京シティターミナルPost Office",
            address = "Nihon橋箱崎町22-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68982, 139.786794),
            name = "Central浜町IchiPost Office",
            address = "Nihon橋浜町1-5-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.685154, 139.78946),
            name = "Nihon橋浜町Post Office",
            address = "Nihon橋浜町3-25-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.688792, 139.774016),
            name = "新Nihon橋Station FrontPost Office",
            address = "Nihon橋本町3-3-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690487, 139.776377),
            name = "Nihon橋本町Post Office",
            address = "Nihon橋本町4-14-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.680562, 139.769381),
            name = "Hachi重洲地下街Post Office",
            address = "Hachi重洲2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676238, 139.775099),
            name = "CentralHachi丁堀Post Office",
            address = "Hachi丁堀2-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.653456, 139.769836),
            name = "Central豊海Post Office",
            address = "豊海町5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671489, 139.779015),
            name = "Central湊Post Office",
            address = "湊2-7-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667267, 139.778682),
            name = "聖路加ガーデンPost Office",
            address = "明石町8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.653851, 139.762212),
            name = "港竹芝Post Office",
            address = "海岸1-16-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669658, 139.74933),
            name = "虎ノ門Post Office",
            address = "虎ノ門1-7-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666905, 139.744046),
            name = "HotelオークラPost Office",
            address = "虎ノ門2-10-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664933, 139.747824),
            name = "港虎ノ門SanPost Office",
            address = "虎ノ門3-10-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664435, 139.745427),
            name = "神谷町Post Office",
            address = "虎ノ門4-1-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.626162, 139.742062),
            name = "品川インターシティPost Office",
            address = "港Minami2-15-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.628715, 139.744076),
            name = "港港MinamiPost Office",
            address = "港Minami2-4-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.638269, 139.74002),
            name = "泉岳寺Station FrontPost Office",
            address = "高輪2-20-30",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.637269, 139.733604),
            name = "高輪NiPost Office",
            address = "高輪2-4-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.631964, 139.731382),
            name = "高輪台Post Office",
            address = "高輪3-10-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.63077, 139.737909),
            name = "品川Station FrontPost Office",
            address = "高輪3-25-27",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652934, 139.744547),
            name = "San田国際BuildingPost Office",
            address = "San田1-4-28",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.64213, 139.74152),
            name = "高輪Post Office",
            address = "San田3-8-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.646907, 139.740464),
            name = "港San田YonPost Office",
            address = "San田4-1-31",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.650907, 139.754102),
            name = "芝IchiPost Office",
            address = "芝1-11-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652684, 139.749074),
            name = "芝SanPost Office",
            address = "芝3-4-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.647574, 139.74938),
            name = "港芝YonPost Office",
            address = "芝4-13-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.649598, 139.745024),
            name = "慶應義塾前Post Office",
            address = "芝5-13-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.646963, 139.746075),
            name = "港芝GoPost Office",
            address = "芝5-27-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.651704, 139.757795),
            name = "Higashi芝BuildingPost Office",
            address = "芝浦1-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.649935, 139.756935),
            name = "シーバンスＮBuildingPost Office",
            address = "芝浦1-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.64358, 139.74706),
            name = "港芝浦Post Office",
            address = "芝浦3-4-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.640228, 139.748025),
            name = "芝浦海岸通Post Office",
            address = "芝浦4-13-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657239, 139.751491),
            name = "芝公園Post Office",
            address = "芝公園1-8-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659517, 139.745102),
            name = "機械振興HallPost Office",
            address = "芝公園3-5-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659795, 139.754018),
            name = "芝大門Post Office",
            address = "芝大門1-1-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.656629, 139.753768),
            name = "港浜松町Post Office",
            address = "芝大門2-4-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667128, 139.761239),
            name = "新橋Post Office",
            address = "新橋1-6-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666329, 139.757389),
            name = "ニュー新橋BuildingPost Office",
            address = "新橋2-16-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664572, 139.753518),
            name = "新橋YonPost Office",
            address = "新橋4-30-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669411, 139.753337),
            name = "Nishi新橋Post Office",
            address = "Nishi新橋1-5-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662878, 139.75199),
            name = "芝Post Office",
            address = "Nishi新橋3-22-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660405, 139.724993),
            name = "Nishi麻布Post Office",
            address = "Nishi麻布1-8-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667294, 139.740075),
            name = "アーク森BuildingPost Office",
            address = "赤坂1-12-32",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670905, 139.74213),
            name = "小松BuildingPost Office",
            address = "赤坂2-3-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673043, 139.738602),
            name = "赤坂通Post Office",
            address = "赤坂2-6-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676182, 139.735852),
            name = "赤坂Ichiツ木通Post Office",
            address = "赤坂3-20-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67021, 139.732214),
            name = "赤坂NanaPost Office",
            address = "赤坂7-6-38",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673182, 139.726242),
            name = "赤坂Post Office",
            address = "赤坂8-4-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666203, 139.731259),
            name = "Higashi京ミッドタウンPost Office",
            address = "赤坂9-7-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.630243, 139.778184),
            name = "お台場海浜公園前Post Office",
            address = "台場1-5-4-301",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.665368, 139.761012),
            name = "汐留シティCenterPost Office",
            address = "Higashi新橋1-5-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66921, 139.716493),
            name = "外苑前Post Office",
            address = "Minami青山2-27-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648879, 139.737075),
            name = "Minami麻布NiPost Office",
            address = "Minami麻布2-6-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.651434, 139.723382),
            name = "Minami麻布GoPost Office",
            address = "Minami麻布5-16-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.646601, 139.730576),
            name = "港白金SanPost Office",
            address = "白金3-1-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.645657, 139.724577),
            name = "港白金Post Office（ (Temporarily Closed)）",
            address = "白金5-9-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.637936, 139.726632),
            name = "港白金台Post Office",
            address = "白金台3-2-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.65624, 139.75674),
            name = "世界貿易CenterPost Office",
            address = "浜松町2-4-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.655378, 139.73527),
            name = "麻布Ju番Post Office",
            address = "麻布Ju番2-3-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660739, 139.739741),
            name = "麻布Post Office",
            address = "麻布台1-6-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66385, 139.739103),
            name = "全特Roku本木BuildingPost Office",
            address = "Roku本木1-7-27",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660646, 139.729262),
            name = "Roku本木ヒルズPost Office",
            address = "Roku本木6-10-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662072, 139.732742),
            name = "Roku本木Station FrontPost Office",
            address = "Roku本木6-7-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666405, 139.727715),
            name = "乃木坂Station FrontPost Office（ (Temporarily Closed)）",
            address = "Roku本木7-3-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703121, 139.743268),
            name = "IidabashiStationHigashi口Post Office",
            address = "下宮比町3-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722767, 139.703901),
            name = "Shinjuku下落合SanPost Office",
            address = "下落合3-18-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.716759, 139.697842),
            name = "新目白通Post Office",
            address = "下落合4-1-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722036, 139.696243),
            name = "Shinjuku下落合YonPost Office",
            address = "下落合4-26-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.694095, 139.703521),
            name = "Shinjuku区役所Post Office",
            address = "歌舞伎町1-4-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697984, 139.702966),
            name = "Shinjuku歌舞伎町Post Office",
            address = "歌舞伎町2-41-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706539, 139.73449),
            name = "Shinjuku改代町Post Office",
            address = "改代町3-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701623, 139.714825),
            name = "Shinjuku戸山Post Office",
            address = "戸山2-10-101",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710899, 139.704493),
            name = "Shinjuku諏訪町Post Office",
            address = "高田馬場1-29-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713204, 139.707353),
            name = "高田馬場NiPost Office",
            address = "高田馬場2-14-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713121, 139.701632),
            name = "高田馬場Post Office",
            address = "高田馬場4-13-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709899, 139.694778),
            name = "Shinjuku小滝橋Post Office",
            address = "高田馬場4-40-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.688847, 139.723936),
            name = "Yotsuya通NiPost Office",
            address = "San栄町25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.694069, 139.735963),
            name = "Shinjuku保健HallPost Office",
            address = "市谷砂土原町1-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.698638, 139.726096),
            name = "市谷柳町Post Office（ (Temporarily Closed)）",
            address = "市谷柳町24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699984, 139.721019),
            name = "牛込若松町Post Office",
            address = "若松町6-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.692096, 139.721353),
            name = "Shinjuku住吉Post Office",
            address = "住吉町2-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713176, 139.686994),
            name = "Shinjuku上落合Post Office",
            address = "上落合2-23-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681708, 139.71977),
            name = "YotsuyaPost Office",
            address = "信濃町31",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.689346, 139.710187),
            name = "ShinjukuIchiPost Office",
            address = "Shinjuku1-14-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690652, 139.71402),
            name = "Shinjuku花園Post Office",
            address = "Shinjuku1-27-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690179, 139.707604),
            name = "ShinjukuNiPost Office",
            address = "Shinjuku2-11-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691679, 139.703799),
            name = "ShinjukuSanPost Office",
            address = "Shinjuku3-17-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695151, 139.707104),
            name = "Shinjuku明治通Post Office",
            address = "Shinjuku6-28-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701845, 139.739393),
            name = "Shinjuku神楽坂Post Office",
            address = "神楽坂4-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691401, 139.695772),
            name = "ShinjukuCenterBuildingPost Office",
            address = "NishiShinjuku1-25-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693023, 139.695348),
            name = "Shinjuku野村BuildingPost Office",
            address = "NishiShinjuku1-26-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690207, 139.696827),
            name = "ShinjukuPost Office",
            address = "NishiShinjuku1-8-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691929, 139.693911),
            name = "ShinjukuSan井BuildingPost Office",
            address = "NishiShinjuku2-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687513, 139.694856),
            name = "ＫＤＤＩBuildingPost Office",
            address = "NishiShinjuku2-3-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691595, 139.691161),
            name = "ShinjukuDai-ichi LifeBuildingPost Office",
            address = "NishiShinjuku2-7-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.689568, 139.691717),
            name = "Higashi京都庁Post Office",
            address = "NishiShinjuku2-8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682902, 139.686606),
            name = "Higashi京オペラシティPost Office",
            address = "NishiShinjuku3-20-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.685235, 139.691162),
            name = "ShinjukuParkTowerPost Office",
            address = "NishiShinjuku3-7-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69004, 139.685328),
            name = "NishiShinjukuYonPost Office",
            address = "NishiShinjuku4-4-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693456, 139.687634),
            name = "ShinjukuアイタウンPost Office",
            address = "NishiShinjuku6-21-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693012, 139.692883),
            name = "ShinjukuアイランドPost Office",
            address = "NishiShinjuku6-5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696595, 139.697883),
            name = "NishiShinjukuNanaPost Office",
            address = "NishiShinjuku7-7-28",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695151, 139.698411),
            name = "Shinjuku広小路Post Office",
            address = "NishiShinjuku7-9-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696734, 139.692883),
            name = "NishiShinjukuHachiPost Office",
            address = "NishiShinjuku8-8-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709924, 139.720614),
            name = "Nishi早稲田IchiPost Office",
            address = "Nishi早稲田1-8-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710205, 139.713575),
            name = "早稲田通Post Office",
            address = "Nishi早稲田3-14-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.720175, 139.680522),
            name = "ShinjukuNishi落合Post Office",
            address = "Nishi落合1-21-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708011, 139.722047),
            name = "早稲田大学前Post Office",
            address = "早稲田鶴巻町533",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701935, 139.706225),
            name = "Shinjuku大久保Post Office",
            address = "大久保2-13-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.7074, 139.708548),
            name = "ShinjukuKitaPost Office",
            address = "大久保3-14-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722647, 139.689327),
            name = "Shinjuku中落合Post Office",
            address = "中落合3-16-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705248, 139.73115),
            name = "Shinjuku天神Post Office",
            address = "天神町22-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.685728, 139.715763),
            name = "Yotsuya大木戸Post Office",
            address = "藤町1-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706067, 139.720464),
            name = "Shinjuku馬場下Post Office",
            address = "馬場下町61",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700683, 139.699981),
            name = "新大久保Station FrontPost Office",
            address = "百人町1-10-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706705, 139.696088),
            name = "Shinjuku百人町Post Office",
            address = "百人町3-28-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699651, 139.73113),
            name = "牛込Post Office",
            address = "Kita山伏町1-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701983, 139.692383),
            name = "KitaShinjukuSanPost Office",
            address = "KitaShinjuku3-9-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687097, 139.729686),
            name = "YotsuyaStation FrontPost Office",
            address = "本塩町3-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697984, 139.715409),
            name = "牛込抜弁天Post Office",
            address = "余丁町8-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.715844, 139.729324),
            name = "文京音羽Post Office",
            address = "音羽1-15-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710122, 139.729879),
            name = "文京関口IchiPost Office",
            address = "関口1-23-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703596, 139.75188),
            name = "Higashi京ドームシティPost Office",
            address = "後楽1-3-61",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.721422, 139.753444),
            name = "文京白山上Post Office",
            address = "向丘1-9-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.723566, 139.755405),
            name = "文京向丘NiPost Office",
            address = "向丘2-30-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718039, 139.758154),
            name = "文京向丘Post Office",
            address = "向丘2-3-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.719039, 139.763793),
            name = "文京根津Post Office",
            address = "根津1-17-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70829, 139.752127),
            name = "文京春日Post Office",
            address = "春日1-16-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714983, 139.750683),
            name = "小石川IchiPost Office",
            address = "小石川1-27-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713594, 139.742406),
            name = "小石川Post Office",
            address = "小石川4-4-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718399, 139.737323),
            name = "小石川GoPost Office",
            address = "小石川5-6-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709872, 139.736351),
            name = "文京水道Post Office",
            address = "水道2-14-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727426, 139.742044),
            name = "文京千石Post Office",
            address = "千石4-37-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727288, 139.763987),
            name = "文京千駄木SanPost Office",
            address = "千駄木3-41-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731204, 139.759876),
            name = "文京千駄木YonPost Office",
            address = "千駄木4-6-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.721121, 139.731768),
            name = "文京大塚NiPost Office",
            address = "大塚2-16-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.721788, 139.738323),
            name = "文京大塚SanPost Office",
            address = "大塚3-39-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725148, 139.730129),
            name = "文京大塚GoPost Office",
            address = "大塚5-7-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70068, 139.764071),
            name = "御茶ノ水Post Office",
            address = "湯島1-5-45",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705207, 139.766349),
            name = "湯島NiPost Office",
            address = "湯島2-21-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708623, 139.768876),
            name = "湯島YonPost Office",
            address = "湯島4-6-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.716983, 139.752433),
            name = "文京白山下Post Office",
            address = "白山1-11-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.726149, 139.747433),
            name = "文京白山GoPost Office",
            address = "白山5-18-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70495, 139.757114),
            name = "本郷IchiPost Office",
            address = "本郷1-27-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706012, 139.763405),
            name = "本郷SanPost Office",
            address = "本郷3-25-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.707706, 139.759905),
            name = "本郷YonPost Office",
            address = "本郷4-2-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.711067, 139.755544),
            name = "本郷GoPost Office",
            address = "本郷5-9-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.712317, 139.759238),
            name = "本郷Post Office",
            address = "本郷6-1-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.711706, 139.760793),
            name = "Higashi京大学Post Office",
            address = "本郷7-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731954, 139.749377),
            name = "本駒込NiPost Office",
            address = "本駒込2-28-29",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729898, 139.747488),
            name = "文京グリーンコートPost Office",
            address = "本駒込2-28-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.728121, 139.752488),
            name = "本駒込Post Office",
            address = "本駒込3-22-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714927, 139.724574),
            name = "文京目白台IchiPost Office",
            address = "目白台1-23-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718315, 139.721685),
            name = "文京目白台NiPost Office",
            address = "目白台2-12-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718928, 139.781097),
            name = "上野Post Office",
            address = "下谷1-5-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.7274, 139.789235),
            name = "下谷SanPost Office",
            address = "下谷3-20-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714623, 139.799207),
            name = "台Higashi花川戸Post Office",
            address = "花川戸2-8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.707124, 139.782848),
            name = "元浅草Post Office",
            address = "元浅草1-5-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.724872, 139.778125),
            name = "台Higashi根岸NiPost Office",
            address = "根岸2-18-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722095, 139.782403),
            name = "台Higashi根岸SanPost Office",
            address = "根岸3-2-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70493, 139.785293),
            name = "台HigashiSan筋Post Office",
            address = "San筋2-7-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.711179, 139.787209),
            name = "台Higashi松が谷Post Office",
            address = "松が谷1-2-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705124, 139.772793),
            name = "上野黒門Post Office",
            address = "上野3-14-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706179, 139.773348),
            name = "上野SanPost Office",
            address = "上野3-21-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705596, 139.775348),
            name = "仲御徒町Post Office",
            address = "上野5-16-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710651, 139.775959),
            name = "上野Station FrontPost Office",
            address = "上野6-15-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.715262, 139.779875),
            name = "上野NanaPost Office",
            address = "上野7-9-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72024, 139.769728),
            name = "台Higashi桜木Post Office",
            address = "上野桜木1-10-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.726384, 139.804281),
            name = "台Higashi清川Post Office",
            address = "清川1-28-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710151, 139.790875),
            name = "浅草Post Office",
            address = "Nishi浅草1-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714984, 139.790375),
            name = "Nishi浅草Post Office",
            address = "Nishi浅草3-12-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.719445, 139.791969),
            name = "台Higashi千束Post Office",
            address = "千束1-16-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718762, 139.796541),
            name = "浅草YonPost Office",
            address = "浅草4-42-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718429, 139.801235),
            name = "台Higashi聖天前Post Office",
            address = "浅草6-34-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701625, 139.784959),
            name = "鳥越神社前Post Office",
            address = "浅草橋3-33-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699902, 139.780765),
            name = "浅草橋Post Office",
            address = "浅草橋5-5-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701291, 139.790431),
            name = "くらまえ橋Post Office",
            address = "蔵前1-3-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70443, 139.793986),
            name = "蔵前Post Office",
            address = "蔵前2-15-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701652, 139.779154),
            name = "台HigashiIchiPost Office",
            address = "台Higashi1-23-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.704326, 139.776725),
            name = "台HigashiSanPost Office",
            address = "台Higashi3-12-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.7214, 139.76446),
            name = "台Higashi谷中Post Office",
            address = "谷中2-5-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710679, 139.781098),
            name = "下谷神社前Post Office",
            address = "Higashi上野3-29-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714707, 139.784875),
            name = "Higashi上野RokuPost Office",
            address = "Higashi上野6-19-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722685, 139.801158),
            name = "Higashi浅草Post Office",
            address = "Higashi浅草1-21-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725983, 139.796956),
            name = "台HigashiNihon堤Post Office",
            address = "Nihon堤1-31-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.720543, 139.78492),
            name = "台Higashi入谷Post Office",
            address = "入谷1-17-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709513, 139.79693),
            name = "雷門Post Office",
            address = "雷門2-2-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.724706, 139.792207),
            name = "台Higashi竜泉Post Office",
            address = "竜泉3-9-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.704319, 139.814873),
            name = "墨田横川Post Office",
            address = "横川4-7-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697993, 139.796865),
            name = "墨田横網Post Office",
            address = "横網1-6-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.688321, 139.807153),
            name = "墨田菊川Post Office",
            address = "菊川3-8-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.716541, 139.819067),
            name = "墨田京島IchiPost Office",
            address = "京島1-23-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713514, 139.823483),
            name = "墨田京島Post Office",
            address = "京島3-47-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70893, 139.814734),
            name = "押上Station FrontPost Office",
            address = "業平4-17-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697632, 139.814201),
            name = "錦糸町Station FrontPost Office",
            address = "錦糸3-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708874, 139.803402),
            name = "墨田吾妻橋Post Office",
            address = "吾妻橋2-3-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714346, 139.812123),
            name = "向島YonPost Office",
            address = "向島4-25-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69507, 139.810736),
            name = "墨田江Higashi橋Post Office",
            address = "江Higashi橋1-7-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701153, 139.803569),
            name = "墨田石原Post Office",
            address = "石原3-25-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700791, 139.81043),
            name = "墨田太平町Post Office",
            address = "太平1-12-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702652, 139.817679),
            name = "本所Post Office",
            address = "太平4-21-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722485, 139.814123),
            name = "Higashi向島IchiPost Office",
            address = "Higashi向島1-4-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.719708, 139.817289),
            name = "向島Post Office",
            address = "Higashi向島2-32-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727596, 139.815845),
            name = "墨田白鬚Post Office",
            address = "Higashi向島4-9-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.726263, 139.82165),
            name = "Higashi向島GoPost Office",
            address = "Higashi向島5-17-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718735, 139.828621),
            name = "墨田Hachi広SanPost Office",
            address = "Hachi広3-32-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72293, 139.830732),
            name = "墨田Hachi広YonPost Office",
            address = "Hachi広4-51-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.732984, 139.816817),
            name = "墨田NiPost Office",
            address = "墨田2-6-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730485, 139.824455),
            name = "墨田YonPost Office",
            address = "墨田4-50-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.704236, 139.800097),
            name = "本所NiPost Office",
            address = "本所2-15-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705541, 139.830344),
            name = "墨田立花団地Post Office",
            address = "立花1-26-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.71118, 139.829954),
            name = "墨田立花Post Office",
            address = "立花5-23-1-102",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693126, 139.793988),
            name = "墨田両国SanPost Office",
            address = "両国3-7-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.694348, 139.800237),
            name = "墨田緑町Post Office",
            address = "緑1-14-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676322, 139.790571),
            name = "江Higashi永代Post Office",
            address = "永代1-14-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664157, 139.812957),
            name = "江Higashi塩浜Post Office",
            address = "塩浜2-23-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670406, 139.793098),
            name = "江Higashi牡丹IchiPost Office",
            address = "牡丹1-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.668712, 139.798598),
            name = "江Higashi牡丹Post Office",
            address = "牡丹3-8-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.694126, 139.823706),
            name = "江Higashi亀戸IchiPost Office",
            address = "亀戸1-17-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703291, 139.825317),
            name = "江Higashi亀戸Post Office",
            address = "亀戸3-62-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699987, 139.832482),
            name = "江Higashi亀戸GoPost Office",
            address = "亀戸5-42-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696487, 139.831427),
            name = "江Higashi亀戸RokuPost Office",
            address = "亀戸6-42-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69782, 139.839065),
            name = "江Higashi亀戸NanaPost Office",
            address = "亀戸7-38-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.688737, 139.814402),
            name = "江Higashi住吉Post Office",
            address = "住吉2-3-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660824, 139.824345),
            name = "江Higashi新砂Post Office",
            address = "新砂2-4-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660936, 139.826345),
            name = "新Higashi京Post Office",
            address = "新砂2-4-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664189, 139.83822),
            name = "Higashi京国際Post Office",
            address = "新砂3-5-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.644132, 139.825707),
            name = "新木場CenterBuildingPost Office",
            address = "新木場1-18-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687904, 139.79785),
            name = "森下町Post Office",
            address = "森下1-12-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676405, 139.796099),
            name = "深川IchiPost Office",
            address = "深川1-8-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68216, 139.798183),
            name = "江Higashi清澄Post Office（ (Temporarily Closed)）",
            address = "清澄3-6-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.617609, 139.780312),
            name = "テレコムCenterPost Office",
            address = "青海2-5-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681627, 139.816235),
            name = "江Higashi千田Post Office",
            address = "千田21-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.689793, 139.829205),
            name = "城HigashiPost Office",
            address = "大島3-15-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69046, 139.839843),
            name = "江Higashi大島Post Office",
            address = "大島7-22-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691326, 139.844598),
            name = "Higashi大島Station FrontPost Office",
            address = "大島9-4-1-110",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.650186, 139.809291),
            name = "江Higashi辰巳Post Office",
            address = "辰巳1-9-49",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.658713, 139.813874),
            name = "江Higashi潮見Post Office",
            address = "潮見1-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.64502, 139.799876),
            name = "江HigashiHigashi雲Post Office",
            address = "Higashi雲1-8-3-101",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68563, 139.842808),
            name = "江HigashiHigashi砂NiPost Office",
            address = "Higashi砂2-13-10-103",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.677878, 139.840538),
            name = "江HigashiHigashi砂Post Office",
            address = "Higashi砂3-1-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.668268, 139.841649),
            name = "江HigashiHigashi砂HachiPost Office",
            address = "Higashi砂8-19-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.668906, 139.811068),
            name = "江Higashi洲崎橋Post Office",
            address = "Higashi陽3-20-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.672323, 139.818818),
            name = "江Higashi区文化CenterPost Office",
            address = "Higashi陽4-11-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670379, 139.817901),
            name = "深川Post Office",
            address = "Higashi陽4-4-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67224, 139.82254),
            name = "江HigashiMinami砂団地Post Office",
            address = "Minami砂2-3-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673656, 139.826872),
            name = "江HigashiMinami砂Post Office",
            address = "Minami砂4-1-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674601, 139.834844),
            name = "江HigashiMinami砂KitaPost Office",
            address = "Minami砂5-24-11-101",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67049, 139.835622),
            name = "江HigashiMinami砂RokuPost Office",
            address = "Minami砂6-10-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681933, 139.80682),
            name = "江Higashi白河Post Office",
            address = "白河4-1-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.656745, 139.795398),
            name = "江Higashi豊洲Post Office",
            address = "豊洲3-2-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.680239, 139.823234),
            name = "江HigashiKita砂IchiPost Office",
            address = "Kita砂1-11-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682405, 139.830483),
            name = "江HigashiKita砂SanPost Office",
            address = "Kita砂3-20-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.6851, 139.836038),
            name = "江HigashiKita砂GoPost Office",
            address = "Kita砂5-19-25-101",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.679878, 139.834983),
            name = "江HigashiKita砂NanaPost Office",
            address = "Kita砂7-5-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669656, 139.806513),
            name = "江Higashi木場Post Office",
            address = "木場5-5-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.630869, 139.790561),
            name = "ＴＦＴPost Office",
            address = "有明3-6-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.6078, 139.70458),
            name = "品川旗の台Post Office",
            address = "旗の台2-1-29",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.610744, 139.718801),
            name = "品川戸越Post Office",
            address = "戸越4-9-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.620604, 139.707051),
            name = "品川小山SanPost Office",
            address = "小山3-8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.615299, 139.70208),
            name = "品川小山GoPost Office",
            address = "小山5-16-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.609355, 139.695858),
            name = "品川洗足Post Office",
            address = "小山7-16-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.634241, 139.71905),
            name = "目黒Station FrontPost Office",
            address = "上大崎3-5-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.625242, 139.718939),
            name = "大崎Post Office",
            address = "NishiGo反田2-32-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.625687, 139.713051),
            name = "品川不動前Post Office",
            address = "NishiGo反田4-29-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.620298, 139.716773),
            name = "品川NishiGo反田RokuPost Office",
            address = "NishiGo反田6-15-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.622493, 139.719606),
            name = "ＴＯＣBuildingPost Office",
            address = "NishiGo反田7-22-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.60019, 139.730467),
            name = "品川Nishi大井NiPost Office",
            address = "Nishi大井2-17-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.598551, 139.719634),
            name = "品川Nishi大井GoPost Office",
            address = "Nishi大井5-15-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.612688, 139.707274),
            name = "荏原Post Office",
            address = "Nishi中延1-7-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.613855, 139.727828),
            name = "Nishi品川Post Office",
            address = "Nishi品川2-14-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.604051, 139.72955),
            name = "品川大井NiPost Office",
            address = "大井2-24-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.604884, 139.725772),
            name = "品川大井SanPost Office",
            address = "大井3-27-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.594774, 139.729439),
            name = "品川大井NanaPost Office",
            address = "大井7-27-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.61966, 139.730605),
            name = "ゲートシティ大崎Post Office",
            address = "大崎1-11-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.619743, 139.729466),
            name = "大崎Station FrontPost Office",
            address = "大崎1-6-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.617299, 139.723106),
            name = "大崎SanPost Office",
            address = "大崎3-20-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.609216, 139.711773),
            name = "品川中延SanPost Office",
            address = "中延3-2-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.603023, 139.709302),
            name = "品川中延GoPost Office",
            address = "中延5-5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.626771, 139.725994),
            name = "品川HigashiGo反田Post Office",
            address = "HigashiGo反田1-18-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.631017, 139.725482),
            name = "大崎Post OfficeＮＴＴ関Higashi病院 Branch",
            address = "HigashiGo反田5-9-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.60519, 139.745688),
            name = "品川鮫洲Post Office",
            address = "Higashi大井1-6-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.602162, 139.740021),
            name = "品川Higashi大井NiPost Office",
            address = "Higashi大井2-12-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.604995, 139.736577),
            name = "品川Post Office",
            address = "Higashi大井5-23-34",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.619494, 139.745937),
            name = "Higashi品川IchiPost Office",
            address = "Higashi品川1-34-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.624271, 139.751075),
            name = "品川天王洲Post Office",
            address = "Higashi品川2-3-10-116",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.595663, 139.736578),
            name = "品川Minami大井Post Office",
            address = "Minami大井4-11-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.591219, 139.732134),
            name = "品川水神Post Office",
            address = "Minami大井6-12-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.58867, 139.731608),
            name = "大森ベルポートPost Office",
            address = "Minami大井6-26-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.614953, 139.744016),
            name = "Minami品川IchiPost Office",
            address = "Minami品川1-8-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.610856, 139.744437),
            name = "Minami品川NiPost Office",
            address = "Minami品川2-17-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.613272, 139.740632),
            name = "Minami品川YonPost Office",
            address = "Minami品川4-18-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.607717, 139.729605),
            name = "品川区役所前Post Office",
            address = "Ni葉1-18-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.599662, 139.723884),
            name = "品川Ni葉Post Office",
            address = "Ni葉2-4-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.598885, 139.750438),
            name = "品川Hachi潮Post Office",
            address = "Hachi潮5-5-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.615049, 139.715828),
            name = "品川平塚IchiPost Office",
            address = "平塚1-7-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.615688, 139.70944),
            name = "品川平塚橋Post Office",
            address = "平塚3-16-32",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.6048, 139.717134),
            name = "品川豊Post Office",
            address = "豊町6-13-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.622132, 139.740076),
            name = "Kita品川Post Office",
            address = "Kita品川1-22-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.622049, 139.737215),
            name = "品川御殿山Post Office",
            address = "Kita品川4-7-35",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.631686, 139.708773),
            name = "下目黒Post Office",
            address = "下目黒3-2-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657766, 139.686413),
            name = "目黒駒場Post Office",
            address = "駒場1-9-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.637359, 139.686776),
            name = "目黒Go本木Post Office",
            address = "Go本木1-22-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.640838, 139.711878),
            name = "目黒San田Post Office",
            address = "San田2-4-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.607976, 139.667161),
            name = "目黒自由が丘Post Office",
            address = "自由が丘2-11-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.642518, 139.697996),
            name = "中目黒Station FrontPost Office",
            address = "上目黒2-15-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.641795, 139.690802),
            name = "上目黒YonPost Office",
            address = "上目黒4-21-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.615382, 139.694414),
            name = "目黒原町Post Office",
            address = "洗足1-11-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.651254, 139.686509),
            name = "目黒大橋Post Office",
            address = "大橋1-10-1-103",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.626575, 139.686386),
            name = "目黒鷹番Post Office",
            address = "鷹番1-14-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.635046, 139.692247),
            name = "目黒中町Post Office",
            address = "中町2-48-31",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648517, 139.692274),
            name = "目黒Higashi山IchiPost Office",
            address = "Higashi山1-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648461, 139.687274),
            name = "目黒Higashi山NiPost Office",
            address = "Higashi山2-15-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.613743, 139.685026),
            name = "目黒MinamiSanPost Office",
            address = "Minami3-3-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.618853, 139.674665),
            name = "目黒柿ノ木坂Post Office",
            address = "Hachi雲1-3-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.61752, 139.668638),
            name = "目黒Hachi雲NiPost Office",
            address = "Hachi雲2-24-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.624658, 139.666082),
            name = "目黒Hachi雲GoPost Office",
            address = "Hachi雲5-10-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.621881, 139.689525),
            name = "目黒碑文谷NiPost Office",
            address = "碑文谷2-5-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.621575, 139.682498),
            name = "目黒碑文谷YonPost Office",
            address = "碑文谷4-16-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.635797, 139.705856),
            name = "目黒SanPost Office",
            address = "目黒3-1-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.631769, 139.702773),
            name = "目黒YonPost Office",
            address = "目黒4-9-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.627631, 139.693691),
            name = "目黒Post Office",
            address = "目黒本町1-15-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.621437, 139.695552),
            name = "目黒本町Post Office",
            address = "目黒本町6-12-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.60966, 139.676638),
            name = "目黒緑が丘Post Office",
            address = "緑が丘1-19-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.55117, 139.747913),
            name = "大田羽田Post Office",
            address = "羽田4-4-27",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.554922, 139.75471),
            name = "羽田整備場Station FrontPost Office",
            address = "羽田空港1-6-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.54837, 139.783959),
            name = "羽田空港Post Office",
            address = "羽田空港3-3-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.575776, 139.680612),
            name = "大田鵜の木Post Office",
            address = "鵜の木2-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.566944, 139.685195),
            name = "大田下丸子Post Office",
            address = "下丸子2-8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.565277, 139.71833),
            name = "蒲田IchiPost Office",
            address = "蒲田1-26-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.560665, 139.718093),
            name = "アロマスクエアPost Office",
            address = "蒲田5-37-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.558445, 139.717886),
            name = "蒲田Post Office",
            address = "蒲田本町1-2-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.580775, 139.697831),
            name = "大田久が原Post Office",
            address = "久が原2-24-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.582692, 139.692693),
            name = "大田久が原NishiPost Office",
            address = "久が原4-1-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.57089, 139.76491),
            name = "大田京浜島Post Office",
            address = "京浜島2-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.590525, 139.727967),
            name = "大田SannoPost Office",
            address = "Sanno2-5-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.582442, 139.723162),
            name = "大森Post Office",
            address = "Sanno3-9-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.601856, 139.699164),
            name = "大田上池台Post Office",
            address = "上池台1-6-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.599162, 139.69222),
            name = "大田洗足Post Office",
            address = "上池台2-31-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.559501, 139.705054),
            name = "新蒲田NiPost Office",
            address = "新蒲田2-17-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.571499, 139.714053),
            name = "Nishi蒲田IchiPost Office",
            address = "Nishi蒲田1-6-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.562861, 139.714581),
            name = "蒲田Station FrontPost Office",
            address = "Nishi蒲田7-46-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.586885, 139.704525),
            name = "大田Nishi馬込Post Office",
            address = "Nishi馬込2-3-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.553946, 139.706805),
            name = "大田NishiRoku郷Post Office",
            address = "NishiRoku郷1-19-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.545642, 139.706916),
            name = "大田NishiRoku郷SanPost Office",
            address = "NishiRoku郷3-28-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.557473, 139.72883),
            name = "大田Nishi糀谷Post Office",
            address = "Nishi糀谷1-21-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.558473, 139.73483),
            name = "大田Nishi糀谷NiPost Office",
            address = "Nishi糀谷2-21-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.55478, 139.739413),
            name = "大田Nishi糀谷SanPost Office",
            address = "Nishi糀谷3-20-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.572443, 139.693388),
            name = "千鳥町Station FrontPost Office",
            address = "千鳥1-16-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.568583, 139.695638),
            name = "千鳥Post Office",
            address = "千鳥2-34-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.564639, 139.698694),
            name = "蒲田安方Post Office",
            address = "多摩川1-10-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.577415, 139.727246),
            name = "大森NishiNiPost Office",
            address = "大森Nishi2-19-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.567666, 139.727218),
            name = "大森NishiRokuPost Office",
            address = "大森Nishi6-14-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.57636, 139.735773),
            name = "大森HigashiIchiPost Office",
            address = "大森Higashi1-9-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.566334, 139.736357),
            name = "大森HigashiYonPost Office",
            address = "大森Higashi4-37-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.563751, 139.744273),
            name = "大森MinamiNiPost Office",
            address = "大森Minami2-4-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.585831, 139.728162),
            name = "大森Station FrontPost Office",
            address = "大森Kita1-29-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.58422, 139.734134),
            name = "大田入新井Post Office",
            address = "大森Kita3-9-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.580637, 139.731412),
            name = "大森KitaRokuPost Office",
            address = "大森Kita6-2-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.573276, 139.700415),
            name = "池上Post Office",
            address = "池上3-39-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.567222, 139.704082),
            name = "大田池上RokuPost Office",
            address = "池上6-38-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.580331, 139.719718),
            name = "大田CentralIchiPost Office",
            address = "Central1-16-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.582553, 139.714413),
            name = "大田CentralYonPost Office",
            address = "Central4-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.575887, 139.713136),
            name = "大田CentralNanaPost Office",
            address = "Central7-4-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.57361, 139.719191),
            name = "大田CentralHachiPost Office",
            address = "Central8-39-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.59494, 139.705385),
            name = "大田中馬込Post Office",
            address = "中馬込1-14-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.551891, 139.715332),
            name = "大田仲Roku郷Post Office",
            address = "仲Roku郷2-9-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.540976, 139.708722),
            name = "Roku郷土手Post Office",
            address = "仲Roku郷4-31-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.591468, 139.672417),
            name = "田園調布IchiPost Office",
            address = "田園調布1-47-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.598106, 139.666861),
            name = "田園調布Station FrontPost Office",
            address = "田園調布3-1-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.596967, 139.656168),
            name = "田園調布GoPost Office",
            address = "田園調布5-34-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.58358, 139.67364),
            name = "田園調布本町Post Office",
            address = "田園調布本町25-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.578778, 139.759438),
            name = "大田市場Post Office",
            address = "Higashi海3-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.562834, 139.729191),
            name = "Higashi蒲田NiPost Office",
            address = "Higashi蒲田2-6-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.596051, 139.68511),
            name = "大田Higashi雪谷NiPost Office",
            address = "Higashi雪谷2-22-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.58944, 139.69622),
            name = "大田Higashi雪谷GoPost Office",
            address = "Higashi雪谷5-9-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.598495, 139.712496),
            name = "大田Higashi馬込Post Office",
            address = "Higashi馬込1-12-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.563833, 139.708387),
            name = "大田Higashi矢口SanPost Office",
            address = "Higashi矢口3-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.585275, 139.686055),
            name = "大田Higashi嶺町Post Office",
            address = "Higashi嶺町3-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.557307, 139.741635),
            name = "大田Higashi糀谷Post Office",
            address = "Higashi糀谷1-19-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.554362, 139.725192),
            name = "大田Minami蒲田Post Office",
            address = "Minami蒲田3-7-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.577359, 139.686028),
            name = "大田Minami久が原Post Office",
            address = "Minami久が原2-16-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.591281, 139.680849),
            name = "大田Minami雪谷Post Office",
            address = "Minami雪谷2-15-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.588941, 139.678416),
            name = "田園調布Post Office",
            address = "Minami雪谷2-21-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.59033, 139.710774),
            name = "大田Minami馬込IchiPost Office",
            address = "Minami馬込1-55-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.590219, 139.718662),
            name = "大田Minami馬込NiPost Office",
            address = "Minami馬込2-28-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.549223, 139.723312),
            name = "大田MinamiRoku郷IchiPost Office",
            address = "MinamiRoku郷1-15-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.544781, 139.720748),
            name = "大田MinamiRoku郷NiPost Office",
            address = "MinamiRoku郷2-35-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.551863, 139.729719),
            name = "大田萩中Post Office",
            address = "萩中2-8-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.549864, 139.740163),
            name = "大田萩中SanPost Office",
            address = "萩中3-23-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.584915, 139.745077),
            name = "大田平和島NiPost Office",
            address = "平和島2-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.580804, 139.748466),
            name = "Higashi京流通CenterPost Office",
            address = "平和島6-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.605355, 139.693775),
            name = "大田Kita千束Post Office",
            address = "Kita千束2-14-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.607216, 139.686192),
            name = "大岡山Station FrontPost Office",
            address = "Kita千束3-26-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.567277, 139.691556),
            name = "大田矢口IchiPost Office",
            address = "矢口1-13-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.560223, 139.695583),
            name = "大田矢口SanPost Office",
            address = "矢口3-7-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666042, 139.658221),
            name = "世田谷羽根木Post Office",
            address = "羽根木1-26-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.6033, 139.67786),
            name = "世田谷奥沢IchiPost Office（ (Temporarily Closed)）",
            address = "奥沢1-38-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.604577, 139.671777),
            name = "世田谷奥沢Post Office",
            address = "奥沢2-10-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.603605, 139.660806),
            name = "世田谷Ku品仏Post Office",
            address = "奥沢8-15-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.625907, 139.618948),
            name = "世田谷岡本Post Office",
            address = "岡本1-30-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.640823, 139.681192),
            name = "世田谷下馬Post Office",
            address = "下馬1-41-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.640545, 139.674831),
            name = "世田谷下馬NiPost Office",
            address = "下馬2-20-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.633629, 139.678526),
            name = "学芸大学前Post Office",
            address = "下馬6-38-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.620797, 139.608922),
            name = "世田谷鎌田Post Office",
            address = "鎌田2-23-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.62724, 139.59995),
            name = "世田谷喜多見SanPost Office",
            address = "喜多見3-21-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.637266, 139.588783),
            name = "喜多見Station FrontPost Office",
            address = "喜多見9-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.636128, 139.610726),
            name = "世田谷砧Post Office",
            address = "砧3-17-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652154, 139.637556),
            name = "経堂Station FrontPost Office",
            address = "宮坂3-11-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666262, 139.591226),
            name = "世田谷給田Post Office",
            address = "給田3-28-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.612696, 139.629272),
            name = "Ni子玉川Post Office",
            address = "玉川2-20-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.622241, 139.629836),
            name = "世田谷瀬田Post Office",
            address = "玉川台2-10-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.638156, 139.658249),
            name = "世田谷駒沢NiPost Office",
            address = "駒沢2-61-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.630791, 139.654406),
            name = "世田谷駒沢Post Office",
            address = "駒沢3-15-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.649376, 139.632418),
            name = "千歳Post Office",
            address = "経堂1-40-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.636128, 139.649139),
            name = "世田谷弦巻Post Office",
            address = "弦巻2-33-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.655265, 139.648222),
            name = "豪徳寺Station FrontPost Office",
            address = "豪徳寺1-38-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.641544, 139.640001),
            name = "世田谷桜Post Office",
            address = "桜3-26-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.644155, 139.626252),
            name = "世田谷桜丘NiPost Office",
            address = "桜丘2-8-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.638711, 139.625808),
            name = "世田谷桜丘SanPost Office",
            address = "桜丘3-28-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.643155, 139.619419),
            name = "世田谷桜丘GoPost Office",
            address = "桜丘5-28-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.655848, 139.629807),
            name = "世田谷桜上水IchiPost Office",
            address = "桜上水1-22-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664764, 139.630168),
            name = "世田谷桜上水GoPost Office",
            address = "桜上水5-6-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.63024, 139.643834),
            name = "世田谷桜新町Post Office",
            address = "桜新町1-14-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.640212, 139.667832),
            name = "世田谷Post Office",
            address = "San軒茶屋2-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.642433, 139.655249),
            name = "世田谷若林SanPost Office",
            address = "若林3-16-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.646266, 139.659471),
            name = "世田谷若林YonPost Office",
            address = "若林4-3-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669652, 139.650638),
            name = "世田谷明大前Post Office",
            address = "松原1-38-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662153, 139.65561),
            name = "Higashi松原Station FrontPost Office",
            address = "松原5-4-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.656404, 139.653221),
            name = "梅ケ丘Station FrontPost Office",
            address = "松原6-2-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.65743, 139.599587),
            name = "世田谷上祖師谷NiPost Office",
            address = "上祖師谷2-7-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.658742, 139.592131),
            name = "世田谷上祖師谷Post Office",
            address = "上祖師谷7-16-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.63824, 139.667721),
            name = "世田谷上馬IchiPost Office",
            address = "上馬1-15-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.634767, 139.663178),
            name = "世田谷上馬Post Office",
            address = "上馬4-2-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.614798, 139.637141),
            name = "世田谷上野毛Post Office",
            address = "上野毛1-34-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.614909, 139.660305),
            name = "世田谷深沢IchiPost Office",
            address = "深沢1-9-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.620964, 139.653778),
            name = "世田谷深沢Post Office",
            address = "深沢4-35-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.641989, 139.645722),
            name = "世田谷IchiPost Office",
            address = "世田谷1-25-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.645166, 139.65099),
            name = "世田谷YonPost Office",
            address = "世田谷4-16-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.637183, 139.599949),
            name = "世田谷成城NiPost Office",
            address = "成城2-15-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.64091, 139.597928),
            name = "成城学園前Post Office",
            address = "成城6-16-30",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648209, 139.592977),
            name = "成城Post Office",
            address = "成城8-30-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659626, 139.643305),
            name = "世田谷赤堤NiPost Office",
            address = "赤堤2-44-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.665847, 139.638806),
            name = "世田谷赤堤Post Office",
            address = "赤堤5-43-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.656014, 139.609586),
            name = "世田谷千歳台Post Office",
            address = "千歳台5-7-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648639, 139.624075),
            name = "千歳船橋Station FrontPost Office",
            address = "船橋1-3-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.654959, 139.622557),
            name = "世田谷船橋Post Office",
            address = "船橋4-2-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.643182, 139.606809),
            name = "祖師谷大蔵Station FrontPost Office",
            address = "祖師谷3-27-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648015, 139.608142),
            name = "世田谷祖師谷YonPost Office",
            address = "祖師谷4-23-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648405, 139.668609),
            name = "世田谷太子堂Post Office",
            address = "太子堂3-18-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.644128, 139.669637),
            name = "San軒茶屋Station FrontPost Office",
            address = "太子堂4-22-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660124, 139.672983),
            name = "池ノ上Station FrontPost Office",
            address = "代沢2-42-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.658376, 139.667637),
            name = "世田谷代沢Post Office",
            address = "代沢5-30-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662154, 139.660443),
            name = "新代田Station FrontPost Office",
            address = "代田5-29-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670791, 139.659165),
            name = "世田谷大原Post Office",
            address = "大原2-18-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648628, 139.677803),
            name = "世田谷池尻Post Office",
            address = "池尻3-28-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.654794, 139.673803),
            name = "世田谷淡島Post Office",
            address = "池尻4-38-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.619658, 139.643168),
            name = "世田谷中町Post Office",
            address = "中町5-16-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.599106, 139.674833),
            name = "Higashi玉川Post Office",
            address = "Higashi玉川1-40-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.609159, 139.648001),
            name = "世田谷等々力Post Office",
            address = "等々力3-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.607521, 139.653723),
            name = "尾山台Station FrontPost Office",
            address = "等々力5-5-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.616047, 139.650139),
            name = "玉川Post Office",
            address = "等々力8-22-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669401, 139.609247),
            name = "芦花公園Station FrontPost Office",
            address = "Minami烏山1-12-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669818, 139.599059),
            name = "千歳烏山Post Office",
            address = "Minami烏山6-29-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.651571, 139.657555),
            name = "世田谷梅丘Post Office",
            address = "梅丘3-14-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.663263, 139.60417),
            name = "世田谷粕谷Post Office",
            address = "粕谷4-13-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.661014, 139.620363),
            name = "世田谷Hachi幡山Post Office",
            address = "Hachi幡山1-12-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66929, 139.616391),
            name = "Hachi幡山Station FrontPost Office",
            address = "Hachi幡山3-34-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676956, 139.599531),
            name = "世田谷Kita烏山Post Office",
            address = "Kita烏山3-26-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676233, 139.591753),
            name = "世田谷Kita烏山HachiPost Office",
            address = "Kita烏山8-3-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66432, 139.66547),
            name = "世田谷Kita沢Post Office",
            address = "Kita沢2-40-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666903, 139.672775),
            name = "世田谷Kita沢SanPost Office",
            address = "Kita沢3-2-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.630269, 139.671915),
            name = "世田谷野沢Post Office",
            address = "野沢3-39-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.601188, 139.645224),
            name = "世田谷野毛Post Office",
            address = "野毛1-5-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.629685, 139.634418),
            name = "世田谷用賀Post Office",
            address = "用賀3-18-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.626578, 139.633784),
            name = "用賀Station FrontPost Office",
            address = "用賀4-10-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648962, 139.701912),
            name = "渋谷代官山Post Office",
            address = "猿楽町23-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.64574, 139.716661),
            name = "渋谷恵比寿Post Office",
            address = "恵比寿2-10-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.642046, 139.713189),
            name = "恵比寿ガーデンプレイスPost Office",
            address = "恵比寿4-20-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.646228, 139.708031),
            name = "恵比寿Station FrontPost Office",
            address = "恵比寿Minami1-2-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.64674, 139.71005),
            name = "恵比寿StationBuildingPost Office",
            address = "恵比寿Minami1-5-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671264, 139.68719),
            name = "元代々木Post Office",
            address = "元代々木町30-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648712, 139.712911),
            name = "渋谷橋Post Office",
            address = "広尾1-3-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.656358, 139.718058),
            name = "渋谷広尾YonPost Office",
            address = "広尾4-1-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648462, 139.720771),
            name = "渋谷広尾Post Office",
            address = "広尾5-8-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.654239, 139.701079),
            name = "渋谷桜丘Post Office",
            address = "桜丘町12-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674014, 139.669053),
            name = "笹塚Station FrontPost Office",
            address = "笹塚1-48-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67393, 139.664636),
            name = "渋谷笹塚Post Office",
            address = "笹塚2-21-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660183, 139.703995),
            name = "渋谷Post Office",
            address = "渋谷1-12-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.655989, 139.704606),
            name = "渋谷SanPost Office",
            address = "渋谷3-27-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660155, 139.694995),
            name = "渋谷松濤Post Office",
            address = "松濤1-29-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669709, 139.682302),
            name = "渋谷上原Post Office",
            address = "上原1-36-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67282, 139.709466),
            name = "渋谷神宮前Post Office",
            address = "神宮前2-18-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662655, 139.708911),
            name = "渋谷青山通Post Office",
            address = "神宮前5-52-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666668, 139.705139),
            name = "神宮前RokuPost Office",
            address = "神宮前6-12-28",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662071, 139.700606),
            name = "渋谷神MinamiPost Office",
            address = "神Minami1-21-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664682, 139.696801),
            name = "放送CenterPost Office",
            address = "神Minami2-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676758, 139.682441),
            name = "代々木Post Office",
            address = "Nishi原1-42-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.679514, 139.708188),
            name = "渋谷千駄ケ谷Post Office",
            address = "千駄ヶ谷1-23-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681152, 139.702244),
            name = "代々木Station Front通Post Office",
            address = "代々木1-18-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687846, 139.697744),
            name = "ShinjukuStationMinami口Post Office",
            address = "代々木2-10-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.686985, 139.699789),
            name = "代々木NiPost Office",
            address = "代々木2-2-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682708, 139.693939),
            name = "代々木SanPost Office",
            address = "代々木3-35-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.677903, 139.691995),
            name = "代々木GoPost Office",
            address = "代々木5-55-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.654111, 139.708149),
            name = "渋谷HigashiNiPost Office",
            address = "Higashi2-22-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657322, 139.698967),
            name = "渋谷Central街Post Office",
            address = "道玄坂1-10-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657211, 139.69619),
            name = "渋谷道玄坂Post Office",
            address = "道玄坂1-19-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676097, 139.676746),
            name = "幡ヶ谷MinamiPost Office",
            address = "幡ヶ谷1-32-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.677708, 139.671275),
            name = "渋谷幡ヶ谷Post Office",
            address = "幡ヶ谷2-56-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.668098, 139.690412),
            name = "渋谷富ケ谷IchiPost Office",
            address = "富ヶ谷1-9-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.663293, 139.68508),
            name = "渋谷富ケ谷NiPost Office",
            address = "富ヶ谷2-18-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.684179, 139.684551),
            name = "渋谷本町NiPost Office",
            address = "本町2-3-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.683429, 139.675552),
            name = "渋谷本町GoPost Office",
            address = "本町5-43-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722869, 139.656774),
            name = "中野丸山Post Office",
            address = "丸山1-2-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725952, 139.65433),
            name = "中野KitaPost Office",
            address = "丸山1-28-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.726285, 139.667829),
            name = "中野江古田SanPost Office",
            address = "江古田3-10-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.724951, 139.645108),
            name = "中野鷺宮KitaPost Office",
            address = "鷺宮2-5-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.724437, 139.638043),
            name = "鷺ノ宮Station FrontPost Office",
            address = "鷺宮4-34-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72709, 139.632415),
            name = "中野鷺宮GoPost Office",
            address = "鷺宮5-23-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.720869, 139.644886),
            name = "中野若宮Post Office",
            address = "若宮3-37-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.719869, 139.663079),
            name = "中野沼袋Post Office",
            address = "沼袋3-26-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708871, 139.675245),
            name = "中野上高田IchiPost Office",
            address = "上高田1-35-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.715509, 139.67219),
            name = "中野上高田Post Office",
            address = "上高田3-19-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734811, 139.63047),
            name = "Fujimi台Station FrontPost Office",
            address = "上鷺宮4-16-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.712731, 139.667524),
            name = "中野新井Post Office",
            address = "新井1-31-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709787, 139.64997),
            name = "中野大和町Post Office",
            address = "大和町1-64-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697586, 139.666199),
            name = "新中野Station FrontPost Office",
            address = "Central5-6-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703371, 139.666663),
            name = "中野Post Office",
            address = "中野2-27-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705204, 139.664274),
            name = "中野SanPost Office",
            address = "中野3-37-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709132, 139.664525),
            name = "中野サンクォーレPost Office",
            address = "中野4-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709537, 139.666996),
            name = "中野GoPost Office",
            address = "中野5-50-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701237, 139.683046),
            name = "中野CentralIchiPost Office",
            address = "Higashi中野1-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710788, 139.687966),
            name = "落合Post Office",
            address = "Higashi中野4-27-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708454, 139.688772),
            name = "Higashi中野Post Office",
            address = "Higashi中野5-11-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.685651, 139.669358),
            name = "中野Minami台NiPost Office",
            address = "Minami台2-51-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682679, 139.664581),
            name = "中野Minami台Post Office",
            address = "Minami台3-37-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.719118, 139.636248),
            name = "中野白鷺Post Office",
            address = "白鷺2-35-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696539, 139.683162),
            name = "中野坂上Post Office",
            address = "本町1-32-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693206, 139.674802),
            name = "中野新橋Station FrontPost Office",
            address = "本町3-2-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697483, 139.676051),
            name = "中野本町SanPost Office",
            address = "本町3-31-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693119, 139.668888),
            name = "中野本町GoPost Office",
            address = "本町5-33-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718224, 139.653281),
            name = "中野野方GoPost Office",
            address = "野方5-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690456, 139.679218),
            name = "中野弥生Post Office",
            address = "弥生町1-19-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703426, 139.62775),
            name = "阿佐谷MinamiSanPost Office",
            address = "阿佐谷Minami3-13-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703944, 139.636003),
            name = "阿佐谷Station FrontPost Office",
            address = "阿佐谷Minami3-35-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.711758, 139.629999),
            name = "阿佐谷KitaSanPost Office",
            address = "阿佐谷Kita3-40-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713008, 139.638915),
            name = "阿佐谷KitaRokuPost Office",
            address = "阿佐谷Kita6-9-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727673, 139.620582),
            name = "杉並井草Post Office",
            address = "井草2-26-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674568, 139.642916),
            name = "杉並永福Post Office",
            address = "永福2-50-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697787, 139.617751),
            name = "荻窪川MinamiPost Office",
            address = "荻窪2-31-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696426, 139.623472),
            name = "荻窪NiPost Office",
            address = "荻窪2-4-31",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702398, 139.622639),
            name = "荻窪YonPost Office",
            address = "荻窪4-22-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.715841, 139.629554),
            name = "下井草MinamiPost Office",
            address = "下井草1-25-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722368, 139.62486),
            name = "杉並下井草Post Office",
            address = "下井草3-30-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66893, 139.632834),
            name = "杉並桜上水Post Office",
            address = "下高井戸1-23-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669735, 139.625001),
            name = "杉並下高井戸Post Office",
            address = "下高井戸1-40-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687204, 139.598502),
            name = "杉並久我山Post Office",
            address = "久我山3-18-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.684927, 139.607391),
            name = "杉並Fujimiヶ丘Post Office",
            address = "久我山5-1-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697676, 139.607557),
            name = "杉並宮前SanPost Office",
            address = "宮前3-31-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695842, 139.601669),
            name = "杉並宮前GoPost Office",
            address = "宮前5-19-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68265, 139.615946),
            name = "高井戸Station FrontPost Office",
            address = "高井戸Higashi2-26-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.688177, 139.621056),
            name = "杉並高井戸HigashiPost Office",
            address = "高井戸Higashi4-19-30",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.698093, 139.649137),
            name = "新高円寺Station FrontPost Office",
            address = "高円寺Minami2-16-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702648, 139.647887),
            name = "高円寺MinamiSanPost Office",
            address = "高円寺Minami3-37-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702343, 139.654164),
            name = "高円寺Central通Post Office",
            address = "高円寺Minami4-2-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.707221, 139.650533),
            name = "高円寺Station FrontPost Office",
            address = "高円寺Kita2-20-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70737, 139.645942),
            name = "高円寺KitaSanPost Office",
            address = "高円寺Kita3-10-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.716035, 139.607334),
            name = "杉並今川SanPost Office",
            address = "今川3-14-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.716118, 139.597696),
            name = "杉並今川YonPost Office",
            address = "今川4-20-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.692039, 139.646054),
            name = "杉並松ノ木Post Office",
            address = "松ノ木2-33-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697211, 139.593526),
            name = "杉並松庵Post Office",
            address = "松庵2-17-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722423, 139.613944),
            name = "井荻Station FrontPost Office",
            address = "上井草1-23-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.724322, 139.604004),
            name = "杉並上井草Post Office",
            address = "上井草3-31-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.704991, 139.619643),
            name = "Nishi友荻窪Post Office",
            address = "上荻1-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.672679, 139.610697),
            name = "杉並上高井戸Post Office",
            address = "上高井戸1-30-50",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691205, 139.631805),
            name = "杉並成田NishiPost Office",
            address = "成田Nishi1-29-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.698871, 139.635971),
            name = "杉並Post Office",
            address = "成田Higashi4-38-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701786, 139.599502),
            name = "杉並Nishi荻MinamiPost Office",
            address = "Nishi荻Minami2-22-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705814, 139.601724),
            name = "Nishi荻窪Post Office",
            address = "Nishi荻Kita2-13-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708202, 139.592891),
            name = "杉並Nishi荻KitaPost Office",
            address = "Nishi荻Kita4-31-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710868, 139.599029),
            name = "杉並善福寺Post Office",
            address = "善福寺1-4-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.707673, 139.616094),
            name = "杉並Yon面道Post Office",
            address = "天沼3-12-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714924, 139.614222),
            name = "杉並桃井Post Office",
            address = "桃井1-40-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.711119, 139.609334),
            name = "荻窪Post Office",
            address = "桃井2-3-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.6849, 139.626389),
            name = "杉並浜田山Post Office",
            address = "浜田山3-36-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68004, 139.635556),
            name = "杉並Nishi永福Post Office",
            address = "浜田山3-6-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681567, 139.634111),
            name = "杉並MinamiPost Office",
            address = "浜田山4-5-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682984, 139.657276),
            name = "杉並方MinamiNiPost Office",
            address = "方Minami2-12-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682845, 139.648999),
            name = "杉並堀ノPost Office",
            address = "堀ノ1-12-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.717035, 139.621527),
            name = "杉並本天沼Post Office",
            address = "本天沼3-40-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67568, 139.658276),
            name = "杉並和泉Post Office",
            address = "和泉1-31-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67468, 139.651471),
            name = "杉並和泉NiPost Office",
            address = "和泉2-36-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691594, 139.655026),
            name = "杉並和田Post Office",
            address = "和田2-40-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.689376, 139.657965),
            name = "杉並聖堂前Post Office",
            address = "和田2-9-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697233, 139.660636),
            name = "Higashi高円寺Post Office",
            address = "和田3-60-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.735842, 139.746655),
            name = "駒込Station FrontPost Office",
            address = "駒込1-44-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.736341, 139.701659),
            name = "豊島高松Post Office",
            address = "高松1-11-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.715621, 139.711825),
            name = "豊島高田Post Office",
            address = "高田3-40-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.719871, 139.715019),
            name = "雑司が谷Post Office",
            address = "雑司が谷2-7-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734897, 139.718602),
            name = "上池袋Post Office",
            address = "上池袋1-9-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737785, 139.723157),
            name = "Nishi巣鴨IchiPost Office",
            address = "Nishi巣鴨1-9-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741063, 139.727628),
            name = "Nishi巣鴨Post Office",
            address = "Nishi巣鴨2-38-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.744396, 139.728739),
            name = "Nishi巣鴨YonPost Office",
            address = "Nishi巣鴨4-13-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729769, 139.707989),
            name = "Higashi京芸術劇場Post Office",
            address = "Nishi池袋1-8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730193, 139.706589),
            name = "Nishi池袋Post Office",
            address = "Nishi池袋3-22-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731813, 139.703297),
            name = "立教学院Post Office",
            address = "Nishi池袋5-10-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.739352, 139.693928),
            name = "豊島千川IchiPost Office",
            address = "千川1-14-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731869, 139.692882),
            name = "豊島千早Post Office",
            address = "千早2-2-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731703, 139.738378),
            name = "巣鴨Station FrontPost Office（ (Temporarily Closed)）",
            address = "巣鴨1-31-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737202, 139.733017),
            name = "巣鴨Post Office",
            address = "巣鴨4-26-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.73323, 139.708936),
            name = "池袋Post Office",
            address = "池袋2-40-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737424, 139.711185),
            name = "池袋YonPost Office",
            address = "池袋4-25-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.744868, 139.713685),
            name = "池袋本町SanPost Office",
            address = "池袋本町3-23-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742146, 139.715963),
            name = "池袋本町Post Office",
            address = "池袋本町4-4-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72848, 139.695132),
            name = "豊島長崎IchiPost Office",
            address = "長崎1-16-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730202, 139.685743),
            name = "豊島長崎Post Office",
            address = "長崎4-25-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734056, 139.681149),
            name = "豊島長崎RokuPost Office",
            address = "長崎6-20-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731925, 139.714991),
            name = "池袋Station FrontPost Office",
            address = "Higashi池袋1-17-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730808, 139.715559),
            name = "池袋サンシャイン通Post Office",
            address = "Higashi池袋1-20-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729231, 139.719241),
            name = "サンシャイン６０Post Office",
            address = "Higashi池袋3-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729592, 139.721185),
            name = "豊島Post Office",
            address = "Higashi池袋3-18-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725592, 139.722074),
            name = "Higashi池袋Post Office",
            address = "Higashi池袋5-10-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729009, 139.731851),
            name = "豊島Minami大塚Post Office",
            address = "Minami大塚1-48-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.728975, 139.71162),
            name = "池袋Nishi武簡易Post Office",
            address = "Minami池袋1-28-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72712, 139.713213),
            name = "Minami池袋Post Office",
            address = "Minami池袋2-24-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727314, 139.71613),
            name = "池袋グリーン通Post Office",
            address = "Minami池袋2-30-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72648, 139.684299),
            name = "豊島Minami長崎Post Office",
            address = "Minami長崎4-27-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730563, 139.678661),
            name = "豊島Minami長崎RokuPost Office",
            address = "Minami長崎6-9-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.733377, 139.729014),
            name = "大塚Station FrontPost Office",
            address = "Kita大塚2-25-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.73373, 139.695492),
            name = "豊島要町IchiPost Office",
            address = "要町1-8-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737757, 139.689048),
            name = "豊島千川Station FrontPost Office",
            address = "要町3-11-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.75945, 139.73696),
            name = "王子SanPost Office",
            address = "王子3-11-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.765607, 139.735838),
            name = "王子GoPost Office",
            address = "王子5-10-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.76095, 139.74046),
            name = "王子Post Office",
            address = "王子6-2-28",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752645, 139.734877),
            name = "王子本町Post Office",
            address = "王子本町1-2-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.779391, 139.707767),
            name = "Kita桐ケ丘Post Office",
            address = "桐ケ丘2-7-27",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.776226, 139.73071),
            name = "Kita志茂IchiPost Office",
            address = "志茂1-3-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77988, 139.731329),
            name = "Kita志茂Post Office",
            address = "志茂4-5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.763144, 139.72035),
            name = "Ju条仲原Post Office",
            address = "Ju条仲原1-22-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.75845, 139.725545),
            name = "上Ju条Post Office",
            address = "上Ju条1-2-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762636, 139.714356),
            name = "上Ju条YonPost Office",
            address = "上Ju条4-17-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77006, 139.729655),
            name = "Kita神谷Post Office",
            address = "神谷2-12-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743952, 139.745099),
            name = "Nishiヶ原Post Office",
            address = "Nishiケ原3-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741785, 139.739794),
            name = "Nishiヶ原YonPost Office",
            address = "Nishiケ原4-1-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.783101, 139.72107),
            name = "赤羽岩淵Station FrontPost Office",
            address = "赤羽1-55-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.780392, 139.726377),
            name = "赤羽NiPost Office",
            address = "赤羽2-28-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.777725, 139.717406),
            name = "赤羽Station FrontPost Office",
            address = "赤羽Nishi1-33-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77226, 139.721466),
            name = "赤羽NishiNiPost Office",
            address = "赤羽Nishi2-2-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.771587, 139.714045),
            name = "赤羽NishiYonPost Office",
            address = "赤羽Nishi4-44-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.774848, 139.704778),
            name = "赤羽NishiRokuPost Office",
            address = "赤羽Nishi6-17-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.777773, 139.713834),
            name = "赤羽台Post Office",
            address = "赤羽台2-4-51",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.777114, 139.723822),
            name = "赤羽Post Office",
            address = "赤羽Minami1-12-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.784841, 139.707361),
            name = "赤羽KitaNiPost Office",
            address = "赤羽Kita2-13-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.750868, 139.73696),
            name = "飛鳥山前Post Office",
            address = "滝野川2-1-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.751367, 139.728295),
            name = "滝野川SanPost Office",
            address = "滝野川3-79-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743896, 139.724601),
            name = "滝野川Post Office",
            address = "滝野川6-28-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.747618, 139.722962),
            name = "滝野川RokuPost Office",
            address = "滝野川6-76-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762172, 139.726155),
            name = "中Ju条Post Office",
            address = "中Ju条2-12-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737869, 139.748849),
            name = "中里Post Office",
            address = "中里2-1-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734926, 139.754765),
            name = "Kita田端Post Office",
            address = "田端3-4-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.738314, 139.758237),
            name = "田端Post Office",
            address = "田端5-7-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.740564, 139.764265),
            name = "Kita田端新町Post Office",
            address = "田端新町2-14-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.763644, 139.729016),
            name = "HigashiJu条Station FrontPost Office",
            address = "HigashiJu条2-14-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.767254, 139.727516),
            name = "HigashiJu条Post Office",
            address = "HigashiJu条4-13-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.771698, 139.725933),
            name = "HigashiJu条RokuPost Office",
            address = "HigashiJu条6-7-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741009, 139.760904),
            name = "Higashi田端Post Office",
            address = "Higashi田端2-10-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.791167, 139.698796),
            name = "Kita浮間NiPost Office",
            address = "浮間2-10-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.787251, 139.699462),
            name = "Kita浮間Post Office",
            address = "浮間3-19-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.756478, 139.741682),
            name = "Kita豊島NiPost Office",
            address = "豊島2-1-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.76195, 139.747181),
            name = "Kita豊島SanPost Office",
            address = "豊島3-17-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.766674, 139.752078),
            name = "Kita豊島団地Post Office",
            address = "豊島5-5-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.766477, 139.742237),
            name = "Kita豊島Post Office",
            address = "豊島7-32-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752201, 139.747015),
            name = "Kita堀船Post Office",
            address = "堀船2-2-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734622, 139.784014),
            name = "荒川Post Office",
            address = "荒川3-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.739038, 139.777597),
            name = "荒川GoPost Office",
            address = "荒川5-11-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734982, 139.772542),
            name = "Nishi日暮里Post Office",
            address = "Nishi日暮里1-60-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729557, 139.770055),
            name = "日暮里Station FrontPost Office",
            address = "Nishi日暮里2-21-6-102",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.732427, 139.768987),
            name = "Nishi日暮里Station FrontPost Office",
            address = "Nishi日暮里5-11-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.74773, 139.762959),
            name = "荒川Nishi尾久NiPost Office",
            address = "Nishi尾久2-16-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752646, 139.762014),
            name = "荒川Nishi尾久SanPost Office",
            address = "Nishi尾久3-25-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749007, 139.75657),
            name = "荒川Nishi尾久NanaPost Office",
            address = "Nishi尾久7-16-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743898, 139.782069),
            name = "荒川町屋Post Office",
            address = "町屋1-19-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749925, 139.778624),
            name = "荒川町屋GoPost Office",
            address = "町屋5-6-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.748425, 139.784651),
            name = "荒川Kita町屋Post Office",
            address = "町屋8-3-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729233, 139.78518),
            name = "Higashi日暮里NiPost Office",
            address = "Higashi日暮里2-27-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.7309, 139.778125),
            name = "Higashi日暮里RokuPost Office",
            address = "Higashi日暮里6-7-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743398, 139.770931),
            name = "荒川Higashi尾久NiPost Office",
            address = "Higashi尾久2-40-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742811, 139.766996),
            name = "荒川Higashi尾久YonPost Office",
            address = "Higashi尾久4-21-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.746592, 139.774902),
            name = "荒川Higashi尾久RokuPost Office",
            address = "Higashi尾久6-11-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749434, 139.769339),
            name = "熊野前Post Office",
            address = "Higashi尾久8-14-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.733816, 139.797318),
            name = "荒川Minami千住GoPost Office",
            address = "Minami千住5-39-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.735594, 139.790235),
            name = "荒川Minami千住Post Office",
            address = "Minami千住6-1-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737263, 139.807232),
            name = "荒川汐入Post Office",
            address = "Minami千住8-4-5-118",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741006, 139.681799),
            name = "板橋向原Post Office",
            address = "向原2-24-27",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742951, 139.697381),
            name = "板橋幸町Post Office",
            address = "幸町37-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.782556, 139.66902),
            name = "大Higashi文化学園Post Office",
            address = "高島平1-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.785945, 139.658744),
            name = "板橋NishiPost Office",
            address = "高島平3-12-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.788944, 139.645023),
            name = "板橋高島平Post Office",
            address = "高島平5-10-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.790833, 139.658327),
            name = "板橋高島平NanaPost Office",
            address = "高島平7-27-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.789306, 139.672548),
            name = "板橋Nishi台Station FrontPost Office",
            address = "高島平9-3-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.779521, 139.681898),
            name = "板橋坂下Post Office",
            address = "坂下1-16-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.779891, 139.689074),
            name = "板橋坂下IchiPost Office",
            address = "坂下1-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.789139, 139.682991),
            name = "志村橋Post Office",
            address = "坂下3-25-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.789332, 139.637384),
            name = "板橋San園Post Office",
            address = "San園1-22-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77428, 139.696796),
            name = "板橋志村Post Office",
            address = "志村1-12-27",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.778446, 139.686436),
            name = "板橋KitaPost Office",
            address = "志村3-24-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.794194, 139.688574),
            name = "板橋舟渡Post Office",
            address = "舟渡2-5-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.74795, 139.678632),
            name = "板橋小茂根Post Office",
            address = "小茂根2-31-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762281, 139.673937),
            name = "上板橋Post Office",
            address = "上板橋2-2-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762782, 139.690297),
            name = "板橋常盤台Post Office",
            address = "常盤台1-30-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.761115, 139.683909),
            name = "板橋常盤台SanPost Office",
            address = "常盤台3-16-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.796721, 139.664049),
            name = "板橋新河岸団地Post Office",
            address = "新河岸2-10-15-107",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77614, 139.63133),
            name = "板橋成増Post Office",
            address = "成増1-28-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.778445, 139.632218),
            name = "板橋成増ヶ丘Post Office",
            address = "成増3-13-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.764254, 139.705685),
            name = "板橋清水Post Office",
            address = "清水町20-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.773808, 139.670132),
            name = "板橋Nishi台Post Office",
            address = "Nishi台3-18-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.776414, 139.637513),
            name = "赤塚SanPost Office",
            address = "赤塚3-7-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.779945, 139.646384),
            name = "板橋赤塚Post Office",
            address = "赤塚6-40-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.770585, 139.643523),
            name = "板橋赤塚新町Post Office",
            address = "赤塚新町1-25-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.771528, 139.691723),
            name = "板橋前野Post Office",
            address = "前野町4-21-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.748728, 139.696853),
            name = "板橋大山Post Office",
            address = "大山Nishi町52-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.747506, 139.702269),
            name = "板橋ハッピーロードPost Office",
            address = "大山町3-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.748589, 139.704602),
            name = "大山Station FrontPost Office",
            address = "大山Higashi町16-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.746506, 139.691192),
            name = "板橋大谷口Post Office",
            address = "大谷口上町49-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.7487, 139.686409),
            name = "板橋大谷口KitaPost Office",
            address = "大谷口Kita町76-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.758172, 139.706463),
            name = "新板橋Post Office",
            address = "大和町6-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741951, 139.705547),
            name = "板橋中丸Post Office",
            address = "中丸町17-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.765503, 139.678492),
            name = "板橋中台Post Office",
            address = "中台1-34-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77253, 139.682075),
            name = "板橋中台NiPost Office",
            address = "中台2-30-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.755338, 139.697269),
            name = "中板橋Post Office",
            address = "中板橋13-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.753005, 139.683076),
            name = "板橋Higashi新町Post Office",
            address = "Higashi新町2-56-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.775196, 139.663049),
            name = "板橋徳丸Post Office",
            address = "徳丸2-28-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.770002, 139.654717),
            name = "板橋徳丸SanPost Office",
            address = "徳丸3-10-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.776307, 139.654105),
            name = "板橋徳丸GoPost Office",
            address = "徳丸5-5-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.756505, 139.689714),
            name = "板橋Minami常盤台Post Office",
            address = "Minami常盤台1-20-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749201, 139.713796),
            name = "板橋Post Office",
            address = "板橋2-42-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.75295, 139.716046),
            name = "板橋YonPost Office",
            address = "板橋4-62-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.761004, 139.696019),
            name = "板橋FujimiPost Office",
            address = "Fujimi町31-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752589, 139.69427),
            name = "板橋弥生Post Office",
            address = "弥生町12-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.784179, 139.677985),
            name = "板橋蓮根Post Office",
            address = "蓮根2-31-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.769337, 139.702101),
            name = "板橋蓮沼Post Office",
            address = "蓮沼町23-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.736895, 139.672328),
            name = "練馬旭丘Post Office",
            address = "旭丘1-76-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.771779, 139.631219),
            name = "練馬旭町Post Office",
            address = "旭町2-43-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.733005, 139.606695),
            name = "下石神井SanPost Office",
            address = "下石神井3-7-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.74231, 139.634025),
            name = "練馬貫井Post Office",
            address = "貫井5-10-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.720034, 139.591835),
            name = "練馬関IchiPost Office",
            address = "関町Minami1-6-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.723228, 139.574642),
            name = "練馬関町Post Office",
            address = "関町Kita2-3-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.728588, 139.576919),
            name = "武蔵関Station FrontPost Office",
            address = "関町Kita4-6-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.759225, 139.63058),
            name = "光が丘Post Office",
            address = "光が丘2-9-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.763419, 139.625469),
            name = "練馬光が丘団地Post Office",
            address = "光が丘5-5-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.751004, 139.628109),
            name = "練馬高松SanPost Office",
            address = "高松3-21-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.757614, 139.618415),
            name = "練馬高松Post Office",
            address = "高松6-7-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741477, 139.615971),
            name = "練馬高野台Station FrontPost Office",
            address = "高野台1-7-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749975, 139.608471),
            name = "練馬高野台Post Office",
            address = "高野台5-39-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743728, 139.667383),
            name = "練馬桜台NiPost Office",
            address = "桜台2-17-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.748143, 139.647718),
            name = "練馬春日MinamiPost Office",
            address = "春日町1-12-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.753818, 139.646886),
            name = "練馬春日NiPost Office",
            address = "春日町2-7-31",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.751726, 139.63883),
            name = "練馬春日Post Office",
            address = "春日町6-1-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743284, 139.676188),
            name = "練馬小竹Post Office",
            address = "小竹町2-42-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725783, 139.589974),
            name = "上石神井Post Office",
            address = "上石神井1-17-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.732366, 139.599112),
            name = "練馬下石神井通Post Office",
            address = "上石神井3-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730699, 139.58828),
            name = "練馬上石神井KitaPost Office",
            address = "上石神井4-8-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.754141, 139.580057),
            name = "練馬Nishi大泉NiPost Office",
            address = "Nishi大泉2-1-32",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.760751, 139.576224),
            name = "練馬Nishi大泉SanPost Office",
            address = "Nishi大泉3-32-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.759806, 139.566502),
            name = "練馬Nishi大泉GoPost Office",
            address = "Nishi大泉5-29-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741115, 139.59389),
            name = "石神井Post Office",
            address = "石神井台3-3-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.738421, 139.582391),
            name = "石神井台RokuPost Office",
            address = "石神井台6-15-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743587, 139.6045),
            name = "石神井公園Station FrontPost Office",
            address = "石神井町3-25-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.750197, 139.601444),
            name = "石神井YonPost Office",
            address = "石神井町4-28-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.751393, 139.655106),
            name = "練馬早宮Post Office",
            address = "早宮3-9-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.764945, 139.587889),
            name = "大泉Post Office",
            address = "大泉学園町4-20-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.769667, 139.586001),
            name = "練馬大泉学園Post Office",
            address = "大泉学園町6-11-44",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.774138, 139.593194),
            name = "練馬大泉学園KitaPost Office",
            address = "大泉学園町8-32-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.761141, 139.606443),
            name = "練馬大泉NiPost Office",
            address = "大泉町2-51-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762696, 139.595667),
            name = "練馬大泉YonPost Office",
            address = "大泉町4-28-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.751364, 139.617471),
            name = "練馬谷原Post Office",
            address = "谷原2-2-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.732562, 139.645025),
            name = "練馬中村NiPost Office",
            address = "中村2-5-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.736367, 139.638803),
            name = "練馬中村Post Office",
            address = "中村Kita3-15-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.761864, 139.649217),
            name = "練馬田柄HigashiPost Office",
            address = "田柄1-19-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.765836, 139.644773),
            name = "練馬田柄NiPost Office",
            address = "田柄2-19-36",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.759753, 139.636358),
            name = "練馬田柄Post Office",
            address = "田柄3-14-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.764891, 139.613109),
            name = "練馬土支田Post Office",
            address = "土支田2-29-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752697, 139.597306),
            name = "練馬Higashi大泉NiPost Office",
            address = "Higashi大泉2-15-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.751528, 139.586431),
            name = "練馬Higashi大泉SanPost Office",
            address = "Higashi大泉3-19-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.756002, 139.587223),
            name = "練馬Higashi大泉YonPost Office",
            address = "Higashi大泉4-31-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.747336, 139.579057),
            name = "練馬Higashi大泉NanaPost Office",
            address = "Higashi大泉7-35-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.736726, 139.576114),
            name = "練馬Minami大泉IchiPost Office",
            address = "Minami大泉1-15-38",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743947, 139.569281),
            name = "練馬Minami大泉SanPost Office",
            address = "Minami大泉3-19-34",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749863, 139.573419),
            name = "練馬Minami大泉GoPost Office",
            address = "Minami大泉5-21-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.73245, 139.617888),
            name = "練馬Minami田中NiPost Office",
            address = "Minami田中2-14-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737977, 139.620526),
            name = "練馬Minami田中Post Office",
            address = "Minami田中3-5-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.75187, 139.666235),
            name = "練馬氷川台Post Office",
            address = "氷川台4-49-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743977, 139.625248),
            name = "練馬Fujimi台YonPost Office",
            address = "Fujimi台4-11-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.760256, 139.663962),
            name = "練馬平和台IchiPost Office",
            address = "平和台1-38-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.759587, 139.656245),
            name = "練馬平和台Post Office",
            address = "平和台4-21-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737395, 139.660718),
            name = "練馬桜台Post Office",
            address = "豊玉上2-22-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731534, 139.662579),
            name = "練馬豊玉中Post Office",
            address = "豊玉中1-17-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730368, 139.657107),
            name = "練馬豊玉Post Office",
            address = "豊玉中2-27-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.735423, 139.652774),
            name = "練馬Post Office",
            address = "豊玉Kita6-4-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.766892, 139.664744),
            name = "練馬Kita町Post Office",
            address = "Kita町1-32-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714479, 139.583531),
            name = "練馬立野Post Office",
            address = "立野町8-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742339, 139.656134),
            name = "練馬NiPost Office",
            address = "練馬2-21-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742172, 139.649468),
            name = "練馬YonPost Office",
            address = "練馬4-25-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.768618, 139.824258),
            name = "足立綾瀬Post Office",
            address = "綾瀬4-31-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762313, 139.821286),
            name = "綾瀬Station FrontPost Office",
            address = "綾瀬4-5-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.793753, 139.782316),
            name = "足立伊興NiPost Office",
            address = "伊興2-18-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.801102, 139.786681),
            name = "足立Higashi伊興Post Office",
            address = "伊興本町2-7-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.783088, 139.810953),
            name = "足立ひとつやPost Office",
            address = "Ichiツ家2-13-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.799086, 139.812119),
            name = "足立花畑IchiPost Office",
            address = "花畑1-15-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.80453, 139.806786),
            name = "花畑NishiPost Office",
            address = "花畑4-28-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.806443, 139.811228),
            name = "足立花畑GoPost Office",
            address = "花畑5-14-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.767701, 139.788483),
            name = "足立関原Post Office",
            address = "関原2-37-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.75859, 139.757042),
            name = "足立宮城Post Office",
            address = "宮城1-12-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.768061, 139.77454),
            name = "足立興野Post Office",
            address = "興野2-31-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.784921, 139.788871),
            name = "足立栗原KitaPost Office",
            address = "栗原2-17-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.805946, 139.774149),
            name = "足立古千谷Post Office",
            address = "古千谷本町2-20-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77034, 139.810509),
            name = "足立弘道Post Office",
            address = "弘道1-30-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.769729, 139.764466),
            name = "足立江KitaNiPost Office（ (Temporarily Closed)）",
            address = "江Kita2-28-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.772699, 139.769346),
            name = "足立江KitaYonPost Office",
            address = "江Kita4-2-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.782865, 139.769706),
            name = "足立江KitaPost Office",
            address = "江Kita6-30-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.786477, 139.841117),
            name = "足立佐野Post Office",
            address = "佐野2-11-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.783642, 139.751236),
            name = "足立鹿浜Post Office",
            address = "鹿浜2-34-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.788114, 139.76204),
            name = "足立鹿浜HachiPost Office",
            address = "鹿浜8-11-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.811873, 139.766483),
            name = "足立舎人Post Office",
            address = "舎人5-17-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.772421, 139.741626),
            name = "足立新田Post Office",
            address = "新田2-12-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.766451, 139.815425),
            name = "足立Nishi綾瀬Post Office",
            address = "Nishi綾瀬3-39-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.790336, 139.775011),
            name = "足立Nishi伊興Post Office",
            address = "Nishi伊興1-9-30",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.778421, 139.780705),
            name = "足立Nishi新井Post Office",
            address = "Nishi新井1-5-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.78517, 139.779372),
            name = "足立Nishi新井NiPost Office",
            address = "Nishi新井2-21-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.773172, 139.790621),
            name = "足立Nishi新井栄町Post Office",
            address = "Nishi新井栄町1-4-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.777783, 139.789121),
            name = "Nishi新井Station FrontPost Office",
            address = "Nishi新井栄町2-7-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.775699, 139.774067),
            name = "足立Nishi新井本町Post Office",
            address = "Nishi新井本町2-21-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.772339, 139.780872),
            name = "足立NishiPost Office",
            address = "Nishi新井本町4-4-30",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.795836, 139.788649),
            name = "足立Nishi竹の塚Post Office",
            address = "Nishi竹の塚2-4-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.801168, 139.794353),
            name = "足立Nishi保木間Post Office",
            address = "Nishi保木間4-5-14-106",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.776478, 139.821563),
            name = "足立Nishi加平Post Office",
            address = "青井4-45-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.777894, 139.813842),
            name = "足立青井Post Office",
            address = "青井6-22-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.754036, 139.804177),
            name = "Kita千住Post Office",
            address = "千住4-15-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749176, 139.809011),
            name = "足立旭町Post Office",
            address = "千住旭町27-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741427, 139.798317),
            name = "千住河原Post Office",
            address = "千住河原町23-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.746287, 139.794762),
            name = "足立宮元町Post Office",
            address = "千住宮元町19-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743427, 139.811455),
            name = "足立Post Office",
            address = "千住曙町42-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.755619, 139.798427),
            name = "足立大川町Post Office",
            address = "千住大川町20-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749287, 139.799039),
            name = "足立中居Post Office",
            address = "千住中居町17-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.746176, 139.80165),
            name = "足立仲町Post Office",
            address = "千住仲町19-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752759, 139.794688),
            name = "千住竜田Post Office",
            address = "千住龍田町20-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.764396, 139.804121),
            name = "足立IchiPost Office",
            address = "足立1-11-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762091, 139.810898),
            name = "足立SanPost Office",
            address = "足立3-18-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.778478, 139.844589),
            name = "足立大谷田団地Post Office",
            address = "大谷田1-1-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.776923, 139.830451),
            name = "足立谷中Post Office",
            address = "谷中2-5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.781311, 139.834617),
            name = "足立谷中SanPost Office",
            address = "谷中3-19-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.790948, 139.802342),
            name = "足立KitaPost Office",
            address = "竹の塚3-9-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.794448, 139.795815),
            name = "足立竹の塚Post Office",
            address = "竹の塚5-8-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.769563, 139.852727),
            name = "足立中川Post Office",
            address = "中川3-3-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.780059, 139.760513),
            name = "足立椿Post Office",
            address = "椿2-18-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.78231, 139.79787),
            name = "足立島根Post Office",
            address = "島根2-19-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.764646, 139.833896),
            name = "足立Higashi綾瀬Post Office",
            address = "Higashi綾瀬1-18-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.769035, 139.843006),
            name = "足立Higashi和NiPost Office",
            address = "Higashi和2-15-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77259, 139.841367),
            name = "足立Higashi和Post Office",
            address = "Higashi和4-8-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.790748, 139.827459),
            name = "足立花畑Post Office",
            address = "Minami花畑3-19-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.807973, 139.756762),
            name = "足立入谷Post Office",
            address = "入谷9-15-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.770034, 139.797454),
            name = "足立梅田Post Office",
            address = "梅田6-32-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.776311, 139.803009),
            name = "足立梅島Post Office",
            address = "梅島2-2-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.789532, 139.81023),
            name = "足立保木間Post Office",
            address = "保木間1-31-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.797003, 139.803675),
            name = "足立保木間YonPost Office",
            address = "保木間4-1-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.761507, 139.787872),
            name = "足立本木IchiPost Office",
            address = "本木1-1-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.764728, 139.781067),
            name = "足立本木Post Office",
            address = "本木Kita町1-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.746593, 139.812844),
            name = "足立柳原Post Office",
            address = "柳原1-9-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.788171, 139.797593),
            name = "Roku月町Post Office",
            address = "Roku月2-22-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.785584, 139.821558),
            name = "足立Roku町Post Office",
            address = "Roku町4-2-27",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.79617, 139.83745),
            name = "足立Roku木Post Office",
            address = "Roku木4-7-30",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734567, 139.863589),
            name = "葛飾奥戸Post Office",
            address = "奥戸3-28-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.747038, 139.880448),
            name = "葛飾鎌倉Post Office",
            address = "鎌倉4-10-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.758147, 139.848978),
            name = "葛飾亀有NiPost Office",
            address = "亀有2-15-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.764313, 139.848839),
            name = "亀有Post Office",
            address = "亀有3-33-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.768618, 139.848228),
            name = "亀有Station FrontPost Office",
            address = "亀有5-37-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.763563, 139.86556),
            name = "葛飾ShinjukuPost Office",
            address = "金町1-8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.768813, 139.86956),
            name = "金町Post Office",
            address = "金町5-31-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.747038, 139.87106),
            name = "葛飾高砂YonPost Office",
            address = "高砂4-2-26-101",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.751732, 139.866616),
            name = "高砂Post Office",
            address = "高砂5-27-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.754906, 139.866183),
            name = "葛飾高砂NanaPost Office",
            address = "高砂7-21-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741122, 139.871199),
            name = "葛飾細田Post Office",
            address = "細田3-28-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.736095, 139.839508),
            name = "葛飾Post Office",
            address = "Yonつ木2-28-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.736255, 139.833034),
            name = "葛飾Yonつ木Post Office",
            address = "Yonつ木4-2-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.755759, 139.871227),
            name = "葛飾柴又IchiPost Office",
            address = "柴又1-11-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.75587, 139.876754),
            name = "葛飾柴又Post Office",
            address = "柴又4-10-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.754897, 139.819425),
            name = "葛飾小菅Post Office",
            address = "小菅1-10-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762369, 139.857838),
            name = "葛飾ShinjukuNiPost Office",
            address = "Shinjuku2-25-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.716457, 139.859563),
            name = "新小岩Station FrontPost Office",
            address = "新小岩1-48-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.789032, 139.857865),
            name = "葛飾水元GoPost Office",
            address = "水元5-1-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.762063, 139.841895),
            name = "葛飾Nishi亀有Post Office",
            address = "Nishi亀有3-39-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.723401, 139.851924),
            name = "葛飾Nishi新小岩Post Office",
            address = "Nishi新小岩5-31-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.784588, 139.85456),
            name = "葛飾Nishi水元Post Office",
            address = "Nishi水元5-11-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.747232, 139.849117),
            name = "葛飾青戸SanPost Office",
            address = "青戸3-12-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.745982, 139.855311),
            name = "葛飾青戸Post Office",
            address = "青戸3-38-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752843, 139.850728),
            name = "葛飾青戸YonPost Office",
            address = "青戸4-28-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.772284, 139.867893),
            name = "葛飾Higashi金町NiPost Office",
            address = "Higashi金町2-17-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.771756, 139.875198),
            name = "葛飾Higashi金町Post Office",
            address = "Higashi金町3-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.777844, 139.878631),
            name = "葛飾Higashi金町GoPost Office",
            address = "Higashi金町5-32-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729389, 139.839687),
            name = "葛飾HigashiYonつ木Post Office",
            address = "HigashiYonつ木3-47-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722651, 139.860701),
            name = "葛飾Higashi新小岩Post Office",
            address = "Higashi新小岩3-11-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727706, 139.859784),
            name = "葛飾Higashi新小岩RokuPost Office",
            address = "Higashi新小岩6-16-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.78306, 139.866198),
            name = "葛飾水元Post Office",
            address = "Higashi水元3-4-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.750676, 139.83623),
            name = "葛飾Higashi堀切NiPost Office",
            address = "Higashi堀切2-21-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.754759, 139.840174),
            name = "葛飾Higashi堀切SanPost Office",
            address = "Higashi堀切3-29-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734428, 139.846785),
            name = "葛飾Higashi立石Post Office",
            address = "Higashi立石3-27-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.778089, 139.860199),
            name = "葛飾Minami水元NiPost Office",
            address = "Minami水元2-27-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.753148, 139.845562),
            name = "葛飾白鳥Post Office",
            address = "白鳥3-22-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.746954, 139.839702),
            name = "お花茶屋Station FrontPost Office",
            address = "宝町2-34-13-113",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742261, 139.833425),
            name = "葛飾堀切IchiPost Office",
            address = "堀切1-42-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.747426, 139.826287),
            name = "葛飾堀切Post Office",
            address = "堀切4-11-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.753703, 139.830924),
            name = "葛飾堀切RokuPost Office",
            address = "堀切6-28-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.756867, 139.828774),
            name = "葛飾堀切HachiPost Office",
            address = "堀切8-1-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.736705, 139.845952),
            name = "葛飾立石IchiPost Office",
            address = "立石1-8-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743677, 139.847396),
            name = "葛飾区役所Post Office",
            address = "立石5-13-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737733, 139.852201),
            name = "立石Post Office",
            address = "立石8-7-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68949, 139.879951),
            name = "江戸川Ichi之江Post Office",
            address = "Ichi之江4-6-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687881, 139.906393),
            name = "江戸川IchiPost Office",
            address = "江戸川1-26-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682853, 139.888867),
            name = "江戸川今井Post Office",
            address = "江戸川3-50-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709989, 139.893837),
            name = "江戸川鹿骨NiPost Office",
            address = "鹿骨2-45-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.71171, 139.888004),
            name = "江戸川鹿骨Post Office",
            address = "鹿骨5-14-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.704823, 139.913697),
            name = "江戸川篠崎Post Office",
            address = "篠崎町3-23-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706268, 139.90067),
            name = "江戸川篠崎NanaPost Office",
            address = "篠崎町7-8-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695935, 139.890727),
            name = "江戸川椿Post Office",
            address = "春江町3-48-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.680435, 139.874507),
            name = "江戸川春江GoPost Office",
            address = "春江町5-11-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.698214, 139.851767),
            name = "江戸川小松川Post Office",
            address = "小松川3-11-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.689489, 139.872368),
            name = "江戸川松江Post Office",
            address = "松江7-2-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702653, 139.863175),
            name = "江戸川Post Office",
            address = "松島1-19-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.71018, 139.859313),
            name = "江戸川松島Post Office",
            address = "松島3-2-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72637, 139.875039),
            name = "江戸川上Ichi色Post Office",
            address = "上Ichi色2-18-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710157, 139.900891),
            name = "江戸川上篠崎Post Office",
            address = "上篠崎3-14-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662325, 139.855787),
            name = "葛NishiクリーンタウンPost Office",
            address = "清新町1-3-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695739, 139.877673),
            name = "江戸川NishiIchi之江Post Office",
            address = "NishiIchi之江3-17-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664242, 139.857981),
            name = "Nishi葛NishiStation FrontPost Office",
            address = "Nishi葛Nishi6-8-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.733179, 139.877644),
            name = "Nishi小岩IchiPost Office",
            address = "Nishi小岩1-15-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.738761, 139.878921),
            name = "Nishi小岩YonPost Office",
            address = "Nishi小岩4-4-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681712, 139.861814),
            name = "江戸川船堀Post Office",
            address = "船堀2-21-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706348, 139.868673),
            name = "江戸川区役所前Post Office",
            address = "Central1-3-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.704765, 139.873812),
            name = "江戸川CentralPost Office",
            address = "Central2-24-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709904, 139.875311),
            name = "江戸川CentralSanPost Office",
            address = "Central3-24-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.711653, 139.865035),
            name = "江戸川CentralYonPost Office",
            address = "Central4-4-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669741, 139.86773),
            name = "葛NishiPost Office",
            address = "中葛Nishi1-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673852, 139.871646),
            name = "江戸川中葛NishiIchiPost Office",
            address = "中葛Nishi1-49-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.665575, 139.872897),
            name = "葛NishiStation FrontPost Office",
            address = "中葛Nishi3-29-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66177, 139.865175),
            name = "江戸川中葛NishiGoPost Office",
            address = "中葛Nishi5-7-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666103, 139.881062),
            name = "江戸川長島Post Office",
            address = "Higashi葛Nishi5-45-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66127, 139.874036),
            name = "江戸川Higashi葛NishiRokuPost Office",
            address = "Higashi葛Nishi6-8-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.65816, 139.878619),
            name = "葛Nishi仲町Post Office",
            address = "Higashi葛Nishi7-19-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697657, 139.916114),
            name = "江戸川Higashi篠崎Post Office",
            address = "Higashi篠崎1-7-48",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722736, 139.891337),
            name = "Higashi小岩IchiPost Office",
            address = "Higashi小岩1-13-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731873, 139.890254),
            name = "Higashi小岩GoPost Office",
            address = "Higashi小岩5-26-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699774, 139.86772),
            name = "江戸川Higashi小松川Post Office",
            address = "Higashi小松川1-12-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691905, 139.86323),
            name = "江戸川Higashi小松川SanPost Office",
            address = "Higashi小松川3-13-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.71807, 139.887726),
            name = "江戸川Higashi松本Post Office",
            address = "Higashi松本1-14-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.688205, 139.895274),
            name = "江戸川Higashi瑞江NiPost Office",
            address = "Higashi瑞江2-52-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.645022, 139.875897),
            name = "江戸川Minami葛NishiRokuPost Office",
            address = "Minami葛Nishi6-7-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.692777, 139.89936),
            name = "瑞江Station FrontPost Office",
            address = "Minami篠崎町2-10-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.691791, 139.904305),
            name = "江戸川Minami篠崎NiPost Office",
            address = "Minami篠崎町2-45-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.723013, 139.880366),
            name = "Minami小岩GoPost Office",
            address = "Minami小岩5-3-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729462, 139.881781),
            name = "Minami小岩フラワーロードPost Office",
            address = "Minami小岩7-13-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.728207, 139.886532),
            name = "小岩Post Office",
            address = "Minami小岩8-1-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.733512, 139.884948),
            name = "小岩Station FrontPost Office",
            address = "Minami小岩8-18-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705319, 139.845231),
            name = "江戸川平井Post Office",
            address = "平井4-8-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709708, 139.845148),
            name = "江戸川平井GoPost Office",
            address = "平井5-48-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705736, 139.839759),
            name = "平井Station FrontPost Office",
            address = "平井5-6-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713235, 139.841537),
            name = "江戸川平井NanaPost Office",
            address = "平井7-29-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670518, 139.85837),
            name = "江戸川Kita葛NishiSanPost Office",
            address = "Kita葛Nishi3-1-32",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.7369, 139.891753),
            name = "Kita小岩SanPost Office",
            address = "Kita小岩3-13-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743566, 139.883893),
            name = "Kita小岩RokuPost Office",
            address = "Kita小岩6-9-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743011, 139.891587),
            name = "Kita小岩NanaPost Office",
            address = "Kita小岩7-17-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.716975, 139.873567),
            name = "江戸川本Ichi色Post Office",
            address = "本Ichi色2-3-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.645392, 139.870009),
            name = "江戸川臨海Post Office",
            address = "臨海町5-2-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.633574, 139.328936),
            name = "Hachi王子MinamiPost Office",
            address = "みなみ野1-6-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.643341, 139.306723),
            name = "めじろ台Station FrontPost Office",
            address = "めじろ台4-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657448, 139.339035),
            name = "Hachi王子Station FrontPost Office",
            address = "旭町9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.680894, 139.354274),
            name = "Hachi王子宇津木Post Office",
            address = "宇津木町627",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.685088, 139.34683),
            name = "Hachi王子左入Post Office",
            address = "宇津木町798-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659618, 139.335443),
            name = "Hachi王子横山町Post Office",
            address = "横山町10-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.665478, 139.304167),
            name = "Hachi王子横川Post Office",
            address = "横川町536-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.628119, 139.38328),
            name = "Hachi王子由木Post Office",
            address = "下柚木2-7-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.627599, 139.288358),
            name = "Hachi王子Building町Post Office",
            address = "Building町1097",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.688921, 139.357468),
            name = "Hachi王子宇津木台Post Office",
            address = "久保山町2-43-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.684809, 139.312194),
            name = "Hachi王子犬目Post Office",
            address = "犬目町132-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.63676, 139.358108),
            name = "Hachi王子絹ケ丘Post Office",
            address = "絹ケ丘2-21-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.663673, 139.335303),
            name = "Hachi王子元横山町Post Office",
            address = "元横山町3-9-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.655228, 139.277587),
            name = "元Hachi王子SanPost Office",
            address = "元Hachi王子町3-2256",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.665228, 139.315805),
            name = "Hachi王子市役所前Post Office",
            address = "元本郷町3-17-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667784, 139.369467),
            name = "Hachi王子高倉Post Office",
            address = "高倉町40-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.641841, 139.276282),
            name = "浅川Post Office",
            address = "高尾町1528",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.646952, 139.300585),
            name = "Hachi王子NishiPost Office",
            address = "散田町5-27-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.647758, 139.339609),
            name = "Hachi王子子安MinamiPost Office",
            address = "子安町2-29-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.654535, 139.336832),
            name = "Hachi王子子安Post Office",
            address = "子安町4-6-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657777, 139.33351),
            name = "Hachi王子寺町Post Office",
            address = "寺町49-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.622824, 139.306673),
            name = "Hachi王子グリーンヒル寺田Post Office",
            address = "寺田町432-101-102",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.64173, 139.285114),
            name = "京王高尾Station FrontPost Office",
            address = "初沢町1231-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.63812, 139.31875),
            name = "Hachi王子小比企Post Office",
            address = "小比企町1774",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.627929, 139.415743),
            name = "Hachi王子松が谷Post Office",
            address = "松が谷11-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671257, 139.212511),
            name = "上恩方Post Office",
            address = "上恩方町2135",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.655701, 139.327499),
            name = "Hachi王子上野町Post Office",
            address = "上野町38-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.620873, 139.36658),
            name = "Hachi王子上柚木Post Office",
            address = "上柚木682-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673198, 139.264059),
            name = "恩方Post Office",
            address = "Nishi寺方町69-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669021, 139.361203),
            name = "KitaHachi王子Station FrontPost Office",
            address = "石川町2955-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657312, 139.307445),
            name = "Hachi王子千人町Post Office",
            address = "千人町4-6-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696168, 139.276085),
            name = "下川口Post Office",
            address = "川口町3279",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68542, 139.29875),
            name = "Hachi王子川口HigashiPost Office",
            address = "川口町3737-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667365, 139.26767),
            name = "Hachi王子川町Post Office",
            address = "川町281-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.644566, 139.35502),
            name = "Hachi王子Kita野Post Office",
            address = "打越町344-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.656229, 139.314695),
            name = "Hachi王子台町Post Office",
            address = "台町4-46-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662312, 139.330054),
            name = "Hachi王子大横Post Office",
            address = "大横町2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673199, 139.295223),
            name = "元Hachi王子Post Office",
            address = "大楽寺町408",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.63715, 139.416714),
            name = "大塚・帝京大学Station FrontPost Office",
            address = "大塚9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.66214, 139.350958),
            name = "Hachi王子大和田Post Office",
            address = "大和田町3-20-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.666701, 139.340747),
            name = "Hachi王子Post Office",
            address = "大和田町7-21-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697086, 139.324387),
            name = "Hachi王子丹木Post Office",
            address = "丹木町3-107-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67431, 139.324499),
            name = "Hachi王子中野SannoPost Office",
            address = "中野Sanno3-6-4-108",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667617, 139.330748),
            name = "Hachi王子中野上町IchiPost Office",
            address = "中野上町1-3-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.6677, 139.323082),
            name = "Hachi王子中野上町Post Office",
            address = "中野上町1-32-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67331, 139.315472),
            name = "Hachi王子中野上町GoPost Office",
            address = "中野上町5-5-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67967, 139.317903),
            name = "Hachi王子中野Post Office",
            address = "中野町2545-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.65539, 139.295385),
            name = "Hachi王子長房Post Office",
            address = "長房町588",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660812, 139.31725),
            name = "Hachi王子追分町Post Office",
            address = "追分町10-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.638927, 139.404049),
            name = "Central大学Post Office",
            address = "Higashi中野742-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.612513, 139.381802),
            name = "Minami大沢Station FrontPost Office",
            address = "Minami大沢2-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.607208, 139.378941),
            name = "Hachi王子Minami大沢Post Office",
            address = "Minami大沢3-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.612124, 139.369386),
            name = "Hachi王子Minami大沢GoPost Office",
            address = "Minami大沢5-14-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68153, 139.276891),
            name = "Hachi王子弐分方Post Office",
            address = "弐分方町4-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660368, 139.326665),
            name = "Hachi王子Hachi幡町Post Office",
            address = "Hachi幡町3-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667728, 139.350747),
            name = "Hachi王子FujimiPost Office",
            address = "Fujimi町4-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652423, 139.302029),
            name = "Hachi王子並木町Post Office",
            address = "並木町12-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.623624, 139.401272),
            name = "京王堀之Station FrontPost Office",
            address = "別所2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.632149, 139.347193),
            name = "Hachi王子片倉台Post Office",
            address = "片倉町1101-61",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.642713, 139.339049),
            name = "Hachi王子片倉Post Office",
            address = "片倉町439-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659334, 139.343559),
            name = "Hachi王子明神町Post Office",
            address = "明神町4-2-2-105",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.645786, 139.321611),
            name = "Hachi王子緑町Post Office",
            address = "緑町291-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.637842, 139.295947),
            name = "Hachi王子狭間通Post Office",
            address = "椚田町1214-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.634648, 139.308113),
            name = "Hachi王子椚田Post Office",
            address = "椚田町203",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.732555, 139.375243),
            name = "立川松中Post Office",
            address = "Ichiban-cho5-8-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.711864, 139.428044),
            name = "立川栄Post Office",
            address = "栄町2-45-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69606, 139.421267),
            name = "立川錦Post Office",
            address = "Nishiki-cho1-11-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.689422, 139.420129),
            name = "立川Nishiki-choYonPost Office",
            address = "Nishiki-cho4-11-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.719446, 139.422989),
            name = "立川幸Post Office",
            address = "幸町1-13-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725474, 139.428321),
            name = "立川幸YonPost Office",
            address = "幸町4-56-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702976, 139.416795),
            name = "立川高松Post Office",
            address = "高松町3-17-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.723307, 139.403407),
            name = "砂川Post Office",
            address = "砂川町1-52-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.694366, 139.411324),
            name = "立川柴崎Post Office",
            address = "柴崎町3-14-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.717319, 139.435199),
            name = "立川けやき台Post Office",
            address = "若葉町1-13-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722613, 139.442459),
            name = "立川若葉町Post Office",
            address = "若葉町4-25-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699115, 139.415407),
            name = "立川Post Office",
            address = "曙町2-14-36",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702198, 139.414795),
            name = "ファーレ立川Post Office",
            address = "曙町2-34-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.721112, 139.387686),
            name = "立川大山Post Office",
            address = "上砂町3-11-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730582, 139.364021),
            name = "立川Nishi砂Post Office",
            address = "Nishi砂町5-26-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729834, 139.415239),
            name = "立川柏町Post Office",
            address = "柏町4-51-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701003, 139.39788),
            name = "立川FujimiPost Office",
            address = "Fujimi町1-12-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693532, 139.39377),
            name = "立川FujimiRokuPost Office",
            address = "Fujimi町6-15-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.712451, 139.554005),
            name = "武蔵野関前SanPost Office",
            address = "関前3-13-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713367, 139.543006),
            name = "武蔵野関前Post Office",
            address = "関前5-9-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70698, 139.587697),
            name = "吉祥寺Higashi町Post Office",
            address = "吉祥寺Higashi町3-3-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703357, 139.581991),
            name = "アトレ吉祥寺Post Office",
            address = "吉祥寺Minami町2-1-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703147, 139.588475),
            name = "吉祥寺Minami町Post Office",
            address = "吉祥寺Minami町5-1-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.720117, 139.568337),
            name = "吉祥寺Kita町Post Office",
            address = "吉祥寺Kita町5-10-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.705285, 139.580753),
            name = "吉祥寺Station FrontPost Office",
            address = "吉祥寺本町1-13-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.704396, 139.575282),
            name = "吉祥寺本町NiPost Office",
            address = "吉祥寺本町2-26-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708368, 139.576254),
            name = "吉祥寺本町Post Office",
            address = "吉祥寺本町2-31-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703924, 139.544201),
            name = "武蔵野境Post Office",
            address = "境1-3-4-105",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699285, 139.538368),
            name = "武蔵野境MinamiPost Office",
            address = "境Minami町3-18-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702647, 139.576615),
            name = "武蔵野御殿山Post Office",
            address = "御殿山1-1-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709034, 139.533979),
            name = "武蔵野桜堤Post Office",
            address = "桜堤1-8-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718338, 139.527202),
            name = "武蔵野上向台Post Office",
            address = "桜堤3-31-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710201, 139.561866),
            name = "武蔵野Post Office",
            address = "Nishi久保3-1-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703424, 139.564699),
            name = "武蔵野中町Post Office",
            address = "中町1-30-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690509, 139.587142),
            name = "San鷹台Post Office",
            address = "井の頭1-29-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69412, 139.579421),
            name = "San鷹井の頭Post Office",
            address = "井の頭5-3-29",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695202, 139.546035),
            name = "San鷹井口Post Office",
            address = "井口1-25-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693259, 139.569449),
            name = "San鷹下連雀Post Office",
            address = "下連雀1-11-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.701535, 139.559922),
            name = "San鷹Station FrontPost Office",
            address = "下連雀3-36-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69312, 139.559756),
            name = "San鷹下連雀YonPost Office",
            address = "下連雀4-18-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.699285, 139.551673),
            name = "San鷹上連雀GoPost Office",
            address = "上連雀5-15-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.685647, 139.550189),
            name = "San鷹上連雀Post Office",
            address = "上連雀9-42-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.668067, 139.575338),
            name = "San鷹新川IchiPost Office",
            address = "新川1-11-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.67639, 139.571219),
            name = "San鷹新川GoPost Office",
            address = "新川5-3-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682913, 139.568467),
            name = "San鷹新川Post Office",
            address = "新川6-3-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690185, 139.538167),
            name = "San鷹深大寺Post Office",
            address = "深大寺1-14-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.680976, 139.538498),
            name = "San鷹大沢Post Office",
            address = "大沢2-2-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.686064, 139.529786),
            name = "国際基督教大学Post Office",
            address = "大沢3-10-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.665595, 139.540925),
            name = "San鷹大沢YonPost Office",
            address = "大沢4-16-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664984, 139.568756),
            name = "San鷹中原YonPost Office",
            address = "中原4-11-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673789, 139.581671),
            name = "San鷹Kita野Post Office",
            address = "Kita野3-6-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687676, 139.581198),
            name = "San鷹牟礼NiPost Office",
            address = "牟礼2-11-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.683398, 139.560478),
            name = "San鷹Post Office",
            address = "野崎1-1-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.782324, 139.285859),
            name = "青梅河辺Post Office",
            address = "河辺町5-17-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.785463, 139.295497),
            name = "青梅若草Post Office",
            address = "河辺町8-12-28",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.80082, 139.178481),
            name = "御岳Post Office",
            address = "御岳本町163-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.786907, 139.284859),
            name = "青梅霞台Post Office",
            address = "師岡町4-5-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.788662, 139.259884),
            name = "青梅住江町Post Office",
            address = "住江町61-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.790045, 139.267194),
            name = "青梅勝沼Post Office",
            address = "勝沼3-78-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.820426, 139.275429),
            name = "小曽木Post Office",
            address = "小曾木3-1887-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.790096, 139.254361),
            name = "青梅上町Post Office",
            address = "上町371",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.789576, 139.302897),
            name = "青梅新町Post Office",
            address = "新町2-22-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.823124, 139.248057),
            name = "成木Post Office",
            address = "成木5-1498",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.80532, 139.194035),
            name = "沢井Station FrontPost Office",
            address = "沢井2-771-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.779574, 139.272472),
            name = "青梅長淵Post Office",
            address = "長淵4-1366",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.787351, 139.275443),
            name = "青梅Post Office",
            address = "Higashi青梅1-13-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.802878, 139.303579),
            name = "青梅藤橋Post Office",
            address = "藤橋2-117-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.787615, 139.22332),
            name = "吉野Post Office",
            address = "梅郷3-777-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.778825, 139.306469),
            name = "青梅末広Post Office",
            address = "末広町2-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.687202, 139.479429),
            name = "府中栄町Post Office",
            address = "栄町2-10-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.675759, 139.515427),
            name = "府中紅葉丘Post Office",
            address = "紅葉丘3-37-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.663565, 139.446655),
            name = "府中YotsuyaPost Office",
            address = "Yotsuya3-27-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.677287, 139.5054),
            name = "府中若松町Post Office",
            address = "若松町4-37-49",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674481, 139.478652),
            name = "武蔵府中Post Office",
            address = "寿町1-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657621, 139.459765),
            name = "府中中河原Post Office",
            address = "住吉町2-11-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.656345, 139.504984),
            name = "府中小柳町Post Office",
            address = "小柳町5-36-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657816, 139.485124),
            name = "府中是政Post Office",
            address = "是政3-34-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667093, 139.49429),
            name = "府中清水が丘Post Office",
            address = "清水が丘2-3-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.677174, 139.457071),
            name = "府中Nishi府町Post Office",
            address = "Nishi府町3-13-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.684221, 139.499071),
            name = "府中浅間Post Office",
            address = "浅間町2-12-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.68477, 139.489666),
            name = "府中学園通Post Office",
            address = "天神町3-12-32",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.654622, 139.462765),
            name = "Higashi京多摩Post Office",
            address = "Minami町4-40-35",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674758, 139.472236),
            name = "府中日鋼町Post Office",
            address = "日鋼町1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669064, 139.457126),
            name = "府中日新Post Office",
            address = "日新町1-5-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.667732, 139.509928),
            name = "府中白糸台Post Office",
            address = "白糸台2-1-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.661817, 139.512566),
            name = "府中車返団地Post Office",
            address = "白糸台5-25-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670537, 139.483846),
            name = "府中Hachi幡宿Post Office",
            address = "Hachi幡町1-4-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.672897, 139.464959),
            name = "府中美好Post Office",
            address = "美好町2-12-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.676259, 139.485235),
            name = "府中SanPost Office",
            address = "府中町3-5-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664148, 139.46582),
            name = "府中分梅Post Office",
            address = "分梅町2-43-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670426, 139.469348),
            name = "府中片町Post Office",
            address = "片町1-19-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.688118, 139.457792),
            name = "府中Kita山Post Office",
            address = "Kita山町2-8-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.665982, 139.476264),
            name = "府中本町NiPost Office",
            address = "本町2-20-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713418, 139.36866),
            name = "昭島つつじが丘ハイツPost Office",
            address = "つつじが丘3-5-6-117",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700614, 139.370327),
            name = "昭和Post Office",
            address = "宮沢町2-33-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695059, 139.383604),
            name = "昭島郷地Post Office",
            address = "郷地町2-36-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.703864, 139.381437),
            name = "昭島玉川Post Office",
            address = "玉川町3-23-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713736, 139.355395),
            name = "昭島Post Office",
            address = "松原町1-9-31",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.718195, 139.343634),
            name = "昭島松原YonPost Office",
            address = "松原町4-4-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.710462, 139.379616),
            name = "昭島中神Post Office",
            address = "中神町1277-1601",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708558, 139.375715),
            name = "中神Station FrontPost Office",
            address = "朝日町1-6-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.702586, 139.35244),
            name = "昭島田中Post Office",
            address = "田中町2-22-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714557, 139.361439),
            name = "昭島Station FrontPost Office",
            address = "田中町562-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.707557, 139.340246),
            name = "拝島Post Office",
            address = "拝島町5-1-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.713334, 139.348662),
            name = "昭島緑Post Office",
            address = "緑町2-28-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.65368, 139.566645),
            name = "柴崎Station FrontPost Office",
            address = "菊野台2-21-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.650177, 139.556717),
            name = "国領Station FrontPost Office",
            address = "国領町1-43-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648014, 139.561591),
            name = "調布くすのきPost Office",
            address = "国領町3-8-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.647597, 139.550814),
            name = "調布国領GoPost Office",
            address = "国領町5-4-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.654374, 139.543147),
            name = "調布Station FrontPost Office",
            address = "小島町1-13-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.649268, 139.541531),
            name = "調布市役所前Post Office",
            address = "小島町2-40-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.657302, 139.53),
            name = "調布上石原Post Office",
            address = "上石原1-25-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652069, 139.527094),
            name = "調布上石原SanPost Office",
            address = "上石原3-29-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671132, 139.544232),
            name = "神代植物公園前Post Office",
            address = "深大寺元町4-30-35",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.672788, 139.558812),
            name = "調布深大寺Post Office",
            address = "深大寺Higashi町6-16-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.658818, 139.570117),
            name = "調布Nishiつつじケ丘Post Office",
            address = "Nishiつつじケ丘1-24-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659457, 139.575394),
            name = "神代Post Office",
            address = "Nishiつつじケ丘3-37-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652529, 139.576669),
            name = "調布金子Post Office",
            address = "Nishiつつじケ丘4-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.661513, 139.586254),
            name = "調布仙川Post Office",
            address = "仙川町1-20-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.664346, 139.583143),
            name = "調布仙川NiPost Office",
            address = "仙川町2-18-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.64046, 139.55998),
            name = "調布染地Post Office",
            address = "染地3-1-253",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.644459, 139.537733),
            name = "調布小島Post Office",
            address = "多摩川5-8-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652236, 139.586783),
            name = "ＮＴＴHigashiNihon研修センタPost Office",
            address = "入間町1-44",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.655679, 139.552119),
            name = "調布Hachi雲台Post Office",
            address = "Hachi雲台1-26-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652846, 139.558174),
            name = "調布Post Office",
            address = "Hachi雲台2-6-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659484, 139.525149),
            name = "調布飛田給Post Office",
            address = "飛田給1-44-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.669706, 139.586004),
            name = "調布緑ケ丘Post Office",
            address = "緑ケ丘2-40-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.528138, 139.479213),
            name = "町田つくし野Post Office",
            address = "つくし野1-36-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.555355, 139.444853),
            name = "町田Post Office",
            address = "旭町3-2-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.56266, 139.460046),
            name = "玉川学園前Post Office",
            address = "玉川学園2-10-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.534148, 139.459821),
            name = "町田金森Post Office",
            address = "金森Higashi1-24-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.528401, 139.468791),
            name = "町田金森HigashiPost Office",
            address = "金森Higashi4-35-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.541912, 139.451298),
            name = "原町田Post Office",
            address = "原町田3-7-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.540958, 139.448144),
            name = "町田Station FrontPost Office",
            address = "原町田4-1-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.544856, 139.447104),
            name = "原町田RokuPost Office",
            address = "原町田6-17-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.544051, 139.458603),
            name = "町田高ケ坂Post Office",
            address = "高ケ坂526-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.573606, 139.489101),
            name = "町田San輪Post Office",
            address = "San輪緑山1-5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.568103, 139.436465),
            name = "町田山崎Post Office",
            address = "山崎町2200",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.580815, 139.436223),
            name = "町田山崎KitaPost Office",
            address = "山崎町776-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.60007, 139.360665),
            name = "町田NishiPost Office",
            address = "小山町4275-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.592155, 139.380775),
            name = "町田小山Post Office",
            address = "小山町827",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.590822, 139.402884),
            name = "町田小山田桜台Post Office",
            address = "小山田桜台1-20-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.548633, 139.442048),
            name = "町田森野Post Office",
            address = "森野2-30-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.580185, 139.416744),
            name = "忠生Post Office",
            address = "図師町626-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.545521, 139.473728),
            name = "成瀬清水谷Post Office",
            address = "成瀬1-2-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.554384, 139.477629),
            name = "町田成瀬台Post Office",
            address = "成瀬台3-8-28",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.606247, 139.303371),
            name = "町田大戸Post Office",
            address = "相原町3160-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.604486, 139.333778),
            name = "町田相原Post Office",
            address = "相原町792-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.588158, 139.463768),
            name = "鶴川Post Office",
            address = "大蔵町446",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.522471, 139.468853),
            name = "MinamiPost Office",
            address = "鶴間182",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.511112, 139.470242),
            name = "グランベリーモールPost Office",
            address = "鶴間3-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.597018, 139.462129),
            name = "町田鶴川YonPost Office",
            address = "鶴川4-28-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.590908, 139.470157),
            name = "鶴川団地Post Office",
            address = "鶴川6-7-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.512335, 139.47752),
            name = "町田Minamiつくし野Post Office",
            address = "Minamiつくし野2-31-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.537524, 139.47138),
            name = "成瀬Station FrontPost Office",
            address = "Minami成瀬1-11-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.553355, 139.456714),
            name = "町田Minami大谷Post Office",
            address = "Minami大谷301",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.585686, 139.483046),
            name = "鶴川Station FrontPost Office",
            address = "能ヶ谷4-3-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.563326, 139.447075),
            name = "町田本町田Post Office",
            address = "本町田1227",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.570853, 139.44888),
            name = "町田藤の台Post Office",
            address = "本町田3486",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.570791, 139.418103),
            name = "町田木曽NishiPost Office",
            address = "木曽Nishi3-4-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.563608, 139.426075),
            name = "町田木曽Post Office",
            address = "木曽Higashi3-33-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693757, 139.493234),
            name = "小金井貫井MinamiPost Office",
            address = "貫井Minami町4-3-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.707839, 139.496316),
            name = "小金井貫井KitaPost Office",
            address = "貫井Kita町2-19-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69759, 139.505899),
            name = "小金井前原SanPost Office",
            address = "前原町3-40-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690924, 139.502622),
            name = "小金井前原GoPost Office",
            address = "前原町5-9-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695119, 139.533036),
            name = "小金井HigashiNiPost Office",
            address = "Higashi町2-1-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.694535, 139.52287),
            name = "小金井Higashi町Post Office",
            address = "Higashi町4-12-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700285, 139.524536),
            name = "Higashi小金井Station FrontPost Office",
            address = "Higashi町4-43-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.709366, 139.502871),
            name = "小金井本町Post Office",
            address = "本町4-21-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.7052, 139.50626),
            name = "小金井Post Office",
            address = "本町5-38-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70515, 139.520051),
            name = "小金井緑町Post Office",
            address = "緑町2-2-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.723003, 139.459624),
            name = "たかの台Station FrontPost Office",
            address = "たかの台39-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727115, 139.514258),
            name = "花小金井Station FrontPost Office",
            address = "花小金井1-9-13-101",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.735929, 139.506398),
            name = "小平花小金井GoPost Office",
            address = "花小金井8-34-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.715528, 139.496681),
            name = "小平回田町Post Office",
            address = "回田町278-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.720753, 139.471207),
            name = "小平学園Nishi町Post Office",
            address = "学園Nishi町1-37-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.724253, 139.478789),
            name = "Ichi橋学園Station FrontPost Office",
            address = "学園Nishi町2-28-29",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.719059, 139.489168),
            name = "小平喜平Post Office",
            address = "喜平町3-2-5-102",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.740278, 139.460512),
            name = "小平小川NishiPost Office",
            address = "小川Nishi町3-7-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730363, 139.464235),
            name = "小平小川Post Office",
            address = "Ogawa-machi1-2095",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731335, 139.447625),
            name = "小平上宿Post Office",
            address = "Ogawa-machi1-625",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737334, 139.465623),
            name = "小平ブリヂストン前Post Office",
            address = "小川Higashi町1-22-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.733307, 139.471984),
            name = "小平Post Office",
            address = "小川Higashi町5-16-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.712519, 139.485402),
            name = "小平上水MinamiPost Office",
            address = "上水Minami町2-3-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.717123, 139.464991),
            name = "小平上水本町Post Office",
            address = "上水本町1-31-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729613, 139.481594),
            name = "小平仲町Post Office",
            address = "仲町630",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727793, 139.492402),
            name = "小平天神Post Office",
            address = "天神町1-1-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737474, 139.488093),
            name = "小平Station FrontPost Office",
            address = "美園町2-2-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.723408, 139.507457),
            name = "小平鈴木NiPost Office",
            address = "鈴木町2-186-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.658008, 139.369829),
            name = "日野旭が丘Post Office",
            address = "旭が丘3-3-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670896, 139.404159),
            name = "日野Post Office",
            address = "宮345",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660532, 139.412621),
            name = "日野高幡Post Office",
            address = "高幡1003-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.682394, 139.387826),
            name = "日野新町Post Office",
            address = "新町3-3-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.670062, 139.395715),
            name = "日野神明Post Office",
            address = "神明1-11-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.662424, 139.3798),
            name = "日野多摩平Post Office",
            address = "多摩平2-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.671562, 139.384132),
            name = "日野多摩平RokuPost Office",
            address = "多摩平6-39-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.653148, 139.413158),
            name = "日野高幡台Post Office",
            address = "程久保650",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.655147, 139.395132),
            name = "日野Minami平Post Office",
            address = "Minami平8-14-21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.678465, 139.41024),
            name = "日野KitaPost Office",
            address = "日野1047-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.674478, 139.375272),
            name = "日野台Post Office",
            address = "日野台4-31-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.679041, 139.397879),
            name = "日野Station FrontPost Office",
            address = "日野本町4-2-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.649062, 139.418569),
            name = "日野百草Post Office",
            address = "百草999",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.646565, 139.381356),
            name = "Nana生Post Office",
            address = "平山5-19-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.660014, 139.383819),
            name = "豊田Station FrontPost Office",
            address = "豊田4-24-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.673594, 139.419975),
            name = "日野下田Post Office",
            address = "万願寺2-29-29",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.659259, 139.429463),
            name = "百草園Station FrontPost Office",
            address = "落川416-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749167, 139.476927),
            name = "Higashi村山栄町Post Office",
            address = "栄町1-15-56",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.748917, 139.472733),
            name = "久米川Station FrontPost Office",
            address = "栄町2-8-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.745972, 139.467345),
            name = "Hachi坂Station FrontPost Office",
            address = "栄町3-10-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.773581, 139.478954),
            name = "Higashi村山秋津Post Office",
            address = "秋津町3-10-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.777108, 139.49037),
            name = "新秋津Station FrontPost Office",
            address = "秋津町5-36-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.76907, 139.468144),
            name = "Higashi村山諏訪Post Office",
            address = "諏訪町1-21-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.760943, 139.48712),
            name = "Higashi村山青葉Post Office",
            address = "青葉町2-4-43",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.76586, 139.497203),
            name = "青葉Higashi簡易Post Office",
            address = "青葉町4-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.739335, 139.479233),
            name = "萩山Station FrontPost Office",
            address = "萩山町1-2-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.758304, 139.466844),
            name = "Higashi村山Post Office",
            address = "本町2-1-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.755084, 139.46994),
            name = "Higashi村山市役所前Post Office",
            address = "本町4-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.76322, 139.455706),
            name = "Higashi村山野口Post Office",
            address = "野口町3-14-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700533, 139.446737),
            name = "国立StationKita口Post Office",
            address = "光町1-41-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706754, 139.442849),
            name = "国分寺光Post Office",
            address = "光町3-16-25",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.708948, 139.434766),
            name = "国分寺Nishi町Post Office",
            address = "Nishi町3-26-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.695534, 139.464736),
            name = "国分寺泉Post Office",
            address = "泉町3-6-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.711254, 139.470457),
            name = "国分寺Higashi恋ケ窪YonPost Office",
            address = "Higashi恋ヶ窪4-21-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696172, 139.455709),
            name = "国分寺藤Post Office",
            address = "藤2-9-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69795, 139.479373),
            name = "国分寺MinamiPost Office",
            address = "Minami町3-13-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.70931, 139.46243),
            name = "国分寺Post Office",
            address = "日吉町4-1-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.704393, 139.452959),
            name = "国分寺富士本Post Office",
            address = "富士本1-1-20",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.706116, 139.482762),
            name = "国分寺本多Post Office",
            address = "本多5-10-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700922, 139.477735),
            name = "国分寺本町Post Office",
            address = "本町4-7-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.697894, 139.433655),
            name = "国立NishiPost Office",
            address = "Nishi1-8-34",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.693394, 139.431433),
            name = "Central郵政研修所Post Office",
            address = "Nishi2-18-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.681285, 139.441488),
            name = "国立天神下Post Office",
            address = "谷保5859",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.680146, 139.431489),
            name = "国立谷保Post Office",
            address = "谷保6249",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.696755, 139.446404),
            name = "国立Station FrontPost Office",
            address = "中1-17-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.69695, 139.448571),
            name = "国立旭通Post Office",
            address = "Higashi1-15-33",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.690589, 139.451848),
            name = "国立HigashiPost Office",
            address = "Higashi4-4-29",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.684646, 139.447571),
            name = "国立Fujimi台Post Office",
            address = "Fujimi台1-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.686673, 139.442127),
            name = "国立Post Office",
            address = "Fujimi台2-43-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.700949, 139.429489),
            name = "国立KitaPost Office",
            address = "Kita3-24-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.746302, 139.326496),
            name = "福生加美Post Office",
            address = "加美平1-6-10",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.714723, 139.335969),
            name = "福生熊川MinamiPost Office",
            address = "熊川161-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725499, 139.33794),
            name = "福生熊川Post Office",
            address = "熊川545-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.732915, 139.331413),
            name = "福生牛浜Post Office",
            address = "熊川987",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.744358, 139.333218),
            name = "福生武蔵野台Post Office",
            address = "福生2126",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.739247, 139.32633),
            name = "福生Post Office",
            address = "本町77-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.629045, 139.58509),
            name = "狛江岩戸MinamiPost Office",
            address = "岩戸Minami2-19-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.645986, 139.572645),
            name = "狛江Nishi野川Post Office",
            address = "Nishi野川4-2-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.639487, 139.568118),
            name = "狛江中和泉Post Office",
            address = "中和泉5-3-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.645959, 139.580644),
            name = "狛江Higashi野川Post Office",
            address = "Higashi野川3-6-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.632211, 139.577618),
            name = "狛江Station FrontPost Office",
            address = "Higashi和泉1-16-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.62674, 139.57523),
            name = "和泉多摩川Station FrontPost Office",
            address = "Higashi和泉3-5-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.642042, 139.57484),
            name = "狛江Post Office",
            address = "和泉本町3-29-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.754498, 139.413016),
            name = "Higashi大和芋窪Post Office",
            address = "芋窪3-1731-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.740944, 139.437875),
            name = "Higashi大和向原Post Office",
            address = "向原3-816-60",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.744796, 139.415375),
            name = "Higashi大和上Kita台Post Office",
            address = "上Kita台1-4-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.736473, 139.445208),
            name = "Higashi大和新堀Post Office",
            address = "新堀3-11-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.754971, 139.44318),
            name = "武蔵大和Station FrontPost Office",
            address = "清水3-799",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.745444, 139.450207),
            name = "Higashi大和清水Post Office",
            address = "清水6-1190-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.75011, 139.42657),
            name = "大和Post Office",
            address = "奈良橋5-775",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.735972, 139.432654),
            name = "Higashi大和Minami街Post Office",
            address = "Minami街5-64-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.794913, 139.541115),
            name = "清瀬旭が丘Post Office",
            address = "旭が丘2-5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.776748, 139.518173),
            name = "清瀬Post Office",
            address = "元町2-28-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.771026, 139.520367),
            name = "清瀬Station FrontPost Office",
            address = "松山1-4-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.772526, 139.512507),
            name = "清瀬松山Post Office",
            address = "松山3-1-27",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.776859, 139.533838),
            name = "清瀬中清戸Post Office",
            address = "中清戸5-83-278",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.785336, 139.517926),
            name = "清瀬中里Post Office",
            address = "中里4-825",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.778025, 139.500535),
            name = "清瀬野塩Post Office",
            address = "野塩1-194-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.758222, 139.507785),
            name = "Higashi久留米本村Post Office",
            address = "下里1-11-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752306, 139.498619),
            name = "Higashi久留米下里Post Office",
            address = "下里3-17-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749057, 139.533839),
            name = "Higashi久留米学園町Post Office",
            address = "学園町2-1-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.763833, 139.516729),
            name = "Higashi久留米小山Post Office",
            address = "小山5-2-26",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77011, 139.543087),
            name = "Higashi久留米団地Post Office",
            address = "上の原1-4-28-115",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742557, 139.513841),
            name = "Higashi久留米前沢Post Office",
            address = "前沢3-7-9",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.764444, 139.53606),
            name = "Higashi久留米大門Post Office",
            address = "大門町2-6-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.745612, 139.504591),
            name = "Higashi久留米滝山Post Office",
            address = "滝山4-1-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.758806, 139.526172),
            name = "Higashi久留米Post Office",
            address = "Central町1-1-44",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.753056, 139.515951),
            name = "Higashi久留米Central町Post Office",
            address = "Central町5-9-24",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.743419, 139.525673),
            name = "Higashi久留米Minami沢GoPost Office",
            address = "Minami沢5-18-48",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.761528, 139.530616),
            name = "Higashi久留米本町Post Office",
            address = "本町1-2-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.748276, 139.405406),
            name = "武蔵村山Post Office",
            address = "学園3-24-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752245, 139.377243),
            name = "武蔵村山Sanツ藤Post Office",
            address = "Sanツ藤2-36-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737583, 139.401157),
            name = "武蔵村山大MinamiPost Office",
            address = "大Minami3-4-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.735055, 139.411128),
            name = "武蔵村山大MinamiYonPost Office",
            address = "大Minami4-61-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.757921, 139.36431),
            name = "武蔵村山中原Post Office",
            address = "中原2-8-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.755608, 139.387129),
            name = "村山Post Office",
            address = "本町4-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.744165, 139.408683),
            name = "村山団地Post Office",
            address = "緑が丘1460",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.628013, 139.427131),
            name = "多摩CenterPost Office",
            address = "愛宕4-17-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.628174, 139.448674),
            name = "永山Station FrontPost Office",
            address = "永山1-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.621514, 139.450518),
            name = "多摩永山Post Office",
            address = "永山4-2-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.623557, 139.4382),
            name = "多摩貝取KitaPost Office",
            address = "貝取1-45-1-105",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.614959, 139.439241),
            name = "多摩貝取Post Office",
            address = "貝取4-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.652066, 139.447322),
            name = "せいせきＣBuildingPost Office",
            address = "関戸1-7-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.641178, 139.450818),
            name = "多摩関戸Post Office",
            address = "関戸5-11-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.641095, 139.444184),
            name = "多摩桜ヶ丘Post Office",
            address = "桜ヶ丘4-1-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.632235, 139.45735),
            name = "多摩聖ケ丘Post Office",
            address = "聖ヶ丘2-20-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.621874, 139.421659),
            name = "多摩Post Office",
            address = "鶴牧1-24-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.611542, 139.42377),
            name = "多摩鶴牧Post Office",
            address = "鶴牧5-2-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.615597, 139.411688),
            name = "唐木田Station FrontPost Office",
            address = "唐木田1-1-22",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.648458, 139.446143),
            name = "聖蹟桜ケ丘Post Office",
            address = "Higashi寺方1-2-13",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.616542, 139.430242),
            name = "多摩落合Post Office",
            address = "落合3-17-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.635623, 139.436129),
            name = "多摩和田Post Office",
            address = "和田3-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.639902, 139.487459),
            name = "稲城向陽台Post Office",
            address = "向陽台3-7-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.619934, 139.472273),
            name = "稲城若葉台Post Office",
            address = "若葉台2-4-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.646198, 139.5083),
            name = "稲城押立Post Office",
            address = "Higashi長沼384-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.645847, 139.501846),
            name = "稲城長沼Post Office",
            address = "Higashi長沼450",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.636404, 139.499569),
            name = "稲城Station FrontPost Office",
            address = "百村1612-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.609825, 139.488544),
            name = "稲城平尾Post Office",
            address = "平尾3-1-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.63921, 139.519707),
            name = "矢野口Post Office",
            address = "矢野口636",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.767354, 139.300136),
            name = "羽村加美Post Office",
            address = "羽加美3-5-28",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.756272, 139.308775),
            name = "羽村MinamiPost Office",
            address = "羽Higashi3-8-28",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.760911, 139.321218),
            name = "羽村FujimiPost Office",
            address = "Goノ神2-8-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.774556, 139.30002),
            name = "羽村小作台Post Office",
            address = "小作台5-5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.767846, 139.311592),
            name = "羽村Post Office",
            address = "緑ヶ丘5-3-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.726691, 139.253253),
            name = "増戸Post Office",
            address = "伊奈1044-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.734579, 139.255864),
            name = "Go日市伊奈Post Office",
            address = "伊奈466-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.733665, 139.183037),
            name = "乙津Post Office",
            address = "乙津1997",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.733429, 139.229848),
            name = "武蔵Go日市Station FrontPost Office",
            address = "舘谷台25-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727887, 139.222617),
            name = "Go日市仲町Post Office",
            address = "Go日市35",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.731899, 139.285046),
            name = "あきる野Post Office",
            address = "秋川3-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.720222, 139.319164),
            name = "あきる野小川Post Office",
            address = "小川Higashi2-11-14",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.73858, 139.303942),
            name = "多NishiPost Office",
            address = "草花3059",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727874, 139.315853),
            name = "Higashi秋留Post Office",
            address = "Ni宮2306-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.724082, 139.310137),
            name = "秋川野辺Post Office",
            address = "野辺384",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725914, 139.288028),
            name = "Nishi秋留Post Office",
            address = "油平99-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725775, 139.273918),
            name = "秋川渕上Post Office",
            address = "渕上192-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752751, 139.54606),
            name = "ひばりが丘KitaPost Office",
            address = "ひばりが丘Kita3-5-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.752668, 139.558753),
            name = "下保谷NiPost Office",
            address = "下保谷2-4-12",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749447, 139.566781),
            name = "保谷Station FrontPost Office",
            address = "下保谷4-15-11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72606, 139.531479),
            name = "田無芝久保Post Office",
            address = "芝久保町1-3-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.727408, 139.526062),
            name = "田無芝久保NiPost Office",
            address = "芝久保町2-14-33",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.732531, 139.522868),
            name = "田無Kita芝久保Post Office",
            address = "芝久保町4-14-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742697, 139.550699),
            name = "保谷住吉Post Office",
            address = "住吉町1-2-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.741725, 139.53295),
            name = "田無Nishi原Post Office",
            address = "Nishi原町5-4-17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.749668, 139.544005),
            name = "ひばりが丘Post Office",
            address = "谷戸町3-25-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737337, 139.566198),
            name = "保谷中町YonPost Office",
            address = "中町4-8-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.73029, 139.54031),
            name = "NishiHigashi京Post Office",
            address = "田無町3-2-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.745308, 139.561864),
            name = "保谷Higashi町Post Office",
            address = "Higashi町1-5-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.724394, 139.560893),
            name = "保谷Higashi伏見Post Office",
            address = "Higashi伏見6-6-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.725865, 139.545922),
            name = "田無Minami町NiPost Office",
            address = "Minami町2-1-15",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.722282, 139.536506),
            name = "田無向台Post Office",
            address = "Minami町5-14-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.730449, 139.56592),
            name = "保谷富士町Post Office",
            address = "富士町4-5-23",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.737524, 139.553871),
            name = "保谷Post Office",
            address = "保谷町1-1-7",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.729245, 139.553485),
            name = "柳沢Station FrontPost Office",
            address = "保谷町3-10-16",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.742114, 139.542505),
            name = "田無緑町Post Office",
            address = "緑町3-5-19",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.760761, 139.337701),
            name = "瑞穂むさし野Post Office",
            address = "むさし野2-54-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.772661, 139.356659),
            name = "瑞穂Post Office",
            address = "石畑1990-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.77927, 139.329495),
            name = "瑞穂長岡Post Office",
            address = "長岡4-1-4",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.744856, 139.235003),
            name = "大久野Post Office",
            address = "大久野1177",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.740912, 139.266196),
            name = "平井Post Office",
            address = "平井1186",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.72683, 139.149152),
            name = "檜原Post Office",
            address = "467",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.786793, 139.042273),
            name = "小河Post Office",
            address = "原71",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.815708, 139.149816),
            name = "古里Post Office",
            address = "小丹波109",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.841866, 139.044661),
            name = "日原簡易Post Office",
            address = "日原760",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(35.805793, 139.094905),
            name = "奥多摩Post Office",
            address = "氷川1379-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.783061, 139.387547),
            name = "岡田Post Office",
            address = "岡田榎戸17-18",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.750675, 139.356217),
            name = "大島Post Office",
            address = "元町4-1-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.775478, 139.363244),
            name = "大島Kitaの山簡易Post Office",
            address = "元町風待31-8",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.682934, 139.413993),
            name = "差木地Post Office",
            address = "差木地1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.783089, 139.413656),
            name = "泉津Post Office",
            address = "泉津28-5",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.689766, 139.440768),
            name = "波浮港Post Office",
            address = "波浮港17",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.730288, 139.35483),
            name = "野増Post Office",
            address = "野増11",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.529879, 139.278279),
            name = "利島Post Office",
            address = "21",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.327715, 139.215766),
            name = "式根島Post Office",
            address = "式根島160",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.42198, 139.283476),
            name = "若郷Post Office",
            address = "若郷5-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.37718, 139.256518),
            name = "新島Post Office",
            address = "本村1-7-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.203719, 139.135999),
            name = "神津島Post Office",
            address = "1114",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.064844, 139.482933),
            name = "San宅島阿古Post Office",
            address = "阿古700-6",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.100323, 139.492905),
            name = "San宅島伊ヶ谷Post Office",
            address = "伊ヶ谷432",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.11398, 139.50077),
            name = "San宅島伊豆Post Office",
            address = "伊豆1054",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.121574, 139.525137),
            name = "San宅島Post Office",
            address = "神着222",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(34.060191, 139.546239),
            name = "坪田Post Office",
            address = "坪田3007",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(33.896532, 139.591655),
            name = "御蔵島Post Office",
            address = "詳細住所不明",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(33.070939, 139.796097),
            name = "Hachi丈島樫立Post Office",
            address = "樫立365-1",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(33.115517, 139.800566),
            name = "San根川向簡易Post Office",
            address = "San根1830",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(33.122267, 139.802399),
            name = "San根Post Office",
            address = "San根433-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(33.103629, 139.788957),
            name = "Hachi丈島Post Office",
            address = "大賀郷1255",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(33.067746, 139.80818),
            name = "中ノ郷Post Office",
            address = "中之郷2571-2",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(33.082724, 139.852684),
            name = "末吉Post Office",
            address = "末吉791-3",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(32.46634, 139.758464),
            name = "青ケ島Post Office",
            address = "詳細住所不明",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(27.094886, 142.191631),
            name = "小笠原Post Office",
            address = "父島Nishi町",
        ),
        PostOffice(
            position = GeoPointImpl.fromLatLong(26.640098, 142.160847),
            name = "母島簡易Post Office",
            address = "母島元地",
        ),
    )
