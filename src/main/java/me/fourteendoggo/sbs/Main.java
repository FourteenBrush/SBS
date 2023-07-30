package me.fourteendoggo.sbs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    @SuppressWarnings("unused")
    public static void main(String[] args) {

        // IntStream.range(1, 4).forEach(System.out::println);
        String loopCode = """
                    mov $ax 1 ; i
                    mov $bx 4 ; max
               lp:  mov $cx $bx
                    sub $cx $ax ; max - i
                    jnz0 $cx hlt
                    out $ax
                    inc $ax ; i++
                    jmp lp
               hlt: hlt
                """;

        // for (int i = 0; i < 3; i++) { stdout(i); }
        String code = """
                    ; constants
                    STA 0 0 ; i at offset 0
                    STA 4 3 ; const 3 for max
                    STA 8 1 ; const 1 for incrementing i
                    ; loop
                    LDA 4 ; load max
                    SUB 0 ; max - i
                    JNZ 11 ; jmp to hlt if i == max
                    ; loop body
                    LDA 0 ; load i
                    OUT ; stdout(i)
                    ADD 8 ; i++
                    STC 0 ; i = incremented i
                    JMP 3 ; next iteration
                    HLT ; break and exit
                """;

        String printArrayElements = """
                    @def arr_l 0
                    @def arr_st 4
                    @def i_off 12
                    
                    sta [i_off] 0 ; int i = 0
                l:  lda [arr_l]
                    sub [i_off] ; arr.length - i
                    jnz h ; halt if zero
                    lda [arr_st + [i_off]] ; load element
                    out
                    inc [i_off] ; i++
                    jmp l ; next iter
                h:  hlt
                """;
        /*
            sta 12 0
        l:  lda 0
            sub 12
            jnz h
            lde 4 12
            out
            inc 12
            jmp l
        h:  hlt
         */

        String testCode = """
                    @define arr_l $ax
                    @define arr_st_o [0]
                    @define i_o [4]
                    
                    sta arr_st_o 100
                    sta [arr_st_o + 4] 300
                    sta i_o 0
                l:  lda i_o
                    sub arr_l
                    out
                    jpz h
                    lda [arr_st_o + i_o]
                    inc i_o
                    jmp l
                h:  hlt
                """;

        String print100200 = """
                    mov [0] 100
                    mov [4] 200
                    mov $ax 2 ; len
                    mov $bx 0 ; i
                
              lp:   mov $cx $bx ; i
                    sub $cx $ax ; i - len
                    jpz0 $cx hlt
                    ; load element
                    ; do not overwrite ax and bx in loop body
                    mov $dx $bx
                    mul $dx 4 ; elem width
                    ps
                    out [$dx]
                    
                    inc $bx ; i++
                    jmp lp
              hlt:  hlt
                """;

        if (args.length != 2 || !args[0].equals("--input")) {
            System.err.println("Usage: sbs --input <file>");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Path.of(args[1]));
            new SBS(lines).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
