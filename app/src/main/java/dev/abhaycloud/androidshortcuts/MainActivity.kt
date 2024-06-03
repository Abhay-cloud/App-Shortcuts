package dev.abhaycloud.androidshortcuts

import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import dev.abhaycloud.androidshortcuts.ui.theme.AndroidShortcutsTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        addDynamicShortcut(this)
        setContent {
            AndroidShortcutsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = {
                            pinShortcut(applicationContext)
                        }) {
                            Text(text = "Pin shortcut")
                        }
                    }
                }
            }
        }

        // receive extras after clicking on static/dynamic shortcut
        val shortcutId = intent.getStringExtra("shortcut_id") ?: ""
    }


    // method to add dynamic shortcut
    private fun addDynamicShortcut(context: Context) {
        val shortcutManager = context.getSystemService<ShortcutManager>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Log.d(
                "myapp",
                "${shortcutManager?.dynamicShortcuts?.size} ${shortcutManager?.maxShortcutCountPerActivity}"
            )

            shortcutManager?.dynamicShortcuts.let {
                it?.forEach {
                    Log.d("myapp", "${it.id} ${it.shortLabel} ${it.isPinned}")
                }
            }


            if ((shortcutManager?.dynamicShortcuts?.size
                    ?: 0) < (shortcutManager?.maxShortcutCountPerActivity ?: 0)
            ) {
                val dynamicShortcut = ShortcutInfo.Builder(context, "dynamic_shortcut_kotlin_book")
                    .setShortLabel("Kotlin in action")
                    .setLongLabel("Kotlin in action by Svetlana Isakova")
                    .setIcon(Icon.createWithResource(context, R.drawable.baseline_book_24))
                    .setIntent(Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("shortcut_id", "dynamic_shortcut_kotlin_book")
                    })
                    .build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    shortcutManager?.pushDynamicShortcut(dynamicShortcut)
                } else {
                    shortcutManager?.addDynamicShortcuts(listOf(dynamicShortcut))
                }

            }
        }
    }

    // method to pin shortcut
    private fun pinShortcut(context: Context) {
        val shortcutManager = context.getSystemService<ShortcutManager>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dynamicShortcut = shortcutManager?.dynamicShortcuts?.filter {
                it.id == "dynamic_shortcut_kotlin_book"
            }?.get(0)

            dynamicShortcut?.let { shortcutInfo ->
                if (shortcutManager.isRequestPinShortcutSupported) {
                    val pinnedShortcutCallbackIntent =
                        shortcutManager.createShortcutResultIntent(shortcutInfo)

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        pinnedShortcutCallbackIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    shortcutManager.requestPinShortcut(shortcutInfo, pendingIntent.intentSender)
                } else {
                    Toast.makeText(
                        context,
                        "Pinned shortcut is not supported",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }
}
