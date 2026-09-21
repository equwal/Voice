package voice.core.subtitles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SrtParserTest {

  @Test
  fun `empty input returns no cues`() {
    assertEquals(expected = emptyList(), actual = SrtParser.parse(""))
  }

  @Test
  fun `blank input returns no cues`() {
    assertEquals(expected = emptyList(), actual = SrtParser.parse("   \n\n  \n"))
  }

  @Test
  fun `single cue with dot decimal separator`() {
    val srt = """
      1
      00:00:01.000 --> 00:00:04.500
      Hello there
    """.trimIndent()

    val cues = SrtParser.parse(srt)

    assertEquals(
      expected = listOf(SubtitleCue(startMs = 1_000L, endMs = 4_500L, text = "Hello there")),
      actual = cues,
    )
  }

  @Test
  fun `single cue with comma decimal separator`() {
    val srt = """
      1
      00:00:01,000 --> 00:00:04,500
      Hello there
    """.trimIndent()

    val cues = SrtParser.parse(srt)

    assertEquals(
      expected = listOf(SubtitleCue(startMs = 1_000L, endMs = 4_500L, text = "Hello there")),
      actual = cues,
    )
  }

  @Test
  fun `multi-line cue text is kept as separate lines`() {
    val srt = """
      1
      00:00:01,000 --> 00:00:04,500
      First line
      Second line
    """.trimIndent()

    val cues = SrtParser.parse(srt)

    assertEquals(expected = "First line\nSecond line", actual = cues.single().text)
  }

  @Test
  fun `leading byte order mark is stripped`() {
    val srt = "﻿1\n00:00:01,000 --> 00:00:02,000\nHello"

    val cues = SrtParser.parse(srt)

    assertEquals(expected = 1, actual = cues.size)
    assertEquals(expected = "Hello", actual = cues.single().text)
  }

  @Test
  fun `CRLF line endings are handled like LF`() {
    val srt = "1\r\n00:00:01,000 --> 00:00:02,000\r\nHello\r\n\r\n2\r\n00:00:03,000 --> 00:00:04,000\r\nWorld"

    val cues = SrtParser.parse(srt)

    assertEquals(expected = listOf("Hello", "World"), actual = cues.map { it.text })
  }

  @Test
  fun `blocks without a timestamp line are skipped`() {
    val srt = """
      This is a stray comment, not a cue.

      1
      00:00:01,000 --> 00:00:02,000
      Real cue
    """.trimIndent()

    val cues = SrtParser.parse(srt)

    assertEquals(expected = listOf("Real cue"), actual = cues.map { it.text })
  }

  @Test
  fun `cues are sorted by start time regardless of input order`() {
    val srt = """
      2
      00:00:10,000 --> 00:00:11,000
      Second

      1
      00:00:01,000 --> 00:00:02,000
      First
    """.trimIndent()

    val cues = SrtParser.parse(srt)

    assertEquals(expected = listOf("First", "Second"), actual = cues.map { it.text })
    assertTrue(cues.zipWithNext().all { (a, b) -> a.startMs <= b.startMs })
  }

  @Test
  fun `unicode subtitle text is preserved`() {
    val srt = """
      1
      00:00:01,000 --> 00:00:02,000
      日本語のテキスト и русский текст и texto en español
    """.trimIndent()

    val cues = SrtParser.parse(srt)

    assertEquals(expected = "日本語のテキスト и русский текст и texto en español", actual = cues.single().text)
  }

  @Test
  fun `a cue with end before start is skipped`() {
    val srt = """
      1
      00:00:05,000 --> 00:00:02,000
      Broken cue
    """.trimIndent()

    assertEquals(expected = emptyList(), actual = SrtParser.parse(srt))
  }

  @Test
  fun `hours are parsed for long audiobooks`() {
    val srt = """
      1
      12:34:56,789 --> 12:35:00,000
      Deep into the book
    """.trimIndent()

    val cues = SrtParser.parse(srt)

    val expectedStartMs = 12 * 3_600_000L + 34 * 60_000L + 56 * 1_000L + 789L
    assertEquals(expected = expectedStartMs, actual = cues.single().startMs)
  }
}
