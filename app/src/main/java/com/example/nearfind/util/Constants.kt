package com.example.nearfind.util

object Constants {
    // BLE Service UUID for scanning - using standard UUID for demonstration
    const val SCAN_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb"

    // Custom Service and Characteristic UUIDs for NearFind
    const val PAIRING_SERVICE_UUID = "00002000-0000-1000-8000-00805f9b34fb"
    const val PAIRING_CHARACTERISTIC_UUID = "00002001-0000-1000-8000-00805f9b34fb"
    const val USER_DATA_CHARACTERISTIC_UUID = "00002002-0000-1000-8000-00805f9b34fb"

    // Distance calculation constants - these will need calibration
    const val RSSI_AT_ONE_METER = -69
    const val ENVIRONMENTAL_FACTOR = 2.0 // Signal propagation constant (depends on environment)

    // Notification constants
    const val SCANNING_NOTIFICATION_ID = 1001
    const val DEVICE_DETECTION_NOTIFICATION_ID = 2001
    const val PAIRING_REQUEST_NOTIFICATION_ID = 3001

    // Scan settings
    const val SCAN_PERIOD_MS = 10000L
    const val SCAN_INTERVAL_MS = 5000L

    // Distance thresholds (in meters)
    const val CLOSE_DISTANCE_THRESHOLD = 3.0
    const val MEDIUM_DISTANCE_THRESHOLD = 10.0

    // DataStore Keys
    const val DATASTORE_NAME = "near_find_preferences"
}
