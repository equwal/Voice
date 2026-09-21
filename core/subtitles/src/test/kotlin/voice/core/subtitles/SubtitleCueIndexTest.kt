package voice.core.subtitles

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubtitleCueIndexTest {

  @Test
  fun `empty cue list has no cue at any position`() {
    val index = SubtitleCueIndex(emptyList())

    assertNull(index.cueAt(0L))
    assertNull(index.cueAt(1_000L))
  }

  @Test
  fun `finds the cue that covers the position`() {
    val cues = listOf(
      SubtitleCue(startMs = 0L, endMs = 1_000L, text = "A"),
      SubtitleCue(startMs = 1_000L, endMs = 2_000L, text = "B"),
      SubtitleCue(startMs = 3_000L, endMs = 4_000L, text = "C"),
    )
    val index = SubtitleCueIndex(cues)

    assertEquals(expected = "A", actual = index.cueAt(500L)?.text)
    assertEquals(expected = "B", actual = index.cueAt(1_500L)?.text)
    assertEquals(expected = "C", actual = index.cueAt(4_000L)?.text)
  }

  @Test
  fun `position in a gap between cues has no cue`() {
    val cues = listOf(
      SubtitleCue(startMs = 0L, endMs = 1_000L, text = "A"),
      SubtitleCue(startMs = 3_000L, endMs = 4_000L, text = "B"),
    )
    val index = SubtitleCueIndex(cues)

    assertNull(index.cueAt(2_000L))
  }

  @Test
  fun `position before the first cue has no cue`() {
    val index = SubtitleCueIndex(listOf(SubtitleCue(startMs = 5_000L, endMs = 6_000L, text = "A")))

    assertNull(index.cueAt(0L))
  }

  @Test
  fun `overlapping cues, the latest one that is still on wins`() {
    val cues = listOf(
      SubtitleCue(startMs = 0L, endMs = 10_000L, text = "long running"),
      SubtitleCue(startMs = 2_000L, endMs = 3_000L, text = "short interjection"),
    )
    val index = SubtitleCueIndex(cues)

    // Before the short cue starts, the long one is the only cue on.
    assertEquals(expected = "long running", actual = index.cueAt(1_000L)?.text)
    // While both are on, the one with the later start wins.
    assertEquals(expected = "short interjection", actual = index.cueAt(2_500L)?.text)
    // After the short cue ends, the long one is on again.
    assertEquals(expected = "long running", actual = index.cueAt(5_000L)?.text)
  }

  @Test
  fun `input order does not matter`() {
    val cues = listOf(
      SubtitleCue(startMs = 5_000L, endMs = 6_000L, text = "later"),
      SubtitleCue(startMs = 0L, endMs = 1_000L, text = "earlier"),
    )
    val index = SubtitleCueIndex(cues)

    assertEquals(expected = "earlier", actual = index.cueAt(500L)?.text)
    assertEquals(expected = "later", actual = index.cueAt(5_500L)?.text)
  }

  /**
   * Property test: for many random cue lists (including heavy overlap) and random query
   * positions, the binary-search lookup must agree with a plain linear scan that applies the same
   * "latest cue still on wins" rule. A fixed seed keeps the test reproducible.
   */
  @Test
  fun `binary search agrees with linear scan on random cue lists`() {
    val random = Random(42)
    repeat(200) {
      val cueCount = random.nextInt(0, 50)
      val cues = List(cueCount) {
        val start = random.nextLong(0L, 10_000L)
        val end = start + random.nextLong(1L, 2_000L)
        SubtitleCue(startMs = start, endMs = end, text = "cue-$it")
      }
      val index = SubtitleCueIndex(cues)

      repeat(50) {
        val position = random.nextLong(-1_000L, 12_000L)

        val expected = linearScan(cues, position)
        val actual = index.cueAt(position)

        assertEquals(
          expected = expected,
          actual = actual,
          message = "cues=$cues position=$position",
        )
      }
    }
  }

  /**
   * Reference implementation: sort by start (stable, so cues with an identical start keep their
   * relative order), keep the ones covering [positionMs], and take the last one. That is the cue
   * with the latest start still on, matching [SubtitleCueIndex.cueAt] tie for tie.
   */
  private fun linearScan(
    cues: List<SubtitleCue>,
    positionMs: Long,
  ): SubtitleCue? {
    return cues
      .sortedBy { it.startMs }
      .filter { positionMs in it.startMs..it.endMs }
      .lastOrNull()
  }
}
