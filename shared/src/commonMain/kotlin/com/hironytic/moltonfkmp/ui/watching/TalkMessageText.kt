package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.hironytic.moltonfkmp.ui.theme.MoltonfColors

private const val LINK_ANNOTATION_TAG = "link"

/**
 * Renders [lines] (one [MessageSegment] list per source line, joined with line breaks),
 * underlining [MessageSegment.LinkToTalk] segments and reporting taps on them via [onLinkClick].
 * The occurrence key passed to [onLinkClick] (`"<lineIndex>:<segmentIndex>"`) uniquely identifies
 * that link within the talk, for use as a [TalkThreadEntry] expansion key.
 */
@Composable
fun TalkMessageText(
    lines: List<List<MessageSegment>>,
    onLinkClick: (linkKey: String, segment: MessageSegment.LinkToTalk) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    linkColor: Color = MoltonfColors.talkLinkPublic,
) {
    val linkMap = HashMap<String, MessageSegment.LinkToTalk>()
    val annotatedString = buildAnnotatedString {
        lines.forEachIndexed { lineIndex, segments ->
            if (lineIndex != 0) append('\n')
            segments.forEachIndexed { segmentIndex, segment ->
                when (segment) {
                    is MessageSegment.General -> append(segment.text)
                    is MessageSegment.LinkToTalk -> {
                        val key = "$lineIndex:$segmentIndex"
                        linkMap[key] = segment
                        pushStringAnnotation(LINK_ANNOTATION_TAG, key)
                        withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                            append(segment.text)
                        }
                        pop()
                    }
                }
            }
        }
    }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = annotatedString,
        color = color,
        onTextLayout = { layoutResult = it },
        modifier = modifier.pointerInput(lines) {
            detectTapGestures { offset: Offset ->
                val result = layoutResult ?: return@detectTapGestures
                val position = result.getOffsetForPosition(offset)
                annotatedString.getStringAnnotations(LINK_ANNOTATION_TAG, position, position)
                    .firstOrNull()
                    ?.let { annotation -> linkMap[annotation.item]?.let { onLinkClick(annotation.item, it) } }
            }
        },
    )
}
