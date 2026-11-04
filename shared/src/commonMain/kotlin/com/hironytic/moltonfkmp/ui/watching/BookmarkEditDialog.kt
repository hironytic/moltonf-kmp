package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hironytic.moltonfkmp.ui.theme.moltonfTextFieldColors

/**
 * Dialog to add a bookmark (with a comment) or, when [isExisting], to edit its comment.
 * Deleting is deliberately not offered here so it cannot be tapped by mistake while editing.
 */
@Composable
fun BookmarkEditDialog(
    initialComment: String,
    isExisting: Boolean,
    onSave: (comment: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var comment by remember { mutableStateOf(initialComment) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isExisting) "ブックマークを編集" else "ブックマークに追加") },
        text = {
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("コメント") },
                colors = moltonfTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(comment) }) { Text(if (isExisting) "保存" else "追加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        },
    )
}
