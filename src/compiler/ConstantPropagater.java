package compiler;

public class ConstantPropagater {
	public static void propagateConstants(SyntaxTree t) {
		Parser.Data retType = t.getType();
		for(SyntaxTree child:t.getChildren()) {
			propagateConstants(child);
		}
		
		switch(t.getTokenType()) {
		case ADD:
			switch(retType) {
			case Byte:
				
				break;
			case Float:
				break;
			case Int:
				break;
			case Ptr:
				break;
			case Ubyte:
				break;
			case Uint:
				break;
			default:
				break;
			
			}
			break;
		case ALIAS:
			break;
		case AS:
			break;
		case BITWISE_AND:
			break;
		case BITWISE_OR:
			break;
		case BITWISE_XOR:
			break;
		case BYTE_LITERAL:
			break;
		case CLOSE_BRACE:
			break;
		case CLOSE_RANGE_EXCLUSIVE:
			break;
		case CLOSE_RANGE_INCLUSIVE:
			break;
		case COMPLEMENT:
			break;
		case CORRECT:
			break;
		case DECREMENT_LOC:
			break;
		case DIVIDE:
			break;
		case EMPTY_BLOCK:
			break;
		case EQ_SIGN:
			break;
		case FALSE:
			break;
		case FLOAT_LITERAL:
			break;
		case FOR:
			break;
		case FUNCTION:
			break;
		case FUNCTION_ARG:
			break;
		case FUNCTION_ARG_COLON:
			break;
		case FUNCTION_ARG_TYPE:
			break;
		case FUNCTION_COMMA:
			break;
		case FUNCTION_NAME:
			break;
		case FUNCTION_PAREN_L:
			break;
		case FUNCTION_PAREN_R:
			break;
		case FUNCTION_RETTYPE:
			break;
		case FUNC_CALL_NAME:
			break;
		case GEQUAL:
			break;
		case GTHAN:
			break;
		case IDENTIFIER:
			break;
		case IF:
			break;
		case IFNOT:
			break;
		case IF_EQ:
			break;
		case IF_GE:
			break;
		case IF_GT:
			break;
		case IF_LE:
			break;
		case IF_LT:
			break;
		case IF_NE:
			break;
		case IN:
			break;
		case INCREMENT_LOC:
			break;
		case INT_LITERAL:
			break;
		case IS:
			break;
		case LEQUAL:
			break;
		case LOGICAL_AND:
			break;
		case LOGICAL_OR:
			break;
		case LTHAN:
			break;
		case MODULO:
			break;
		case NEGATE:
			break;
		case OPEN_BRACE:
			break;
		case OPEN_RANGE_EXCLUSIVE:
			break;
		case OPEN_RANGE_INCLUSIVE:
			break;
		case POINTER_TO:
			break;
		case RANGE_COMMA:
			break;
		case RESET:
			break;
		case RETURN:
			break;
		case SET:
			break;
		case SHIFT_LEFT:
			break;
		case SHIFT_RIGHT:
			break;
		case STRING_LITERAL:
			break;
		case SUBTRACT:
			break;
		case TIMES:
			break;
		case TRUE:
			break;
		case TYPE:
			break;
		case UBYTE_LITERAL:
			break;
		case UINT_LITERAL:
			break;
		case WHILE:
			break;
		case WHILENOT:
			break;
		case WITH:
			break;
		default:
			break;
		
		}
		
		
		
	}
}
