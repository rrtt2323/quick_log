package com.rrtt2323.quick_log

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.rrtt2323.quick_log.notification.createNotification
import com.rrtt2323.quick_log.notification.showFullNotification

class HelloAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        if(project == null)
        {
            print("project is null")
            return
        }

        Messages.showMessageDialog(
            project,
            "测试消息", "消息窗", Messages.getInformationIcon()
        )

        val notification = createNotification(
            "测试通知",
            "通知内容",
            NotificationType.INFORMATION
        )
        showFullNotification(project, notification)
    }
}
