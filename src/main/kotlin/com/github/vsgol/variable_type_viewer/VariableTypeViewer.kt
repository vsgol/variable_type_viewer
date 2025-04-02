package com.github.vsgol.variable_type_viewer

import com.jetbrains.python.psi.types.TypeEvalContext
import com.jetbrains.python.psi.PyReferenceExpression
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.psi.PsiDocumentManager
import com.jetbrains.python.psi.PyTargetExpression
import com.jetbrains.python.psi.types.PyType
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class VariableTypeWidget : CustomStatusBarWidget {

    private val label = JLabel("Word: —")
    private val panel = JPanel(BorderLayout()).apply { add(label, BorderLayout.CENTER) }

    private var caretListener: CaretListener? = null
    private var disposable: Disposable? = null

    override fun ID(): String = "VariableTypeViewer"

    override fun install(statusBar: StatusBar) {
        val listener = object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val editor = event.editor
                val project = editor.project ?: return
                val document = editor.document

                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return
                val offset = editor.caretModel.offset
                val element = psiFile.findElementAt(offset) ?: return

                val context = TypeEvalContext.userInitiated(project, psiFile)

                val parent = element.parent

                val tokenType = element.node.elementType.toString()

                val type: PyType? = when (parent) {
                    is PyReferenceExpression -> context.getType(parent)
                    is PyTargetExpression -> parent.findAssignedValue()?.let { context.getType(it) }
                    else -> null
                }

                val typeName = when {
                    tokenType.startsWith("Py:KEYWORD") -> "Keyword"
                    type?.name != null -> type.name
                    else -> "Any"
                }

                label.text = "Type: $typeName"
            }

        }

        caretListener = listener

        val d = Disposable { }
        disposable = d

        EditorFactory.getInstance().eventMulticaster.addCaretListener(listener, d)
    }

    override fun dispose() {
        caretListener = null
        disposable = null
    }

    override fun getComponent() = panel

}
