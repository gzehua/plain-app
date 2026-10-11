package com.ismartcoding.plain.chat.models

import com.ismartcoding.plain.chat.peer.RustPeerStore

fun PeerRecord.getBestIp(): String = address?.bestIp.orEmpty()
fun PeerRecord.getBaseUrl(): String = address?.baseUrl.orEmpty()
fun PeerRecord.getApiUrl(): String = address?.apiUrl.orEmpty()
fun PeerRecord.getStatusWsUrl(): String = address?.statusWsUrl.orEmpty()
suspend fun PeerRecord.getFileUrl(fileId: String): String = RustPeerStore.fileUrl(this, fileId)
fun PeerRecord.getName(): String = address?.name ?: name
