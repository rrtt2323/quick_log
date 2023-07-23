package com.rrtt2323.quick_log.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.rrtt2323.quick_log.definition.EOperation
import com.rrtt2323.quick_log.definition.EScope
import org.jetbrains.annotations.Nullable

//internal const val DEFAULT_LOG_PATTERN = """console.log("=>( {FN} - {LN} ): $$", $$);"""

internal const val QUICK_LOG_MARK = "ql=> "

internal const val DEFAULT_METHOD = """console.log($$);"""
internal const val DEFAULT_PATTERN = """"( {FN} - {LN} - $$ ): ", $$"""

@State(name = "QuickLogSettings", storages = [(Storage("quick_log.xml"))])
class QuickLogSettings : PersistentStateComponent<QuickLogSettings> {

    companion object {
        val instance: QuickLogSettings
            get() = ApplicationManager.getApplication().getService(QuickLogSettings::class.java)
    }

    @Nullable
    override fun getState(): QuickLogSettings {
        return this
    }

    override fun loadState(state: QuickLogSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    var version = "Unknown"

    var method: String = DEFAULT_METHOD
    var pattern: String = DEFAULT_PATTERN

    var scope: EScope = EScope.CURRENT_FILE
    var operation: EOperation = EOperation.REMOVE
    
}