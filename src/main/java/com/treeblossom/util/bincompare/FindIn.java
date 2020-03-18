package com.treeblossom.util.bincompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Looks for a sequence in a file. If the sequence is found in the file, reports the starting position in that file.
 * @author jdonald
 *
 */
public class FindIn {

    public class Chunk {
        long startPos = 0L;
        long endPos = 0L;
        String filePath = null;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        FindIn bc = new FindIn();
        bc.doFindIn(args);
    }

    /**
     * @param args
     */
    void doFindIn(String[] args) {
        FindIn.Chunk chunkA = new Chunk();
        FindIn.Chunk chunkB = new Chunk();
        chunkA.filePath = args[0];
        chunkB.filePath = args[1];
        chunkA.startPos = 32;
        chunkA.endPos = new File(chunkA.filePath).length() - 1;
        chunkB.startPos = 0;
        chunkB.endPos = new File(chunkB.filePath).length() - 1;
        long foundPos = -1;
        try {
            foundPos = findIn(chunkA, chunkB);
        } catch (IOException ioe) {
            System.out.printf("I/O error: " + ioe.getMessage());
        }
        System.out.println();
        if (foundPos > -1) {
            System.out.printf("File %s occurs in %s starting at position %d", chunkA.filePath, chunkB.filePath, foundPos);
        } else {
            System.out.printf("File %s is not found in file %s", chunkA.filePath, chunkB.filePath);
        }

    }

    /**
     * Returns position of file A in file B.
     * 
     * @param chunkA
     * @param chunkB
     * @return
     */
    long findIn(Chunk chunkA, Chunk chunkB) throws IOException {

        long aPosition = chunkA.startPos;
        long bPosition = chunkB.startPos;
        long foundPos = -1;
        boolean result = false;

        while (result == false) {
            foundPos = scan(chunkA, chunkB);
            if (foundPos > -1) {
                result = true;
            } else {
                result = false;
               // System.out.printf("It's not at position %d\n", chunkB.startPos);//DEBUG
               System.out.println("-----------------------------------------------------------------------------------");
                chunkB.startPos++;
                if (chunkB.startPos >= chunkB.endPos) {
                    break;
                }
            }
        } // end while
        return foundPos;
    }

    /**
     * Scan the file pointed to by chunkB to see if it contains the sequence of bytes pointed to by chunkA.
     * If found, it returns the position in the 'B' file where the 'A' sequence begins.
     * @param chunkA
     * @param chunkB
     * @return
     */
    long scan(Chunk chunkA, Chunk chunkB) throws IOException {

        long position = chunkB.startPos;
        boolean result = true;
        FileInputStream inputStreamA = null;
        FileInputStream inputStreamB = null;
        try {
            inputStreamA = new FileInputStream(chunkA.filePath);
            inputStreamB = new FileInputStream(chunkB.filePath);
            int byteA, byteB;
            boolean moreBytesA = true, moreBytesB = true;

            fastForward(inputStreamA, chunkA.startPos);
            fastForward(inputStreamB, chunkB.startPos);
            while (moreBytesA && moreBytesB) {
                moreBytesA = ((byteA = inputStreamA.read()) != -1);
                moreBytesB = ((byteB = inputStreamB.read()) != -1);
                if(!moreBytesA) { break; }
                //System.out.printf("\t%d %c : %d %c\n",byteA,byteA,byteB,byteB);//DEBUG
                position++;
                if (byteA != byteB) {

                    System.out.print("-");
                    result = false;
                    break;
                }
                System.out.print("+");
            }
//            System.out.println("reached end of file " + (moreBytesA ? " A" : moreBytesB ? " B" : ""));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (inputStreamA != null)
                    inputStreamA.close();
            } catch (IOException ioe1) {
            }
            try {
                if (inputStreamB != null)
                    inputStreamB.close();
            } catch (IOException ioe1) {
            }
        }
        return (result == true) ? position : -1;
    }

    /**
     * Moves the <tt>read()</tt> pointer in <tt>fis</tt> by <tt>numBytes</tt>.
     * @param fis
     * @param numBytes
     * @throws IOException
     */
    void fastForward(FileInputStream fis, long numBytes) throws IOException {
        if (fis == null) {
            throw new IllegalArgumentException("null file specified");
        }
        if (numBytes < 0) {
            throw new IllegalArgumentException(
                    "Cannot fast forward a negative number of bytes");
        }
        long counter = 0L;
        while (counter < numBytes) {
            fis.read();
            counter++;
        }
    }


}
