package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.GameEntity
import com.example.data.RoundEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface CsvStringProvider {
    fun getString(resId: Int): String
    fun getString(resId: Int, vararg formatArgs: Any): String
}

class ContextCsvStringProvider(private val context: Context) : CsvStringProvider {
    override fun getString(resId: Int): String = context.getString(resId)
    override fun getString(resId: Int, vararg formatArgs: Any): String = context.getString(resId, *formatArgs)
}

object CsvExporter {

    fun escapeCsv(value: Any?): String {
        val str = value?.toString() ?: ""
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"${str.replace("\"", "\"\"")}\""
        } else {
            str
        }
    }

    fun getDefaultFileName(game: GameEntity): String {
        val dateStr = try {
            SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date(game.createdAt))
        } catch (_: Exception) {
            "${game.createdAt}"
        }
        val cleanTeam1 = game.team1Name.replace("[^a-zA-Z0-9\\u0600-\\u06FF]".toRegex(), "_")
        val cleanTeam2 = game.team2Name.replace("[^a-zA-Z0-9\\u0600-\\u06FF]".toRegex(), "_")
        return "shelem_${cleanTeam1}_vs_${cleanTeam2}_$dateStr.csv"
    }

    fun generateCsv(
        game: GameEntity,
        rounds: List<RoundEntity>,
        context: Context
    ): String = generateCsv(game, rounds, ContextCsvStringProvider(context))

    fun generateCsv(
        game: GameEntity,
        rounds: List<RoundEntity>,
        strings: CsvStringProvider
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM so Microsoft Excel and Google Sheets immediately recognize UTF-8 Persian and English characters
        sb.append("\uFEFF")

        val completedRounds = rounds.filter { it.isCompleted && it.status != "Ongoing" }
            .sortedBy { it.roundNumber }
        val totalT1Score = completedRounds.sumOf { it.team1FinalScore }
        val totalT2Score = completedRounds.sumOf { it.team2FinalScore }
        val scoreDiff = kotlin.math.abs(totalT1Score - totalT2Score)

        val isGameDone = game.isFinished ||
            totalT1Score >= game.targetScore ||
            totalT2Score >= game.targetScore ||
            scoreDiff >= game.targetScore

        val winnerName = when {
            totalT1Score >= game.targetScore || (totalT1Score - totalT2Score >= game.targetScore) || (isGameDone && totalT1Score > totalT2Score) -> game.team1Name
            totalT2Score >= game.targetScore || (totalT2Score - totalT1Score >= game.targetScore) || (isGameDone && totalT2Score > totalT1Score) -> game.team2Name
            else -> "-"
        }

        val gameStatus = if (isGameDone) {
            strings.getString(R.string.csv_status_finished)
        } else {
            strings.getString(R.string.csv_status_ongoing)
        }

        val rule1000Text = if (game.winHandOnlyOver1000) {
            strings.getString(R.string.csv_field_active)
        } else {
            strings.getString(R.string.csv_field_inactive)
        }

        val trackAceText = if (game.trackAceInGround) {
            strings.getString(R.string.csv_field_active)
        } else {
            strings.getString(R.string.csv_field_inactive)
        }

        val formattedDate = try {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(game.createdAt))
        } catch (_: Exception) {
            game.createdAt.toString()
        }

        // --- SECTION 1: GAME METADATA ---
        sb.append(escapeCsv(strings.getString(R.string.csv_game_summary_header))).append("\n")
        sb.append(escapeCsv(strings.getString(R.string.csv_field_game_id))).append(",").append(escapeCsv(game.id)).append("\n")
        sb.append(escapeCsv(strings.getString(R.string.csv_field_date))).append(",").append(escapeCsv(formattedDate)).append("\n")
        sb.append(escapeCsv(strings.getString(R.string.csv_field_target_score))).append(",").append(escapeCsv(game.targetScore)).append("\n")
        sb.append(escapeCsv(strings.getString(R.string.csv_field_rule_1000))).append(",").append(escapeCsv(rule1000Text)).append("\n")
        sb.append(escapeCsv(strings.getString(R.string.csv_field_track_ace))).append(",").append(escapeCsv(trackAceText)).append("\n")
        sb.append(escapeCsv(strings.getString(R.string.csv_field_status))).append(",").append(escapeCsv(gameStatus)).append("\n")
        sb.append(escapeCsv(strings.getString(R.string.csv_field_winner))).append(",").append(escapeCsv(winnerName)).append("\n")
        sb.append("\n")

        // Teams Summary
        sb.append(escapeCsv(strings.getString(R.string.team1_default_name))).append(",")
            .append(escapeCsv(game.team1Name)).append(",")
            .append(escapeCsv("${game.team1Player1Name} - ${game.team1Player2Name}")).append(",")
            .append(escapeCsv(totalT1Score)).append("\n")

        sb.append(escapeCsv(strings.getString(R.string.team2_default_name))).append(",")
            .append(escapeCsv(game.team2Name)).append(",")
            .append(escapeCsv("${game.team2Player1Name} - ${game.team2Player2Name}")).append(",")
            .append(escapeCsv(totalT2Score)).append("\n")
        sb.append("\n")

        // --- SECTION 2: ROUND-BY-ROUND BREAKDOWN TABLE ---
        val headers = mutableListOf(
            strings.getString(R.string.csv_header_round),
            strings.getString(R.string.csv_header_hakem_team),
            strings.getString(R.string.csv_header_hakem_player),
            strings.getString(R.string.csv_header_dealer),
            strings.getString(R.string.csv_header_bid),
            strings.getString(R.string.csv_header_claim_type),
            strings.getString(R.string.csv_header_t1_raw, game.team1Name),
            strings.getString(R.string.csv_header_t2_raw, game.team2Name),
            strings.getString(R.string.csv_header_t1_score, game.team1Name),
            strings.getString(R.string.csv_header_t2_score, game.team2Name),
            strings.getString(R.string.csv_header_t1_total, game.team1Name),
            strings.getString(R.string.csv_header_t2_total, game.team2Name),
            strings.getString(R.string.csv_header_result)
        )
        if (game.trackAceInGround) {
            headers.add(strings.getString(R.string.csv_header_ace_in_ground))
        }
        sb.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

        var cumT1 = 0
        var cumT2 = 0

        completedRounds.forEach { round ->
            cumT1 += round.team1FinalScore
            cumT2 += round.team2FinalScore

            val hakemTeamName = if (round.hakemTeam == 1) game.team1Name else game.team2Name
            val claimType = when {
                round.isNaras -> strings.getString(R.string.csv_claim_naras)
                round.bidAmount >= 165 -> strings.getString(R.string.csv_claim_shelem)
                else -> strings.getString(R.string.csv_claim_normal)
            }

            val statusLabel = when (round.status) {
                "Shelem" -> strings.getString(R.string.status_shelem_card)
                "NegativeShelem" -> strings.getString(R.string.status_negative_shelem_card)
                "BidAccomplished" -> strings.getString(R.string.status_bid_accomplished)
                "BidFailed" -> strings.getString(R.string.status_bid_failed)
                else -> round.status
            }

            val row = mutableListOf<Any>(
                round.roundNumber,
                hakemTeamName,
                round.hakemPlayerName,
                round.dealerPlayerName,
                round.bidAmount,
                claimType,
                round.team1CollectedRaw,
                round.team2CollectedRaw,
                round.team1FinalScore,
                round.team2FinalScore,
                cumT1,
                cumT2,
                statusLabel
            )
            if (game.trackAceInGround) {
                row.add(
                    if (round.hasAceInGround) strings.getString(R.string.csv_ace_yes) else strings.getString(R.string.csv_ace_no)
                )
            }

            sb.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
        }

        return sb.toString()
    }

    fun writeCsvToUri(context: Context, uri: Uri, csvContent: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareCsvFile(context: Context, game: GameEntity, csvContent: String): Boolean {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, getDefaultFileName(game))
            file.writeBytes(csvContent.toByteArray(Charsets.UTF_8))

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    context.getString(R.string.export_csv_share_subject, game.team1Name, game.team2Name)
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, context.getString(R.string.export_csv_share)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
