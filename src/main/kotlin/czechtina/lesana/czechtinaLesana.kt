package czechtina.lesana

import AST.*
import compiler.Compiler
import compiler.DefinedType
import cz.j_jzk.klang.lesana.LesanaBuilder
import cz.j_jzk.klang.lesana.lesana
import cz.j_jzk.klang.parse.NodeID
import czechtina.*


fun czechtinaLesana() = lesana<ASTNode> {
    val types = NodeID<ASTUnaryNode>("types")
    val variables = NodeID<ASTVariableNode>("variables")
    val operands = NodeID<String>("operands")
    val endOfLine = re(";|kafe")
    val konec = re("konec")
    val main = NodeID<ASTFunctionNode>("main")
    val tFunction = NodeID<ASTFunctionNode>("function")
    val blockCode = NodeID<ASTUnaryNode>("blockCode")
    val programLines = NodeID<ASTProgramLines>("programLines")
    val line = NodeID<ASTNode>("line")
    val typeDefinition = NodeID<ASTBinaryNode>("typeDefinition")
    val varDefinition = NodeID<ASTTypedNode>("varDefinition")
    val listableDefinition = include(listAble(listOf(varDefinition)))
    val import = NodeID<ASTUnaryNode>("import")
    val r_expression = include(expression(variables, types))
    val program = NodeID<ASTProgramNode>("program")


    typeDefinition to def(
        re(cAndCzechtinaRegex(listOf(GrammarToken.KEYWORD_TYPE_DEFINITION))),
        variables,
        re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_ASSIGN))),
        types,
        endOfLine
    ) { (_, v,_, t,_) ->
        if (Compiler.definedTypes.contains(v.toC()))
            throw Exception("Type ${v.toC()} is already defined")
        else if (Compiler.addToDefinedTypes(v.toC()))
            ASTBinaryNode(ASTBinaryTypes.TYPE_DEFINITION, t, v.addType(t.getType()))
        else
            throw Exception("Error")
    }

    types to def(re("@"),types) { (_, t) -> ASTUnaryNode(ASTUnaryTypes.TYPE,t.data!!,t.getType().toConst()) }

    types to def(re("&"),types) { (_, t) -> ASTUnaryNode(ASTUnaryTypes.TYPE,t.data!!,t.getType().toHeap()) }

    types to def(re(czechtina[GrammarToken.TYPE_POINTER]!!),
        re("<"),
        types,
        re(">"))
    { (_, _, t2, _) -> ASTUnaryNode(ASTUnaryTypes.TYPE_POINTER, t2, t2.getType().toPointer()) }

    types to def(re(cAndCzechtinaRegex(Alltypes)+"|T[0-9]*")) { ASTUnaryNode(ASTUnaryTypes.TYPE, if (it.v1.contains("T")) "*${it.v1}" else it.v1, DefinedType(if (it.v1.contains("T")) "*${it.v1}" else cTypeFromCzechtina(it.v1) )) }

    operands to def(re(cAndCzechtinaRegex(listOf(GrammarToken.OPERATOR_ASSIGN)))) { it.v1 }

    // FOR LOOP
    forLoops(line, variables, types, r_expression, blockCode, endOfLine)


    variableDefinition(varDefinition, variables, types)

    line to def(
        varDefinition,
        endOfLine
    ) { (v, _) -> ASTUnaryNode(ASTUnaryTypes.SEMICOLON, v) }

    line to def(
        varDefinition,
        operands,
        r_expression,
        endOfLine
    ) { (v, o, l) -> ASTUnaryNode(ASTUnaryTypes.SEMICOLON, ASTOperandNode(o, v, l)) }

    line to def(
        re(cAndCzechtinaRegex(listOf(GrammarToken.KEYWORD_RETURN))),
        r_expression,
        endOfLine
    ) { ASTUnaryNode(ASTUnaryTypes.SEMICOLON, ASTUnaryNode(ASTUnaryTypes.RETURN, it.v2)) }

    line to def(
        re(cAndCzechtinaRegex(listOf(GrammarToken.KEYWORD_RETURN))),
        endOfLine
    ) { ASTUnaryNode(ASTUnaryTypes.SEMICOLON, ASTUnaryNode(ASTUnaryTypes.RETURN, ASTUnaryNode(ASTUnaryTypes.LITERAL, ""))) }


    flowControl(line, r_expression, blockCode)



    line to def(
        r_expression,
        endOfLine
    ) { (l) -> ASTUnaryNode(ASTUnaryTypes.SEMICOLON, l) }




    blockCode to def(re("{"), programLines, re("}")) {
        ASTUnaryNode(ASTUnaryTypes.CURLY, it.v2)
    }

    // MAIN FUNCTION
    main to def(
        re("main"), blockCode
    )
    {
       ASTFunctionNode(
           ASTUnaryNode(ASTUnaryTypes.TYPE, "cele"), it.v1,
           emptyList(), it.v2
       )
    }

    blockFunction(tFunction, varDefinition, types, programLines, listableDefinition)
    inlineTypedFunction(tFunction, varDefinition, types, r_expression, konec, listableDefinition)

    inlineFunction(tFunction, varDefinition, r_expression, konec, listableDefinition)




    programLines to def(line, programLines) { ASTProgramLines(listOf(it.v1) + it.v2.programLines) }
    programLines to def(line) { ASTProgramLines(listOf(it.v1)) }

    import to def(re(czechtinaRegex(listOf(GrammarToken.KEYWORD_IMPORT_C))), re("[a-zA-Z][a-zA-Z0-9]*")) {
        ASTUnaryNode(
            ASTUnaryTypes.IMPORT_C,
            it.v2
        )
    }
    variables to def(re("[a-zA-Z][a-zA-Z0-9]*")) { ASTVariableNode(it.v1, DefinedType("none")) }


    program to def(import, program) { (import, program) -> program.appendImport(import) }
    program to def(typeDefinition, program) { (typ, program) -> program.appendTypeDefinition(typ) }
    program to def(program, typeDefinition) { (program, typ) -> program.appendTypeDefinition(typ) }
    program to def(tFunction, program) { (func, program) -> program.appendFunction(func) }
    program to def(program, tFunction) { (program, func) -> program.appendFunction(func) }
    program to def(main) { ASTProgramNode(listOf(), listOf(), it.v1) }

    line to def (blockCode) { it.v1 }

    setTopNode(program)
    ignoreRegexes("\\s")
    onUnexpectedToken { err ->
        println(err.got.toString())
        println("excepted: " + err.expectedIDs)
        println("------------------")
        println(err)
    }
}.getLesana()
