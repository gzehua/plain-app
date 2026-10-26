package com.ismartcoding.plain.ui.web.components

import com.ismartcoding.plain.preferences.*

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.base.PRangeDots
import kotlinx.coroutines.launch

/**
 * Swipeable http/https address pager with page indicator dots. The selected
 * page is persisted to Prefs.https so every placement stays in sync.
 */
@Composable
fun WebAddressPager(lanIpv4Addresses: List<String>, modifier: Modifier = Modifier) {
    val isHttps = Prefs.https.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = if (isHttps.value) 1 else 0,
        pageCount = { 2 },
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            Prefs.set(PrefKeys.HTTPS, page == 1)
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            WebAddressBar(isHttps = page == 1, lanIpv4Addresses = lanIpv4Addresses)
        }
        PRangeDots(
            items = listOf(0, 1),
            selected = pagerState.currentPage,
            onSelect = { page -> scope.launch { pagerState.animateScrollToPage(page) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
