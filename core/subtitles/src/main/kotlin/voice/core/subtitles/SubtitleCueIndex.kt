package voice.core.subtitles

/**
 * Looks up the [SubtitleCue] that is on at a given whole-book position, by binary search.
 *
 * When cues overlap, the cue with the latest start time that still covers the position wins.
 */
class SubtitleCueIndex(cues: List<SubtitleCue>) {

  private val sortedCues = cues.sortedBy { it.startMs }

  /**
   * The running maximum end time over `sortedCues[0..i]`. Used to stop the backward scan for
   * overlapping cues as soon as no earlier cue could possibly cover the position any more.
   */
  private val maxEndSoFar = LongArray(sortedCues.size).also { array ->
    var runningMax = Long.MIN_VALUE
    for (index in sortedCues.indices) {
      runningMax = maxOf(runningMax, sortedCues[index].endMs)
      array[index] = runningMax
    }
  }

  fun cueAt(positionMs: Long): SubtitleCue? {
    val lastStartAtOrBeforePosition = lastIndexWithStartAtOrBefore(positionMs)
    var index = lastStartAtOrBeforePosition
    while (index >= 0 && maxEndSoFar[index] >= positionMs) {
      val cue = sortedCues[index]
      if (positionMs in cue.startMs..cue.endMs) {
        return cue
      }
      index--
    }
    return null
  }

  private fun lastIndexWithStartAtOrBefore(positionMs: Long): Int {
    var low = 0
    var high = sortedCues.lastIndex
    var result = -1
    while (low <= high) {
      val mid = (low + high) / 2
      if (sortedCues[mid].startMs <= positionMs) {
        result = mid
        low = mid + 1
      } else {
        high = mid - 1
      }
    }
    return result
  }
}
