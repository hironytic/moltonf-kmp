package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.CharacterMap
import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkMap
import com.hironytic.moltonfkmp.story.TalkType
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.story.resolveAvatarFaceIcon
import com.hironytic.moltonfkmp.story.resolveGraveIcon
import com.hironytic.moltonfkmp.story.timeString

private fun talkTypeContainerColor(talkType: TalkType) = when (talkType) {
    TalkType.PUBLIC -> androidx.compose.ui.graphics.Color(0xFFECECEC)
    TalkType.WOLF -> androidx.compose.ui.graphics.Color(0xFFFFB3B3)
    TalkType.PRIVATE -> androidx.compose.ui.graphics.Color(0xFFCFCFCF)
    TalkType.GRAVE -> androidx.compose.ui.graphics.Color(0xFFB9CDE0)
}

private val talkTypeContentColor = androidx.compose.ui.graphics.Color(0xFF000000)

/**
 * Renders a single [Talk] (or an [com.hironytic.moltonfkmp.story.Assault] event disguised as a
 * wolf talk by the caller) as a chat-like item, with talk-mention links tappable via [onLinkClick].
 */
@Composable
fun TalkView(
    talk: Talk,
    day: Int,
    story: Story,
    characterMap: CharacterMap,
    talkMap: TalkMap,
    isTalkVisible: (TalkWithDay) -> Boolean,
    onLinkClick: (linkKey: String, targets: List<TalkWithDay>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val avatar = characterMap[talk.avatarId]?.avatar
    val faceIcon = if (talk.talkType == TalkType.GRAVE) {
        resolveGraveIcon(story)
    } else {
        talk.avatarId.let { resolveAvatarFaceIcon(story, it) }
    }
    val lines = talk.messageLines.map { line -> parseMessageSegments(line, day, talkMap, isTalkVisible) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            if (talk.talkNo != null) {
                Text("${talk.talkNo}.", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(4.dp))
            }
            Text(avatar?.fullName ?: talk.xname, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(4.dp))
            Text(timeString(talk.time), color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
        }
        Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Top) {
            FaceIconImage(
                icon = faceIcon,
                contentDescription = avatar?.shortName,
                modifier = Modifier.size(40.dp).clip(CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            TalkMessageText(
                lines = lines,
                onLinkClick = { linkKey, segment -> onLinkClick(linkKey, segment.talks) },
                color = talkTypeContentColor,
                modifier = Modifier
                    .weight(1f)
                    .background(talkTypeContainerColor(talk.talkType), RoundedCornerShape(8.dp))
                    .padding(8.dp),
            )
        }
    }
}
