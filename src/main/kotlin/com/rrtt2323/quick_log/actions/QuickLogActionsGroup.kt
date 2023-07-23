package com.rrtt2323.quick_log.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup

class QuickLogActionsGroup : DefaultActionGroup() {
    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.isVisible = getMenuVisible(event)
    }

    private fun getMenuVisible(event: AnActionEvent): Boolean {
        return true

        /*
        // 如果光标位于 JavaScript 代码中，则显示下拉菜单
        val result = event.getData(PlatformDataKeys.EDITOR)?.let {
            editor ->
            val js = Language.findLanguageByID("JavaScript")
            val psiFile = event.getData(PlatformDataKeys.PSI_FILE)
            val offset = editor.caretModel.currentCaret.offset
            val psiElement = psiFile?.findElementAt(offset)
            val isJs = psiElement?.language == js || psiElement?.language?.baseLanguage == js
            isJs
        } ?: false

        print("isJs $result")
        return result
        */
    }
}