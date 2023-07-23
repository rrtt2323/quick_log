package com.rrtt2323.quick_log.actions

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.actionListener
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toMutableProperty
import com.rrtt2323.quick_log.definition.EOperation
import com.rrtt2323.quick_log.definition.EScope
import com.rrtt2323.quick_log.settings.QuickLogSettings
import java.awt.Dimension
import javax.swing.JComponent

class QuickLogRemoveDlg : DialogWrapper(false) {
    var scope: EScope
    var operation: EOperation

    init {
        title = "Remove QuickLog's Logs"
        // 从配置里读初值
        scope = QuickLogSettings.instance.scope
        operation = QuickLogSettings.instance.operation
        init()
    }

    override fun createCenterPanel(): JComponent {
        val pan = panel {
            buttonsGroup("Choose Scope:") {
                row {
                    radioButton("Current file", EScope.CURRENT_FILE).apply {
                        actionListener { _, _ ->
                            QuickLogSettings.instance.scope = EScope.CURRENT_FILE
                        }
                    }
                }
                row {
                    radioButton("Project", EScope.PROJECT).apply {
                        actionListener { _, _ ->
                            QuickLogSettings.instance.scope = EScope.PROJECT
                        }
                    }
                }
            }.bind(::scope.toMutableProperty(), EScope::class.java)

            buttonsGroup("Choose Operation:") {
                row {
                    radioButton("Remove logs", EOperation.REMOVE).apply {
                        actionListener { _, _ ->
                            QuickLogSettings.instance.operation = EOperation.REMOVE
                        }
                    }
                }
                row {
                    radioButton("Annotation logs", EOperation.ANNOTATION).apply {
                        actionListener { _, _ ->
                            QuickLogSettings.instance.operation = EOperation.ANNOTATION
                        }
                    }
                }
                row {
                    radioButton("Not Annotation logs", EOperation.NOT_ANNOTATION).apply {
                        actionListener { _, _ ->
                            QuickLogSettings.instance.operation = EOperation.NOT_ANNOTATION
                        }
                    }
                }
            }.bind(::operation.toMutableProperty(), EOperation::class.java)
        }

        pan.minimumSize = Dimension(300, 100)

        return pan
    }
}