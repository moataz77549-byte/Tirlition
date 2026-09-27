package com.rateel.app.core.model
sealed interface AppResult<out T>{data class Success<T>(val data:T):AppResult<T>;data object Empty:AppResult<Nothing>;sealed interface Error:AppResult<Nothing>{data object Network:Error;data object Server:Error;data object Timeout:Error;data object Parsing:Error;data class Unknown(val cause:Throwable?=null):Error}}
