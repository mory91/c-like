import Codegen.*;
import javafx.util.Pair;
import jdk.nashorn.internal.ir.Symbol;

import java.io.*;
import java.util.*;

public class CodeGenerator {
    Scanner scanner; // This is one way of informing CG about tokens detected by Scanner, you can do whatever you prefer

    // Define any variables needed for code generation
	private Stack<SymbolTableNode> SemanticStack = new Stack<SymbolTableNode>();
	private Stack<Pair<Integer, String>> registerStack = new Stack<>();
	private Stack<String> operators = new Stack<>();

	HashMap<String, ArrayList<String>> labelsHolder = new HashMap<>(); // FOR HANDLING LABEL - GOTO
	private Stack<ThreeAddressCode> codeStack = new Stack<>();
	private Stack<String> afterLoopStack = new Stack<>();
	private Stack<String> beforeLoopStack = new Stack<>();
	private Stack<String> labelStack = new Stack<>();
	//private Stack<Integer> CodeStack = new Stack<>();
	//private Stack<Pair<String,Integer> > adressingStack = new Stack<>();
	private Stack<Integer> pushSize = new Stack<>() ;

	private String funcCallName = "" ;
	private int numberOfParams = 0 ;
	private int sizeOfParams = 0 ;
	private int current_dim_lvl = 0 ;
	private int arrayAddressRegister = 0 ;
	private int lastJPLocation = 0 ;
	private boolean isFunctionBlock = false ;
	private boolean isConstDecl = false ;
	private int val = 0 ;
	//TODO : is initial value -1 ?? check direction
	private int prFramePointer = 0 ;
	private int paramAddress = 0 ;
	private int lastHeapFreeSpace = 0 ;

	private int blockBeginPC = 0 ;
	private int lastRegister = 0;
	//private int memorySpace = 0 ;
	//TODO : Find a better way for creating arrays in SymTab and for storing their dimension : int[10] x ;
	private ArrayList<Integer> dim = new ArrayList<Integer>() ;

    public Code outputCode = new Code();
    int PC = 0;
    int labelCounter = 1;

	private String currentFunction = "$global";

    int prTopStack = 0; // position relative to top stack

    public CodeGenerator(Scanner scanner) {
        this.scanner = scanner;
        //pushSize.push(new Pair<>(0,0)) ; //TODO : inja yek tof e : pushi ro az sare tavabe bayad bardasht
    }


    public void doSemantic(String sem) {



		switch (sem) {
			// DO NOT FORGET TO break AT THE END OF EACH case !!!
			case "NoSem":
				return;
			case "@test":
				System.out.println(scanner.getToken() + " : test : " + scanner.analyzer.string.length());
				break ;
			case "@test2":

				//SemanticStack.pop();
				break ;
			case "@isConst":{
				isConstDecl = true ;
			}
			break ;
			case "@get_string_value":{
				SymbolTableNode string = new SymbolTableNode() ;
				string.stringValue  = scanner.analyzer.string.toString() ;
				string.varType = "string" ;
				string.type = IdType.VALUE ;
				string.size = 4 ;
				SemanticStack.push(string) ;
			}
				break ;
			case "@assign": {
				SymbolTableNode tempSS = SemanticStack.pop();
				if (tempSS.varType.equals("string")){
					if (SemanticStack.peek().varType == tempSS.varType) {
						outputCode.codes.add(
								new ThreeAddressCode(
										"STR",
										"@#" + String.valueOf(SemanticStack.peek().address),
										"\"" + tempSS.stringValue + "\"",
										""
								));

						SemanticStack.peek().chunks.add(new Pair<>(
								lastHeapFreeSpace,
								lastHeapFreeSpace+tempSS.stringValue.length()
						));
						lastHeapFreeSpace += tempSS.stringValue.length() ;

						PC++;

					}
					break ;
				}
				if (SemanticStack.peek().isConst && !isConstDecl){
					throw new Error("cannot change const") ;
				}
				else if (SemanticStack.peek().isConst && isConstDecl){
					isConstDecl = false ;
				}
				Pair<Integer, String> temp = registerStack.pop();

				String addressType = "" ;

				if (SemanticStack.peek().memType == MemType.HEAP)
					addressType = "@#";
				else if (SemanticStack.peek().memType == MemType.STACK)
					addressType = "@^";

				if (SemanticStack.peek().addressingType == MemType.REGISTER)
					addressType += "R" ;

				if (SemanticStack.peek().varType == tempSS.varType) {
					outputCode.codes.add(
							new ThreeAddressCode(
									"STR",
									addressType + String.valueOf(SemanticStack.peek().address),
									"R" + String.valueOf(temp.getKey()),
									String.valueOf(SemanticStack.peek().size)
							));
					SemanticStack.pop();
					PC++;

				} else if ((SemanticStack.peek().varType.equals("double") ||
							SemanticStack.peek().varType.equals("float")) && ((tempSS.varType.equals("int")
				|| tempSS.varType.equals("long") || tempSS.varType.equals("byte") || tempSS.varType.equals("char")))) {
					outputCode.codes.add(new ThreeAddressCode(
							"ITF",
							"R" + ((Integer) lastRegister).toString(),
							"R" + String.valueOf(temp.getKey()),
							"" + tempSS.size
					));
					outputCode.codes.add(
							new ThreeAddressCode(
									"STR",
									addressType + String.valueOf(SemanticStack.peek().address),
									"R" + lastRegister,
									String.valueOf(SemanticStack.peek().size)
							));
					lastRegister++;
					PC++;
					SemanticStack.pop();
				} else if ((tempSS.varType.equals("double") ||
						tempSS.varType.equals("float")) && ((SemanticStack.peek().varType.equals("int")
						|| SemanticStack.peek().varType.equals("long") || SemanticStack.peek().varType.equals("byte") || SemanticStack.peek().varType.equals("char")))) {
					outputCode.codes.add(new ThreeAddressCode(
							"FTI",
							"R" + ((Integer) lastRegister).toString(),
							"R" + String.valueOf(temp.getKey()),
							"" + tempSS.size
					));
					outputCode.codes.add(new ThreeAddressCode(
							"STR",
							addressType + String.valueOf(SemanticStack.peek().address),
							"R" + lastRegister,
							String.valueOf(SemanticStack.peek().size)
					));
					lastRegister++;
					PC++;
					SemanticStack.pop();
				}
				else {
					// throw new Error("type casting needed if possible"); // TODO : KHOB KOSKESH CAST KON AMMAM BYAD BARAT CAST KONE?
				}

//				System.out.println(scanner.getToken() + " /:/:/  " +scanner.getText() );
//				System.out.println(SemanticStack.pop().intValue + " : " + SemanticStack.pop().name);
			}
				break ;
			case "@func_call" :
				prFramePointer = 0 ;
				break ;


			case "@begin_array_call":{
				SymbolTableNode cell = new SymbolTableNode(IdType.VAR,scanner.stp.address,scanner.stp.name,scanner.stp.cellSize);
				cell.varType = scanner.stp.varType ;
				cell.memType = scanner.stp.memType ;
				current_dim_lvl = 0 ;
				arrayAddressRegister = lastRegister ;
				lastRegister++ ;
				SemanticStack.push(cell) ;

			}
			break ;
			case "@calc_dim":{
				SymbolTableNode dim = SemanticStack.pop();
				Pair<Integer, String> dimReg = registerStack.pop();
				SymbolTableNode cell = SemanticStack.pop() ;
				SymbolTableNode array = SemanticStack.pop() ;

				if (current_dim_lvl != array.dimension.size()){
					if (current_dim_lvl == 0){
						outputCode.codes.add(new ThreeAddressCode(
								"MOV" ,
								"R" + String.valueOf(arrayAddressRegister) ,
								"R" + String.valueOf(dim.address),
								"4"

						));
						PC++ ;
						current_dim_lvl++ ;
					}
					else {
						outputCode.codes.add(new ThreeAddressCode(
								"LOD",
								"R" + String.valueOf(lastRegister),
								"#" + array.dimension.get(current_dim_lvl).toString(),
								"4"
						));
						PC++ ;
						lastRegister++;
						outputCode.codes.add(new ThreeAddressCode(
								"MUL",
								"R" + String.valueOf(arrayAddressRegister),
								"R" + String.valueOf(arrayAddressRegister),
								"R" + String.valueOf(lastRegister - 1)
						));
						PC++ ;
						outputCode.codes.add(new ThreeAddressCode(
								"ADD",
								"R" + String.valueOf(arrayAddressRegister),
								"R" + String.valueOf(arrayAddressRegister),
								"R" + String.valueOf(dimReg.getKey())
						));
						PC++ ;
						current_dim_lvl++;
					}
				}
				else{

				}

				SemanticStack.push(array) ;
				SemanticStack.push(cell) ;


			}
			break ;
			case "@end_dim":{
				SymbolTableNode cell = SemanticStack.pop() ;
				SymbolTableNode array = SemanticStack.pop() ;
				if (current_dim_lvl == array.dimension.size()){
					outputCode.codes.add(new ThreeAddressCode(
							"LOD" ,
							"R" + String.valueOf(lastRegister) ,
							"#" + String.valueOf(array.address),
							"4"
					));
					lastRegister++ ;
					PC++ ;
					outputCode.codes.add(new ThreeAddressCode(
							"ADD" ,
							"R" + String.valueOf(arrayAddressRegister) ,
							"R" + String.valueOf(arrayAddressRegister),
							"R" + String.valueOf(lastRegister-1)
					));
					PC++ ;
					cell.addressingType = MemType.REGISTER ;
					cell.address = arrayAddressRegister ;
					cell.isDynamiclyAllocated = true ;

					SemanticStack.push(cell) ;
				}
				else{
					SemanticStack.push(array) ;
					SemanticStack.push(cell) ;
				}

			}
			break ;
			case "@next_dim":{

			}
			break ;

			case "@load" :{
				SymbolTableNode tmp = SemanticStack.peek() ;
				if (tmp.type != IdType.FUNC) {
					String addressType = "";
					if (tmp.memType == MemType.HEAP)
						addressType = "@#";
					else if (tmp.memType == MemType.STACK)
						addressType = "@^";

					if (tmp.addressingType == MemType.REGISTER)
						addressType += "R" ;

					outputCode.codes.add(new ThreeAddressCode(
							"LOD",
							"R" + ((Integer) lastRegister).toString(),
							addressType + ((Integer) tmp.address).toString(),
							((Integer) tmp.size).toString()
					));
					registerStack.push(new Pair<Integer, String>(lastRegister, tmp.varType));
					lastRegister++;

					PC++;
				}

			}
			break ;
			case "@push&load": {
				SemanticStack.push(scanner.stp);
				// TODO: Care for Block Things
				if (scanner.stp.type != IdType.FUNC && (scanner.stp.dimension == null || scanner.stp.dimension.size() == 0)) {

					String addressType = "" ;

					if (SemanticStack.peek().memType == MemType.HEAP)
						addressType = "@#";
					else if (SemanticStack.peek().memType == MemType.STACK)
						addressType = "@^";

					if (SemanticStack.peek().addressingType == MemType.REGISTER)
						addressType += "R" ;

					outputCode.codes.add(new ThreeAddressCode(
							"LOD",
							"R" + ((Integer) lastRegister).toString(),
							addressType + ((Integer) scanner.stp.address).toString(),
							((Integer) scanner.stp.size).toString()
					));
					registerStack.push(new Pair<Integer, String>(lastRegister, scanner.stp.varType));
					lastRegister++;

					PC++;
				}
			}
				break ;
			case "@push":
				SemanticStack.push(scanner.stp) ;
//				if (scanner.stp.type == IdType.VAR)
//				{
//					String addressType = "";
//					if (scanner.stp.memType == MemType.HEAP)
//						addressType = "@#";
//					else if (scanner.stp.memType == MemType.STACK)
//						addressType = "@^";
//					outputCode.codes.add(new ThreeAddressCode("LOD", "R" + ((Integer) lastRegister).toString(),
//							addressType + ((Integer) scanner.stp.address).toString(), ((Integer) scanner.stp.size).toString()));
//					registerStack.push(new Pair<Integer, String>(lastRegister, scanner.stp.varType));
//					lastRegister++;
//
//					PC++;
//				}
				//System.out.println(scanner.stp.name);
				break;
			case "@pushi": {
				if (!dim.isEmpty()) {
					scanner.stp.dimension = new ArrayList<>(dim);
					scanner.stp.cellSize = scanner.stp.size ;
				}
				for (Integer a : dim) {
					scanner.stp.size *= a;
				}
				dim.clear();

//				scanner.stp.address = prFramePointer - scanner.stp.size;
				scanner.stp.varType = SemanticStack.peek().name;
				if (isConstDecl){
					scanner.stp.isConst = true ;
				}
				if (scanner.blockManager.getDepth(scanner.getText()) == 1){ // It means this variable is global :)
					scanner.stp.memType = MemType.HEAP ;
					scanner.stp.address = lastHeapFreeSpace ;
					lastHeapFreeSpace += scanner.stp.size ;
				}
				else
				{
					scanner.stp.memType = MemType.STACK ;
					scanner.stp.address = prFramePointer - scanner.stp.size;
				}
                if (scanner.stp.memType == MemType.STACK && (scanner.stp.type == IdType.VAR || scanner.stp.type == IdType.VALUE))
                    pushSize.push(pushSize.pop() + scanner.stp.size ) ;
				codeStack.push(new ThreeAddressCode("PSH","","",String.valueOf(scanner.stp.size)));
				if(scanner.stp.varType.equals("string")){
					scanner.stp.lastCharPlace = lastHeapFreeSpace ;
				}
			}
				break;
			case "@push_param":{
				SymbolTableNode type = SemanticStack.pop();
				SymbolTableNode func = SemanticStack.peek();


				if (func.type != IdType.FUNC){
					throw new Error("not a function") ;
				}

				Parameter param = new Parameter(type.name,scanner.getText(),type.size) ;
				scanner.blockManager.getLastSymTab().symTab.remove(scanner.getText()) ;
				if (!dim.isEmpty()) {
					param.dimension = new ArrayList<>(dim);
				}
				for (Integer a : dim) {
					param.size *= a;
				}

				param.address = paramAddress ;
				paramAddress += param.size ;
				func.argSize += param.size ;
				func.arguments.add(param) ;

				func.name += type.name ;

			}
				break ;
			case "@begin_call":{
				funcCallName = scanner.stp.name ;
				numberOfParams = 0 ;
				sizeOfParams = 0 ;

			}
				break ;
			case "@call_param":{
				SymbolTableNode param = SemanticStack.pop() ;
				funcCallName += param.varType ;
				numberOfParams++ ;
				sizeOfParams += param.size ;


			}
				break ;
			case "@end_call":{
				SymbolTableNode func = SemanticStack.pop() ;
				boolean isFound = false ;
				for (SymbolTableNode s : func.overloads){
					//System.out.println(s.name);
					if (s.name.equals(funcCallName)) {
						isFound = true;
						func = s ;
						break ;
					}
				}
				if (isFound){

					outputCode.codes.add(new ThreeAddressCode(
							"PSH",
							"",
							"",
							String.valueOf(sizeOfParams + func.size)
					));
					PC++ ;
					for (int i = func.arguments.size()-1 ;i >= 0;i--){
						Parameter p = func.arguments.get(i) ;
						outputCode.codes.add(new ThreeAddressCode(
								"STR",
								"@^"+String.valueOf(p.address - ((-1) * prFramePointer+sizeOfParams+func.size)),
								"R" + String.valueOf(registerStack.pop().getKey()),
								String.valueOf(p.size)
						)) ;
						PC++ ;
					}
					outputCode.codes.add(new ThreeAddressCode(
							"CAL",
							funcCallName,
							"",
							""
					));
					PC++ ;

					SymbolTableNode retvalue = new SymbolTableNode() ;
					retvalue.varType = func.varType ;
					retvalue.type = IdType.VAR ;
					retvalue.size = func.size ;
					retvalue.addressingType = MemType.REGISTER ;
					retvalue.address = lastRegister ;
					retvalue.isDynamiclyAllocated = true ;

					SemanticStack.push(retvalue) ;

					outputCode.codes.add(new ThreeAddressCode(
							"LOD",
							"R" + ((Integer) lastRegister).toString(),
							"@^" + String.valueOf(prFramePointer - func.size),
							String.valueOf(retvalue.size)
					)) ;
					PC++;

					registerStack.push(new Pair<>(lastRegister, retvalue.varType));
					lastRegister++;

				}
				else{
					throw new Error ("undefined function call") ;
				}

			}
				break ;
			case "@func_ret":{
				System.out.println(SemanticStack.peek());
				SymbolTableNode ret = SemanticStack.pop();
				SymbolTableNode func = SemanticStack.pop();
				//TODO : error check needed here(optional)

				if (!registerStack.peek().getValue().equals(func.varType)){
					throw new Error("return type does not match") ;
				}
				outputCode.codes.add(new ThreeAddressCode(
						"STR",
						"@^" + String.valueOf(func.argSize),
						"R" + registerStack.pop().getKey(),
						String.valueOf(func.size)
				));
				PC++ ;


				outputCode.codes.add(
						new ThreeAddressCode("POP","","",String.valueOf(pushSize.peek()))) ;
				PC++ ;

				ThreeAddressCode lCode = new ThreeAddressCode("RET",String.valueOf(func.argSize), "", "");
				outputCode.codes.add(lCode);
				PC++;


			}
			break ;
			case "@new_func" :{
				//TODO : complete this part
			}
			break ;
			case "@func_dcl" :{
				SymbolTableNode type = SemanticStack.pop(); //poping function type
				if (scanner.stp.type == IdType.VAR && scanner.stp.overloads.isEmpty()){
					//scanner.stp.overloads.add(new SymbolTableNode(IdType.FUNC,)) ;
					SymbolTableNode tmp = new SymbolTableNode() ;
					scanner.stp.type = IdType.FUNC ;
					tmp.name = scanner.stp.name ;
					tmp.type = IdType.FUNC ;
					tmp.address = 0 ;
					scanner.stp.overloads.add(tmp) ;
					scanner.stp = scanner.stp.overloads.get(scanner.stp.overloads.size()-1) ;
					scanner.stp.size = type.size ;
					scanner.stp.varType = type.name ;
				}
				else {

					scanner.stp = scanner.stp.overloads.get(scanner.stp.overloads.size()-1);
					scanner.stp.type = IdType.FUNC;
					scanner.stp.address = 0;
					scanner.stp.size = type.size ;
					scanner.stp.varType = type.name ;
				}
				SemanticStack.push(scanner.stp) ;
				paramAddress = 0;scanner.stp.type = IdType.FUNC;
				scanner.stp.address = 0;


				ThreeAddressCode jCode = new ThreeAddressCode("JMP","","","") ;
				outputCode.codes.add(jCode);
				lastJPLocation = PC ;
				PC++;


				//scanner.stp.name = scanner.stp.varType + scanner.stp.name ; TODO : for now my function overloading does not handle return type over loading

			}
				break ;
			case "@func_save_label":{
				SymbolTableNode tmp = SemanticStack.peek() ;
				scanner.blockManager.add(new SymbolTable());
				for (Parameter p : tmp.arguments){
					SymbolTableNode entry = new SymbolTableNode(IdType.VAR,p.address,p.name,p.size) ;
					entry.memType = MemType.STACK;
					entry.dimension = p.dimension ;
					entry.varType = p.varType ;
					scanner.blockManager.getLastSymTab().symTab.put(p.name,entry) ;
				}
				isFunctionBlock = true ;
				currentFunction = tmp.name;
				prFramePointer = 0 ;

				ThreeAddressCode lCode = new ThreeAddressCode("LBL", tmp.name, "", "");
				if (scanner.blockManager.overloadNames.contains(tmp.name)){
					throw new Error("duplicate function declarations !") ;
				}
				scanner.blockManager.overloadNames.add(tmp.name) ;
				outputCode.codes.add(lCode);
				PC++;

			}
				break ;
			case "@end_func_dcl":{
				codeStack.pop(); // for removing the PSH command we added in the @pushi section.
				//SymbolTableNode s = SemanticStack.pop() ;

				String labelName = "LABEL" + String.valueOf(labelCounter);

				outputCode.codes.add(new ThreeAddressCode("LBL", labelName, "", ""));
				PC++;
				labelCounter++;
				outputCode.codes.get(lastJPLocation).destincation = labelName ;
				currentFunction = "$global";
			}

				break ;

			case "@end_var_dcl":{
			//	System.out.println(scanner.stp.name);
				SemanticStack.pop() ;
				outputCode.codes.add(codeStack.pop()) ;
				scanner.stp.type = IdType.VAR ;
				prFramePointer -= scanner.stp.size;
				PC++ ;
			}
				break ;
			case "@end_var_dcl_init":{
				SemanticStack.pop();
				outputCode.codes.add(codeStack.pop()) ;
				scanner.stp.type = IdType.VAR ;
				SemanticStack.push(scanner.stp);
				prFramePointer -= scanner.stp.size;
				PC++ ;
			}
			break ;
			case "@simple_pop": {
//				SemanticStack.pop();
//				SemanticStack.pop();
			}
				break ;
			case "@get_int_val": {
				SymbolTableNode value = new SymbolTableNode() ;
				value.intValue = Integer.parseInt(scanner.getText());
				value.varType = "int" ;
				value.type = IdType.VALUE ;
				value.size = 4 ;
				SemanticStack.push(value) ;
				outputCode.codes.add(new ThreeAddressCode(
						"LOD",
						"R" + ((Integer) lastRegister).toString(),
						"#" + scanner.getText(),
						String.valueOf(value.size)
				)) ;
				PC++;
				registerStack.push(new Pair<>(lastRegister, value.varType));
				lastRegister++;


			}
				break ;
			case "@newdim": {
				//dim.add(val);
				if (SemanticStack.peek().varType.equals("int")) {
					dim.add(SemanticStack.pop().intValue);
					registerStack.pop() ;
				}
				else
					throw new Error("not int for array size") ;


			}
				break ;
			case "@begin_block": {
				prTopStack = 0;
				if (!isFunctionBlock) {
					scanner.blockManager.add(new SymbolTable());
				}else{
					isFunctionBlock = false ;
				}
				pushSize.push(0) ;
			}
				break ;
			case "@end_block": {
				scanner.blockManager.delete();
				outputCode.codes.add(
						new ThreeAddressCode("POP","","",String.valueOf(pushSize.pop()))) ;
				PC++ ;
			}
				break ;
			case "@eql":
				operators.push("EQL");
				break;
			case "@lwr":
				operators.push("LWR");
				break;
			case "@leq":
				operators.push("LEQ");
				break;
			case "@geq":
				operators.push("GEQ");
				break;
			case "@grt":
				operators.push("GRT");
				break;
			case "@and":
				operators.push("AND");
				break;
			case "@orr":
				operators.push("ORR");
				break;
			case "@logand":
				operators.push("LOGAND");
				break;
			case "@logorr":
				operators.push("LOGORR");
				break;
			case "@xor":
				operators.push("XOR");
				break;
			case "@mult":
				operators.push("MULT");
				break;
			case "@add":
				operators.push("ADD");
				break;
			case "@sub":
				operators.push("SUB");
				break;
			case "@div":
				operators.push("DIV");
				break;
			case "@mod":
				operators.push("MOD");
				break;
			case "@neq":
				operators.push("NEQ");
				break;
			case "@cexpr": {
				String top = operators.pop();
				SymbolTableNode stpa = SemanticStack.pop();
				SymbolTableNode stpb = SemanticStack.pop();
                SymbolTableNode stpc = new SymbolTableNode();
                stpc.memType = MemType.REGISTER;
				Pair<Integer, String> rega = registerStack.pop();
				Pair<Integer, String> regb = registerStack.pop();
				if (top == "NEQ" || top == "LWR" || top == "EQL"
						|| top == "LEQ" || top == "GRT" || top == "GEQ") {
					if (stpa.varType.equals(stpb.varType)) {
						outputCode.codes.add(new ThreeAddressCode(top, "R" + ((Integer) lastRegister).toString(),
								"R" + rega.getKey().toString(), "R" + regb.getKey().toString()));
						registerStack.push(new Pair<Integer, String>(lastRegister, "bool"));
                        stpc.address = lastRegister;
                        stpc.varType = "bool";
                        stpc.size = 1;
						lastRegister++;
						PC++;
					} else {
						// TODO : THROW EXCEPTION
						throw new Error("Compile Error at token \"" + scanner.getText() + "\" at line " + scanner.lineNumber + " ;");
					}
				} else if (top == "ORR" || top == "AND" || top == "XOR" || top == "MOD") {
					if (stpa.varType.equals("int") && stpb.varType.equals("int")) {
						outputCode.codes.add(new ThreeAddressCode(top, "R" + ((Integer) lastRegister).toString(),
								"R" + rega.getKey().toString(), "R" + regb.getKey().toString()));
						registerStack.push(new Pair<Integer, String>(lastRegister, "int"));
                        stpc.address = lastRegister;
                        stpc.varType = "int";
                        stpc.size = 4;
						lastRegister++;
						PC++;
					} else {
						// TODO : THROW EXCEPTION
						throw new Error("Compile Error at token \"" + scanner.getText() + "\" at line " + scanner.lineNumber + " ;");
					}
				} else if (top == "LOGORR" || top == "LOGAND") {
					boolean regaC = true, regbC = true;
					if (!rega.toString().equals("bool")) {
						outputCode.codes.add(new ThreeAddressCode("LOD", "R" + ((Integer) lastRegister).toString(),
								"#0", null));
                        PC++;
						lastRegister++;
						outputCode.codes.add(new ThreeAddressCode("LWR", "R" + ((Integer) lastRegister).toString(),
								"R" + ((Integer) (lastRegister-1)).toString(), "R" + rega.getKey().toString()));
                        PC++;
						registerStack.push(new Pair<Integer, String>(lastRegister, "bool"));
						regaC = false;
						lastRegister++;
					}
					if (!regb.toString().equals("bool")) {
						outputCode.codes.add(new ThreeAddressCode("LOD", "R" + ((Integer) lastRegister).toString(),
								"#0", null));
                        PC++;
						lastRegister++;
						outputCode.codes.add(new ThreeAddressCode("LWR", "R" + ((Integer) lastRegister).toString(),
								"R" + ((Integer) (lastRegister-1)).toString(), "R" + regb.getKey().toString()));
                        PC++;
						registerStack.push(new Pair<Integer, String>(lastRegister, "bool"));
						regbC = false;
						lastRegister++;
					}
					int firstReg = rega.getKey();
					int secondReg = regb.getKey();
					if (!regbC) {
						firstReg = registerStack.pop().getKey();
					}
					if (!regbC) {
						secondReg = registerStack.pop().getKey();
					}
					if (top == "LOGORR")
						outputCode.codes.add(new ThreeAddressCode("ORR", "R" + ((Integer) lastRegister).toString(),
								"R" + ((Integer) firstReg).toString(), "R" + ((Integer) secondReg).toString()));
					if (top == "LOGAND")
						outputCode.codes.add(new ThreeAddressCode("AND", "R" + ((Integer) lastRegister).toString(),
								"R" + ((Integer) firstReg).toString(), "R" + ((Integer) secondReg).toString()));
					registerStack.push(new Pair<Integer, String>(lastRegister, "bool"));
                    stpc.address = lastRegister;
                    stpc.varType = "bool";
                    stpc.size = 1;
					lastRegister++;
					PC++;
				} else if (top == "MULT" || top == "ADD"
						|| top == "SUB" || top == "DIV") {
					String cmd = "", cmdf = "";
					if (top == "MULT") {
						cmd = "MUL";
						cmdf = "MULF";
					}
					if (top == "ADD") {
						cmd = "ADD";
						cmdf = "ADF";
					}
					if (top == "SUB") {
						cmd = "SUB";
						cmdf = "SBF";
					}
					if (top == "DIV") {
						cmd = "DIV";
						cmdf = "DVF";
					}
					if (stpa.varType.equals("float") && stpb.varType.equals("float")) {
						outputCode.codes.add(new ThreeAddressCode(cmdf, "R" + ((Integer) lastRegister).toString(),
								"R" + rega.getKey().toString(), "R" + regb.getKey().toString()));
                        PC++;
						registerStack.push(new Pair<Integer, String>(lastRegister, "float"));
                        stpc.address = lastRegister;
                        stpc.varType = "float";
                        stpc.size = 4;
						lastRegister++;
					}
					else if ((stpa.varType.equals("int") && stpb.varType.equals("int")) ||
							stpa.varType.equals("long") && stpb.varType.equals("long")) {
						outputCode.codes.add(new ThreeAddressCode(cmd, "R" + ((Integer) lastRegister).toString(),
								"R" + rega.getKey().toString(), "R" + regb.getKey().toString()));
                        PC++;
						registerStack.push(new Pair<Integer, String>(lastRegister, "int"));
                        stpc.address = lastRegister;
                        stpc.varType = "int";
                        stpc.size = 4;
						lastRegister++;
					}
					else if ((stpa.varType.equals("float") || stpa.varType.equals("double")) ||
							(stpb.varType.equals("long") || stpb.varType.equals("int"))) {
						outputCode.codes.add(new ThreeAddressCode("ITF", "R" + ((Integer) lastRegister).toString(),
								"R" + regb.getKey().toString(), ((Integer) stpb.size).toString()));
						lastRegister++;
                        PC++;
						outputCode.codes.add(new ThreeAddressCode(cmdf, "R" + ((Integer) lastRegister).toString(),
								"R" + rega.getKey().toString(), "R" + ((Integer) (lastRegister-1)).toString()));
                        PC++;
						registerStack.push(new Pair<Integer, String>(lastRegister, "float"));
                        stpc.address = lastRegister;
                        stpc.varType = "float";
                        stpc.size = 4;
						lastRegister++;
					}
					else if ((stpb.varType.equals("float") || stpb.varType.equals("double")) ||
							(stpa.varType.equals("long") || stpa.varType.equals("int"))) {
						outputCode.codes.add(new ThreeAddressCode("ITF", "R" + ((Integer) lastRegister).toString(),
								"R" + rega.getKey().toString(), ((Integer) stpa.size).toString()));
                        PC++;
						lastRegister++;
						outputCode.codes.add(new ThreeAddressCode(cmdf, "R" + ((Integer) lastRegister).toString(),
								"R" + regb.getKey().toString(), "R" + ((Integer) (lastRegister-1)).toString()));
                        PC++;
						registerStack.push(new Pair<Integer, String>(lastRegister, "float"));
                        stpc.address = lastRegister;
                        stpc.varType = "float";
                        stpc.size = 4;
						lastRegister++;
					}
				}
                SemanticStack.push(stpc);
				break;
			}
            case "@jz":{
                ThreeAddressCode jzCode = new ThreeAddressCode("JIZ", "", "R" + registerStack.pop().getKey().toString(), "1");
                SemanticStack.pop();
				outputCode.codes.add(jzCode);
                codeStack.push(jzCode);
                PC++;
                break;
            }
            case "@cjz":{
                String labelName = "LABEL" + String.valueOf(labelCounter);
                outputCode.codes.add(new ThreeAddressCode("LBL", labelName, "",""));
                PC++;
                labelCounter++;
                codeStack.pop().destincation = labelName;
                break;
            }
            case "@jp":{
                ThreeAddressCode jzCode = new ThreeAddressCode("JMP", "", "", "");
                outputCode.codes.add(outputCode.codes.size() - 1, jzCode);
                codeStack.push(jzCode);
                PC++;
                break;
            }
            case "@cjp":{
                String labelName = "LABEL" + String.valueOf(labelCounter);
                ThreeAddressCode jzCode = new ThreeAddressCode("LBL", labelName, "", "");
                outputCode.codes.add(jzCode);
                PC++;
                labelCounter++;
                codeStack.pop().destincation = labelName;
                break;
            }
            case "@savelabel":{
                String labelName = "LABEL" + String.valueOf(labelCounter);
				beforeLoopStack.push(labelName);
				labelCounter++;
				String nextLabelName = "LABEL" + String.valueOf(labelCounter);
				afterLoopStack.push(nextLabelName);
                ThreeAddressCode jzCode = new ThreeAddressCode("LBL", labelName, "", "");
                outputCode.codes.add(jzCode);
                PC++;
                labelCounter++;
                break;
            }
            case "@wjz":{
                ThreeAddressCode jzCode = new ThreeAddressCode("JIZ", afterLoopStack.peek(), "R" + registerStack.pop().getKey().toString(), "1");
                SemanticStack.pop();
				outputCode.codes.add(jzCode);
                PC++;
                break;
            }
            case "@wjp":{
                ThreeAddressCode jzCode = new ThreeAddressCode("JMP", beforeLoopStack.pop(), "", "");
                ThreeAddressCode labelCode = new ThreeAddressCode("LBL", afterLoopStack.pop(), "", "");
                outputCode.codes.add(jzCode);
                outputCode.codes.add(labelCode);
                PC+=2;
                break;
            }
			case "@brk":{
				if (afterLoopStack.isEmpty())
					throw new Error("break must be in loop");
				else {
					ThreeAddressCode code = new ThreeAddressCode("JMP", afterLoopStack.peek(), "", "");
					outputCode.codes.add(code);
					PC++;
				}
				break;
			}
			case "@cnt":{
				if (beforeLoopStack.isEmpty())
					throw new Error("Continue must be in loop");
				else {
					ThreeAddressCode code = new ThreeAddressCode("JMP", beforeLoopStack.peek(), "", "");
					outputCode.codes.add(code);
					PC++;
				}
				break;
			}
			case "@switchbegin":{
				int lastReg = registerStack.peek().getKey();
				String type = registerStack.peek().getValue();
				// TODO : this register must be popped at the end of switch statement
				if (!type.equals("int") && !type.equals("char")) {
					throw new Error("switch type must be char or int");
				}
				SymbolTableNode stp = new SymbolTableNode();
				SemanticStack.pop();
				SemanticStack.push(stp);
				ThreeAddressCode tacMaxCheck = new ThreeAddressCode("LEQ", "R" + String.valueOf(lastRegister), "R" +
				String.valueOf(lastReg), "");
				ThreeAddressCode tacJumpOfMax = new ThreeAddressCode("JIZ", "",
						"R" + String.valueOf(lastRegister), "1");
				ThreeAddressCode tacMinCheck = new ThreeAddressCode("GEQ", "R" + String.valueOf(lastRegister), "R" +
						String.valueOf(lastReg), "");
				ThreeAddressCode tacJumpOfMin = new ThreeAddressCode("JIZ", "",
						"R" + String.valueOf(lastRegister), "1");
				ThreeAddressCode jumpTableJump = new ThreeAddressCode("JMP", "", "", "");
				outputCode.codes.add(tacMaxCheck);
				outputCode.codes.add(tacJumpOfMax);
				outputCode.codes.add(tacMinCheck);
				outputCode.codes.add(tacJumpOfMin);
				outputCode.codes.add(jumpTableJump);
				codeStack.push(tacMaxCheck);
				codeStack.push(tacJumpOfMax);
				codeStack.push(tacMinCheck);
				codeStack.push(tacJumpOfMin);
				codeStack.push(jumpTableJump);
				afterLoopStack.push("LABEL" + String.valueOf(labelCounter));
				labelCounter++;
				lastRegister++;
				break;
			}
			case "@switchcaseliterals":{
				outputCode.codes.remove(outputCode.codes.size() - 1); // TODO: AMAZING TOF
				registerStack.pop();
				SymbolTableNode sampleStp = SemanticStack.pop();
				SemanticStack.peek().switchCaseUtility.cases.add(new Pair<>(sampleStp.intValue, labelCounter));
				ThreeAddressCode caseLabel = new ThreeAddressCode("LBL", "LABEL" + String.valueOf(labelCounter), "", "");
				outputCode.codes.add(caseLabel);
				labelCounter++;
				break;
			}
			case "@switchhasdefault":{
				SemanticStack.peek().switchCaseUtility.defaultCase = labelCounter;
				ThreeAddressCode caseLabel = new ThreeAddressCode("LBL", "LABEL" + String.valueOf(labelCounter), "", "");
				outputCode.codes.add(caseLabel);
				labelCounter++;
				break;
			}
			case "@switchfinalize":{
				SymbolTableNode sampleStp = SemanticStack.pop();
				sampleStp.switchCaseUtility.cases.sort(new Comparator<Pair<Integer, Integer>>() {
					@Override
					public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
						return o1.getKey() < o2.getKey() ? -1 : 1;
					}
				});
				int reg = registerStack.pop().getKey();
				String outlbl = afterLoopStack.peek();
				if (sampleStp.switchCaseUtility.defaultCase != -1)
					outlbl = "LABEL"  + String.valueOf(sampleStp.switchCaseUtility.defaultCase);
				ThreeAddressCode jumpTableJump = codeStack.pop();
				ThreeAddressCode tacJumpOfMin = codeStack.pop();
				ThreeAddressCode tacMinCheck = codeStack.pop();
				tacMinCheck.operand2 = "#" + String.valueOf(sampleStp.switchCaseUtility.cases.get(0).getKey());
				ThreeAddressCode tacJumpOfMax = codeStack.pop();
				ThreeAddressCode tacMaxCheck = codeStack.pop();
				tacMaxCheck.operand2 = "#" + String.valueOf(
						sampleStp.switchCaseUtility.cases.get(sampleStp.switchCaseUtility.cases.size() - 1).getKey());
				ThreeAddressCode jumpNormalizer = new ThreeAddressCode("SUB", "R" + String.valueOf(reg),
						"R" + String.valueOf(reg), "#" + (sampleStp.switchCaseUtility.cases.get(0).getKey()-1));
				outputCode.codes.add(outputCode.codes.lastIndexOf(jumpTableJump), jumpNormalizer);
				ArrayList<ThreeAddressCode> jmpTable = new ArrayList<>();
				tacJumpOfMax.destincation = outlbl;
				tacJumpOfMin.destincation = outlbl;
				jumpTableJump.destincation = "R" + String.valueOf(reg);
				for(int i = 0; i < sampleStp.switchCaseUtility.cases.size(); i++) {
					jmpTable.add(new ThreeAddressCode("JMP", "LABEL" + sampleStp.switchCaseUtility.cases.get(i).getValue(), "", ""));
					int diff = 0;
					if (i + 1 <  sampleStp.switchCaseUtility.cases.size() &&
							(diff = sampleStp.switchCaseUtility.cases.get(i + 1).getKey() - sampleStp.switchCaseUtility.cases.get(i).getKey()) > 1) {
						for(int j = 0; j < diff - 1; j++) {
							jmpTable.add(new ThreeAddressCode("JMP", outlbl, "", ""));
						}
					}
				}
				outputCode.codes.addAll(outputCode.codes.lastIndexOf(jumpTableJump) + 1, jmpTable);
				outputCode.codes.add(new ThreeAddressCode("LBL", afterLoopStack.pop(), "", ""));
				break;
			}
			case "@startfor":{
				String l1 = "LABEL" + labelCounter++;
				String l2 = "LABEL" + labelCounter++;
				String l3 = "LABEL" + labelCounter++;
				String l4 = "LABEL" + labelCounter++;
				labelStack.push(l1);
				labelStack.push(l2);
				labelStack.push(l3);
				labelStack.push(l4);
				beforeLoopStack.push(l4);
				afterLoopStack.push(l3);
				outputCode.codes.add(new ThreeAddressCode("LBL", l1, "", ""));
				break;
			}
			case "@forjizjp":{
				int tbe = registerStack.pop().getKey();
				SemanticStack.pop();
				outputCode.codes.add(new ThreeAddressCode("JIZ", labelStack.get(labelStack.size() - 2),
						"R" + tbe, "1"));
				outputCode.codes.add(new ThreeAddressCode("JMP", labelStack.get(labelStack.size() - 3), "", ""));
				outputCode.codes.add(new ThreeAddressCode("LBL", labelStack.peek(), "", ""));
				break;
			}
			case "@normalizefor":{
				outputCode.codes.add(new ThreeAddressCode("JMP", labelStack.get(labelStack.size() - 4), "", ""));
				outputCode.codes.add(new ThreeAddressCode("LBL", labelStack.get(labelStack.size() - 3), "", ""));
				break;
			}
			case "@endfor":{
				outputCode.codes.add(new ThreeAddressCode("JMP", labelStack.peek(), "", ""));
				outputCode.codes.add(new ThreeAddressCode("LBL", labelStack.get(labelStack.size() - 2), "", ""));
				labelStack.pop();
				labelStack.pop();
				labelStack.pop();
				labelStack.pop();
				afterLoopStack.pop();
				beforeLoopStack.pop();
				break;
			}
			case "@startofdo":{
				String labelName = "LABEL" + labelCounter++;
				outputCode.codes.add(new ThreeAddressCode("LBL", labelName, "", ""));
				labelStack.push(labelName);
				break;
			}
			case "@endofdo":{
				SemanticStack.pop();
				outputCode.codes.add(new ThreeAddressCode("JNZ", labelStack.pop(),
						"R" + registerStack.pop().getKey(), "1"));
				break;
			}
			case "@putlabel":{
				if (labelsHolder.get(currentFunction) == null)
					labelsHolder.put(currentFunction, new ArrayList<>());
				labelsHolder.get(currentFunction).add(SemanticStack.peek().name);
				outputCode.codes.add(new ThreeAddressCode("LBL", "%" + SemanticStack.pop().name, "", ""));
				break;
			}
			case "@goto":{
				if (labelsHolder.get(currentFunction).lastIndexOf(scanner.stp.name) >= 0) {
					outputCode.codes.add(new ThreeAddressCode("JMP", "%" + scanner.stp.name, "", ""));
				} else {
					throw new Error("the goto id must be in same function");
				}
				break;
			}
			case "@ppa":{
				String addressType = "";
				if (scanner.stp.memType == MemType.HEAP)
					addressType = "@#";
				else if (scanner.stp.memType == MemType.STACK)
					addressType = "@^";
				outputCode.codes.add(new ThreeAddressCode("LOD", "R" + ((Integer) lastRegister).toString(),
						addressType + ((Integer) scanner.stp.address).toString(), ((Integer) scanner.stp.size).toString()));
				registerStack.push(new Pair<Integer, String>(lastRegister, scanner.stp.varType));
				String reg = "R" + registerStack.peek().getKey();
				outputCode.codes.add(new ThreeAddressCode(
						"ADD",
						"R" + lastRegister,
						reg,
						"#1"
				));

				addressType = "" ;

				outputCode.codes.add(new ThreeAddressCode("ADD", "R" + lastRegister,
						reg, "#1"));
				addressType = "";
				if (SemanticStack.peek().memType == MemType.HEAP)
					addressType = "@#";
				else if (SemanticStack.peek().memType == MemType.STACK)
					addressType = "@^";

				if (SemanticStack.peek().addressingType == MemType.REGISTER)
					addressType += "R" ;

				outputCode.codes.add(new ThreeAddressCode(
						"STR",
						addressType + SemanticStack.peek().address,
						"R" + lastRegister,
						""+SemanticStack.peek().size
				));
				lastRegister++;
				break;
			}
			case "@ppb":{
				String addressType = "";
				if (scanner.stp.memType == MemType.HEAP)
					addressType = "@#";
				else if (scanner.stp.memType == MemType.STACK)
					addressType = "@^";
				outputCode.codes.add(new ThreeAddressCode("LOD", "R" + ((Integer) lastRegister).toString(),
						addressType + ((Integer) scanner.stp.address).toString(), ((Integer) scanner.stp.size).toString()));
				registerStack.push(new Pair<Integer, String>(lastRegister, scanner.stp.varType));
				String reg = "R" + registerStack.peek().getKey();
				outputCode.codes.add(new ThreeAddressCode("ADD", "R" + lastRegister,
						reg, "#1"));
				addressType = "";
				if (SemanticStack.peek().memType == MemType.HEAP)
					addressType = "@#";
				else if (SemanticStack.peek().memType == MemType.STACK)
					addressType = "@^";

				if (SemanticStack.peek().addressingType == MemType.REGISTER)
					addressType += "R" ;

				outputCode.codes.add(new ThreeAddressCode(
						"STR",
						addressType + SemanticStack.peek().address,
						"R" + lastRegister,
						""+SemanticStack.peek().size
				));
				registerStack.pop();
				registerStack.push(new Pair<>(lastRegister, SemanticStack.peek().varType));
				lastRegister++;
				break;
			}
			case "@mmb":{
				String addressType = "";
				if (scanner.stp.memType == MemType.HEAP)
					addressType = "@#";
				else if (scanner.stp.memType == MemType.STACK)
					addressType = "@^";
				outputCode.codes.add(new ThreeAddressCode("LOD", "R" + ((Integer) lastRegister).toString(),
						addressType + ((Integer) scanner.stp.address).toString(), ((Integer) scanner.stp.size).toString()));
				registerStack.push(new Pair<Integer, String>(lastRegister, scanner.stp.varType));
				String reg = "R" + registerStack.peek().getKey();
				outputCode.codes.add(new ThreeAddressCode("SUB", "R" + lastRegister,
						reg, "#1"));
				addressType = "";
				if (SemanticStack.peek().memType == MemType.HEAP)
					addressType = "@#";
				else if (SemanticStack.peek().memType == MemType.STACK)
					addressType = "@^";

				if (SemanticStack.peek().addressingType == MemType.REGISTER)
					addressType += "R" ;

				outputCode.codes.add(new ThreeAddressCode(
						"STR",
						addressType + SemanticStack.peek().address,
						"R" + lastRegister,
						""+SemanticStack.peek().size
				));
				lastRegister++;
				registerStack.push(new Pair<>(lastRegister, SemanticStack.peek().varType));
				break;
			}
			case "@mma":{
				String addressType = "";
				if (scanner.stp.memType == MemType.HEAP)
					addressType = "@#";
				else if (scanner.stp.memType == MemType.STACK)
					addressType = "@^";
				outputCode.codes.add(new ThreeAddressCode("LOD", "R" + ((Integer) lastRegister).toString(),
						addressType + ((Integer) scanner.stp.address).toString(), ((Integer) scanner.stp.size).toString()));
				registerStack.push(new Pair<Integer, String>(lastRegister, scanner.stp.varType));
				String reg = "R" + registerStack.peek().getKey();
				outputCode.codes.add(new ThreeAddressCode("SUB", "R" + lastRegister,
						reg, "#1"));
				addressType = "";
				if (SemanticStack.peek().memType == MemType.HEAP)
					addressType = "@#";
				else if (SemanticStack.peek().memType == MemType.STACK)
					addressType = "@^";

				if (SemanticStack.peek().addressingType == MemType.REGISTER)
					addressType += "R" ;

				outputCode.codes.add(new ThreeAddressCode(
						"STR",
						addressType + SemanticStack.peek().address,
						"R" + lastRegister,
						""+SemanticStack.peek().size
				));
				lastRegister++;
				break;
			}
			case "@include":{
				FileInputStream fin = null;
				FileOutputStream out = null;
				try {
					fin = new FileInputStream("input2.clike");
					out = new FileOutputStream("tmp");

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				Parser parser = ParserInitializer.createParser("PT_new_40.npt", fin, out, scanner.blockManager);

				if (parser == null) {
					System.err.println("Parser Initializing Error -> Parser not initialized");
					return;
				}

				try
				{
					outputCode.codes.addAll(0, parser.parse().codes); // parse all the input
				}
				catch (Exception ex)
				{
					System.err.println("Compile Error -> " + ex.getMessage());
				}

				break;
			}
            case "@externtype":{
                break;
            }
            case "@extern":{
                SymbolTableNode tmp = scanner.blockManager.get(scanner.stp.name);
                if (tmp == null)
                    throw new Error("extern function must be declared");
                break;
            }
			default:
				System.err.println("Code Generation Error -> Unknown semantic function " + sem);
		}


    }

    void Optimize() {
    	boolean changed = true;
        while (changed) {
            changed = false;
            ArrayList<RegisterHolder> registersHolder = new ArrayList<>();
            for (int i = 0; i < outputCode.codes.size(); i++) {
                ThreeAddressCode tac = outputCode.codes.get(i);
                switch (tac.instruction) {
                    case "LOD": {
                        if (tac.operand1.contains("#")) {
                            registersHolder.add(new RegisterHolder(tac.destincation, i,
                                    tac.operand1));
                        }
                        break;
                    }
                }
            }
            ArrayList<Integer> mustBeRemoved = new ArrayList<>();
            for(RegisterHolder rh: registersHolder) {
                boolean find = false;
                for (int i = rh.getStart() + 1; i < outputCode.codes.size(); i++) {
                    ThreeAddressCode tac = outputCode.codes.get(i);
                    if (tac.destincation.equals(rh.getName())) {
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    mustBeRemoved.add(rh.getStart());
                    for (int i = rh.getStart() + 1; i < outputCode.codes.size(); i++) {
                        ThreeAddressCode tac = outputCode.codes.get(i);
                        if (tac.operand1.equals(rh.getName())) {
                            tac.operand1 = rh.getVal();
                            changed = true;
                        }
                        if (tac.operand2.equals(rh.getName())) {
                            tac.operand2 = rh.getVal();
                            changed = true;
                        }
                    }
                }
            }
            Collections.sort(mustBeRemoved, Collections.reverseOrder());
            for (int k = 0; k < mustBeRemoved.size(); k++) {
                outputCode.codes.remove((int)mustBeRemoved.get(k));
            }
            for (int i = 0; i < outputCode.codes.size(); i++) {
                ThreeAddressCode tac = outputCode.codes.get(i);
                switch (tac.instruction) {
                    case "ADD": {
                        if (tac.operand1.contains("#") && tac.operand2.contains("#")) {
                            tac.instruction = "LOD";
                            tac.operand2 = "4";
                            tac.operand1 = "" + (Integer.parseInt(tac.operand1.substring(1, tac.operand1.length()))
                                    + Integer.parseInt(tac.operand2.substring(1, tac.operand2.length())));
                            changed = true;
                        }
                        break;
                    }
                    case "SUB": {
                        if (tac.operand1.contains("#") && tac.operand2.contains("#")) {
                            tac.instruction = "LOD";
                            tac.operand2 = "4";
                            tac.operand1 = "" + (Integer.parseInt(tac.operand1.substring(1, tac.operand1.length()))
                                    - Integer.parseInt(tac.operand2.substring(1, tac.operand2.length())));
                            changed = true;
                        }
                        break;
                    }
                    case "MUL": {
                        if (tac.operand1.contains("#") && tac.operand2.contains("#")) {
                            tac.instruction = "LOD";
                            tac.operand2 = "4";
                            tac.operand1 = "" + (Integer.parseInt(tac.operand1.substring(1, tac.operand1.length()))
                                    * Integer.parseInt(tac.operand2.substring(1, tac.operand2.length())));
                            changed = true;
                        }
                        break;
                    }
                    case "DIV": {
                        if (tac.operand1.contains("#") && tac.operand2.contains("#")) {
                            tac.instruction = "LOD";
                            tac.operand2 = "4";
                            tac.operand1 = "" + (Integer.parseInt(tac.operand1.substring(1, tac.operand1.length()))
                                    / Integer.parseInt(tac.operand2.substring(1, tac.operand2.length())));
                            changed = true;
                        }
                        break;
                    }
                    case "ADF": {
                        if (tac.operand1.contains("#") && tac.operand2.contains("#")) {
                            tac.instruction = "LOD";
                            tac.operand2 = "8";
                            tac.operand1 = "" + (Integer.parseInt(tac.operand1.substring(1, tac.operand1.length()))
                                    + Integer.parseInt(tac.operand2.substring(1, tac.operand2.length())));
                            changed = true;
                        }
                        break;
                    }
                    case "SBF": {
                        if (tac.operand1.contains("#") && tac.operand2.contains("#")) {
                            tac.instruction = "LOD";
                            tac.operand2 = "8";
                            tac.operand1 = "" + (Integer.parseInt(tac.operand1.substring(1, tac.operand1.length()))
                                    - Integer.parseInt(tac.operand2.substring(1, tac.operand2.length())));
                            changed = true;
                        }
                        break;
                    }
                    case "MLF": {
                        if (tac.operand1.contains("#") && tac.operand2.contains("#")) {
                            tac.instruction = "LOD";
                            tac.operand2 = "8";
                            tac.operand1 = "" + (Integer.parseInt(tac.operand1.substring(1, tac.operand1.length()))
                                    * Integer.parseInt(tac.operand2.substring(1, tac.operand2.length())));
                            changed = true;
                        }
                        break;
                    }
                    case "DVF": {
                        if (tac.operand1.contains("#") && tac.operand2.contains("#")) {
                            tac.instruction = "LOD";
                            tac.operand2 = "8";
                            tac.operand1 = "" + (Integer.parseInt(tac.operand1.substring(1, tac.operand1.length()))
                                    / Integer.parseInt(tac.operand2.substring(1, tac.operand2.length())));
                            changed = true;
                        }
                        break;
                    }
                }
            }
        }
	}

    /**
     * It is called after parsing is done without errors
     */
    void FinishCode() // You may need this, or may not
    {

    	if (scanner.blockManager.get("start") == null){
    		throw new Error("need a start function") ;
		}

		SymbolTableNode start = scanner.blockManager.get("start") ;
    	outputCode.codes.add(new ThreeAddressCode(
    			"CAL",
				start.overloads.get(0).name,
				"",
				""
		));

    	for (int i = 0 ;i < outputCode.codes.size();i++){
			System.out.println(outputCode.codes.get(i).instruction+ " " +
					outputCode.codes.get(i).destincation + " " +
					outputCode.codes.get(i).operand1 + " " +
					outputCode.codes.get(i).operand2 + " ");
		}

    }

    /**
     * Used to write any needed output after the parsing is done.
     */
    public void WriteOutput(OutputStream os)
    {
        // This is called after the parsing is done.
    	// Can be used to print the generated code to output
    	// This is used because in the process of compiling,the generated code is stored in some sort of structure
    	// You can output code lines just when they are generated,
        // but this not recommended due to possible further errors, controls structures of program that change the order of generated code according to source code, ...
		OutputStreamWriter osw = new OutputStreamWriter(os);
		try {
			osw.write("\nCODE: \n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < outputCode.codes.size(); i++)
			try {
				osw.flush();
				osw.write(outputCode.codes.get(i).instruction + " " + outputCode.codes.get(i).destincation + " " +
						outputCode.codes.get(i).operand1 + " " + outputCode.codes.get(i).operand2);
				osw.write("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		try {
			osw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
