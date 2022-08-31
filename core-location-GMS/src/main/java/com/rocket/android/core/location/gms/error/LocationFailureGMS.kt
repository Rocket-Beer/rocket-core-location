package com.rocket.android.core.location.gms.error

import com.rocket.core.domain.error.Failure

sealed class LocationFailureGMS(message: String? = null) : Failure.FeatureFailure(message) {
    object Cancelled : LocationFailureGMS()
    class Error(msg: String?) : LocationFailureGMS(msg)
    object NoData : LocationFailureGMS()
}
