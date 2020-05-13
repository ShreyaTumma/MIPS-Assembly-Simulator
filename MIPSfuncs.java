import java.io.*;
import java.util.*;

public class MIPSfuncs {
    
    public static void writeReg(int [] reg_file, int register, int val){
        reg_file[register] = val;
    }
    public static int readReg(int [] reg_file, int register){
        return reg_file[register];
    }
    
    public static int readData(int [] data_mem, int index ){
        return data_mem[index]; 
    }
    public static void writeData(int [] data_mem, int index, int val){
        data_mem[index] = val;
    }    
    
    public void and(int [] reg_file, int rs, int rt, int rd){

        int rs_val = readReg(reg_file, rs);
        int rt_val = readReg(reg_file, rt);
        int rd_val = rs_val & rt_val;
        writeReg(reg_file, rd, rd_val);
    }
    public void add(int [] reg_file, int rs, int rt, int rd){
        
        int rs_val = readReg(reg_file, rs);
        int rt_val = readReg(reg_file, rt);
        int rd_val = rs_val + rt_val;
        writeReg(reg_file, rd, rd_val);
    }
    public void or(int [] reg_file, int rs, int rt, int rd){
        int rs_val = readReg(reg_file, rs);
        int rt_val = readReg(reg_file, rt);
        int rd_val = rs_val | rt_val;
        writeReg(reg_file, rd, rd_val);
    }
    public void sll(int [] reg_file, int rt, int rd, int shamt){
        int rt_val = readReg(reg_file, rt);
        int rd_val = rt_val << shamt;
        writeReg(reg_file, rd, rd_val);
    }
    public void sub(int [] reg_file, int rs, int rt, int rd){
        int rs_val = readReg(reg_file, rs);
        int rt_val = readReg(reg_file, rt);
        int rd_val = rs_val - rt_val;
        writeReg(reg_file, rd, rd_val);
        
    }
    public void slt(int [] reg_file, int rs, int rt, int rd){
        int rs_val = readReg(reg_file, rs);
        int rt_val = readReg(reg_file, rt);
        int rd_val =0;
        if(rs_val < rt_val){
            rd_val = 1;
        }
        else
        {
            rd_val = 0;
        }
       
        writeReg(reg_file, rd, rd_val);
       
    }

    public void addi(int [] reg_file, int rs, int rt, int imm){
        int rs_val = readReg(reg_file, rs);
        int rt_val = rs_val + imm;
        
        // flipped literally everything
        writeReg(reg_file, rt, rt_val);
    }
    public void sw(int [] reg_file, int [] data_mem, int rs, int rt, int imm){
        int rs_val = readReg(reg_file, rs);
        int rt_val = readReg(reg_file, rt);
        writeData(data_mem, rs_val + imm, rt_val);
    }
    public void lw(int [] reg_file, int [] data_mem, int rs, int rt, int imm){
        int rs_val = readReg(reg_file, rs);       
        int mem_val = readData(data_mem, rs_val + imm);
        writeReg(reg_file, rt, mem_val);
    }

    /* these functions involve the PC , needs to go to that address */
    /* should we store the address for each line in mCodes ? */

    public int jr(int [] reg_file, int rs, int PC){
        int rs_val = readReg(reg_file, rs);
        
        // this is the new PC val to index with
        return rs_val - PC;
    }
    public int beq(int[] reg_file, int rs, int rt, int imm, int PC){
        int rs_val = readReg(reg_file, rs);
        int rt_val = readReg(reg_file, rt);
        if (rs_val == rt_val) {
            //return PC + 1 + imm;
            return 1 + imm;
        }
        return 1;
    }
    public int bne(int[] reg_file, int rs, int rt, int imm, int PC){
        int rs_val = readReg(reg_file, rs);
        int rt_val = readReg(reg_file, rt);
        if (rs_val != rt_val) {
            return 1 + imm;
        }
        return 1;
    }
    
    public int j(int imm, int PC){
        return imm - PC;
    }
    public int jal(int[] reg_file, int imm, int PC){
        writeReg(reg_file, 31, PC + 1);
        return imm - PC;
    }
}
