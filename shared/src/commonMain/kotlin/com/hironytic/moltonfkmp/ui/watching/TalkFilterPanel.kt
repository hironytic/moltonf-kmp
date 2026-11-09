package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.Avatar
import com.hironytic.moltonfkmp.story.TalkType
import com.hironytic.moltonfkmp.ui.theme.MoltonfFilterChip
import com.hironytic.moltonfkmp.ui.theme.NeutralOutlinedButton
import moltonf_kmp.shared.generated.resources.Res
import moltonf_kmp.shared.generated.resources.ic_filter_alt
import org.jetbrains.compose.resources.painterResource

private val filterableTalkTypes = listOf(
    TalkType.PUBLIC to "通常",
    TalkType.WOLF to "人狼",
    TalkType.GRAVE to "墓下",
    TalkType.PRIVATE to "独り言",
)

/**
 * Panel for choosing which characters' talks and which talk types are displayed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TalkFilterPanel(
    avatars: List<Avatar>,
    filter: TalkFilter,
    onToggleAvatar: (String) -> Unit,
    onToggleTalkType: (TalkType) -> Unit,
    onShowOnlyAvatar: (String) -> Unit,
    onShowOnlyTalkType: (TalkType) -> Unit,
    onCheckAllAvatars: () -> Unit,
    onUncheckAllAvatars: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // If everything is checked when the panel opens, the first tap on an item
    // leaves only that item checked instead of unchecking it.
    var soloAvatarPending by remember { mutableStateOf(filter.hiddenAvatarIds.isEmpty()) }
    var soloTalkTypePending by remember { mutableStateOf(filter.hiddenTalkTypes.isEmpty()) }
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("キャラクター")
            NeutralOutlinedButton(onClick = onCheckAllAvatars, compact = true, text = "すべてチェック")
            NeutralOutlinedButton(onClick = onUncheckAllAvatars, compact = true, text = "すべて外す")
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            avatars.forEach { avatar ->
                MoltonfFilterChip(
                    selected = avatar.avatarId !in filter.hiddenAvatarIds,
                    onClick = {
                        if (soloAvatarPending) onShowOnlyAvatar(avatar.avatarId) else onToggleAvatar(avatar.avatarId)
                        soloAvatarPending = false
                    },
                    label = avatar.shortName,
                )
            }
        }
        Text("発言の種別", modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filterableTalkTypes.forEach { (type, label) ->
                MoltonfFilterChip(
                    selected = type !in filter.hiddenTalkTypes,
                    onClick = {
                        if (soloTalkTypePending) onShowOnlyTalkType(type) else onToggleTalkType(type)
                        soloTalkTypePending = false
                    },
                    label = label,
                )
            }
        }
    }
}

/**
 * An icon button that toggles the filter panel. It is tinted with the accent color
 * while a filter is applied.
 */
@Composable
fun TalkFilterToggleButton(
    isFilterActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.ic_filter_alt),
            contentDescription = if (isFilterActive) "フィルタ（適用中）" else "フィルタ",
            tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
