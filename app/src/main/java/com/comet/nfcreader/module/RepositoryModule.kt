package com.comet.nfcreader.module

import com.comet.nfcbasedmdm.common.storage.LocalStorage
import com.comet.nfcreader.reader.data.repository.PreferenceServerDataRepository
import com.comet.nfcreader.reader.data.repository.ServerDataRepository
import com.comet.nfcreader.reader.server.api.ServerAPI
import com.comet.nfcreader.reader.server.repository.RemoteTagRepository
import com.comet.nfcreader.reader.server.repository.TagRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideServerDataRepository(localStorage: LocalStorage) : ServerDataRepository {
        return PreferenceServerDataRepository(localStorage)
    }

    @Provides
    @Singleton
    fun provideTagRepository(serverAPI: ServerAPI) : TagRepository {
        return RemoteTagRepository(serverAPI)
    }
}