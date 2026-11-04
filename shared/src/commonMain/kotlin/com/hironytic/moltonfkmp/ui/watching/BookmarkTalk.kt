package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.Assault
import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkType
import com.hironytic.moltonfkmp.workspace.Bookmark

/**
 * An [Assault] event is displayed as the wolf talk it represents; this converts it to that [Talk].
 */
fun Assault.asWolfTalk(): Talk = Talk(
    elementId = elementId,
    talkType = TalkType.WOLF,
    avatarId = byWhom,
    xname = xname,
    time = time,
    talkNo = null,
    messageLines = messageLines,
)

/**
 * Finds the talk (or assault displayed as a wolf talk) identified by [elementId] on [day],
 * along with its position within the day.
 */
fun findBookmarkableTalk(story: Story, day: Int, elementId: String): Pair<Int, Talk>? {
    val period = story.periods.getOrNull(day) ?: return null
    val index = period.elements.indexOfFirst { it.elementId == elementId }
    if (index < 0) return null
    val talk = when (val element = period.elements[index]) {
        is Talk -> element
        is Assault -> element.asWolfTalk()
        else -> return null
    }
    return index to talk
}

/** A [Bookmark] resolved against the story, for display. */
data class BookmarkItem(val bookmark: Bookmark, val talk: Talk)

/**
 * Resolves [bookmarks] to [BookmarkItem]s in story order (by day, then by position in the day).
 * Bookmarks whose talk cannot be found are dropped.
 */
fun resolveBookmarks(story: Story, bookmarks: List<Bookmark>): List<BookmarkItem> =
    bookmarks
        .mapNotNull { bookmark ->
            findBookmarkableTalk(story, bookmark.day, bookmark.elementId)?.let { (index, talk) ->
                Triple(bookmark.day, index, BookmarkItem(bookmark, talk))
            }
        }
        .sortedWith(compareBy({ it.first }, { it.second }))
        .map { it.third }
