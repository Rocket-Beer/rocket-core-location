package com.rocket.android.core.location.error

import com.rocket.core.domain.error.Failure

sealed class LocationFailure(message: String? = null) : Failure.FeatureFailure(message) {
    object Cancelled : LocationFailure()
    class Error(msg: String?) : LocationFailure(msg)
    object NoData : LocationFailure()
}
