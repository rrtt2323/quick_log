package com.rrtt2323.quick_log.settings

import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

class QuickLogConfigurableUI(setting: QuickLogSettings) : ConfigurableUi<QuickLogSettings> {

    private val _methodComment = "Use \$\$ for the insertion point"
    private val _patternComment = "Use \$\$ for the insertion point<br> {FP} for file path<br> {FN} for filename<br> {LN} for line number"

    private val _ui: DialogPanel = panel {

        var methodField: Cell<JBTextField>
        row("Method:") {

            methodField = textField()
                .comment(_methodComment)
                .bindText(setting::method)
                .gap(RightGap.SMALL)
                .resizableColumn()

            button("Default", actionListener = {
                methodField.component.text = DEFAULT_METHOD
            }).apply {
                component.toolTipText = "Reset to default"
            }

        }.layout(RowLayout.PARENT_GRID)

        var patternField: Cell<JBTextField>
        row("Pattern:") {

            patternField = textField()
                .comment(_patternComment)
                .bindText(setting::pattern)
                .gap(RightGap.SMALL)
                .resizableColumn()

            button("Default", actionListener = {
                patternField.component.text = DEFAULT_PATTERN
            }).apply {
                component.toolTipText = "Reset to default"
            }

        }.layout(RowLayout.PARENT_GRID)
    }

    override fun reset(settings: QuickLogSettings) {
        _ui.reset()
    }

    override fun isModified(settings: QuickLogSettings): Boolean {
        return _ui.isModified()
    }

    override fun apply(settings: QuickLogSettings) {
        _ui.apply()
    }

    override fun getComponent(): JComponent {
        return _ui
    }

}
