package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.api.graphql.models.Peer

import com.ismartcoding.plain.features.chat.peer.RustPeerStore

fun Peer.getBestIp(): String = address?.bestIp.orEmpty()
fun Peer.getBaseUrl(): String = address?.baseUrl.orEmpty()
fun Peer.getApiUrl(): String = address?.apiUrl.orEmpty()
fun Peer.getStatusWsUrl(): String = address?.statusWsUrl.orEmpty()
suspend fun Peer.getFileUrl(fileId: String): String = RustPeerStore.fileUrl(this, fileId)
fun Peer.getName(): String = address?.name ?: name
