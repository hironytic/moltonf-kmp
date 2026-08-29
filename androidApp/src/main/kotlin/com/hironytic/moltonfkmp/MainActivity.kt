package com.hironytic.moltonfkmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hironytic.moltonfkmp.storage.DatabaseDriverFactory
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.storage.createMoltonfDatabase

class MainActivity : ComponentActivity() {
    private lateinit var workspaceStore: WorkspaceStore

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        workspaceStore = WorkspaceStore(
            createMoltonfDatabase(DatabaseDriverFactory(applicationContext).createDriver())
        )

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}