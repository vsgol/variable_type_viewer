package com.github.vsgol.variable_type_viewer

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class VariableTypeWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "VariableTypeViewer"
    override fun getDisplayName(): String = "Variable Type Viewer"
    override fun isAvailable(project: Project): Boolean = true
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = VariableTypeWidget()

    override fun disposeWidget(widget: StatusBarWidget) {}
}
