package com.example.studymate.ui.locationSelector

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place

class Location(
    val id: String?,
    val name: String?,
    val lat_lng: LatLng?,
    val types: List<Place.Type>?,
    val iconURL: String?,
    val iconColor: Int?,
    val addressComponent: AddressComponents?,
    val address:String?
    ){

}