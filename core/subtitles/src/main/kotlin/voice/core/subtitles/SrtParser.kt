package voice.core.subtitles

/**
 * Reads SubRip (`.srt`) subtitle text into [SubtitleCue]s.
 *
 * The parser is lenient. A block that has no valid time line is skipped instead of failing the
 * whole file, because subtitle files in the wild carry stray index numbers, comments, or broken
 * blocks.
 */
object SrtParser {

  private val TIMESTAMP_LINE = Regex(
    """(\d{1,2}):(\d{2}):(\d{2})[.,](\d{1,3})\s*-->\s*(\d{1,2}):(\d{2}):(\d{2})[.,](\d{1,3})""",
  )

  /**
   * Parses [content] into cues, sorted by start time.
   *
   * Handles a leading UTF-8 byte order mark, CRLF and LF line endings, and `.` or `,` as the
   * decimal separator in a timestamp. Returns an empty list for empty or fully invalid input.
   */
  fun parse(content: String): List<SubtitleCue> {
    val byteOrderMark = '﻿'
    val normalized = content
      .removePrefix(byteOrderMark.toString())
      .replace("\r\n", "\n")
      .replace('\r', '\n')
    return normalized
      .split(Regex("\n{2,}"))
      .mapNotNull(::parseBlock)
      .sortedBy { it.startMs }
  }

  private fun parseBlock(block: String): SubtitleCue? {
    val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val timestampLineIndex = lines.indexOfFirst { TIMESTAMP_LINE.containsMatchIn(it) }
    if (timestampLineIndex == -1) return null
    val match = TIMESTAMP_LINE.find(lines[timestampLineIndex]) ?: return null
    val groups = match.groupValues
    val startMs = toMillis(hours = groups[1], minutes = groups[2], seconds = groups[3], millisPart = groups[4])
    val endMs = toMillis(hours = groups[5], minutes = groups[6], seconds = groups[7], millisPart = groups[8])
    if (endMs <= startMs) return null
    val text = lines.drop(timestampLineIndex + 1).joinToString("\n")
    if (text.isEmpty()) return null
    return SubtitleCue(startMs = startMs, endMs = endMs, text = text)
  }

  private fun toMillis(
    hours: String,
    minutes: String,
    seconds: String,
    millisPart: String,
  ): Long {
    val millis = millisPart.padEnd(length = 3, padChar = '0').take(3).toLong()
    return hours.toLong() * HOUR_MS + minutes.toLong() * MINUTE_MS + seconds.toLong() * SECOND_MS + millis
  }

  private const val HOUR_MS = 3_600_000L
  private const val MINUTE_MS = 60_000L
  private const val SECOND_MS = 1_000L
}
