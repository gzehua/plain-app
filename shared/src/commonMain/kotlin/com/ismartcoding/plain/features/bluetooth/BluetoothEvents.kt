package com.ismartcoding.plain.features.bluetooth

import com.ismartcoding.plain.lib.ChannelEvent

class RequestEnableBluetoothEvent: ChannelEvent()

class RequestScanConnectBluetoothEvent: ChannelEvent()

class RequestBluetoothLocationPermissionEvent: ChannelEvent()

class RequestBluetoothLocationGPSPermissionEvent: ChannelEvent()

class BluetoothPermissionResultEvent: ChannelEvent()
