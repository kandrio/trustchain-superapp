package com.example.musicdao

import android.content.Context
import android.net.Uri
import androidx.preference.PreferenceManager
import com.example.musicdao.domain.usecases.CreateReleaseUseCase
import com.example.musicdao.domain.usecases.GetTorrentUseCase
import com.example.musicdao.ipv8.MusicCommunity
import com.example.musicdao.repositories.ReleaseRepository
import com.example.musicdao.repositories.SwarmHealthRepository
import com.example.musicdao.repositories.TorrentRepository
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.SessionParams
import com.frostwire.jlibtorrent.SettingsPack


object AppContainer {
    lateinit var getTorrentUseCase: GetTorrentUseCase
    lateinit var createReleaseUseCase: CreateReleaseUseCase
    lateinit var sessionManager: SessionManager

    lateinit var currentCallback: (List<Uri>) -> Unit

    //    lateinit var swarmHealthMap = mutableMapOf<Sha1Hash, SwarmHealth>()
    lateinit var swarmHealthRepository: SwarmHealthRepository
    lateinit var releaseRepository: ReleaseRepository
    lateinit var activity: MusicActivity

    //    lateinit var torrentRepository: TorrentRepository
    lateinit var releaseTorrentRepository: TorrentRepository

    fun provide(
        applicationContext: Context,
        musicCommunity: MusicCommunity,
        _activity: MusicActivity
    ) {
        activity = _activity
        sessionManager = SessionManager().apply {
            start(createSessionParams(applicationContext))
        }
//        contentSeeder =
//            ContentSeeder(applicationContext.cacheDir, sessionManager).apply {
//                start()
//            }

        releaseRepository = ReleaseRepository(musicCommunity)
//        torrentRepository = TorrentRepository(sessionManager, applicationContext.cacheDir)
        releaseTorrentRepository = TorrentRepository(
            sessionManager,
            applicationContext.cacheDir,
        ).apply {
            startSeeding()
        }
        swarmHealthRepository = SwarmHealthRepository(
            sessionManager, releaseTorrentRepository, musicCommunity
        )
        createReleaseUseCase = CreateReleaseUseCase(
            releaseTorrentRepository,
            releaseRepository,
        )
        getTorrentUseCase = GetTorrentUseCase(releaseTorrentRepository)
    }

    private fun createSessionParams(applicationContext: Context): SessionParams {
        val settingsPack = SettingsPack()

        val port =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString("musicdao_port", "10148")
                ?.toIntOrNull()
        if (port != null) {
            val interfaceFormat = "0.0.0.0:%1\$d,[::]:%1\$d"
            settingsPack.listenInterfaces(String.format(interfaceFormat, port))
        }

        return SessionParams(settingsPack)
    }
}
