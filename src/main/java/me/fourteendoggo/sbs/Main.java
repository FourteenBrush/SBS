package me.fourteendoggo.sbs;

public class Main {
    public static void main(String[] args) {

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

        /*
        int[] arr = {100, 300};
        for (int x : arr) { stdout(x) }
         */
        int[] arr = {100, 300};
        MemoryAccess.putInt(0, arr.length);
        MemoryAccess.putArray(4, arr);

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
                    @def arr_l 2
                    @def arr_st_o [0]
                    @def i_o [4]
                    
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

        String test2 = """
                    @def arr_st_off 4
                    @def i_off 0
                    lda [arr_st_off + [i_off]]
                    out
                    hlt
                    
                    [arr_st_off] << 100
                    [arr_st_off + 4] << 300
                    [i_o] << 0
                    $ax << i_o
                """;

        new SBS(testCode).exec();
    }
}
