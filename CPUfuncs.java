// Yuh

import java.util.*;

public class CPUfuncs {   

        public static void h() {
            System.out.println("\nh = show help");
            System.out.println("d = dump register state");
            System.out.println("s = single step through the program (i.e. execute 1 instruction and stop)");
            System.out.println("s num = step through num instructions of the program");
            System.out.println("r = run until the program ends");
            System.out.println("m num1 num2 = display data memory from location num1 to num2");
            System.out.println("c = clear all registers, memory, and the program counter to 0");
            System.out.println("q = exit the program\n");
        }

        public static void d(int[] reg_file, int PC) {   
            System.out.println("\npc = " + PC);
            System.out.println("$0 = " + reg_file[0] + "\t\t" +
                               "$v0 = " + reg_file[2] + "\t\t" +
                                "$v1 = " + reg_file[3] + "\t\t" +
                                "$a0 = " + reg_file[4] + "\t\t");
            System.out.println("$a1 = " + reg_file[5] + "\t\t" +
                               "$a2 = " + reg_file[6] + "\t\t" +
                                "$a3 = " + reg_file[7] + "\t\t" +
                                "$t0 = " + reg_file[8] + "\t\t");
            System.out.println("$t1 = " + reg_file[9] + "\t\t" +
                               "$t2 = " + reg_file[10] + "\t\t" +
                                "$t3 = " + reg_file[11] + "\t\t" +
                                "$t4 = " + reg_file[12] + "\t\t");
            System.out.println("$t5 = " + reg_file[13] + "\t\t" +
                               "$t6 = " + reg_file[14] + "\t\t" +
                                "$t7 = " + reg_file[15] + "\t\t" +
                                "$s0 = " + reg_file[16] + "\t\t");
            System.out.println("$s1 = " + reg_file[17] + "\t\t" +
                               "$s2 = " + reg_file[18] + "\t\t" +
                                "$s3 = " + reg_file[19] + "\t\t" +
                                "$s4 = " + reg_file[20] + "\t\t");
            System.out.println("$s5 = " + reg_file[21] + "\t\t" +
                               "$s6 = " + reg_file[22]+ "\t\t" +
                                "$s7 = " + reg_file[23] + "\t\t" +
                                "$t8 = " + reg_file[24] + "\t\t");
            System.out.println("$t9 = " + reg_file[25] + "\t\t" +
                               "$sp = " + reg_file[29] + "\t\t" +
                                "$ra = " + reg_file[31] + "\t\t\n");
        }        
    
        public static void m(int[] data_mem, int idx1, int idx2) {
            System.out.print("\n");
            for (int i = idx1; i <= idx2; i++) {
                System.out.println("[" + i + "] = " + data_mem[i]);
            }
            System.out.print("\n");
        }

        
        public static void c() {
            System.out.println("\tSimulator reset\n");
        }



        public static void s(int steps) { 
            System.out.println("\t" + steps + " instruction(s) executed");
        } 
}
