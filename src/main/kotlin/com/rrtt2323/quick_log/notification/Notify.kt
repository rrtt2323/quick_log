package com.rrtt2323.quick_log.notification

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.BalloonLayoutData
import com.intellij.ui.awt.RelativePoint
import java.awt.Point

class ApplicationServicePlaceholder : Disposable {
    override fun dispose() = Unit

    companion object {
        val instance: ApplicationServicePlaceholder =
            ApplicationManager.getApplication().getService(ApplicationServicePlaceholder::class.java)
    }
}

// 创建通知
fun createNotification(title: String, content: String, type: NotificationType): Notification {
  return NotificationGroupManager.getInstance()
    .getNotificationGroup("com.rrtt2323.quick_log")
    .createNotification(title, content, type)
}

// 显示通知
fun showFullNotification(project: Project, notification: Notification) {
  val frame = WindowManager.getInstance().getIdeFrame(project)
  if (frame == null) {
    notification.notify(project)
    return
  }

  val bounds = frame.component.bounds
  val target = RelativePoint(frame.component, Point(bounds.x + bounds.width, 20))

  try {
    val balloon = NotificationsManagerImpl.createBalloon(
      frame,
      notification,
      true, // showCallout
      true, // hideOnClickOutside
      BalloonLayoutData.fullContent(),
        ApplicationServicePlaceholder.instance
    )
    balloon.show(target, Balloon.Position.atLeft)
  } catch (ex: Exception) {
    notification.notify(project)
  }
}