package com.example.poctransmissao

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.media.MediaRouteSelector
import com.example.poctransmissao.ui.theme.PocTransmissaoTheme
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import java.io.File

class MainActivity : FragmentActivity() {
    private lateinit var castContext: CastContext
    private var remoteMediaClient: RemoteMediaClient? = null
    private var webServer: LocalWebServer? = null

    private val pickVideoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { playLocalVideo(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        castContext = CastContext.getSharedInstance(this)

        setContent {
            PocTransmissaoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CastScreen(
                        modifier = Modifier.padding(innerPadding),
                        onPlayRemote = { playVideo("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") },
                        onPlayLocal = { pickVideoLauncher.launch("video/*") },
                        onPause = { pauseVideo() }
                    )
                }
            }
        }
    }

    private fun playVideo(url: String) {
        val session = castContext.sessionManager.currentCastSession ?: return
        remoteMediaClient = session.remoteMediaClient

        val mediaInfo = MediaInfo.Builder(url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("video/mp4")
            .build()

        val mediaLoadRequestData = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .build()

        remoteMediaClient?.load(mediaLoadRequestData)
    }

    private fun playLocalVideo(uri: Uri) {
        val file = File(getRealPathFromUri(uri) ?: return)

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)

        webServer?.stop()
        webServer = LocalWebServer(file).apply { start() }

        val url = "http://$ip:8080/${file.name}"
        playVideo(url)
    }

    private fun pauseVideo() {
        remoteMediaClient?.pause()
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
        }
        return null
    }
}

@Composable
fun CastScreen(
    modifier: Modifier = Modifier,
    onPlayRemote: () -> Unit,
    onPlayLocal: () -> Unit,
    onPause: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Teste de Cast", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        AndroidView(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surface),
            factory = { context ->
                MediaRouteButton(context).apply {
                    CastButtonFactory.setUpMediaRouteButton(context, this)

                    val selector = MediaRouteSelector.Builder()
                        .addControlCategory(
                            CastMediaControlIntent.categoryForCast(
                                CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
                            )
                        )
                        .build()
                    routeSelector = selector

                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Button(onClick = onPlayRemote) {
                Text("Play Online")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onPlayLocal) {
                Text("Play Local")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onPause) {
                Text("Pause")
            }
        }
    }
}