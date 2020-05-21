/* Description: Two pass assembler to conver assembly code to machine code */

import java.io.*;
import java.util.*;

public class TwoPassAsm {
    
    /* method for first pass */
    public int getLabelAddresses(String line, int hexAddress, 
        Map<String, String> labels, Map<String, String> opCodes, ArrayList<ArrayList <String>> instructions) {
        
        int commentFlag = 0; 
        String[] splitLine = line.split("\\s+|,|\\$");
        ArrayList<String> instr = new ArrayList<String>();

        // Check for empty lines and comments
        if (splitLine != null || splitLine[0].contains("#") == false) {
            for(int i = 0; i < splitLine.length; i++){
                if (commentFlag == 1) { 
                    break;
                }
                // check for comments in same line as instruction                       
                if(splitLine[i].contains("#") == true) {
                    if (splitLine[i].indexOf("#") > 0) {
                        splitLine[i] = splitLine[i].substring(0, splitLine[i].indexOf("#"));
                        commentFlag = 1;
                    } else { 
                        break;
                    }
                }
                if(splitLine[i].length() > 0 ){
                    if (opCodes.containsKey(splitLine[i]) == true) {
                        hexAddress += 1;
                    }
                    // check for labels and store address
                    int indexCol = splitLine[i].indexOf(":");
                    if(indexCol > -1){
                        labels.put(splitLine[i].substring(0,indexCol), Integer.toString(hexAddress + 1));
                        if(indexCol != splitLine[i].length() - 1){
                            hexAddress += 1;
                            instr.add(splitLine[i].substring(indexCol +1));
                        }
                        continue;
                    } 
                    instr.add(splitLine[i]);
                }
            }
            if(instr.isEmpty() == false){
                instructions.add(instr);
            }  
        }
        return hexAddress;
    }

    /* method used for second pass */
    public ArrayList<String> makeMachineCode(Map<String, String> labels,
        Map<String, String> opCodes, Map<String, String> functionCodes, 
        Map<String, String> regCodes,ArrayList<ArrayList <String>> instructions, ArrayList<String> mCodes) {
        
        // i is the address of the current instruction
        for(int i = 0; i < instructions.size(); i++){
            String mCode = "";
            ArrayList<String> line = instructions.get(i);
            String current_inst = line.get(0);
            
            switch (line.size()) {
                case 4:
                    if(opCodes.containsKey(current_inst) == true){
                        if(functionCodes.get(current_inst) == "immediate" || functionCodes.get(current_inst) == "offset" ) {
                            mCode += iType(line, current_inst, labels, i, regCodes, functionCodes, opCodes);
                        } else if(functionCodes.get(current_inst).contains("sa") == true) {
                            mCode += shift(line, current_inst, regCodes, opCodes);
                        } else {
                            mCode += rType(line, current_inst, regCodes, functionCodes, opCodes);
                        }
                    }
                    else {
                        System.out.println("invalid instruction: " + current_inst);
                        System.out.println("Exiting program...");
                        System.exit(0);
                        mCode += "invalid instruction: " + current_inst;
                        mCodes.add(mCode);
                        return mCodes;
                    }
                    break;
                case 2:
                    String destination = line.get(1);
                    if (current_inst.equals("j") || current_inst.equals("jal")) {   // think about storing pc with jal
                        String binary = Integer.toString(Integer.parseInt(labels.get(destination)), 2);
                        String leadZeroes = String.format("%26s", binary).replace(' ', '0');
                        mCode += opCodes.get(current_inst) + " " + leadZeroes;
                        break;
                    } else if (current_inst.equals("jr")) {
                        mCode += (opCodes.get(current_inst) + " " + 
                                    regCodes.get(destination) + " " +
                                    "000000000000000" + " " +
                                    functionCodes.get(current_inst));
                        break;        
                    } else {
                        System.out.println("invalid instruction: " + current_inst);
                        System.out.println("Exiting program...");
                        System.exit(0);
                        mCode += "invalid instruction: " + current_inst;
                        mCodes.add(mCode);
                        return mCodes;
                    }
                default:
                    System.out.println("invalid instruction: " + current_inst);
                    System.out.println("Exiting program...");
                    System.exit(0);
                    System.out.println("Instruction Error.");
                }    
                mCodes.add(mCode);  
            }
            return mCodes;
        }

        public static String rType(ArrayList<String> line, String current_inst, Map<String, String> regCodes, 
            Map<String, String> functionCodes,Map<String, String> opCodes){
            //ex: add rd, rs, rt           
            String rd = regCodes.get(line.get(1));
            String rs = regCodes.get(line.get(2));
            String rt = regCodes.get(line.get(3));
            String opcode = opCodes.get(current_inst);
            String fCode = functionCodes.get(current_inst);

            return opcode + " " + rs + " " + rt + " " + rd + " " + fCode;
        }

        public static String shift(ArrayList<String> line, String current_inst, Map<String, String> regCodes, Map<String, String> opCodes){
            //ex: add rd, rs, rt    
            String rd = regCodes.get(line.get(1));
            String rs = "00000";
            String rt = regCodes.get(line.get(2));
            String opcode = opCodes.get(current_inst);
            String fCode = "000000";
            String binary = Integer.toBinaryString(Integer.parseInt(line.get(3)));
            String shamt = String.format("%5s",binary ).replace(' ', '0');

            return opcode + " " + rs + " " + rt + " " + rd + " " + shamt + " "+ fCode;
        }

        public static String iType(ArrayList<String> line, String current_inst, Map<String, String> labels, int i, 
            Map<String, String> regCodes, Map<String, String> functionCodes, Map<String, String> opCodes){

            String rt = regCodes.get(line.get(1));
            String rs = regCodes.get(line.get(2));
            if(functionCodes.get(current_inst).equals("offset") == true){
                
                rs = regCodes.get(line.get(1));
                rt = regCodes.get(line.get(2));
            }
            String opcode = opCodes.get(current_inst);
            String offset = line.get(3);
            int newOff = 0;
                      
            //check to see if label exists 
            if(labels.containsKey(line.get(3)) == true) {
                newOff = Integer.parseInt(labels.get(offset)) - (i+1);
            } else if(offset.contains(")") == true){
            
                rt = regCodes.get(line.get(1));
                rs = regCodes.get(line.get(3).substring(0,(line.get(3).length())-1));

                offset = line.get(2).substring(0, line.get(2).length()-1);
                newOff = Integer.parseInt(offset);
            } else if (current_inst.equals("bne")|| current_inst == "bne") {
                rs = regCodes.get(line.get(1));
                rt = regCodes.get(line.get(2));
            }
            else {
                newOff = Integer.parseInt(offset);
            }
            //check if offset is negative
            if(newOff < 0){
                offset = Integer.toString(twosCompliment(newOff));
            } else{
                offset = Integer.toString(newOff);
            }
                
            String binary = Integer.toBinaryString(Integer.parseInt(offset));
            String im = String.format("%16s", binary).replace(' ', '0');

            return opcode + " " + rs + " " + rt + " " + im ;
        }

        public static int twosCompliment(int offset){
            offset = offset * -1 ;
            int newOffset = (int)(Math.pow(2, 16)) - offset;
            return newOffset;
        }







}