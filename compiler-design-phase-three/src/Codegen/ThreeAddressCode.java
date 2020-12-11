package Codegen;

/**
 * Created by root on 6/20/17.
 */
public class ThreeAddressCode {
    public String instruction, destincation, operand1, operand2;
    public ThreeAddressCode(){

    }
    public ThreeAddressCode(String instruction, String destincation, String operand1, String operand2) {
        this.instruction = instruction;
        this.destincation = destincation;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
}
