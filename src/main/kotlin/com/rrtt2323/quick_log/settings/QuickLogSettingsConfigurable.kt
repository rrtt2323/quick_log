package com.rrtt2323.quick_log.settings

import com.intellij.openapi.options.ConfigurableBase

class QuickLogSettingsConfigurable : ConfigurableBase<QuickLogConfigurableUI, QuickLogSettings>(
    "com.rrtt2323.quick_log", "QuickLog", "") {

    override fun getSettings(): QuickLogSettings {
        return QuickLogSettings.instance
    }

    override fun createUi(): QuickLogConfigurableUI {
        return QuickLogConfigurableUI(settings)
    }
}
