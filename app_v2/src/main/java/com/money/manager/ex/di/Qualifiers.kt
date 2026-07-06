package com.money.manager.ex.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Actual

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Forecast
