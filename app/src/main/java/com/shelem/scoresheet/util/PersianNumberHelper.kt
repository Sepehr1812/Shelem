package com.shelem.scoresheet.util

import java.util.Locale

object PersianNumberHelper {

    private val englishToPersianDigits = mapOf(
        '0' to '۰',
        '1' to '۱',
        '2' to '۲',
        '3' to '۳',
        '4' to '۴',
        '5' to '۵',
        '6' to '۶',
        '7' to '۷',
        '8' to '۸',
        '9' to '۹'
    )

    private fun isPersianLocale(): Boolean {
        return Locale.getDefault().language.startsWith("fa", ignoreCase = true)
    }

    fun String.toPersianDigits(): String {
        if (!isPersianLocale()) return this
        return this.map { char ->
            englishToPersianDigits[char] ?: char
        }.joinToString("")
    }

    fun Int.toPersianDigits(): String {
        return if (this < 0 && isPersianLocale()) {
            "${kotlin.math.abs(this).toString().toPersianDigits()}-"
        } else {
            this.toString().toPersianDigits()
        }
    }

    fun Long.toPersianDigits(): String {
        return if (this < 0 && isPersianLocale()) {
            "${kotlin.math.abs(this).toString().toPersianDigits()}-"
        } else {
            this.toString().toPersianDigits()
        }
    }

    fun formatScore(score: Int): String {
        return when {
            score < 0 -> {
                val absVal = kotlin.math.abs(score)
                if (isPersianLocale()) {
                    "${absVal.toString().toPersianDigits()}-"
                } else {
                    "-$absVal"
                }
            }
            else -> {
                score.toString().toPersianDigits()
            }
        }
    }

    fun parsePersianInt(input: String): Int? {
        val converted = input.map { char ->
            englishToPersianDigits.entries.firstOrNull { it.value == char }?.key ?: char
        }.joinToString("")
        return converted.toIntOrNull()
    }
}
