package com.rateel.app.feature.home
import com.rateel.app.domain.model.*
data class HomeUiState(val loading:Boolean=false,val featuredRadios:List<RadioStation> = emptyList(),val featuredReciters:List<Reciter> = emptyList(),val offline:Boolean=false,val message:String?=null)
sealed interface HomeAction{data object Retry:HomeAction;data class OpenRadio(val id:String):HomeAction}
