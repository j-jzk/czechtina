package AST

import compiler.DefinedType
import czechtina.GrammarToken

open abstract class ASTNode {

    abstract fun toC(sideEffect:Boolean = true): String

    abstract fun copy(): ASTNode

    abstract fun retype(map: Map<String, DefinedType>)
}
