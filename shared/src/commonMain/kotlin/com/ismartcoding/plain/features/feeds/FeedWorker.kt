package com.ismartcoding.plain.features.feeds

expect fun feedWorkerOneTimeRequest(feedId: String)

expect fun feedWorkerStartRepeat()

expect fun feedWorkerCancelRepeat()
