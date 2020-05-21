//Name: Gabriel Barney and Shreya Tumma
//Section: CPE 315-03
//Description: Two Pass Assembler, MIPS simulator

import java.io.*;
import java.util.*;

class lab4 {
    
    // starting address in MIPS: 0x00400000, but for this assignment start at zero
    static Map<String, String> opCodes = new HashMap<String, String>() {{
        put("and", "000000");
        put("add", "000000"); 
        put("or", "000000");
        put("sll", "000000");
        put("sub", "000000");
        put("slt", "000000");
        put("jr", "000000");
        put("addi", "001000");
        put("beq", "000100");
        put("bne", "000101");
        put("lw", "100011");
        put("sw", "101011");
        put("j", "000010");
        put("jal", "000011"); 
    }};


    static Map<String, String> functionCodes = new HashMap<String, String>() {{
        put("and", "00000 100100");
        put("add", "00000 100000");
        put("or", "00000 100101");
        put("addi", "immediate");
        put("sll", "sa000000"); 
        put("sub", "00000 100010");
        put("slt", "00000 101010");
        put("beq", "offset");
        put("bne", "offset");
        put("lw", "offset");
        put("sw", "offset");
        put("j", "target");
        put("jr", "001000"); 
        put("jal", "target");
    }};

    static Map<String, String> regCodes = new HashMap<String, String>() {{
        put("0", "00000");
        put("zero", "00000");
        put("v0", "00010");
        put("v1", "00011");
        put("a0", "00100");
        put("a1", "00101");
        put("a2", "00110");
        put("a3", "00111");
        put("t0", "01000");
        put("t1", "01001");
        put("t2", "01010");
        put("t3", "01011");
        put("t4", "01100");
        put("t5", "01101");
        put("t6", "01110");
        put("t7", "01111");
        put("s0", "10000");
        put("s1", "10001");
        put("s2", "10010");
        put("s3", "10011");
        put("s4", "10100");
        put("s5", "10101");
        put("s6", "10110");
        put("s7", "10111");
        put("t8", "11000");
        put("t9", "11001");
        put("sp", "11101");
        put("ra", "11111");
    }};

    static int PC = 0;
    static int CYCLES = 0; 
    static String[] PIPELINE = {"empty", "empty", "empty", "empty"};
    static ArrayList<ArrayList<String>> PIPELINE_REGS = new ArrayList<ArrayList<String>>();
  //  static String[][] TEMP = new String[100][100];
    // PIPELINE_REGS.get(i).get(0) == PC
    // PIPELINE_REGS.get(i).get(1) == RS
    // PIPELINE_REGS.get(i).get(2) == RT
    
    public static void main(String args[]) {
        
    
        Map<String, String> labels = new HashMap<String, String>();
        ArrayList<String> mCodes = new ArrayList<String>();
        int [] data_mem = new int [8192];
        int [] reg_file = new int [32];
        MIPSfuncs funcs = new MIPSfuncs();
        TwoPassAsm twoPass =  new TwoPassAsm();
        CPUfuncs cfuncs = new CPUfuncs();

       // CYCLES++;
        try {
            File file = new File(args[0]);
            FileReader fread = new FileReader(file);
            BufferedReader bread = new BufferedReader(fread);
            StringBuffer buff = new StringBuffer();
            String line;
            int hexAddress = -1;
            ArrayList< ArrayList<String>> instructions = new ArrayList<ArrayList<String> >();

            while ((line = bread.readLine()) != null) {
                hexAddress = twoPass.getLabelAddresses(line, hexAddress, labels, opCodes, instructions);
                buff.append(line);
                buff.append('\n'); 
            }
            fread.close();

            mCodes = twoPass.makeMachineCode(labels, opCodes,  functionCodes, regCodes, instructions, mCodes);
    /*        for(int i = 0; i < mCodes.size(); i++){
                System.out.print(mCodes.get(i) + "\n");
            }
            for (int i = 0; i < PIPELINE.length; i++) {
                System.out.println(PIPELINE[i]);
            }*/
            
            PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList("Null", "Null", "Null")));

            for (int i = 0; i < PIPELINE_REGS.size(); i++) {
                for (int j = 0; j < PIPELINE_REGS.get(i).size() ; j++) {
                    System.out.println(PIPELINE_REGS.get(i).get(j));
                }
            }    

            if (args.length > 1) {
                // script
                File script = new File(args[1]);
                FileReader sread = new FileReader(script);
                BufferedReader sbread = new BufferedReader(sread);
            
                script_output(sbread, data_mem, reg_file, funcs, mCodes);
                sread.close();

            } else {
                int status = 1;
                Scanner command = new Scanner(System.in);
                int ret = 0;
                while (ret != -1) {
             //       CYCLES++; // Cycle added regardless of what pc is doing      
                    System.out.print("mips> ");
                    String cmd = command.nextLine();
                    ret = command_output(cmd, data_mem, reg_file, funcs, mCodes); 
                    if (ret == -2) {  
                        Arrays.fill(data_mem, 0);
                        Arrays.fill(reg_file, 0);
                        PC = 0;
                    }
                }
            }
          //  System.out.println("Cycles: " + CYCLES);

            
            } catch(IOException e) { 
                e.printStackTrace(); 
            }
    }

    public static int command_output(String cmd, int[] data_mem, int[] reg_file, MIPSfuncs funcs, ArrayList<String> mCodes) {
        
        String[] splitLine;            
        splitLine = cmd.split(" ");
        int i = 0;
        CPUfuncs cfuncs = new CPUfuncs();
            
        switch(splitLine[0]) {
            case("h"):
                cfuncs.h();
                break;
            case("d"):
                cfuncs.d(reg_file, PC);
                break;
            case("p"):
                cfuncs.p(PIPELINE, PC);
                break;
            case("m"):
                cfuncs.m(data_mem, Integer.parseInt(splitLine[1]), Integer.parseInt(splitLine[2]));
                break;
            case("c"):
                cfuncs.c();
                return -2;
            case("r"):
                i = PC;
                while ( i < mCodes.size()){
                    parseMCode(mCodes, reg_file, data_mem, funcs);
                    i = PC;
                }
              //  System.out.println("TEMP: " + TEMP);
                float CPI = ((float)CYCLES / PC);
                System.out.println("\nProgram complete");
                System.out.print("CPI = " + String.format("%2.03f", CPI));
                System.out.print("\tCycles = " + CYCLES);
                System.out.println("\tInstructions = " + PC + '\n');
                return i;
            case("s"):
                if (splitLine.length > 1) {
                    i = Integer.parseInt(splitLine[1]);
                    while (i > 0) {
                        parseMCode(mCodes, reg_file, data_mem, funcs);
                        i--;
                    }
                    cfuncs.s(Integer.parseInt(splitLine[1]));
                    cfuncs.p(PIPELINE, PC);
                    break;
                } else {
                    parseMCode(mCodes, reg_file, data_mem, funcs);
                    cfuncs.s(1);
                    cfuncs.p(PIPELINE, PC);
                    break;
                }
            default:
                return -1;
            }
            return 0;
    }

    public static int script_output(BufferedReader sbread, int[] data_mem, int[] reg_file, MIPSfuncs funcs, ArrayList<String> mCodes) {
        String line;
        String[] splitLine;  
        int i = 0; 
        CPUfuncs cfuncs = new CPUfuncs();
        try {     
        while ((line = sbread.readLine()) != null) {
        
       //     CYCLES++; // Cycle added regardless of what pc is doing      
            splitLine = line.split(" ");
             
            if (splitLine.length == 1) {
                System.out.println("mips> " + splitLine[0]);
            } else if (splitLine.length == 2) {
                System.out.println("mips> " + splitLine[0] + " " + 
                                              splitLine[1]);
            } else { 
                System.out.println("mips> " + splitLine[0] + " " + 
                                              splitLine[1] + " " +
                                              splitLine[2]);
            }
    
            switch(splitLine[0]) {
                case("h"):
                    cfuncs.h(); 
                    break;
                case("d"):
                    cfuncs.d(reg_file, PC);
                    break;
                case("p"):
                    cfuncs.p(PIPELINE, PC);
                    break;
                case("m"):
                    cfuncs.m(data_mem, Integer.parseInt(splitLine[1]), Integer.parseInt(splitLine[2]));
                    break;
                case("c"):
                    PC = 0;
                    Arrays.fill(data_mem, 0);
                    Arrays.fill(reg_file, 0);
                    cfuncs.c();
                    break;
                case("r"):
                    i = PC;
                    while ( i < mCodes.size()){
                        parseMCode(mCodes, reg_file, data_mem, funcs);
                        i = PC;
                    }

                    float CPI = ((float)CYCLES / PC);
                    System.out.println("\nProgram complete");
                    System.out.print("CPI = " + String.format("%2.03f", CPI));
                    System.out.print("\tCycles = " + CYCLES);
                    System.out.println("\tInstructions = " + PC + '\n');
                    System.out.println("\tInstructions = " + PC + '\n'); // prints when all lines of file execute
                    // BUG: For some reason only gets 40 cycles instead of 42 when running against first test case

                    break;
                case("s"):
                    if (splitLine.length > 1) { // probably have to work with pc value returned 
                        i = Integer.parseInt(splitLine[1]);
                        while (i > 0) {
                            parseMCode(mCodes, reg_file, data_mem, funcs);
                            i--;
                        }
                        cfuncs.s(Integer.parseInt(splitLine[1]));
                    } else {
                        parseMCode(mCodes, reg_file, data_mem, funcs);
                        cfuncs.s(1);
                    }
                    cfuncs.p(PIPELINE, PC);
                    break;
                default:
                    return 0;
            }
        }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
}
    
    
        public static void parseMCode(ArrayList<String> mCodes, int [] reg_file, int [] data_mem, MIPSfuncs funcs){
            
            // The check from our notes translated to code
            
            int ret = 0;  
            String stall = "stall";
    
     ///       System.out.println(" 1 Cycles: " + CYCLES + '\n' + PIPELINE_REGS);
            
          //  CYCLES++; // Cycle added regardless of what pc is doing      
//            if ((PIPELINE[1].equals("lw")) && ((PIPELINE_REGS.get(CYCLES - 1).get(2).equals(PIPELINE_REGS.get(CYCLES).get(1))) ||
//                                            (PIPELINE_REGS.get(CYCLES - 1).get(2).equals(PIPELINE_REGS.get(CYCLES).get(2))))) {
             
            //handle error statements
                if (mCodes.get(PC).contains(":") == false) {
                String[] splitLine = mCodes.get(PC).split(" ");
                
                PIPELINE[3] = PIPELINE[2]; // Moves everything in pipeline to the right
                PIPELINE[2] = PIPELINE[1];
                PIPELINE[1] = PIPELINE[0];
                
                if(splitLine[0].equals("000000")){
                    ret = rTypeFuncs(splitLine, funcs, reg_file);
                } else if(splitLine.length == 4){
                    ret = iTypeFuncs(splitLine, funcs, reg_file, data_mem);
                } else if(splitLine.length == 2){
                    ret = jTypeFuncs(splitLine, funcs, reg_file);
                }
              //  TEMP.append(PIPELINE); 
                System.out.println("1 TEMP: " + Arrays.toString(PIPELINE));
         /*       for (int i = 0; i < RevOpCodes.size() ; i++) {
                    if (RevOpCodes.get(i).equals(splitLine[0])) {
                        PIPELINE[0] = "hello";
                    }
                }*/
//                PIPELINE[0] = RevOpCodes.get(splitLine[0]);
            
                } 
            
            CYCLES++; // Cycle added regardless of what pc is doing      
           // System.out.println(" 2 Cycles: " + CYCLES + '\n' + PIPELINE_REGS);
            if (CYCLES > 1) {    
            if ((PIPELINE_REGS.get(CYCLES - 1).get(2).equals(PIPELINE_REGS.get(CYCLES).get(1))) ||
                                            (PIPELINE_REGS.get(CYCLES - 1).get(2).equals(PIPELINE_REGS.get(CYCLES).get(2))) ||
                                            (PIPELINE_REGS.get(CYCLES - 1).get(1).equals(PIPELINE_REGS.get(CYCLES).get(2))) ||
                                            (PIPELINE_REGS.get(CYCLES - 1).get(1).equals(PIPELINE_REGS.get(CYCLES).get(1)))) {
                PIPELINE[3] = PIPELINE[2]; // moving everything in pipeline to the right
                PIPELINE[2] = PIPELINE[1]; 
                PIPELINE[1] = stall;  // puts a stall in the pipeline
                PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList("Null", "Null", "Null"))); // null values for "stall" cycle
             //   TEMP.append(PIPELINE); 
                System.out.println("2 TEMP: " + Arrays.toString(PIPELINE));

                //CYCLES++;
                    
            }
            }
          //  else {
          //      PC = PC + ret;
          //  }
          //  } else {
            
                PC = PC + ret;
            
//            CYCLES++; // Cycle added regardless of what pc is doing      
        }

        private static int jTypeFuncs(String [] splitline, MIPSfuncs funcs, int [] reg_file) {
            String opCode = splitline[0];
            int imm = 0;
            

            if(splitline[1].charAt(0) == '1'){
                imm = convert_args(splitline[3]);
            } 
            else {
                imm = Integer.parseInt(splitline[1], 2); 
            }
            switch(opCode){
                case "000010":
                    PIPELINE[0] = "j"; // adding command to pipeline
                    PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), "Null", "Null"))); // statements like this for every block in each TypeFuncs method
                    return funcs.j(imm, PC);
                case "000011":
                    PIPELINE[0] = "jal";
                    PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), "Null", "Null")));
                    return funcs.jal(reg_file,imm, PC);
                default:
                    System.out.println("Error in jTypeFuncs"+ Arrays.toString(splitline));
                    break;
            }
            System.out.println("--------------\n");

            return 1;
        }

        private static int iTypeFuncs(String [] splitline, MIPSfuncs funcs, int [] reg_file, int [] data_mem) {
            String opCode = splitline[0];
        
            int rs = Integer.parseInt(splitline[1], 2);
            int rt = Integer.parseInt(splitline[2], 2);
            int imm = 0;
            if(splitline[3].charAt(0) == '1'){
                imm = convert_args(splitline[3]);
            } 
            else {
                imm = Integer.parseInt(splitline[3], 2); 
            }
     
            switch(opCode){
                
                case "001000":
                    PIPELINE[0] = "addi";
                    PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                    funcs.addi(reg_file, rs, rt, imm);
                    break;
                case "000100":
                    PIPELINE[0] = "beq";
                    PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                    return funcs.beq(reg_file, rs, rt, imm, PC);
                    
                    //break;
                case "000101":
                    PIPELINE[0] = "bne";
                    PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                    return funcs.bne(reg_file, rs, rt, imm, PC);
                    
                    //break;
                case "100011":
                    PIPELINE[0] = "lw";
                    PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                    funcs.lw(reg_file, data_mem, rs, rt, imm);
                    break;
                case "101011":
                    PIPELINE[0] = "sw";
                    PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                    funcs.sw(reg_file, data_mem, rs, rt, imm);
                    break;
                
                default:
                    System.out.println("Error in iTypeFuncs");
                    break;
            }

            return 1;
        }

        private static int rTypeFuncs(String [] splitline, MIPSfuncs funcs, int [] reg_file) {
            
            if(splitline.length == 4 && splitline[3].contains("001000")){
                
                return funcs.jr(reg_file, Integer.parseInt(splitline[1], 2), PC);


            }
            else {
                String funcCode = splitline[5];
               
                int rs = Integer.parseInt(splitline[1], 2);
                int rt = Integer.parseInt(splitline[2], 2);
                int rd = Integer.parseInt(splitline[3], 2);  

                
                switch(funcCode){
                    case "100100":
                        PIPELINE[0] = "and";
                        PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                        funcs.and(reg_file, rs, rt, rd);
                        break;
                    case "100000":
                        PIPELINE[0] = "add";
                        PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                        funcs.add(reg_file, rs, rt, rd);
                        break;
                    case "100101":
                        PIPELINE[0] = "or";
                        PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                        funcs.or(reg_file, rs, rt, rd);
                        break;
                    case "100010":
                        PIPELINE[0] = "sub";
                        PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                        funcs.sub(reg_file, rs, rt, rd);
                        break;
                    case "101010":
                        PIPELINE[0] = "slt";
                        PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                        funcs.slt(reg_file, rs, rt, rd);
                        break;
                    case "000000":
                        PIPELINE[0] = "sll";
                        PIPELINE_REGS.add(new ArrayList<String>(Arrays.asList(Integer.toString(PC), Integer.toString(rs), Integer.toString(rt))));
                        int shamt = Integer.parseInt(splitline[4], 2);
                        funcs.sll(reg_file,rt, rd, shamt);
                        break;
                    default:
                        System.out.print("Error in rTypeFuncs" + Arrays.toString(splitline));
                        break;
                }
            }
            return 1;
        }
      
        public static int convert_args(String split) {
            // source: Ivan D. on Stack Overflow 
            // link: https://stackoverflow.com/questions/14012013/java-converting-negative-binary-back-to-integer
            
            StringBuilder builder = new StringBuilder();
            int decimal = 0;
            int power = 0;
            for (int i = 0; i < split.length(); i++) {
                builder.append((split.charAt(i) == '1' ? '0' : '1'));
            }

            while (split.length() > 0) {
                int temp = Integer
                    .parseInt(builder.charAt((split.length()) - 1)+"");
                decimal += temp * Math.pow(2, power++);
                split = split.substring(0, split.length() - 1);
            }

            return ((decimal + 1) * (-1));
        }

}  
