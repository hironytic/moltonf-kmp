package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkWithDay

/**
 * One node of the expandable tree shown in the talk-thread modal: a single [talk], plus
 * whichever of its own talk-mention links the viewer has expanded so far. Each expanded link
 * is keyed by an occurrence key (see [MessageSegment]-derived keys) and holds the entries
 * that were inserted directly below this one when that link was tapped.
 */
data class TalkThreadEntry(
    val id: String,
    val day: Int,
    val talk: Talk,
    val expandedLinks: Map<String, List<TalkThreadEntry>> = emptyMap(),
)

/**
 * Creates a new root entry for [talk] with [linkKey] already expanded to [targets] — this is
 * how the talk-thread modal opens: the tapped talk plus the talk(s) it links to.
 */
fun createTalkThreadRoot(
    id: String,
    day: Int,
    talk: Talk,
    linkKey: String,
    targets: List<TalkWithDay>,
    nextId: () -> String,
): TalkThreadEntry = TalkThreadEntry(
    id = id,
    day = day,
    talk = talk,
    expandedLinks = mapOf(linkKey to targets.map { TalkThreadEntry(nextId(), it.day, it.talk) }),
)

/**
 * Toggles the expansion of [linkKey] on the entry reached by descending [path] from this
 * (root) entry. An empty [path] means "this entry itself". If that link is already expanded,
 * it (and everything nested under it) collapses; otherwise it expands to [targets].
 */
fun TalkThreadEntry.toggleLink(
    path: List<String>,
    linkKey: String,
    targets: List<TalkWithDay>,
    nextId: () -> String,
): TalkThreadEntry {
    if (path.isEmpty()) {
        return if (linkKey in expandedLinks) {
            copy(expandedLinks = expandedLinks - linkKey)
        } else {
            copy(expandedLinks = expandedLinks + (linkKey to targets.map { TalkThreadEntry(nextId(), it.day, it.talk) }))
        }
    }

    val childId = path.first()
    val restOfPath = path.drop(1)
    val newExpandedLinks = expandedLinks.mapValues { (_, children) ->
        children.map { child ->
            if (child.id == childId) child.toggleLink(restOfPath, linkKey, targets, nextId) else child
        }
    }
    return copy(expandedLinks = newExpandedLinks)
}

/** An entry paired with its indentation [depth] and the [path] to reach it from the root. */
data class FlatTalkThreadEntry(
    val entry: TalkThreadEntry,
    val depth: Int,
    val path: List<String>,
)

/** Flattens the tree into display order (depth-first, pre-order) for rendering as a list. */
fun TalkThreadEntry.flatten(): List<FlatTalkThreadEntry> = flattenRec(depth = 0, pathToSelf = emptyList())

private fun TalkThreadEntry.flattenRec(depth: Int, pathToSelf: List<String>): List<FlatTalkThreadEntry> {
    val result = mutableListOf(FlatTalkThreadEntry(this, depth, pathToSelf))
    for (children in expandedLinks.values) {
        for (child in children) {
            result += child.flattenRec(depth + 1, pathToSelf + child.id)
        }
    }
    return result
}
