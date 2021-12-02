package io.paddle.idea.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YamlPsiElementVisitor

class PyPackagesRepositoryYAMLInspection : LocalInspectionTool() {
    private fun String.matchesFalsy() = this.toLowerCase() == "false" || this.toLowerCase() == "no"
    private fun String.matchesTruthy() = this.toLowerCase() == "true" || this.toLowerCase() == "yes"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : YamlPsiElementVisitor() {
            override fun visitKeyValue(keyValue: YAMLKeyValue) {
                super.visitKeyValue(keyValue)
                if (keyValue.containingFile.name != "paddle.yaml") return;

                keyValue.key?.let { key ->
                    if (key.textMatches("repositories")) {
                        checkForMultipleDefaults(keyValue, holder)
                    } else {
                        checkForDefaultAndSecondaryPreconditions(keyValue, holder)
                    }
                }
            }

            private fun checkForMultipleDefaults(keyValue: YAMLKeyValue, holder: ProblemsHolder) {
                val defaults = PsiTreeUtil.findChildrenOfType(keyValue, YAMLKeyValue::class.java)
                    .count { it.key?.textMatches("default") == true && it.value?.text?.matchesTruthy() == true } ?: 0
                if (defaults >= 2) {
                    holder.registerProblem(
                        keyValue,
                        "There can be only one default repository",
                        ProblemHighlightType.ERROR
                    )
                }
            }

            private fun checkForDefaultAndSecondaryPreconditions(keyValue: YAMLKeyValue, holder: ProblemsHolder) {
                val key = keyValue.key ?: return
                val value = keyValue.value ?: return

                val parentKeyValue = PsiTreeUtil.getParentOfType(keyValue, YAMLKeyValue::class.java) ?: return
                val parentKey = parentKeyValue.key ?: return
                if (!parentKey.textMatches("repositories")) return;

                when {
                    (key.textMatches("default") && value.text.matchesFalsy()) ->
                        holder.registerProblem(
                            keyValue,
                            "The <code>default</code> key is already <code>false</code> by default",
                            ProblemHighlightType.WEAK_WARNING
                        )
                    (key.textMatches("secondary") && value.text.matchesFalsy()) ->
                        holder.registerProblem(
                            keyValue,
                            "The <code>secondary</code> key is already <code>false</code> by default",
                            ProblemHighlightType.WEAK_WARNING
                        )
                }
            }
        }
    }
}
