package com.ismartcoding.plain.platform.media

import coil3.request.ImageRequest

expect fun ImageRequest.Builder.applyForceVideoDecoder(force: Boolean): ImageRequest.Builder
