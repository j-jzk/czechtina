package czechtina.lesana

import AST.*
import compiler.DefinedType
import cz.j_jzk.klang.lesana.lesana
import cz.j_jzk.klang.parse.NodeID
import czechtina.AllComparation
import czechtina.GrammarToken
import czechtina.cAndCzechtinaRegex
import czechtina.czechtina

fun expression(variables: NodeID<ASTVariableNode>, types: NodeID<ASTTypedNode>) = lesana {
    val literals = include(literals())
    val elevatedVariable = NodeID<ASTVariableNode>("elevatedVariable")
    val exp1 = NodeID<ASTTypedNode>("expressions")
    val exp2 = NodeID<ASTTypedNode>("expressions")
    val exp3 = NodeID<ASTTypedNode>("expressions")
    val functionCalling = NodeID<ASTTypedNode>("functionCalling")
    var para1 = NodeID<ASTTypedNode>("paragraph")
    var para2 = NodeID<ASTTypedNode>("paragraph")
    var para3 = NodeID<ASTTypedNode>("paragraph")
    var para4 = NodeID<ASTTypedNode>("paragraph")
    var para5 = NodeID<ASTTypedNode>("paragraph")
    val sentence = NodeID<ASTTypedNode>("sentence")
    val listexp3 = include(listAble(listOf(exp3, variables)))


    elevatedVariable to def(re("@"), variables) { (_, e) -> ASTFunctionCallNode( ASTVariableNode("const", DefinedType("none")), e) }
    elevatedVariable to def(re("&"), variables) { (_, e) -> ASTFunctionCallNode( ASTVariableNode("predej", DefinedType("none")), e) }
    elevatedVariable to def(variables, re("\\["), sentence, re("\\]")) { (v, _, e, _) -> ASTArrayAccessNode(v, e) }
    elevatedVariable to def(variables, re("\\["), variables, re("\\]")) { (v, _, e, _) -> ASTArrayAccessNode(v, e) }


    elevatedVariable to def (variables, re("\\."), variables)  {ASTStructureAccessNode(it.v1, it.v3)}
    elevatedVariable to def (elevatedVariable, re("\\."), variables)  {ASTStructureAccessNode(it.v1, it.v3)}

    exp1 to def(literals) { it.v1 }

    exp2 to def(exp2, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_MULTIPLY, GrammarToken.OPERATOR_DIVIDE, GrammarToken.OPERATOR_MODULO))), exp1) { (e1, o, e2) -> ASTOperandNode(o, e1, e2) }
    exp2 to def(elevatedVariable, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_MULTIPLY, GrammarToken.OPERATOR_DIVIDE, GrammarToken.OPERATOR_MODULO))), exp2) { (e1, o, e2) -> ASTOperandNode(o, e1, e2) }
    exp2 to def(exp2, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_MULTIPLY, GrammarToken.OPERATOR_DIVIDE, GrammarToken.OPERATOR_MODULO))), elevatedVariable) { (e1, o, e2) -> ASTOperandNode(o, e1, e2) }
    exp2 to def(elevatedVariable, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_MULTIPLY, GrammarToken.OPERATOR_DIVIDE, GrammarToken.OPERATOR_MODULO))), elevatedVariable) { (e1, o, e2) -> ASTOperandNode(o, e1, e2) }
    exp2 to def(exp1) { it.v1 }

    exp3 to def(exp3, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_PLUS, GrammarToken.OPERATOR_MINUS))), exp2) { (e1, o, e2) -> ASTOperandNode(o, e1, e2) }
    exp3 to def(elevatedVariable, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_PLUS, GrammarToken.OPERATOR_MINUS))), exp3) { (e1, o, e2) -> ASTOperandNode(o, e1, e2) }
    exp3 to def(exp3, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_PLUS, GrammarToken.OPERATOR_MINUS))), elevatedVariable) { (e1, o, e2) -> ASTOperandNode(o, e1, e2) }
    exp3 to def(elevatedVariable, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_PLUS, GrammarToken.OPERATOR_MINUS))), elevatedVariable) { (e1, o, e2) -> ASTOperandNode(o, e1, e2) }
    exp3 to def(exp2) { it.v1 }

    exp1 to def (re("\\(") , para5, re("\\)")) { ASTUnaryNode(ASTUnaryTypes.BRACKET, it.v2, it.v2.getType()) }
    exp1 to def (re("\\(") , functionCalling, re("\\)")) { ASTUnaryNode(ASTUnaryTypes.JUST_C, it.v2, it.v2.getType()) }


    exp3 to def (re("\\["), listexp3, re("\\]")) { ASTUnaryNode(ASTUnaryTypes.ARRAY, it.v2, DefinedType("array-${it.v2.nodes[0].getType().typeString}-${it.v2.nodes.size}")) }


    functionCalling to def(re(czechtina[GrammarToken.KEYWORD_FUNCTION_CALL]!!), elevatedVariable) { ASTFunctionCallNode(it.v2) }
    functionCalling to def(elevatedVariable, exp3) { (v, e) -> ASTFunctionCallNode( v, e) }
    functionCalling to def(elevatedVariable, functionCalling) { (v, e) -> ASTFunctionCallNode( v, e) }
    functionCalling to def(elevatedVariable, elevatedVariable) { (v, e) -> ASTFunctionCallNode( v, e) }
    functionCalling to def(elevatedVariable, listexp3) { (v, e) -> ASTFunctionCallNode( v, e) }


    sentence to def(functionCalling) { it.v1 }
    sentence to def(exp3) { it.v1 }

    para1 to def(para1, re(cAndCzechtinaRegex(AllComparation)), para1 )
    {
        (e1, o, e2) ->
            if (e1 is ASTOperandNode && Regex(cAndCzechtinaRegex(AllComparation)).matches(e1.operand))
                ASTOperandNode(czechtina[GrammarToken.OPERATOR_AND]!!, e1, ASTOperandNode(o,e1.right!!, e2))
            else if (e2 is ASTOperandNode && Regex(cAndCzechtinaRegex(AllComparation)).matches(e2.operand))
                ASTOperandNode(czechtina[GrammarToken.OPERATOR_AND]!!, ASTOperandNode(o,e1, e2.left!!),e2)
            else
                ASTOperandNode(o, e1, e2)
    }
    para1 to def(para1, re(cAndCzechtinaRegex(AllComparation)), elevatedVariable )
    {
            (e1, o, e2) ->
        if (e1 is ASTOperandNode && Regex(cAndCzechtinaRegex(AllComparation)).matches(e1.operand))
            ASTOperandNode(czechtina[GrammarToken.OPERATOR_AND]!!, e1, ASTOperandNode(o,e1.right!!, e2))
        else
            ASTOperandNode(o, e1, e2)
    }
    para1 to def(elevatedVariable, re(cAndCzechtinaRegex(AllComparation)), para1 )
    {
            (e1, o, e2) ->
        if (e2 is ASTOperandNode && Regex(cAndCzechtinaRegex(AllComparation)).matches(e2.operand))
            ASTOperandNode(czechtina[GrammarToken.OPERATOR_AND]!!, ASTOperandNode(o,e1, e2.left!!),e2)
        else
            ASTOperandNode(o, e1, e2)
    }
    para1 to def(elevatedVariable, re(cAndCzechtinaRegex(AllComparation)), elevatedVariable )
    {
            (e1, o, e2) ->
            ASTOperandNode(o, e1, e2)
    }


    para1 to def(sentence) {it.v1}

    para2 to def(para2, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_AND))), para2) { ASTOperandNode(it.v2, it.v1, it.v3) }
    para2 to def(para1) {it.v1}

    para3 to def(para3, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_OR))), para3) { ASTOperandNode(it.v2, it.v1, it.v3) }
    para3 to def(para2) {it.v1}

    para4 to def(para4, re(czechtina[GrammarToken.KEYWORD_AS]!!), types) {
        ASTRetypeNode(it.v1, it.v3)
    }
    para4 to def(elevatedVariable, re(czechtina[GrammarToken.KEYWORD_AS]!!), types) {
        ASTRetypeNode(it.v1, it.v3)
    }

    para4 to def(elevatedVariable, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_ASSIGN))), para4)
    {
        ASTOperandNode(it.v2, it.v1, it.v3)
    }

    para4 to def(elevatedVariable, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_ASSIGN))), elevatedVariable)
    {
        ASTOperandNode(it.v2, it.v1, it.v3)
    }

    para4 to def(para4, re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_ASSIGN))), para4)
    {
        ASTOperandNode(it.v2, it.v1, it.v3)
    }

    para4 to def(para3) {it.v1}

    para5 to def(para4) {it.v1}
    para5 to def(elevatedVariable) {it.v1}

    elevatedVariable to def(variables) {it.v1}

    inheritIgnoredREs()
    setTopNode(para5)
}