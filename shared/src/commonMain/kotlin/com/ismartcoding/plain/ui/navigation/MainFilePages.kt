package com.ismartcoding.plain.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ismartcoding.plain.features.chat.ChatQueries
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.chat.ChatEditTextPage
import com.ismartcoding.plain.ui.chat.ChatTextPage
import com.ismartcoding.plain.ui.chat.ChatViewModel
import com.ismartcoding.plain.ui.chat.updateChatMessageTextAsync
import com.ismartcoding.plain.ui.files.FilesPage
import com.ismartcoding.plain.ui.files.ZipFilePage
import com.ismartcoding.plain.ui.files.documents.DocsPage
import com.ismartcoding.plain.ui.files.stored.AppFilesPage
import com.ismartcoding.plain.ui.media.OtherFilePage
import com.ismartcoding.plain.ui.media.PdfPage
import com.ismartcoding.plain.ui.media.PlayMediaPage
import com.ismartcoding.plain.ui.media.TextFilePage
import com.ismartcoding.plain.ui.media.TextPage
import com.ismartcoding.plain.ui.platform.files.printText
import com.ismartcoding.plain.ui.shares.EditSharePage
import com.ismartcoding.plain.ui.shares.browser.SharedFolderPage
import com.ismartcoding.plain.ui.shares.send.ShareImagePage

internal fun NavGraphBuilder.addMainFilePages(
    navController: NavHostController,
    audioQueueVM: AudioQueueViewModel,
) {
    composable<Routing.Docs> { DocsPage(navController, initialLoadGate = rememberNavLoadGate()) }
    composable<Routing.Text> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.Text>()
        TextPage(navController, r.title, r.content, r.language)
    }
    composable<Routing.TextFile> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.TextFile>()
        TextFilePage(navController, r.path, r.title, r.mediaId, r.type)
    }
    composable<Routing.ChatText> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.ChatText>()
        ChatTextPage(navController, r.content, onPrint = { tm, jobName, content ->
            printText(tm, jobName, content)
        })
    }
    composable<Routing.ChatEditText> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.ChatEditText>()
        ChatEditTextPage(navController, r.content, onSave = { text ->
            coIO {
                val originalChat = ChatQueries.getChatItem(r.id) ?: return@coIO
                updateChatMessageTextAsync(originalChat, text)
                ChatViewModel.update(originalChat)
            }
        })
    }
    composable<Routing.OtherFile> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.OtherFile>()
        OtherFilePage(navController, r.path, r.title)
    }
    composable<Routing.ZipFile> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.ZipFile>()
        ZipFilePage(navController, audioQueueVM, r.path, r.title)
    }
    composable<Routing.PdfViewer> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.PdfViewer>()
        PdfPage(navController, r.uri, r.fileName)
    }
    composable<Routing.Files> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.Files>()
        FilesPage(navController, audioQueueVM, r.folderPath)
    }
    composable<Routing.AppFiles> {
        AppFilesPage(navController, audioQueueVM)
    }
    composable<Routing.EditShare> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.EditShare>()
        EditSharePage(navController, r.id)
    }
    composable<Routing.SharedFolder> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.SharedFolder>()
        SharedFolderPage(navController, r.messageId)
    }
    composable<Routing.PlayMedia> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.PlayMedia>()
        PlayMediaPage(navController, r.path, audioQueueVM)
    }
    composable<Routing.ShareImage> { backStackEntry ->
        val r = backStackEntry.toRoute<Routing.ShareImage>()
        ShareImagePage(navController, r.path, r.name)
    }
}
