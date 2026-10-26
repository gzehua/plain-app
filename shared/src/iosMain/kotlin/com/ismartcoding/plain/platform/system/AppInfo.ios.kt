package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.system.DeviceType
import platform.Foundation.NSBundle
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIUserInterfaceIdiomPhone
import platform.UIKit.UIUserInterfaceIdiomTV



actual fun getDeviceType(): DeviceType {
    val idiom = UIDevice.currentDevice.userInterfaceIdiom
    return when (idiom) {
        UIUserInterfaceIdiomPad -> DeviceType.TABLET
        UIUserInterfaceIdiomTV -> DeviceType.TV
        UIUserInterfaceIdiomPhone -> DeviceType.PHONE
        else -> DeviceType.OTHER
    }
}


actual fun cacheDirPath(): String =
    NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)[0] as String


actual fun getOwnPackageName(): String =
    NSBundle.mainBundle.bundleIdentifier ?: ""

actual fun dataDirPath(): String =
    NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)[0] as String

actual fun isDebugBuild(): Boolean {
    val bundle = NSBundle.mainBundle
    val debugMode = bundle.objectForInfoDictionaryKey("DEBUG") as? String
    if (debugMode == "YES") return true
    val simulatorName = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"]
    return simulatorName != null
}
