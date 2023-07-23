package com.rrtt2323.quick_log.notification

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.NotificationType
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.rrtt2323.quick_log.settings.QuickLogSettings

class UpdateNotify : StartupActivity {
    private val _plugin = PluginManagerCore.getPlugin(PluginId.getId("com.rrtt2323.quick_log"))!!

    override fun runActivity(project: Project) {
        val settings = QuickLogSettings.instance
        if (settings.version == "Unknown") {
            settings.version = _plugin.version
        } else if (_plugin.version != settings.version) {
            settings.version = _plugin.version
            showUpdate(project)
        }
    }

    // 更新内容，用 HTML 语言写
    private val _updateContent: String by lazy { """
    <br>
    In this version you'll find the ability
    <br><br>
    👉 to insert the <b>filename</b> and <b>line number</b> into the log
    <br>
    👉 to remove QuickLog's logs from the <b>current file</b> or the <b>project</b>
    <br>
    """ }

    private fun showUpdate(project: Project) {
        val notification = createNotification(
            "QuickLog plugin updated to version ${_plugin.version}",
            _updateContent,
            NotificationType.INFORMATION
        )
        showFullNotification(project, notification)
    }
}