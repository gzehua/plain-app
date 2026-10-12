package com.ismartcoding.plain.tests

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class PListItemTestRunner : AndroidJUnitRunner() {
    override fun newApplication(classLoader: ClassLoader, className: String, context: Context): Application =
        super.newApplication(classLoader, Application::class.java.name, context)
}
