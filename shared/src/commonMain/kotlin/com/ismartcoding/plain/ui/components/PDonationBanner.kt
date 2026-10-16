package com.ismartcoding.plain.ui.components

import com.ismartcoding.plain.ui.base.PFeatureBanner
import com.ismartcoding.plain.ui.base.PFilledButton

import com.ismartcoding.plain.i18n.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.Image
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.ui.theme.dialogSheetBackground
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.rocket as ui_drawable_rocket

@Composable
fun PDonationBanner(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val locale = LocaleHelper.currentLocale()
    val isZhCN = locale.language == "zh" && locale.country == "CN"
    var showWeChatDialog by remember { mutableStateOf(false) }

    if (showWeChatDialog) {
        WeChatDonateDialog { showWeChatDialog = false }
    }

    val effectiveOnClick: () -> Unit = if (isZhCN) {
        { showWeChatDialog = true }
    } else {
        onClick
    }

    PFeatureBanner(
        modifier = modifier,
        tag = stringResource(Res.string.donation),
        title = stringResource(Res.string.donation_title),
        description = stringResource(Res.string.donation_desc),
        buttonText = stringResource(Res.string.buy_me_a_coffee),
        onClick = effectiveOnClick,
        tagIcon = UiRes.drawable.ui_drawable_rocket,
        buttonIcon = UiRes.drawable.ui_drawable_rocket,
    )
}

@Composable
private fun WeChatDonateDialog(onDismiss: () -> Unit) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.dialogSheetBackground,
        onDismissRequest = onDismiss,
        title = { Text("微信赞赏") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Res.drawable.donate_wechat),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "请用微信扫码",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        confirmButton = {
            PFilledButton(text = "关闭", buttonSize = ButtonSize.MEDIUM, onClick = onDismiss)
        },
    )
}
