package com.syed.soundmixer.service


import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalRetrofit