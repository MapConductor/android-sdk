package com.mapconductor.core.homography

data class PointD(
    val x: Double,
    val y: Double
)

data class CalcHomographyMatrixOptions(
    val farLeftPx: PointD,
    val farRightPx: PointD,
    val nearRightPx: PointD,
    val nearLeftPx: PointD
)

/**
 * Create a homography matrix from given 4 coordinate points.
 * ref: https://mu-777.hatenablog.com/entry/2020/02/02/185012
 *
 * 戻り値は 3x3 行列を row-major ( [h11,h12,h13,h21,...] ) で並べた DoubleArray。
 */
fun calcHomographyMatrix(opt: CalcHomographyMatrixOptions): DoubleArray {
    val x00 = opt.farLeftPx.x
    val y00 = opt.farLeftPx.y
    val x01 = opt.nearLeftPx.x
    val y01 = opt.nearLeftPx.y
    val x10 = opt.farRightPx.x
    val y10 = opt.farRightPx.y
    val x11 = opt.nearRightPx.x
    val y11 = opt.nearRightPx.y

    val a = x10 - x11
    val b = x01 - x11
    val c = x00 - x01 - x10 + x11
    val d = y10 - y11
    val e = y01 - y11
    val f = y00 - y01 - y10 + y11

    val h13 = x00
    val h23 = y00
    val h32 = (c * d - a * f) / (b * d - a * e)
    val h31 = (c * e - b * f) / (a * e - b * d)
    val h11 = x10 - x00 + h31 * x10
    val h12 = x01 - x00 + h32 * x01
    val h21 = y10 - y00 + h31 * y10
    val h22 = y01 - y00 + h32 * y01

    return doubleArrayOf(
        h11, h12, h13,
        h21, h22, h23,
        h31, h32, 1.0
    )
}

/**
 * 3x3 行列（row-major, サイズ9）を逆行列にして返す。
 * 入力は [h11,h12,h13,h21,h22,h23,h31,h32,h33] を想定（元コードは h33=1 前提）。
 */
fun calcInverseMatrix(mat: DoubleArray): DoubleArray {
    require(mat.size >= 9) { "mat must have at least 9 elements (3x3 row-major)" }

    val i11 = mat[0]
    val i12 = mat[1]
    val i13 = mat[2]
    val i21 = mat[3]
    val i22 = mat[4]
    val i23 = mat[5]
    val i31 = mat[6]
    val i32 = mat[7]
    val i33 = mat[8] // TypeScript では 1 固定だが、Kotlin 側は与えられた値を使う

    val det =
        (i11 * i22 * i33) +
            (i12 * i23 * i31) +
            (i13 * i21 * i32) -
            (i13 * i22 * i31) -
            (i12 * i21 * i33) -
            (i11 * i23 * i32)

    // TypeScript の a は「1 / det」に相当していたので、そのまま踏襲
    val a = 1.0 / det

    val o11 = (i22 * i33 - i23 * i32) / a
    val o12 = (-i12 * i33 + i13 * i32) / a
    val o13 = (i12 * i23 - i13 * i22) / a
    val o21 = (-i21 * i33 + i23 * i31) / a
    val o22 = (i11 * i33 - i13 * i31) / a
    val o23 = (-i11 * i23 + i13 * i21) / a
    val o31 = (i21 * i32 - i22 * i31) / a
    val o32 = (-i11 * i32 + i12 * i31) / a
    val o33 = (i11 * i22 - i12 * i21) / a

    return doubleArrayOf(
        o11, o12, o13,
        o21, o22, o23,
        o31, o32, o33
    )
}

fun applyMatrix(pos: PointD, matrix: DoubleArray): PointD {
    val s = matrix[6] * pos.x + matrix[7] * pos.y + matrix[8]
    val x = (matrix[0] * pos.x + matrix[1] * pos.y + matrix[2]) / s
    val y = (matrix[3] * pos.x + matrix[4] * pos.y + matrix[5]) / s
    return PointD(x, y)
}
