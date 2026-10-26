package com.ismartcoding.plain.ui.apps
import com.ismartcoding.plain.ui.theme.PlainTheme

import com.ismartcoding.plain.i18n.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.ismartcoding.plain.lib.extensions.formatBytes
import com.ismartcoding.plain.platform.system.PackageDetails
import com.ismartcoding.plain.platform.system.formatDateTime
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.platform.system.getPackageDetail
import com.ismartcoding.plain.platform.system.isPackageInstalled
import com.ismartcoding.plain.platform.system.isFileShareable
import com.ismartcoding.plain.platform.system.shareFileAs
import com.ismartcoding.plain.ui.base.*
import com.ismartcoding.plain.ui.apps.components.*
import com.ismartcoding.plain.ui.audio.components.*
import com.ismartcoding.plain.ui.notes.components.*
import com.ismartcoding.plain.ui.web.components.*
import com.ismartcoding.plain.ui.components.*
import com.ismartcoding.plain.ui.media.*
import com.ismartcoding.plain.ui.components.rememberLifecycleEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.share_2 as ui_drawable_share_2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPage(navController: NavHostController, id: String) {
    val scope = rememberCoroutineScope()
    var item by remember { mutableStateOf<PackageDetails?>(null) }
    var isShareable by remember { mutableStateOf(true) }
    val lifecycleEvent = rememberLifecycleEvent()

    LaunchedEffect(lifecycleEvent) {
        if (lifecycleEvent == Lifecycle.Event.ON_RESUME && !isPackageInstalled(id)) navController.navigateUp()
    }
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.Default) {
            item = getPackageDetail(id)
            item?.let { isShareable = isFileShareable(it.item.path) }
        }
    }

    PScaffold(
        topBar = {
            PTopAppBar(onNavigateBack = { navController.navigateUp() }, title = item?.item?.name ?: "", actions = {
                if (isShareable) {
                    PIconButton(icon = UiRes.drawable.ui_drawable_share_2, contentDescription = stringResource(Res.string.share),
                        tint = MaterialTheme.colorScheme.onSurface) {
                        item?.let { pkg ->
                            shareFileAs(pkg.item.path, "${pkg.item.name.replace(" ", "")}-${pkg.item.id}.apk")
                        }
                    }
                }
            })
        },
        content = { paddingValues ->
            if (item == null) { NoDataColumn(loading = true); return@PScaffold }
            val details = item!!
            val pkg = details.item
            LazyColumn(Modifier.padding(top = paddingValues.calculateTopPadding())) {
                item { AppPageHeader(navController = navController, details = details) }
                item {
                    VerticalSpace(dp = 16.dp)
                    PCard(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN)) {
                        PListItem(title = stringResource(Res.string.source_directory), subtitle = details.sourceDir)
                        PListItem(title = stringResource(Res.string.data_directory), subtitle = details.dataDir)
                    }
                }
                item {
                    VerticalSpace(dp = 16.dp)
                    PCard(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN)) {
                        PListItem(title = stringResource(Res.string.app_size), value = pkg.size.formatBytes())
                        PListItem(title = "SDK", value = LocaleHelper.getStringF(Res.string.sdk, details.targetSdkVersion, details.minSdkVersion))
                        PListItem(title = stringResource(Res.string.installed_at), value = pkg.installedAt.formatDateTime())
                        PListItem(title = stringResource(Res.string.updated_at), value = pkg.updatedAt.formatDateTime())
                    }
                }
                item { BottomSpace(paddingValues) }
            }
        },
    )
}
