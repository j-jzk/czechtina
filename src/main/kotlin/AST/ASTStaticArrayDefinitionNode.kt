package AST

import compiler.DefinedType

class ASTStaticArrayDefinitionNode : ASTTypedNode {
    var type: ASTNode
    var variable: ASTNode
    var size: String

    constructor(type: ASTNode, variable: ASTNode, size: String): super(DefinedType("array-$type-$size")) {
        this.type = type
        this.variable = variable
        this.size = size
    }

    override fun toString(): String {
        return "Static array definition: \ntype=${type.toString().replace("\n","\n\t")}, \nvariable=${variable.toString().replace("\n","\n\t")}, \nsize=${size}"
    }

    override fun copy(): ASTStaticArrayDefinitionNode {
        return ASTStaticArrayDefinitionNode(type.copy(), variable.copy(), size)
    }

    override fun toC(): String = "${type.toC()} ${variable.toC()}[${size}]"
}