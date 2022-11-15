package com.zoomplayground
import com.facebook.react.bridge.*
import us.zoom.sdk.*

class Zoom(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ZoomSDKInitializeListener {
    private var meetingNumber: String? = null
    private var passcode: String? = null

    private var storedPromise: Promise? = null

    override fun getName(): String {
        return "ZoomSDK"
    }

    @ReactMethod
    fun initialize(settings: ReadableMap, promise: Promise) {
        UiThreadUtil.runOnUiThread {
            try {
                val params = ZoomSDKInitParams().apply {
                    domain = "zoom.us"
                    enableLog = true
                    jwtToken =  if (!settings.hasKey("jwtToken")) null else settings.getString("jwtToken")
                    appKey = if (!settings.hasKey("appKey")) null else settings.getString("appKey")
                    appSecret = if (!settings.hasKey("appSecret")) null else settings.getString("appSecret")
                }

                meetingNumber = if (!settings.hasKey("meetingNumber")) null else settings.getString("meetingNumber")
                passcode = if (!settings.hasKey("passcode")) null else settings.getString("passcode")

                storedPromise = promise

                val sdk = ZoomSDK.getInstance()
                sdk.initialize(reactApplicationContext, this, params)

            } catch (ex: Exception) {
                promise.reject("ZOOM_EXCEPTION", "Failed to initialize", ex)
                storedPromise = null
            }
        }
    }


    private fun joinMeeting() {
        UiThreadUtil.runOnUiThread(Runnable {
            try {
                val zoomSDK = ZoomSDK.getInstance()

                if (!zoomSDK.isInitialized) {
                    storedPromise?.reject("ZOOM_EXCEPTION", "Zoom not initialized")
                    return@Runnable
                }

                val meetingService = zoomSDK.meetingService

                val params = JoinMeetingParams().apply {
                    meetingNo = meetingNumber
                    password = passcode
                    displayName = "TestUser"
                }

                val options = JoinMeetingOptions().apply {
                    no_audio = true
                    no_video = true
                }

                val joinMeetingResult = meetingService.joinMeetingWithParams(
                        reactApplicationContext,
                        params,
                        options
                )

                if (joinMeetingResult != MeetingError.MEETING_ERROR_SUCCESS) {
                    storedPromise?.reject("ZOOM_EXCEPTION", "Failed to join meeting. Error code: ${joinMeetingResult.toString()}" )
                    storedPromise = null
                    return@Runnable
                } else {
                    storedPromise?.resolve(null)
                    storedPromise = null
                }
            } catch (ex: java.lang.Exception) {
                storedPromise?.reject("ZOOM_EXCEPTION", "Failed to join meeting", ex)
                storedPromise = null
            }
        })
    }

    private fun mapErrorCodeToErrorReason(errorCode: Int): String {
        return when (errorCode) {
            ZoomError.ZOOM_ERROR_INVALID_ARGUMENTS -> "InvalidArguments"
            ZoomError.ZOOM_ERROR_ILLEGAL_APP_KEY_OR_SECRET -> "InvalidSdkKeyOrSecret"
            ZoomError.ZOOM_ERROR_AUTHRET_KEY_OR_SECRET_ERROR -> "InvalidSdkKeyOrSecret"
            ZoomError.ZOOM_ERROR_NETWORK_UNAVAILABLE -> "NetworkIssue"
            ZoomError.ZOOM_ERROR_AUTHRET_ACCOUNT_NOT_ENABLE_SDK -> "SDKNotEnabledOnAccount"
            ZoomError.ZOOM_ERROR_AUTHRET_CLIENT_INCOMPATIBLEE -> "SDKVersionIncompatibleWithAccount"
            ZoomError.ZOOM_ERROR_AUTHRET_ACCOUNT_NOT_SUPPORT -> "AccountNotSupported"
            ZoomError.ZOOM_ERROR_AUTHRET_TOKENWRONG -> "WrongJwtToken"
            ZoomError.ZOOM_ERROR_DEVICE_NOT_SUPPORTED -> "UnsupportedDevice"
            ZoomError.ZOOM_ERROR_UNKNOWN -> "UnknownError"
            else -> return "UnknownError"
        }
    }

    override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {
        if (errorCode == ZoomError.ZOOM_ERROR_SUCCESS) {
            val zoomSDK = ZoomSDK.getInstance()
            joinMeeting()
        } else {
            val result = mapErrorCodeToErrorReason(errorCode)
            storedPromise?.reject("ZOOM_EXCEPTION", "Failed to initialize SDK: ${mapErrorCodeToErrorReason(errorCode)}" )
            storedPromise = null
        }
    }

    override fun onZoomAuthIdentityExpired() {
        storedPromise?.reject("ZOOM_EXCEPTION", "Zoom identity expired" )
    }
}


