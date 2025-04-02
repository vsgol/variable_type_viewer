package com.github.vsgol.variable_type_viewer

import com.jetbrains.python.psi.types.TypeEvalContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.PyClassType
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class VariableTypeWidget : CustomStatusBarWidget {

    private val label = JLabel("Word: —")
    private val panel = JPanel(BorderLayout()).apply { add(label, BorderLayout.CENTER) }

    private var caretListener: CaretListener? = null
    private var disposable: Disposable? = null

    private var lastShownRange: IntRange? = null

    override fun ID(): String = "VariableTypeViewer"

    override fun install(statusBar: StatusBar) {
        val listener = object : CaretListener {
            private fun shouldIgnoreElement(element: PsiElement): Boolean {
                val tokenType = element.node.elementType.toString()
                val text = element.text
                val isSymbol = text.all { !it.isLetterOrDigit() && it != '_' } && text.length <= 3

                return element is PsiComment ||
                        element is PsiWhiteSpace ||
                        text.isBlank() ||
                        tokenType.endsWith("KEYWORD") ||
                        isSymbol
            }

            override fun caretPositionChanged(event: CaretEvent) {
                val editor = event.editor
                val project = editor.project ?: return
                val document = editor.document

                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return
                val offset = editor.caretModel.offset
                val element = psiFile.findElementAt(offset) ?: return

                val wordRange = element.textRange.startOffset..element.textRange.endOffset
                if (lastShownRange != null && wordRange == lastShownRange) return
                lastShownRange = wordRange

                if (shouldIgnoreElement(element)) {
                    label.text = "Type: —"
                    return
                }

                val context = TypeEvalContext.userInitiated(project, psiFile)

                // Find the closest relevant PSI parent
                val parent = PsiTreeUtil.getParentOfType(
                    element,
                    PyNamedParameter::class.java,
                    PyReferenceExpression::class.java,
                    PyTargetExpression::class.java,
                    PyQualifiedExpression::class.java,
                    PyStringLiteralExpression::class.java,
                    PyNumericLiteralExpression::class.java,
                    PyBoolLiteralExpression::class.java,
                    PyFunction::class.java
                )

                val guessedType = parent?.let { context.getType(it)?.name }

                val typeName = when {
                    // Python literal `None`
                    element.text == "None" -> "NoneType"

                    // Now check PSI parent
                    else -> when (parent) {
                        // Variable in function signature
                        is PyNamedParameter -> parent.annotation?.text ?: "Any"

                        // Variable declarations `foo = 123`
                        is PyTargetExpression -> {
                            val assigned = parent.findAssignedValue()
                            assigned?.let { context.getType(it)?.name } ?: guessedType ?: "Any"
                        }

                        // Variable references `boo = foo`
                        is PyReferenceExpression -> guessedType ?: "Any"

                        // Function calls `foo()`
                        is PyCallExpression -> context.getType(parent)?.name ?: guessedType ?: "Any"

                        // self in functions `def foo(self):` and qualified expressions `self.attr`
                        is PyQualifiedExpression -> {
                            val qualifier = parent.qualifier
                            if (qualifier != null && qualifier.text == "self") {
                                val qualifierType = context.getType(qualifier)
                                val attrName = parent.referencedName
                                if (qualifierType is PyClassType && attrName != null) {
                                    val attr = qualifierType.pyClass.findClassAttribute(attrName, true, context)
                                    attr?.let { context.getType(it)?.name } ?: qualifierType.name ?: "Any"
                                } else {
                                    guessedType ?: "Any"
                                }
                            } else {
                                // for other qualified expressions like `foo.bar`, `os.path`
                                guessedType ?: "Any"
                            }
                        }

                        // Literals //
                        // String
                        is PyStringLiteralExpression -> "str"

                        // Numeric
                        is PyNumericLiteralExpression -> if (element.text.contains(".")) "float" else "int"

                        // Boolean
                        is PyBoolLiteralExpression -> "bool"

                        // Function definitions `def foo():`
                        is PyFunction -> "function"

                        // Fallback: guessed type or unknown
                        else -> guessedType ?: "—"
                    }
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
        lastShownRange = null
    }

    override fun getComponent() = panel

}
