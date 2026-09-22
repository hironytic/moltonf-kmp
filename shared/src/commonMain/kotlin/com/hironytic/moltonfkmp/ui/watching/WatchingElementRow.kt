package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hironytic.moltonfkmp.story.Assault
import com.hironytic.moltonfkmp.story.CharacterMap
import com.hironytic.moltonfkmp.story.StoryEvent
import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkMap
import com.hironytic.moltonfkmp.story.TalkType
import com.hironytic.moltonfkmp.story.TalkWithDay

/**
 * Dispatches [element] to the right renderer: a [Talk] (or an [Assault] event, displayed as the
 * wolf's talk it represents) to [TalkView], any other [StoryEvent] to [StoryEventView], and a
 * synthesized [WatchingElement.Message] to [MoltonfMessageView].
 */
@Composable
fun WatchingElementRow(
    element: WatchingElement,
    day: Int,
    story: Story,
    characterMap: CharacterMap,
    talkMap: TalkMap,
    isTalkVisible: (TalkWithDay) -> Boolean,
    onLinkClick: (talk: Talk, linkKey: String, targets: List<TalkWithDay>) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (element) {
        is WatchingElement.Message -> MoltonfMessageView(element, modifier)

        is WatchingElement.Element -> when (val storyElement = element.storyElement) {
            is Talk -> TalkView(
                talk = storyElement,
                day = day,
                story = story,
                characterMap = characterMap,
                talkMap = talkMap,
                isTalkVisible = isTalkVisible,
                onLinkClick = { linkKey, targets -> onLinkClick(storyElement, linkKey, targets) },
                modifier = modifier,
            )

            is Assault -> {
                val asWolfTalk = Talk(
                    elementId = storyElement.elementId,
                    talkType = TalkType.WOLF,
                    avatarId = storyElement.byWhom,
                    xname = storyElement.xname,
                    time = storyElement.time,
                    talkNo = null,
                    messageLines = storyElement.messageLines,
                )
                TalkView(
                    talk = asWolfTalk,
                    day = day,
                    story = story,
                    characterMap = characterMap,
                    talkMap = talkMap,
                    isTalkVisible = isTalkVisible,
                    onLinkClick = { linkKey, targets -> onLinkClick(asWolfTalk, linkKey, targets) },
                    modifier = modifier,
                )
            }

            is StoryEvent -> StoryEventView(storyElement, modifier)
        }
    }
}
