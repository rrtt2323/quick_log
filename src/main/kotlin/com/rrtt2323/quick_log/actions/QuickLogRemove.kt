package com.rrtt2323.quick_log.actions

import com.intellij.find.FindModel
import com.intellij.find.FindUtil
import com.intellij.find.replaceInProject.ReplaceInProjectManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.rrtt2323.quick_log.definition.EOperation
import com.rrtt2323.quick_log.definition.EScope
import com.rrtt2323.quick_log.settings.QUICK_LOG_MARK
import com.rrtt2323.quick_log.settings.QuickLogSettings

class QuickLogRemove : AnAction("Remove QuickLog's Logs") {
    override fun actionPerformed(e: AnActionEvent) {
        // display the dialog
        val dlg = QuickLogRemoveDlg()
        if (!dlg.showAndGet()) return

        val project = e.getData(CommonDataKeys.PROJECT)!!
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)

        // 把文本替换成正则表达式
        val methodToReplace = QuickLogSettings.instance.method.run {
            replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
        }
        val patternToReplace = QuickLogSettings.instance.pattern.run {
            replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("^", "\\^")
                .replace("+", "\\+")
                .replace("?", "\\?")
                .replace("|", "\\|")
                .replace(".", "\\.")
                .replace("*", "\\*")
                .replace("$$", ".*")
                .replace("{FN}", ".*")
                .replace("{FP}", ".*")
                .replace("{LN}", "\\d*")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("$", "\\$")
        }

        //把mark拼到第1个"后面
        val startIndex = patternToReplace.indexOf("\"")
        val tempString = patternToReplace.replaceRange(startIndex, startIndex + 1, "\"$QUICK_LOG_MARK")
        val tempString2 = methodToReplace.replace("$$", tempString)
        val removeToFind = ".*$tempString2\n"

        val removeModel = FindModel().apply {
            isPromptOnReplace = false
            isRegularExpressions = true
            isGlobal = true
            stringToFind = removeToFind
            stringToReplace = ""
        }

        // 通过$$来定位方法的内容，并把mark给拼上去
        val markIndex = tempString2.indexOf(QUICK_LOG_MARK) + QUICK_LOG_MARK.length - 1
        val annotationToFind = tempString2.substring(0, markIndex)

        val annotationModel = FindModel().apply {
            isPromptOnReplace = false
            isRegularExpressions = true
            isGlobal = true
            stringToFind = annotationToFind
            stringToReplace = "//${annotationToFind}"
        }
        val notAnnotationModel = FindModel().apply {
            isPromptOnReplace = false
            isRegularExpressions = true
            isGlobal = true
            stringToFind = "//${annotationToFind}"
            stringToReplace = annotationToFind
        }

        when (dlg.scope) {
            EScope.CURRENT_FILE -> {
                when (dlg.operation) {
                    EOperation.REMOVE -> FindUtil.replace(project, editor, 0, removeModel)
                    EOperation.ANNOTATION -> FindUtil.replace(project, editor, 0, annotationModel)
                    EOperation.NOT_ANNOTATION -> FindUtil.replace(project, editor, 0, notAnnotationModel)
                }
            }

            EScope.PROJECT -> {
                when (dlg.operation) {
                    EOperation.REMOVE -> ReplaceInProjectManager(project).replaceInPath(removeModel)
                    EOperation.ANNOTATION -> ReplaceInProjectManager(project).replaceInPath(annotationModel)
                    EOperation.NOT_ANNOTATION -> ReplaceInProjectManager(project).replaceInPath(notAnnotationModel)
                }
            }
        }
    }
}