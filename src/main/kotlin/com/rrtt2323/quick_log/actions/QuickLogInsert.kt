package com.rrtt2323.quick_log.actions

import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.JSIfStatement
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.rrtt2323.quick_log.settings.QUICK_LOG_MARK
import com.rrtt2323.quick_log.settings.QuickLogSettings

class QuickLogInsert : AnAction("Insert log") {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val vFile: VirtualFile? = e.getData(PlatformDataKeys.VIRTUAL_FILE)

        val actionManager = EditorActionManager.getInstance()
        val startNewLineHandler = actionManager.getActionHandler(IdeActions.ACTION_EDITOR_START_NEW_LINE)

        val variableName = moveCursorToInsertionPoint(editor)
        val logVar = variableName?.trim()

        //因为日志是字符串，所以我默认它有被""包裹，就通过第一个"来定位mark的插入位置
        val startIndex = QuickLogSettings.instance.pattern.indexOf("\"")
        val tempString = QuickLogSettings.instance.pattern.replaceRange(
            startIndex, startIndex + 1, "\"$QUICK_LOG_MARK")
        val tempString2 = QuickLogSettings.instance.method.replace("$$", tempString)
        //把文件信息换上去
        val pattern = tempString2.run {
            replace("{FN}", vFile?.name ?: "filename")
                .replace("{FP}", vFile?.path ?: "file_path")
                .replace("{LN}",
                    (editor.caretModel.currentCaret.logicalPosition.line + 2).toString())
        }

        val insertionPositions = "\\$\\$".toRegex().findAll(pattern)
            .map { it.range.first }
            .toList()

        val lineToInsert = if (logVar == "\n") {
            "\n${pattern.replace("$$", "")}"
        } else {
            pattern.replace("$$", "$logVar")
        }

        variableName?.let {
            val line2insert = lineToInsert.replace("<CR>", "")

            val runnable = {
                if (variableName != "") {
                    startNewLineHandler.execute(editor, editor.caretModel.primaryCaret, e.dataContext)
                }

                val offset = editor.caretModel.currentCaret.offset
                editor.document.insertString(offset, line2insert)
            }
            WriteCommandAction.runWriteCommandAction(editor.project, runnable)

            val variableNameWithoutCR = variableName.replace("<CR>", "").trim()
            positionCaret(editor, insertionPositions, line2insert, variableNameWithoutCR)
        }
    }

    /*
    search for the cursor insertion point
    return the name of the element to log
    搜索光标插入点，返回要记录的元素的名称
    */
    private fun moveCursorToInsertionPoint(editor: Editor): String? {
        /*
        parse the file as a simple JavaScript file
        将文件解析为简单的 JavaScript 文件
        */
        val psiFile = PsiFileFactory.getInstance(editor.project).createFileFromText(
            "dummy.js", JavascriptLanguage.INSTANCE, editor.document.text
        )

        val offset: Int
        val element: PsiElement?
        val valueToLog: String

        if (editor.selectionModel.hasSelection()) {
            val value = editor.selectionModel.selectedText
            offset = editor.selectionModel.selectionStart
            element = psiFile.findElementAt(offset)

            valueToLog = value ?: "<CR>"
        } else {
            offset = editor.caretModel.currentCaret.offset
            val elementAtCursor = psiFile.findElementAt(offset)
            if (elementAtCursor?.text?.replace(" ", "")?.endsWith("\n\n") == true) return ""

            element = findElementToLogForSelection(elementAtCursor!!)
            valueToLog = element?.text?.replace(" ", "") ?: "<CR>"
        }

        if (valueToLog.startsWith("\n")
            && element?.hasParentOfType("JS:OBJECT_LITERAL", 2) != true
        ) {
            return "\n"
        }

        val block = findBlockForElement(element ?: psiFile.findElementAt(offset) ?: return null)
        when {
            block is JSIfStatement -> {
                // for "if" statements insert line above
                editor.caretModel.moveToOffset(block.prevSibling.textRange.startOffset - 1)
            }

            block != null -> {
                editor.caretModel.moveToOffset(block.textRange.endOffset)
            }
        }

        return valueToLog
    }

    /*
    when the cursor is on a loggable identifier
    当光标位于可记录标识符上时
    */
    private fun findElementToLogForSelection(element: PsiElement): PsiElement? {

        val elementType = element.node.elementType.toString()
        val parentElementType = element.parent.node.elementType.toString()
        when {
            elementType == "WHITE_SPACE" && element.text.replace(" ", "").startsWith("\n\n")
            -> return null

            element.prevSibling != null && element.prevSibling.node.elementType.toString() == "JS:DOT"
            -> return findElementToLogForSelection(element.parent)

            (elementType != "JS:IDENTIFIER"
                    && elementType != "JS:REFERENCE_EXPRESSION"
                    && elementType != "JS:BINARY_EXPRESSION")
                    || (parentElementType == "JS:REFERENCE_EXPRESSION" && elementType != "JS:IDENTIFIER")
                    || parentElementType == "JS:PROPERTY"
            -> {
                val block = findBlockForElement(element)
                return when {
                    element.text.trim(' ') == "\n" && (element.prevSibling?.lastChild?.text == ";") -> null
                    block?.text?.trim() == "{" -> null
                    block?.node?.elementType.toString() == "JS:IF_STATEMENT" -> element.prevSibling?.let {
                        findElementToLogForSelection(element.prevSibling)
                    } ?: element

                    else -> findElementToLogForBlock(block)
                }
            }

            elementType == "JS:IDENTIFIER" && parentElementType == "JS:VARIABLE"
            -> return findElementToLogForBlock(element)

            elementType == "JS:REFERENCE_EXPRESSION" && parentElementType != "JS:BINARY_EXPRESSION"
            -> return findElementToLogForSelection(element.parent)

            (elementType == "JS:IDENTIFIER"
                    && !element.hasParentOfType("JS:ARGUMENT_LIST", 2)
                    && element.hasParentOfType("JS:CALL_EXPRESSION", 2))
                    && element.prevSibling == null
            -> return null
        }

        return element
    }

    private fun PsiElement.hasParentOfType(type: String, maxRecursion: Int, recursionLevel: Int = 0): Boolean {
        if (this.parent.node.elementType.toString() == type) {
            return true
        } else {
            if (this.parent.node.elementType.toString() != "FILE" && recursionLevel < maxRecursion) {
                return this.parent.hasParentOfType(type, maxRecursion, recursionLevel + 1)
            } else {
                return false
            }
        }
    }

    /*
    find the element to log inside a given block
    查找要记录给定块内的元素
    */
    private fun findElementToLogForBlock(element: PsiElement?): PsiElement? {
        element ?: return null
        element.parent ?: return null

        val elementType = element.node.elementType.toString()
        val parentType = element.parent.node.elementType.toString()

        when {
            (elementType == "JS:IDENTIFIER" && parentType != "JS:PROPERTY")
                    || elementType == "JS:DEFINITION_EXPRESSION"
                    || (elementType == "JS:REFERENCE_EXPRESSION" && parentType == "JS:REFERENCE_EXPRESSION")
            -> return element

            elementType == "JS:VARIABLE" -> return element.firstChild
            elementType == "JS:CALL_EXPRESSION" -> return null
        }

        if (element.firstChild == null) {
            return findElementToLogForBlock(element.nextSibling)
        }

        return findElementToLogForBlock(element.firstChild)
    }

    /*
    find the block containing this element
    找到包含该元素的块
    */
    private fun findBlockForElement(element: PsiElement?): PsiElement? {
        element ?: return null

        val elementType = element.node.elementType.toString()
        val parentElementType =
            if (element.parent == null) null
            else element.parent.node.elementType.toString()

        when {
            (elementType == "JS:EXPRESSION_STATEMENT" && parentElementType != "FILE") -> return element
            elementType == "JS:VAR_STATEMENT" -> return element
            elementType == "JS:IF_STATEMENT" -> return element

            element.text.trim(' ') == "{" -> return element
            element.text.trim(' ') == "\n" -> return findBlockForElement(element.prevSibling)
        }

        return if(element.parent != null) {
            findBlockForElement(element.parent)
        } else {
            element
        }
    }

    private fun positionCaret(
        editor: Editor,
        insertionPositions: List<Int>,
        lineToInsert: String,
        variableName: String
    ) {
        val offset = editor.caretModel.currentCaret.offset
        val logicalPosition = editor.offsetToLogicalPosition(offset)

        val ip0 = if(insertionPositions.isNotEmpty()) insertionPositions[0] else 0
        val ip1 = if(insertionPositions.size > 1) insertionPositions[1] else 0

        editor.caretModel.caretsAndSelections =
            listOf(
                CaretState(
                    LogicalPosition(
                        logicalPosition.line,
                        logicalPosition.column + ip0
                    ),
                    LogicalPosition(
                        logicalPosition.line,
                        logicalPosition.column + ip0
                    ),
                    LogicalPosition(
                        logicalPosition.line,
                        logicalPosition.column + ip0 + variableName.length
                    )
                ),
                CaretState(
                    LogicalPosition(
                        logicalPosition.line,
                        logicalPosition.column + ip1 + variableName.length - 2
                    ),
                    LogicalPosition(
                        logicalPosition.line,
                        logicalPosition.column + ip1 + variableName.length - 2
                    ),
                    LogicalPosition(
                        logicalPosition.line,
                        logicalPosition.column + ip1 + variableName.length * 2 - 2
                    )
                )
            )
        //println(editor.caretModel.caretsAndSelections)
    }

}