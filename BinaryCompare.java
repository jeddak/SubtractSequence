package com.treeblossom.util.bincompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Compares two files byte-by-byte.
 * Bug: if fileA is larger than fileB, (fileA is a subset of fileB), program may declare a perfect match.
 * @author jdonald
 *
 */
public class BinaryCompare {


    /**
     * @param args
     */
    public static void main(String[] args) {
        doCompare(args);
    }

    /**
     * 
     * @param args
     */
    public static void doCompare(String[] args) {
        String inputFileA = args[0];
        String inputFileB = args[1];

        long result = compare(inputFileA, inputFileB);
        if (result == 0) {
            System.out.printf("%s and %s are identical", inputFileA,
                    inputFileB);
        } else {
            System.out.printf(
                    "%s and %s are different (first difference detected at byte %d",
                    inputFileA, inputFileB, result);
        }
    }

    /**
     * @param inputFileA
     * @param inputFileB
     * @return
     */
    static long compare(String inputFileA, String inputFileB) {

        long position = 0L;
        boolean result = true;
        try (InputStream inputStreamA = new FileInputStream(inputFileA);
                InputStream inputStreamB = new FileInputStream(inputFileB);) {
            int byteA, byteB;
            boolean moreBytesA = true, moreBytesB = true;
            while (moreBytesA && moreBytesB) {
                position++;
                moreBytesA = ((byteA = inputStreamA.read()) != -1);
                moreBytesB = ((byteB = inputStreamB.read()) != -1);

                if (byteA != byteB) {

                    System.out.print("-");
                    result = false;
                    break;
                }
                System.out.print("+");
            }
            System.out.println("reached end of file "
                    + (moreBytesA ? " A" : moreBytesB ? " B" : ""));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return (result == true) ? 0 : position;
    }

}
