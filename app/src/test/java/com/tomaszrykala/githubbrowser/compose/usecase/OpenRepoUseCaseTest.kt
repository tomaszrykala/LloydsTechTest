package com.tomaszrykala.githubbrowser.compose.usecase

import android.content.Context
import android.net.Uri
import com.tomaszrykala.githubbrowser.compose.testutil.mockkR
import com.tomaszrykala.githubbrowser.compose.ui.util.CustomTabsLauncher
import io.mockk.verify
import org.junit.Test

class OpenRepoUseCaseTest {

    private val mockTabsLauncher: CustomTabsLauncher = mockkR()

    private val sut = OpenRepoUseCase(mockTabsLauncher)

    @Test
    fun `WHEN execute THEN open in TabsLauncher`() {
        val mockUri = mockkR<Uri>()
        val mockContext = mockkR<Context>()

        sut.execute(mockUri, mockContext)

        verify { mockTabsLauncher.launch(mockUri, mockContext) }
    }
}