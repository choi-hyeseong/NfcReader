package com.comet.nfcreader.module

import com.comet.nfcreader.reader.data.repository.ServerDataRepository
import com.comet.nfcreader.reader.data.usecase.LoadDataUseCase
import com.comet.nfcreader.reader.data.usecase.SaveDataUseCase
import com.comet.nfcreader.reader.server.repository.TagRepository
import com.comet.nfcreader.reader.server.usecase.TagUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    @Singleton
    fun provideTagUseCase(tagRepository: TagRepository) : TagUseCase {
        return TagUseCase(tagRepository)
    }

    @Provides
    @Singleton
    fun provideSaveDataUseCase(serverDataRepository: ServerDataRepository) : SaveDataUseCase {
        return SaveDataUseCase(serverDataRepository)
    }

    @Provides
    @Singleton
    fun provideLoadDataUseCase(serverDataRepository: ServerDataRepository) : LoadDataUseCase {
        return LoadDataUseCase(serverDataRepository)
    }
}