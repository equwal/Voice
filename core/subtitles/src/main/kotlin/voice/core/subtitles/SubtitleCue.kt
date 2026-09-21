package voice.core.subtitles

/**
 * One subtitle line, with a time span on the whole audiobook clock.
 *
 * [startMs] and [endMs] are milliseconds from the start of the book, not from the start of a
 * single chapter file.
 */
data class SubtitleCue(
  val startMs: Long,
  val endMs: Long,
  val text: String,
)
